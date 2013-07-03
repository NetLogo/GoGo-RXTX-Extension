package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, Syntax },
    extensions.gogo.controller.{ Constants, Controller, ControllerManager },
      Constants._

class TalkToOutputPorts(manager: ControllerManager) extends ManagedCommand(manager) {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType))
  override def managedPerform(args: Array[Argument], context: Context, controller: Controller) {
    val portMap  = Map('a' -> OutputPortA, 'b' -> OutputPortB, 'c' -> OutputPortC, 'd' -> OutputPortD)
    val portMask = buildMask(args(0).getList.toVector, portMap)
    controller.talkToOutputPorts(portMask)
  }
}
