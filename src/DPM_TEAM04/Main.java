package DPM_TEAM04;

import static DPM_TEAM04.Resources.*;

import java.io.IOException;
import java.util.HashMap;

import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.FileLogger;
import DPM_TEAM04.logging.LCDLogger;
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

	public static LCDLogger lcdLog;
	
	public static void main(String[] args) {

		// Forces initialization of Resources
		Resources.initialize = true;

		// Initialize the odometer
		final Odometer odometer = Odometer.getOdometer();

		// Initialize the display
		DataEntryProvider versionProvider = new DataEntryProvider("Version") {
			@Override
			public double getEntry() {
				return VERSION_NB;
			}
		};
		
		DataEntryProvider xProvider = new DataEntryProvider("X") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getX();
			}
		};
		
		DataEntryProvider yProvider = new DataEntryProvider("Y") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getY();
			}
		};
		
		DataEntryProvider tProvider = new DataEntryProvider("T") {
			@Override
			public double getEntry() {
				return odometer.getPosition().getDirection(CoordinateSystem.POLAR_RAD);
			}
		};
		
		lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider, xProvider, yProvider, tProvider);
		
		//Display display = new Display(odometer);

		// Initialize the localization thread
		Localization localization = new Localization();

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
		 * WiFiConnection will establish a connection to the server and wait for
		 * data If the server is not running, this will throw an IOException If
		 * the server is running but the user has yet to press start on the Java
		 * GUI with some data, this will wait forever During the competition,
		 * this means you can start your code, place it on the field, and it
		 * will wait for data from the professor's computer If you need it to
		 * stop, access the robot via the EV3Control program and click
		 * "Stop Program" Alternatively, you can reset the robot but you risk SD
		 * card corruption Note that you can set the final argument debugPrint
		 * as false to disable printing to the LCD if desired.
		 */
		WifiConnection conn = null;
		try {
			// System.out.println("Connecting...");
			conn = new WifiConnection(Resources.SERVER_IP, Resources.TEAM_NUMBER, false);
		} catch (IOException e) {
			System.out.println("Connection failed");
			Button.waitForAnyPress();
			System.exit(0);
		}

		lcd.clear();

		/*
		 * This section of the code reads and prints the data received from the
		 * server, stored as a HashMap with String keys and Integer values.
		 */
		if (conn != null) {
			HashMap<String, Integer> connData = conn.StartData;
			if (connData == null) {
				System.out.println("Failed to read transmission");
				Button.waitForAnyPress();
				System.exit(0);

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
					isBuilder = true;
					startingCorner = connData.get("BSC");
				} else {
					isBuilder = false;
					startingCorner = connData.get("CSC");
				}

				/*
				Rectangle2D builderCorner = new Rectangle2D.Double(connData.get("LGZx"), connData.get("LGZy"),
						connData.get("UGZx") - connData.get("LGZx"), connData.get("UGZy") - connData.get("LGZy"));
				Rectangle2D garbageCorner = new Rectangle2D.Double(connData.get("LRZx"), connData.get("LRZy"),
						connData.get("URZx") - connData.get("LRZx"), connData.get("URZy") - connData.get("LRZy"));
				*/

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
		lcdLog.start();
//		display.start();

		/*
		 * 
		 * LOGGER
		 * 
		 */

		// Create basic data providers
		DataEntryProvider angleProvider = new DataEntryProvider("Angle PolarDeg") {
			@Override
			public double getEntry() {
				return Odometer.getOdometer().getPosition().getDirection(CoordinateSystem.POLAR_DEG);
			}
		};

		DataEntryProvider usFrontProvider = new DataEntryProvider("US Front") {

			@Override
			public double getEntry() {
				return getFrontUSData();
			}
		};

		DataEntryProvider usSideProvider = new DataEntryProvider("US Side") {

			@Override
			public double getEntry() {
				return getSideUSData();
			}
		};

		FileLogger fileLog = new FileLogger("Log_Test.csv", 50, angleProvider, usFrontProvider, usSideProvider);

		// start logger
		fileLog.start();
		localization.start();
		Button.waitForAnyPress();

		// save and close logger
		fileLog.interrupt();

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

}
