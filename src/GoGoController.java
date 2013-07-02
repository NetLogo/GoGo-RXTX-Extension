package org.nlogo.extensions.gogo;

import java.util.List;
import java.util.ArrayList;

import jssc.SerialPort;
import org.nlogo.api.ExtensionException;
import scala.actors.threadpool.Arrays;


public class GoGoController {
  String portName;
  jssc.SerialPort port;

  // current burst mode -- not supported in current version
  //public int burstModeMask = 0;

  public static final byte IN_HEADER1 = (byte) 0x55;
  public static final byte IN_HEADER2 = (byte) 0xFF;
  public static final byte OUT_HEADER1 = (byte) 0x54;
  public static final byte OUT_HEADER2 = (byte) 0xFE;
  public static final byte ACK_BYTE = (byte) 0xAA;

  public static final byte CMD_PING = (byte) 0x00;
  public static final byte CMD_READ_SENSOR = (byte) 0x20;
  public static final byte CMD_READ_EXTENDED_SENSOR = (byte) 0xE0;
  public static final byte CMD_OUTPUT_PORT_ON = (byte) 0x40;
  public static final byte CMD_OUTPUT_PORT_OFF = (byte) 0x44;
  public static final byte CMD_OUTPUT_PORT_RD = (byte) 0x48;
  public static final byte CMD_OUTPUT_PORT_THISWAY = (byte) 0x4C;
  public static final byte CMD_OUTPUT_PORT_THATWAY = (byte) 0x50;
  public static final byte CMD_OUTPUT_PORT_COAST = (byte) 0x54;
  public static final byte CMD_OUTPUT_PORT_POWER = (byte) 0x60;
  public static final byte CMD_TALK_TO_OUTPUT_PORT = (byte) 0x80;
  public static final byte CMD_SET_BURST_MODE = (byte) 0xA0;

  public static final byte CMD_PWM_SERVO = (byte) 0xC8;
  

  public static final byte CMD_LED_ON = (byte) 0xC0;
  public static final byte CMD_LED_OFF = (byte) 0xC1;
  public static final byte CMD_BEEP = (byte) 0xC4;

  // read modes for sensors
  public static final byte SENSOR_READ_NORMAL = (byte) 0x00;
  public static final byte SENSOR_READ_MAX = (byte) 0x01;
  public static final byte SENSOR_READ_MIN = (byte) 0x02;

  // Output port identifiers
  // An output port mask is created by XORing these
  // OUTPUT_PORT_A | OUTPUT_PORT_C means output ports A and C
  // See talkToOutputPorts for usage
  public static final int OUTPUT_PORT_A = 0x01;
  public static final int OUTPUT_PORT_B = 0x02;
  public static final int OUTPUT_PORT_C = 0x04;
  public static final int OUTPUT_PORT_D = 0x08;

  // Sensor identifiers
  // A sensor mask is created by XORing these
  // See setBurtMode for usage
  public static final int SENSOR_1 = 0x01;
  public static final int SENSOR_2 = 0x02;
  public static final int SENSOR_3 = 0x04;
  public static final int SENSOR_4 = 0x08;
  public static final int SENSOR_5 = 0x10;
  public static final int SENSOR_6 = 0x20;
  public static final int SENSOR_7 = 0x40;
  public static final int SENSOR_8 = 0x80;

  //NO BURST YET
  // public static final int BURST_SPEED_HIGH = 0x00;
  // public static final int BURST_SPEED_LOW = 0x01;
  // public static final byte BURST_CHUNK_HEADER = (byte) 0x0C;

  private static final int[] sensorIDs = {SENSOR_1,
      SENSOR_2,
      SENSOR_3,
      SENSOR_4,
      SENSOR_5,
      SENSOR_6,
      SENSOR_7,
      SENSOR_8};


  public static int[] sensorValue = {-2,-2,-2,-2,-2,-2,-2,-2};


  public GoGoController(String portName) {
    this.portName = portName;
  }

  public static int sensorID(int sensor) {
    if ((sensor < 1) || (sensor > 8))
      throw new RuntimeException("Sensor number out of range: " + sensor);
    return sensorIDs[(sensor - 1)];
  }


  //PORT ENUMERATION
   public static List<String> serialPorts() {
    return listPorts(false);
  }

  //no longer useful - see note on listPorts.
  public static List<String> availablePorts() {
      return listPorts(true);
  }

