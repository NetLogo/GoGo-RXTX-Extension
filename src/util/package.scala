package org.nlogo.extensions.gogo

import jssc.SerialPortList

package object util {
  def fetchPorts(): Seq[String] = SerialPortList.getPortNames().toSeq
}
