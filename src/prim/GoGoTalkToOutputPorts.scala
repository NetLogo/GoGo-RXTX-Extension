package org.nlogo.extensions.gogo.prim

import
  org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoTalkToOutputPorts extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType))
  override def perform(args: Array[Argument], context: Context) {
    ensureGoGoPort()
    import GoGoController._
    val portMap  = Map('a' -> OUTPUT_PORT_A, 'b' -> OUTPUT_PORT_B, 'c' -> OUTPUT_PORT_C, 'd' -> OUTPUT_PORT_D)
    val portMask = buildMask(args(0).getList.toVector, portMap)
    controller.talkToOutputPorts(portMask)
  }
}
