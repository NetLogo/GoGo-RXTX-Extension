package org.nlogo.extensions.gogo.controller

import Constants._

trait OutputPortController {

  self: CommandWriter =>

  def outputPortOn() {
    outputPortControl(CmdOutputPortOn)
  }

  def outputPortOff() {
    outputPortControl(CmdOutputPortOff)
  }

  def outputPortCoast() {
    outputPortControl(CmdOutputPortCoast)
  }

  def outputPortThatWay() {
    outputPortControl(CmdOutputPortThatWay)
  }

  def outputPortThisWay() {
    outputPortControl(CmdOutputPortThisWay)
  }

  def outputPortReverse() {
    outputPortControl(CmdOutputPortRd)
  }

  private def outputPortControl(cmd: Byte) {
    writeAndWait(cmd)
  }

}
