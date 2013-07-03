package org.nlogo.extensions.gogo.controller

import org.nlogo.api.ExtensionException

class ControllerManager {

  private var controllerOpt: Option[Controller] = None

  def getControllerOpt = controllerOpt

  def init(portName: String) {
    controllerOpt = Option(new Controller(portName))
  }

  def close() {
    controllerOpt foreach {
      controller =>
        controller.currentPort foreach (_ => controller.closePort())
        controllerOpt = None
    }
  }

  def withController[T](f: (Controller) => T) : T = {
    controllerOpt flatMap {
      case controller if (!controller.currentPort.isEmpty) =>
        Some(f(controller))
      case _ =>
        None
    } getOrElse (throw new ExtensionException("No GoGo port open."))
  }

}
