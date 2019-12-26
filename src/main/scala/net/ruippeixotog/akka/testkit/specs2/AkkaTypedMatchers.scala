package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration.FiniteDuration

import akka.actor.testkit.typed.scaladsl.TestProbe
import org.specs2.execute.{ Failure, Success }

import net.ruippeixotog.akka.testkit.specs2.ResultValue.ReceiveTimeout
import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.ReceiveMatcherImpl

trait AkkaTypedMatchers {

  def receive[Msg]: ReceiveMatcher[TestProbe[Msg], Msg] = {
    akkaTypedReceiveMatcher[Msg](
      { msg => s"Received message '$msg'" },
      { timeout => s"Timeout ($timeout) while waiting for message" },
      _.remainingOrDefault)
  }

  def receiveWithin[Msg](max: FiniteDuration): ReceiveMatcher[TestProbe[Msg], Msg] = {
    akkaTypedReceiveMatcher[Msg](
      { msg => s"Received message '$msg' within $max" },
      { timeout => s"Didn't receive any message within $timeout" },
      { _ => max })
  }

  def receiveMessage[Msg]: ReceiveMatcher[TestProbe[Msg], Msg] = receive
  def receiveMessageWithin[Msg](max: FiniteDuration): ReceiveMatcher[TestProbe[Msg], Msg] = receiveWithin(max)

  private[this] def akkaTypedReceiveMatcher[Msg](
    receiveOkMsg: Msg => String,
    receiveKoMsg: FiniteDuration => String,
    timeoutFunc: TestProbe[Msg] => FiniteDuration): ReceiveMatcher[TestProbe[Msg], Msg] = {

    val getMessage = { (probe: TestProbe[Msg], timeout: FiniteDuration) =>
      try {
        val msg = probe.receiveMessage(timeout)
        SuccessValue(Success(receiveOkMsg(msg)), msg)
      } catch {
        case _: AssertionError => FailureValue(Failure(receiveKoMsg(timeout)), ReceiveTimeout)
      }
    }
    new ReceiveMatcherImpl[TestProbe[Msg], Msg](getMessage)(timeoutFunc)
  }
}
