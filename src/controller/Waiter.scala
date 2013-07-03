package org.nlogo.extensions.gogo.controller

import java.io.IOException

import Constants.{ ACK_BYTE, IN_HEADER1, IN_HEADER2 }

trait Waiter {

  self: Reader =>

  protected def waitForAck(): Boolean = {
    waitForReplyHeader()
    waitForByte(ACK_BYTE)
  }

  protected def waitForReplyHeader(): Boolean =
    waitUntil {
      _ => readByte() match {
        case IN_HEADER1 => readByte() == IN_HEADER2
        case IN_HEADER2 => true
        case _          => false
      }
    }

  protected def waitForByte(target: Byte): Boolean =
    waitUntil { case _ if (readByte() == target) => true }

  private def waitUntil(f: (Int) => Boolean) : Boolean =
    try (0 until 256 exists f)
    catch {
      case e: IOException =>
        e.printStackTrace()
        false
    }

}