  //i didn't see a way to distinguish between ports in use & not in the jssc api.
  //so the onlyAvaiable flag is no longer used.
  public static List<String> listPorts(boolean onlyAvailable) {
    String[] ports = jssc.SerialPortList.getPortNames();
    System.err.println(ports.length);
    ArrayList<String> portNames = new ArrayList<String>();
    portNames.addAll(Arrays.asList(ports));
    return portNames;
  }

  public String currentPortName() {
    if (port != null)
      return port.getPortName();
    return null;
  }

  public jssc.SerialPort currentPort() {
    return port;
  }

  public void closePort() {
    if (port != null) {
      try {
        port.removeEventListener();
        port.closePort();
        port = null;
      } catch (jssc.SerialPortException e) {
         e.printStackTrace();
      }
    }
  }

  public boolean openPort() throws ExtensionException {
    // if already open, just return true
    if (port != null) {
      return true;
    }
    System.err.println("about to create Serial Port with name " + portName );
    port = new jssc.SerialPort( portName );

    if (port != null) {
      try {
        port.openPort();
        System.err.println("about to set params and add listener on " + portName);
        //int baudRate, int dataBits, int stopBits, int parity
        //(9600, 8, 1, 0);
        port.setParams( SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        port.addEventListener( new PortListener() );
      } catch (jssc.SerialPortException e) {
          throw new ExtensionException("Unable to open port " + portName, e);
      }
      return true;
    } else {
      return false;
    }
  }



  //this listener interprets the meaning of all inbound communication from the gogo.
  class PortListener implements jssc.SerialPortEventListener {

    int nextSensor = 0;
    ArrayList<Byte> leftovers = new ArrayList<Byte>();

    public void serialEvent(jssc.SerialPortEvent serialPortEvent) {
      System.err.println("MESSAGE: Type ="+serialPortEvent.getEventType() + ", and Value="+serialPortEvent.getEventValue() );

      if ( serialPortEvent.getEventType() == jssc.SerialPortEvent.RXCHAR) {
        int num = serialPortEvent.getEventValue();

        //I have seen zero-length 'messages' in the windows logs.
        // without a check, two in a row can causes our 'leftover' arraylist to be empty.
        if (num == 0) { return; }

        try {
          byte[] bs = port.readBytes(num);
          for (byte b: bs)  {
            leftovers.add(b);
          }

          //Given the check for 'zero' in the 'num' variable, this should be redundant
          if ( leftovers.isEmpty() ) { return; }

          //debug logging.  remove.
          System.err.print("ALL BYTES in current consideration: ");
          for (Byte b:leftovers) { System.err.print(b + " | "); }
          System.err.println();

          int index = 0;
          byte current = leftovers.get(index);
          int cutpoint = 0; //we will discard everything before this index.


          //it would be much nicer to match on 2-tuples here...
          while ( index < leftovers.size() - 3 ) {
            index++;
            byte nextone = leftovers.get(index);

            //is it signaling an outbound message?
            if (current == OUT_HEADER1 && nextone == OUT_HEADER2 ) {
              index++;
              int command = leftovers.get(index);
              if ( command > 31 && command < 61) {
                //it's a sensor read command.  store this value to know which sensor reading is coming back
                nextSensor = (int)((command - 32)/4);
              }
              cutpoint = index; //now we've used up the data up to the current index.

              //is it signaling an inbound message?
            } else if (current == IN_HEADER1 && nextone == IN_HEADER2  ) {
              index++;
              byte possibleHighByte = leftovers.get(index);
              if (possibleHighByte == ACK_BYTE ) {
                cutpoint = index; //it's an ack, nothing to do & the inbound message ends here.
              } else {
                index++;
                int val = possibleHighByte *256 + ((leftovers.get(index) + 256) % 256);
                if ( val >= 0 && val <= 1023 ) {
                  sensorValue[nextSensor] = val;
                } else {
                  System.err.println( "crazy value " + val);
                  System.err.print("FROM: ");
                            for (Byte b:leftovers) { System.err.print(b + " | "); }
                            System.err.println();
                }
                cutpoint = index;
              }
            } else if (current == IN_HEADER2  ) {  //we seem sometimes to miss the first IN_HEADER
              byte possibleHighByte = nextone;
              if (possibleHighByte == ACK_BYTE ) {
                cutpoint = index; //it's an ack, inbound message ends here.
              } else {
                index++;
                int val = possibleHighByte *256 + ((leftovers.get(index) + 256) % 256);
                if ( val >= 0 && val <= 1023 ) {
                  sensorValue[nextSensor] = val;
                } else {
                  System.err.println( "crazy value " + val);
                  System.err.print("FROM: ");
                            for (Byte b:leftovers) { System.err.print(b + " | "); }
                            System.err.println();
                }
                cutpoint = index;
              }
            }

            current = nextone;
          }

          if (cutpoint > 0 ) {
            System.err.println("CLEARING FROM cutpoint index " + cutpoint);
            leftovers.subList(0, cutpoint+1).clear();
          }

          //debug logging remove
          System.err.print("LEFTOVER ARRAYLIST IS NOW: ");
          for (Byte b:leftovers) { System.err.print(b + " | "); }
          System.err.println();

        } catch (jssc.SerialPortException e) {
          e.printStackTrace();
        }
      }
      else {
        System.err.println("NON RXCHAR MESSAGE: Type ="+serialPortEvent.getEventType() + ", and Value="+serialPortEvent.getEventValue() );
        try {
          port.purgePort( jssc.SerialPort.PURGE_RXCLEAR | jssc.SerialPort.PURGE_TXCLEAR );
        } catch (jssc.SerialPortException e) {
          e.printStackTrace();
        }
      }
    }
  }



  public boolean ping() throws ExtensionException {
    if (port == null) {
      return false;
    } else {
      writeCommand(new byte[]{ CMD_PING });
      return true;
      // waitForAck();
    }
  }


  //All writing to the port happens through this method.
  public void writeCommand(byte[] command) throws ExtensionException {

    try {
      port.writeByte(OUT_HEADER1);
      port.writeByte(OUT_HEADER2);
      port.writeBytes(command);
    } catch (jssc.SerialPortException e) {
       throw new ExtensionException("WRITE FAILED", e);
    }
  }

  //READING
  public int internal_readSensor(int sensor, int mode) throws ExtensionException {
    int sensorVal = 0;

    if (sensor < 1)
      throw new RuntimeException("Sensor number out of range: " + sensor);
    if (sensor > 8) {
      throw new ExtensionException("Extended Sensor Range not currently supported");
    }

    int b = CMD_READ_SENSOR | ((sensor - 1) << 2) | mode;
    writeCommand(new byte[]{ (byte) b });
    return sensorValue[sensor-1];
  }

  public int readSensor(int sensor) throws ExtensionException {
    return internal_readSensor(sensor, SENSOR_READ_NORMAL);
  }

  public int readSensorMin(int sensor) throws ExtensionException {
    return internal_readSensor(sensor, SENSOR_READ_MIN);
  }

  public int readSensorMax(int sensor) throws ExtensionException {
    return internal_readSensor(sensor, SENSOR_READ_MAX);
  }



  //SIMPLE OUTPUTS
  public boolean beep() throws ExtensionException {
    if (port == null) {
      return false;
    }
    writeCommand(new byte[]{ CMD_BEEP, (byte) 0x00 });
    return true; //waitForAck();
  }

  public boolean led( boolean on ) throws ExtensionException  {
	  if (port == null) {
	      return false;
	    }
	    byte cmd = CMD_LED_OFF;
	    if (on) { cmd = CMD_LED_ON; }
	    writeCommand(new byte[]{ cmd, (byte) 0x00 });
	    return true; //waitForAck();
  }

  //MAIN OUTPUTS
  public void talkToOutputPorts(int outputPortMask) throws ExtensionException {
    writeCommand(new byte[]{ CMD_TALK_TO_OUTPUT_PORT,  (byte) outputPortMask });
    //waitForAck();
  }

  public void outputPortControl(byte cmd) throws ExtensionException {
    writeCommand(new byte[]{ cmd });
    //waitForAck();
  }

  public void outputPortOn() throws ExtensionException {
    outputPortControl(CMD_OUTPUT_PORT_ON);
  }

  public void outputPortOff() throws ExtensionException  {
    outputPortControl(CMD_OUTPUT_PORT_OFF);
  }

  public void outputPortCoast() throws ExtensionException  {
    outputPortControl(CMD_OUTPUT_PORT_COAST);
  }

  public void outputPortThatWay() throws ExtensionException {
    outputPortControl(CMD_OUTPUT_PORT_THATWAY);
  }

  public void outputPortThisWay() throws ExtensionException {
    outputPortControl(CMD_OUTPUT_PORT_THATWAY);
  }

  public void outputPortReverse() throws ExtensionException {
    outputPortControl(CMD_OUTPUT_PORT_RD);
  }

  public void setOutputPortPower(int level) throws ExtensionException {
    if ((level < 0) || (level > 7))
      throw new RuntimeException(
          "Power level out of range: " + level);
    int comm = CMD_OUTPUT_PORT_POWER | level << 2;
    writeCommand(new byte[]{ (byte) comm });
   // waitForAck();
  }


  //added for servo
  public void setServoPosition(int val) throws ExtensionException {
	  if ( (val < 20) || (val > 40 ) ) {
		  throw new ExtensionException( "Requested servo position (" + val + ") is out of safe range (20-40): ");
    } else {
	    writeCommand(new byte[]{CMD_PWM_SERVO,  (byte) val });
    }
    //waitForAck();
  }


  //NOT YET SUPPORTING EXTENDED SENSOR RANGE (NO CONVENIENT WAY TO TEST).
 /*
  //Reads a sensor with number >8.
  //Such sensors use a different serial format.
  public int readExtendedSensor(int sensor) {
    int sensorVal = 0;

    //Turn sensor number (9+) into 0+
    sensor = sensor - 9;

    //Break sensor value into bytes to send to board
    byte highByte = (byte) (sensor >> 8);
    byte lowByte = (byte) (sensor & 0xFF);

    //Create command string
    byte[] command = { CMD_READ_EXTENDED_SENSOR, highByte, lowByte };

    //Send command
    try {
      writeCommand(command);
      synchronized (inputStream) { //Seize input stream to prevent simultaneous reads
        waitForReplyHeader();
        sensorVal = readInt() << 8;
        sensorVal += readInt();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return sensorVal;
  }

  public void setBurstMode(int sensorMask) {
    setBurstMode(sensorMask, BURST_SPEED_HIGH);
  }

  public void setBurstMode(int sensorMask, int speed) {
    writeCommand(new byte[]{ ((byte) (CMD_SET_BURST_MODE | (byte) speed)),
        (byte) sensorMask} );
    waitForAck();
    burstModeMask = sensorMask;
  }

  public void startBurstReader(BurstCycleHandler handler) {
    burstReader = new BurstReader(this, handler);
    burstReader.start();
  }

  public void stopBurstReader() {

    burstReader.stopReading();
  }

  public int[] readBurstCycle() {

    try {

      int b;
      for (int i = 0; i < 256; i++) {
        synchronized (inputStream) {
          b = peekByte();
          if (b == BURST_CHUNK_HEADER) {
            // we got a burst cycle header, so read it to dump
            // it off the stream

            readByte();

            // grab the controller lock so none of the other
            // operations can get in our way.

            int high = readInt();
            int low = readInt();
            int sensor = (high >> 5) + 1;
            int val = (high & 0x03) << 8;
            val += low;
            if (sensor > 0) {
              return new int[]{sensor, val};
            } else {
              // we got a bad read, return empty values
              return new int[]{};
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new int[]{};
    }
    return new int[]{}; //we didn't see anything before our timeout
  }

  public interface BurstCycleHandler {
    void handleBurstCycle(int sensor, int value);
  }


  public class DefaultBurstCycleHandler
      implements BurstCycleHandler {
    private final int[] sensorValues = new int[8];

    synchronized public void handleBurstCycle(int sensor, int value) {
      System.out.println("Sensor " + sensor + " value: " + value);
      sensorValues[sensor - 1] = value;
    }
  }

  public class BurstReader extends Thread {
    GoGoController controller;
    BurstCycleHandler handler;
    boolean keepRunning = true;

    BurstReader(GoGoController cont, BurstCycleHandler handler) {
      this.controller = cont;
      this.handler = handler;
    }

    public void stopReading() {
      keepRunning = false;
    }

    public void run() {
      int[] result;
      while (keepRunning) {
        result = controller.readBurstCycle();
        if (result.length == 2 && handler != null) {
          handler.handleBurstCycle(result[0], result[1]);
        }
      }
    }
  }
  

  // main for GoGoController class, which functions as a small utility
  public static void main(String[] args)
      throws java.io.IOException {
    String port = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-l")) {
        for (String portName : serialPorts()) {
            System.out.println(portName);
        }
        System.exit(0);
      } else if (args[i].equals("-p")) {
        i++;
        port = args[i];
      }
    }
  }

  */



}
