package org.nlogo.extensions.gogo.controller

import java.io.{ InputStream, OutputStream }

import gnu.io.{ CommPortIdentifier, SerialPort }

private[controller] trait HasPortsAndStreams {

  protected val portName: String

  protected var portIDOpt:       Option[CommPortIdentifier] = None
  protected var portOpt:         Option[SerialPort]         = None
  protected var inputStreamOpt:  Option[InputStream]        = None
  protected var outputStreamOpt: Option[OutputStream]       = None

}
