package org.nlogo.extensions.gogo.prim

import
  org.nlogo.{ api, extensions },
    api.{ Argument, Context, DefaultReporter, LogoList, Syntax },
    extensions.gogo.util.fetchPorts

class ListPorts extends DefaultReporter {
  override def getSyntax = Syntax.reporterSyntax(Syntax.ListType)
  override def report(args: Array[Argument], context: Context) = {
    try LogoList.fromIterator(fetchPorts().toIterator)
    catch {
      case e: NoClassDefFoundError => throw new EE("Could not initialize GoGo Extension.  Please ensure that you have installed RXTX correctly.  Full error message: " + e.getLocalizedMessage)
    }
  }
}
