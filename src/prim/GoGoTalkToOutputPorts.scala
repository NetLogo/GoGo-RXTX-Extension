package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, Syntax },
    extensions.gogo.controller.{ Constants, Controller, ControllerManager },
      Constants._

class GoGoTalkToOutputPorts(manager: ControllerManager) extends ManagedCommand(manager) {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType))
  override def managedPerform(args: Array[Argument], context: Context, controller: Controller) {
    val portMap  = Map('a' -> OUTPUT_PORT_A, 'b' -> OUTPUT_PORT_B, 'c' -> OUTPUT_PORT_C, 'd' -> OUTPUT_PORT_D)
    val portMask = buildMask(args(0).getList.toVector, portMap)
    controller.talkToOutputPorts(portMask)
  }
}
