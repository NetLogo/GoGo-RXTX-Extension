package org.nlogo.extensions.gogo.controller

import Constants._

trait OutputPortController {

  self: CommandWriter =>

  def outputPortOn() {
    outputPortControl(CMD_OUTPUT_PORT_ON)
  }

  def outputPortOff() {
    outputPortControl(CMD_OUTPUT_PORT_OFF)
  }

  def outputPortCoast() {
    outputPortControl(CMD_OUTPUT_PORT_COAST)
  }

  def outputPortThatWay() {
    outputPortControl(CMD_OUTPUT_PORT_THATWAY)
  }

  def outputPortThisWay() {
    outputPortControl(CMD_OUTPUT_PORT_THISWAY)
  }

  def outputPortReverse() {
    outputPortControl(CMD_OUTPUT_PORT_RD)
  }

  private def outputPortControl(cmd: Byte) {
    writeAndWait(cmd)
  }

}
