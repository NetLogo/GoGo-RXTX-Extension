package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, Syntax },
    extensions.gogo.controller.{ BurstCycleHandler, Constants, Controller, ControllerManager },
      Constants._

class GoGoSetBurstMode(manager: ControllerManager) extends ManagedCommand(manager) {

  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType, Syntax.BooleanType))
  override def managedPerform(args: Array[Argument], context: Context, controller: Controller) {
    val mask  = sensorMask(args(0).getList.toVector)
    val speed = if (args(1).getBoolean) BURST_SPEED_HIGH else BURST_SPEED_LOW
    controller.setBurstMode(mask, speed)
    controller.startBurstReader(generateBurster)
  }

  private def sensorMask(sensors: Seq[_]) : Int = {
    val sensorMap = Map('1' -> SENSOR_1, '2' -> SENSOR_2, '3' -> SENSOR_3, '4' -> SENSOR_4, '5' -> SENSOR_5, '6' -> SENSOR_6, '7' -> SENSOR_7, '8' -> SENSOR_8)
    buildMask(sensors, sensorMap)
  }

  private def generateBurster = new BurstCycleHandler {

    private val sensorValues = new Array[Int](8)

    def getValue(sensor: Int) : Int = sensorValues(sensor - 1)
    def handleBurstCycle(sensor: Int, value: Int) {
      sensorValues(sensor - 1) = value
    }

  }

}
