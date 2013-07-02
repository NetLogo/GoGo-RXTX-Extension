package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoSetBurstMode extends DefaultCommand {

  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType, Syntax.BooleanType))
  override def perform(args: Array[Argument], context: Context) {
    val sensorMask = sensorMask(args(0).getList.toVector)
    val speed      = if (args(1).getBoolean) GoGoController.BURST_SPEED_HIGH else GoGoController.BURST_SPEED_LOW
    controller.setBurstMode(sensorMask, speed)
    burstCycleHandler = new GoGoExtension.NLBurstCycleHandler
    controller.startBurstReader(burstCycleHandler)
  }

  private def sensorMask(sensors: Seq[_]) : Int = {
    import GoGoController._
    val sensorMap = Map('1' -> SENSOR_1, '2' -> SENSOR_2, '3' -> SENSOR_3, '4' -> SENSOR_4, '5' -> SENSOR_5, '6' -> SENSOR_6, '7' -> SENSOR_7, '8' -> SENSOR_8)
    buildMask(sensors, sensorMap)
  }

}
