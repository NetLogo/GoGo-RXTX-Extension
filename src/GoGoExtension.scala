/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/
package org.nlogo.extensions.gogo

import java.util.{ List => JList }

import controller.ControllerManager

import installer.GoGoWindowsHandler

import org.nlogo.api.{ DefaultClassManager, ExtensionException, ExtensionManager, PrimitiveManager }

class GoGoExtension extends DefaultClassManager {

  private val manager = new ControllerManager

  override def load(primManager: PrimitiveManager) {
    import prim._
    primManager.addPrimitive("install",               new GoGoInstall)
    primManager.addPrimitive("ports",                 new GoGoListPorts)
    primManager.addPrimitive("open",                  new GoGoOpen(manager))
    primManager.addPrimitive("open?",                 new GoGoIsOpen(manager))
    primManager.addPrimitive("close",                 new GoGoClose(manager))
    primManager.addPrimitive("ping",                  new GoGoPing(manager))
    primManager.addPrimitive("output-port-on",        new GoGoOutputPortOn(manager))
    primManager.addPrimitive("output-port-off",       new GoGoOutputPortOff(manager))
    primManager.addPrimitive("output-port-coast",     new GoGoOutputPortCoast(manager))
    primManager.addPrimitive("output-port-thisway",   new GoGoOutputPortThisWay(manager))
    primManager.addPrimitive("output-port-thatway",   new GoGoOutputPortThatWay(manager))
    primManager.addPrimitive("set-output-port-power", new GoGoOutputPortPower(manager))
    primManager.addPrimitive("output-port-reverse",   new GoGoOutputPortReverse(manager))
    primManager.addPrimitive("talk-to-output-ports",  new GoGoTalkToOutputPorts(manager))
    primManager.addPrimitive("set-burst-mode",        new GoGoSetBurstMode(manager))
    primManager.addPrimitive("stop-burst-mode",       new GoGoStopBurstMode(manager))
    primManager.addPrimitive("burst-value",           new GoGoSensorBurstValue(manager))
    primManager.addPrimitive("sensor",                new GoGoSensor(manager))
    primManager.addPrimitive("beep",                  new GoGoBeep(manager))
    primManager.addPrimitive("led-on",                new GoGoLedOn(manager))
    primManager.addPrimitive("led-off",               new GoGoLedOff(manager))
    primManager.addPrimitive("set-servo",             new GoGoSetServo(manager))
  }

  override def runOnce(em: ExtensionManager) {
    em.addToLibraryPath(this, "lib")
    GoGoWindowsHandler(true)
  }

  override def unload(em: ExtensionManager) {

    import java.util.{ Vector => JVector }

    manager.close()

    try {

      val classLoader = this.getClass.getClassLoader
      val field       = classOf[ClassLoader].getDeclaredField("nativeLibraries")
      field.setAccessible(true)

      field.get(classLoader) match {
        case libs: JVector[_] =>
          import scala.collection.JavaConverters.iterableAsScalaIterableConverter
          libs.asScala.foreach {
            lib =>
              val finalize = lib.getClass.getDeclaredMethod("finalize")
              finalize.setAccessible(true)
              finalize.invoke(lib)
          }
        case _ =>
          throw new ExtensionException("Failed to reflectively cast libraries to `JVector`")
      }

    }
    catch {
      case e: Exception => System.err.println(e.getMessage)
    }

  }

  override def additionalJars: JList[String] = {
    import scala.collection.JavaConverters.seqAsJavaListConverter
    List("RXTXcomm.jar").asJava
  }

}
