package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.navigation.ObstacleAvoidance;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;

public class ObstacleAvoidTest {

	public static LCDLogger lcdLog;

	
	public static void main(String[] args) {

		Resources.initialize = true;

		// Initialize the odometer
		final Odometer odometer = Odometer.getOdometer();
		Driver driver = Driver.getDriver();
		
		// Initialize the display
		DataEntryProvider versionProvider = new DataEntryProvider("Version") {
			@Override
			public double getEntry() {
				return 3.1415;
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
				return odometer.getPosition().getDirection(CoordinateSystem.POLAR_DEG);
			}
		};
		
		lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider, xProvider, yProvider, tProvider);
		
		ExitThreadForCollectingGrabbingTest exit = new ExitThreadForCollectingGrabbingTest();
		exit.start();

		odometer.start();
		lcdLog.start();

		(new ObstacleAvoidance()).start();
		
		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 6 * TILE_WIDTH, 0));
		Button.waitForAnyPress();
		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 0, 0));
		


		
	}

}