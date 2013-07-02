package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoSetBurstMode extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType, Syntax.BooleanType))
  override def perform(args: Array[Argument], context: Context) {
    val sensorMask = sensorMask(args(0).getList)
    val speed      = if (args(1).getBoolean) GoGoController.BURST_SPEED_HIGH else GoGoController.BURST_SPEED_LOW
    controller.setBurstMode(sensorMask, speed)
    burstCycleHandler = new GoGoExtension.NLBurstCycleHandler
    controller.startBurstReader(burstCycleHandler)
  }
}
