package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.DISPLAY_PERIOD;
import static DPM_TEAM04.Resources.TILE_WIDTH;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.navigation.ObstacleAvoidance;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Button;

public class ObstacleAvoidTest {

	public static void main(String[] args) {

		Resources.initialize = true;

		// Initialize the odometer
		final Odometer odometer = Odometer.getOdometer();
		Driver driver = new Driver();
		ExitThreadForCollectingGrabbingTest exit = new ExitThreadForCollectingGrabbingTest();
		exit.start();

		odometer.start();
		(new ObstacleAvoidance()).start();

		while (true) {

			driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 1 * TILE_WIDTH, 0));

			while (ObstacleAvoidance.getIsAvoiding()) {

			}

		}

		
	}

}
