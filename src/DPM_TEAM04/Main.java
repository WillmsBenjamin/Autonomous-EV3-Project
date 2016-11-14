package DPM_TEAM04;

import static DPM_TEAM04.Resources.lcd;

import java.io.IOException;
import java.util.HashMap;

import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.odometry.Localization;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;
import lejos.robotics.geometry.Rectangle2D;
import wifi.WifiConnection;

/**
 * 
 * @author Tristan Toupin, Alexis GJ
 */
public class Main {
	
	public static final int VERSION_NB = 1;
	
	public static void main(String[] args) {
		
		//Forces initialization of Resources
		Resources.initialize = true;
		
		// Initialize the odometer
		Odometer odometer = new Odometer();
		
		// Initialize the display
		Display display = new Display(odometer);
		
		// Initialize the resources (constants and sensors)
		//Resources resources = new Resources();
		
		// Initialize the localization thread
		Localization localization = new Localization(odometer);
		
		
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
		
		lcd.clear();

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
		
		lcd.clear();

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
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
		
		localization.start();
		
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
