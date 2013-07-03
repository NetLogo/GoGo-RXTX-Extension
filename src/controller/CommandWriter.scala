package org.nlogo.extensions.gogo.controller

import java.io.IOException

import Constants.{ OUT_HEADER1, OUT_HEADER2 }

trait CommandWriter {

  self: Waiter with HasPortsAndStreams =>

  protected def writeAndWait(bytes: Byte*) : Boolean = {
    writeCommand(bytes.toArray)
    waitForAck()
  }

  protected def writeAndWaitForReplyHeader(bytes: Byte*) : Boolean = {
    writeCommand(bytes.toArray)
    waitForReplyHeader()
  }

  private def writeCommand(command: Array[Byte]) {
    outputStreamOpt foreach {
      os => os synchronized {
        try {
          writeByte(OUT_HEADER1)
          writeByte(OUT_HEADER2)
          os.write(command)
        }
        catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }
  }

  private def writeByte(b: Byte) {
    outputStreamOpt foreach {
      os => synchronized {
        os.write(b)
      }
    }
  }

}
