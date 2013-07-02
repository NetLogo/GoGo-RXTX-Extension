/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/
package org.nlogo.extensions.gogo

import org.nlogo.api.{ ExtensionException, ExtensionManager, PrimitiveManager }
 object GoGoExtension {

  var controllerOpt: Option[GoGoController]                    = None
  var bursterOpt:    Option[GoGoExtension.NLBurstCycleHandler] = None

  def runWindowsInstaller(verify: Boolean) {
    if (System.getProperty("os.name").startsWith("Windows"))
      GoGoWindowsHandler.run(verify)
  }

  def ensureGoGoPort() {
    controllerOpt flatMap (controller => Option(controller.currentPort)) orElse (throw new ExtensionException("No GoGo port open."))
  }

  def close() {
    controllerOpt foreach {
      controller =>
        Option(controller.currentPort) foreach (_ => controller.closePort())
        controllerOpt = None
    }
  }

  def initController(portName: String) {
    controllerOpt = Option(new GoGoController(portName))
  }

  class NLBurstCycleHandler extends GoGoController.BurstCycleHandler {

    private val sensorValues = new Array[Int](8)

    def getValue(sensor: Int) : Int = sensorValues(sensor - 1)
    def handleBurstCycle(sensor: Int, value: Int) {
      sensorValues(sensor - 1) = value
    }

  }

}

class GoGoExtension extends org.nlogo.api.DefaultClassManager {

  override def load(primManager: PrimitiveManager) {
    import prim._
    primManager.addPrimitive("ports",                 new GoGoListPorts)
    primManager.addPrimitive("open",                  new GoGoOpen)
    primManager.addPrimitive("open?",                 new GoGoOpenPredicate)
    primManager.addPrimitive("close",                 new GoGoClose)
    primManager.addPrimitive("ping",                  new GoGoPing)
    primManager.addPrimitive("output-port-on",        new GoGoOutputPortOn)
    primManager.addPrimitive("output-port-off",       new GoGoOutputPortOff)
    primManager.addPrimitive("output-port-coast",     new GoGoOutputPortCoast)
    primManager.addPrimitive("output-port-thisway",   new GoGoOutputPortThisWay)
    primManager.addPrimitive("output-port-thatway",   new GoGoOutputPortThatWay)
    primManager.addPrimitive("set-output-port-power", new GoGoOutputPortPower)
    primManager.addPrimitive("output-port-reverse",   new GoGoOutputPortReverse)
    primManager.addPrimitive("talk-to-output-ports",  new GoGoTalkToOutputPorts)
    primManager.addPrimitive("set-burst-mode",        new GoGoSetBurstMode)
    primManager.addPrimitive("stop-burst-mode",       new GoGoStopBurstMode)
    primManager.addPrimitive("burst-value",           new GoGoSensorBurstValue)
    primManager.addPrimitive("sensor",                new GoGoSensor)
    primManager.addPrimitive("beep",                  new GoGoBeep)
    primManager.addPrimitive("led-on",                new GoGoLedOn)
    primManager.addPrimitive("led-off",               new GoGoLedOff)
    primManager.addPrimitive("install",               new GoGoInstall)
    primManager.addPrimitive("set-servo",             new GoGoSetServo)
  }

  override def runOnce(em: ExtensionManager) {
    em.addToLibraryPath(this, "lib")
    runWindowsInstaller(true)
  }

  override def unload(em: ExtensionManager) {

    import java.util.{ Vector => JVector }

    close()

    try {

      val classLoader = this.getClass.getClassLoader
      val field       = classOf[ClassLoader].getDeclaredField("nativeLibraries")
      field.setAccessible(true)

      import scala.collection.JavaConverters.iterableAsScalaIterableConverter
      field.get(classLoader).asInstanceOf[JVector[_]].asScala.foreach {
        lib =>
          val finalize = lib.getClass.getDeclaredMethod("finalize", Seq[Class[_]](): _*)
          finalize.setAccessible(true)
          finalize.invoke(lib)
      }

    }
    catch {
      case e: Exception => System.err.println(e.getMessage)
    }

  }

  override def additionalJars: List[String] = List("RXTXcomm.jar")

}
