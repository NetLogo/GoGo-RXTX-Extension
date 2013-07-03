/** (c) 2004 Uri Wilensky. See README.txt for terms of use. **/
package org.nlogo.extensions.gogo

import java.util.{ List => JList }

import controller.ControllerManager

import installer.WindowsInstaller

import org.nlogo.api.{ DefaultClassManager, ExtensionException, ExtensionManager, PrimitiveManager }

class GoGoExtension extends DefaultClassManager {

  private val manager = new ControllerManager

  override def load(primManager: PrimitiveManager) {
    import prim._
    primManager.addPrimitive("install",               new Install)
    primManager.addPrimitive("ports",                 new ListPorts)
    primManager.addPrimitive("open",                  new Open(manager))
    primManager.addPrimitive("open?",                 new IsOpen(manager))
    primManager.addPrimitive("close",                 new Close(manager))
    primManager.addPrimitive("ping",                  new Ping(manager))
    primManager.addPrimitive("output-port-on",        new OutputPortOn(manager))
    primManager.addPrimitive("output-port-off",       new OutputPortOff(manager))
    primManager.addPrimitive("output-port-coast",     new OutputPortCoast(manager))
    primManager.addPrimitive("output-port-thisway",   new OutputPortThisWay(manager))
    primManager.addPrimitive("output-port-thatway",   new OutputPortThatWay(manager))
    primManager.addPrimitive("set-output-port-power", new OutputPortPower(manager))
    primManager.addPrimitive("output-port-reverse",   new OutputPortReverse(manager))
    primManager.addPrimitive("talk-to-output-ports",  new TalkToOutputPorts(manager))
    primManager.addPrimitive("set-burst-mode",        new SetBurstMode(manager))
    primManager.addPrimitive("stop-burst-mode",       new StopBurstMode(manager))
    primManager.addPrimitive("burst-value",           new SensorBurstValue(manager))
    primManager.addPrimitive("sensor",                new Sensor(manager))
    primManager.addPrimitive("beep",                  new Beep(manager))
    primManager.addPrimitive("led-on",                new LedOn(manager))
    primManager.addPrimitive("led-off",               new LedOff(manager))
    primManager.addPrimitive("set-servo",             new SetServo(manager))
  }

  override def runOnce(em: ExtensionManager) {
    em.addToLibraryPath(this, "lib")
    WindowsInstaller(true)
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
