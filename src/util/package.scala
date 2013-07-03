package org.nlogo.extensions.gogo

import gnu.io.CommPortIdentifier

package object util {

  def fetchPorts(): Seq[String] = {
    import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
    CommPortIdentifier.getPortIdentifiers.asScala collect {
      case portID: CommPortIdentifier if (portID.getPortType == CommPortIdentifier.PORT_SERIAL && !portID.isCurrentlyOwned) => portID.getName
    } toSeq
  }

}
