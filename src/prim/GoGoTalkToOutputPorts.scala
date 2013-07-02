package org.nlogo.extensions.gogo.prim

import org.nlogo.api.{ Argument, Context, DefaultCommand, Syntax }

class GoGoTalkToOutputPorts extends DefaultCommand {
  override def getSyntax = Syntax.commandSyntax(Array(Syntax.ListType))
  override def perform(args: Array[Argument], context: Context) {
    ensureGoGoPort()
    import GoGoController._
    val portMap  = Map('a' -> OUTPUT_PORT_A, 'b' -> OUTPUT_PORT_B, 'c' -> OUTPUT_PORT_C, 'd' -> OUTPUT_PORT_D)
    val portMask = args(0).getList.toVector.map(_.toString.toLowerCase.head).distinct.map(portMap).foldLeft(0){ case (acc, x) => acc | x }
    controller.talkToOutputPorts(portMask)
  }
}
