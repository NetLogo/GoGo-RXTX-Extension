package org.nlogo.extensions.gogo.controller


private[controller] trait PortCloser {

  self: HasPortsAndStreams =>

  def closePort() {
    portOpt foreach {
      port =>
        //@ c@ with bursting, come back to this port.removeEventListener()
        port.closePort()
        portOpt         = None
    }
  }

}
