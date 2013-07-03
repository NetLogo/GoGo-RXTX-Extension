package org.nlogo.extensions.gogo.controller

import org.nlogo.api.ExtensionException

trait Reader {

  self: HasPortsAndStreams =>

  protected def readByte() = readInt().toByte

  protected def readInt() =
    inputStreamOpt map {
      is => is synchronized {
        is.read()
      }
    } getOrElse (throw new ExtensionException("Read failed"))

}
