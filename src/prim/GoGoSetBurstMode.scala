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
    val speed = if (args(1).getBoolean) BurstSpeedHigh else BurstSpeedLow
    controller.setBurstMode(mask, speed)
    controller.startBurstReader(generateBurster)
  }

  private def sensorMask(sensors: Seq[_]) : Int = {
    val sensorMap = Map('1' -> Sensor1, '2' -> Sensor2, '3' -> Sensor3, '4' -> Sensor4, '5' -> Sensor5, '6' -> Sensor6, '7' -> Sensor7, '8' -> Sensor8)
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
