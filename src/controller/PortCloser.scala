package org.nlogo.extensions.gogo.controller

import java.io.IOException

private[controller] trait PortCloser {

  self: HasPortsAndStreams =>

  def closePort() {
    portOpt foreach {
      port =>

        port.removeEventListener()

        closeCloseableOpt(inputStreamOpt)
        closeCloseableOpt(outputStreamOpt)
        port.close()

        inputStreamOpt  = None
        outputStreamOpt = None
        portOpt         = None

    }
  }

  private def closeCloseableOpt[T <: { def close() }](opt: Option[T]) {
    opt foreach {
      closeable => closeable synchronized {
        try closeable.close()
        catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }
  }

}
