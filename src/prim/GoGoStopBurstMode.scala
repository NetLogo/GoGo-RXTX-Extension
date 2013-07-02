package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoStopBurstMode extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array[Int]())
  override def perform(args: Array[Argument], context: Context) {
    controller.stopBurstReader()
    burstCycleHandler = null
    controller.setBurstMode(0, GoGoController.BURST_SPEED_HIGH)
    controller.setBurstMode(0, GoGoController.BURST_SPEED_LOW)
  }
}
