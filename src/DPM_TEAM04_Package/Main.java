/*
 * DPM
 * ECSE 211
 * DESIGN PROJECT
 * 
 * 
 * 2016
 * 
 * 
 * 
 * 
 * 
 * VERSION NUMBER WRITTEN AS A CONSTANT
 * 
 */


package DPM_TEAM04_Package;

import java.io.IOException;
import java.util.HashMap;

import DPM_TEAM04_Package.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.geometry.Rectangle2D;
import wifi.WifiConnection;

public class Main {
	
	public static final int VERSION_NB = 1;
	
	
	//private static final Port usPortLeft = LocalEV3.get().getPort("S4");
	private static final Port usPortFront = LocalEV3.get().getPort("S2");		
	private static final Port colorPortDown = LocalEV3.get().getPort("S1");
	//private static final Port colorPortFront = LocalEV3.get().getPort("S2");
	
	
	/**
	 * DOCUMENTATION COMMENTS
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		// Ultrasonic sensors
		/*
		@SuppressWarnings("resource")							    			// Because we don't bother to close this resource
		SensorModes usSensorLeft_Mode = new EV3UltrasonicSensor(usPortLeft);	// usSensor is the instance
		SampleProvider usSensorLeft = usSensorLeft_Mode.getMode("Distance");					// usDistance provides samples from this instance
		float[] usDataLeft = new float[usSensorLeft.sampleSize()];						// usData is the buffer in which data are returned
		*/
		
		@SuppressWarnings("resource")
		SensorModes usSensorFront_Mode = new EV3UltrasonicSensor(usPortFront);
		SampleProvider usSensorFront = usSensorFront_Mode.getMode("Distance");
		float[] usDataFront = new float[usSensorFront.sampleSize()];
		
		SensorModes colorSensorDown_Mode = new EV3ColorSensor(colorPortDown);
		SampleProvider colorSensorDown = colorSensorDown_Mode.getMode("Red");			// colorValue provides samples from this instance
		float[] colorDataDown = new float[colorSensorDown.sampleSize()];			// colorData is the buffer in which data are returned
		
		/*
		SensorModes colorSensorFront_Mode = new EV3ColorSensor(colorPortFront);
		SampleProvider colorSensorFront = colorSensorFront_Mode.getMode("Red");			// colorValue provides samples from this instance
		float[] colorDataFront = new float[colorSensorFront.sampleSize()];			// colorData is the buffer in which data are returned
		*/
		
		
		// LCD Display
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		// Initialize the odometer
		Odometer odometer = new Odometer();
		
		// Initialize the display
		Display display = new Display(odometer, t);
		
		
		Resources resources = new Resources(usSensorFront, usDataFront, colorSensorDown, colorDataDown);
		
		
		// Initialize the resources (constants and sensors)
		//Resources resources = new Resources();
		
		// Initialize the localization thread
		Localization localization = new Localization(odometer, resources);
		
		
		
		
		/*
		 * 
		 * 
		 * START OTHER THREADS
		 * 
		 * START THE WIFI CONNECTION AND WAIT TO RECEIVE THE PARAMETERS
		 * 
		 * PASS THE PARAMETERS TO THE RESOURCES
		 * 
		 * 
		 */
		
		t.clear();

		/*
		 * WiFiConnection will establish a connection to the server and wait for data
		 * If the server is not running, this will throw an IOException
		 * If the server is running but the user has yet to press start on the Java GUI with some data,
		 * this will wait forever
		 * During the competition, this means you can start your code, place it on the field, and it will wait
		 * for data from the professor's computer
		 * If you need it to stop, access the robot via the EV3Control program and click "Stop Program"
		 * Alternatively, you can reset the robot but you risk SD card corruption
		 * Note that you can set the final argument debugPrint as false to disable printing to the LCD if desired.
		 */ 
		WifiConnection conn = null;
		try {
			//System.out.println("Connecting...");
			conn = new WifiConnection(Resources.SERVER_IP, Resources.TEAM_NUMBER, false);
		} catch (IOException e) {
			System.out.println("Connection failed");
		}
		
		t.clear();

		/*
		 * This section of the code reads and prints the data received from the server,
		 * stored as a HashMap with String keys and Integer values.
		 */
		if (conn != null) {
			HashMap<String, Integer> connData = conn.StartData;
			if (connData == null) {
				System.out.println("Failed to read transmission");
				

				/*
				 * 
				 * 
				 * WHAT SHOULD WE DO WHEN TRANSMISSION FAILED???
				 * 
				 * 
				 */
				
				
				
			} else {
				
				Resources.wifiData = connData;
				if (connData.get("BTN") == Resources.TEAM_NUMBER) {
					Resources.isBuilder = true;
				} else {
					Resources.isBuilder = false;
				}
				
				Rectangle2D builderCorner = new Rectangle2D.Double(connData.get("LGZx"), connData.get("LGZy"), connData.get("UGZx")-connData.get("LGZx"), connData.get("UGZy")-connData.get("LGZy"));
				Rectangle2D garbageCorner = new Rectangle2D.Double(connData.get("LRZx"), connData.get("LRZy"), connData.get("URZx")-connData.get("LRZx"), connData.get("URZy")-connData.get("LRZy"));
				
				System.out.println("\n\n\n\n\n" + builderCorner.getHeight());
				
			}
		}
		
		
		
		
		
		/*
		 * 
		 * 
		 * 
		 * END OF WIFI CONNECTION
		 * 
		 * 
		 * 
		 */
		
		
		
		odometer.start();
		display.start();
		
		// Wait 4 seconds for everything to be set up (sensors)
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
		
		localization.start();
		
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
