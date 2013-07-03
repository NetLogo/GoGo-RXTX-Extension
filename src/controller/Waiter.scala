package org.nlogo.extensions.gogo.controller

import java.io.IOException

import Constants.{ AckByte, InHeader1, InHeader2 }

trait Waiter {

  self: Reader =>

  protected def waitForAck(): Boolean = {
    waitForReplyHeader()
    waitForByte(AckByte)
  }

  protected def waitForReplyHeader(): Boolean =
    waitUntil {
      _ => readByte() match {
        case InHeader1 => readByte() == InHeader2
        case InHeader2 => true
        case _          => false
      }
    }

  protected def waitForByte(target: Byte): Boolean =
    waitUntil {
      case _ if (readByte() == target) => true
      case _                           => false
    }

  private def waitUntil(f: (Int) => Boolean): Boolean =
    try (0 until 256 exists f)
    catch {
      case e: IOException =>
        e.printStackTrace()
        false
    }

}
