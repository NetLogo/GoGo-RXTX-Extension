/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/
package org.nlogo.extensions.gogo

import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.DefaultCommand
import org.nlogo.api.DefaultReporter
import org.nlogo.api.ExtensionException
import org.nlogo.api.ExtensionManager
import org.nlogo.api.I18N
import org.nlogo.api.LogoList
import org.nlogo.api.Syntax
import org.nlogo.app.App
import org.nlogo.app.AppFrame
import org.nlogo.swing.OptionDialog
import org.nlogo.workspace.AbstractWorkspace

object GoGoExtension {
  private def runWindowsInstaller(verify: Boolean) {
    if (System.getProperty("os.name").startsWith("Windows")) {
      GoGoWindowsHandler.run(verify)
    }
  }

  def ensureGoGoPort {
    if (controller == null || controller.currentPort == null) {
      throw new ExtensionException("No GoGo port open.")
    }
  }

  def close {
    if (controller != null) {
      if (controller.currentPort != null) {
        controller.closePort
      }
      controller = null
    }
  }

  def initController(portName: String) {
    controller = new Nothing(portName)
  }

  private def sensorMask(sensorList: AbstractSequentialList[_]): Int = {
    var sensorMask: Int = 0
    var iter: Iterator[_] = null
    if (sensorList != null) {
      iter = sensorList.iterator
      while (iter.hasNext) {
        val `val`: AnyRef = iter.next
        `val`.toString.toLowerCase.charAt(0) match {
          case '1' =>
            sensorMask = sensorMask | GoGoController.SENSOR_1
            break //todo: break is not supported
          case '2' =>
            sensorMask = sensorMask | GoGoController.SENSOR_2
            break //todo: break is not supported
          case '3' =>
            sensorMask = sensorMask | GoGoController.SENSOR_3
            break //todo: break is not supported
          case '4' =>
            sensorMask = sensorMask | GoGoController.SENSOR_4
            break //todo: break is not supported
          case '5' =>
            sensorMask = sensorMask | GoGoController.SENSOR_5
            break //todo: break is not supported
          case '6' =>
            sensorMask = sensorMask | GoGoController.SENSOR_6
            break //todo: break is not supported
          case '7' =>
            sensorMask = sensorMask | GoGoController.SENSOR_7
            break //todo: break is not supported
          case '8' =>
            sensorMask = sensorMask | GoGoController.SENSOR_8
            break //todo: break is not supported
        }
      }
    }
    return sensorMask
  }

  var controller: Nothing = null
  var burstCycleHandler: GoGoExtension.NLBurstCycleHandler = null

  class NLBurstCycleHandler extends GoGoController.BurstCycleHandler {
    def handleBurstCycle(sensor: Int, value: Int) {
      sensorValues(sensor - 1) = value
    }

    def sensorValue(sensor: Int): Int = {
      return sensorValues(sensor - 1)
    }

    final val sensorValues: Array[Int] = new Array[Int](8)
  }

}

class GoGoExtension extends org.nlogo.api.DefaultClassManager {
  def load(primManager: PrimitiveManager) {
    primManager.addPrimitive("ports", new GoGoExtension.GoGoListPorts)
    primManager.addPrimitive("open", new GoGoExtension.GoGoOpen)
    primManager.addPrimitive("open?", new GoGoExtension.GoGoOpenPredicate)
    primManager.addPrimitive("close", new GoGoExtension.GoGoClose)
    primManager.addPrimitive("ping", new GoGoExtension.GoGoPing)
    primManager.addPrimitive("output-port-on", new GoGoExtension.GoGoOutputPortOn)
    primManager.addPrimitive("output-port-off", new GoGoExtension.GoGoOutputPortOff)
    primManager.addPrimitive("output-port-coast", new GoGoExtension.GoGoOutputPortCoast)
    primManager.addPrimitive("output-port-thisway", new GoGoExtension.GoGoOutputPortThisWay)
    primManager.addPrimitive("output-port-thatway", new GoGoExtension.GoGoOutputPortThatWay)
    primManager.addPrimitive("set-output-port-power", new GoGoExtension.GoGoOutputPortPower)
    primManager.addPrimitive("output-port-reverse", new GoGoExtension.GoGoOutputPortReverse)
    primManager.addPrimitive("talk-to-output-ports", new GoGoExtension.GoGoTalkToOutputPorts)
    primManager.addPrimitive("set-burst-mode", new GoGoExtension.GoGoSetBurstMode)
    primManager.addPrimitive("stop-burst-mode", new GoGoExtension.GoGoStopBurstMode)
    primManager.addPrimitive("burst-value", new GoGoExtension.GoGoSensorBurstValue)
    primManager.addPrimitive("sensor", new GoGoExtension.GoGoSensor)
    primManager.addPrimitive("beep", new GoGoExtension.GoGoBeep)
    primManager.addPrimitive("led-on", new GoGoExtension.GoGoLedOn)
    primManager.addPrimitive("led-off", new GoGoExtension.GoGoLedOff)
    primManager.addPrimitive("install", new GoGoExtension.GoGoInstall)
    primManager.addPrimitive("set-servo", new GoGoExtension.GoGoSetServo)
  }

  override def runOnce(em: ExtensionManager) {
    em.addToLibraryPath(this, "lib")
    runWindowsInstaller(true)
  }

  override def unload(em: ExtensionManager) {
    close
    try {
      val classLoader: ClassLoader = this.getClass.getClassLoader
      val field: Field = classOf[ClassLoader].getDeclaredField("nativeLibraries")
      field.setAccessible(true)
      val libs: Vector[_] = field.get(classLoader).asInstanceOf[Vector[_]]
      import scala.collection.JavaConversions._
      for (o <- libs) {
        val finalize: Method = o.getClass.getDeclaredMethod("finalize", new Array[Class[_]](0))
        finalize.setAccessible(true)
        finalize.invoke(o)
      }
    }
    catch {
      case e: Exception => {
        System.err.println(e.getMessage)
      }
    }
  }

  override def additionalJars: List[String] = {
    return java.util.Arrays.asList("RXTXcomm.jar")
  }
}
