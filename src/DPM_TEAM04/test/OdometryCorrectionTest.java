package DPM_TEAM04.test;

import static DPM_TEAM04.Resources.DISPLAY_PERIOD;
import static DPM_TEAM04.Resources.TILE_WIDTH;
import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.logging.DataEntryProvider;
import DPM_TEAM04.logging.LCDLogger;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.odometry.Odometer;
import DPM_TEAM04.odometry.OdometryCorrection;
import lejos.hardware.Button;

public class OdometryCorrectionTest {

	public static final int VERSION_NB = 1;

	public static void main(String[] args) {
		Resources.initialize = true;

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
				return odometer.getPosition().getDirection(
						CoordinateSystem.POLAR_DEG);
			}
		};

		LCDLogger lcdLog = new LCDLogger(DISPLAY_PERIOD, 2, versionProvider,
				xProvider, yProvider, tProvider);

		odometer.start();
		lcdLog.start();

		Driver driver = Driver.getDriver();
		
		OdometryCorrection odoCorrection = new OdometryCorrection();
		
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);
		odoCorrection.doCorrection();

		Button.waitForAnyPress();
		driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 0, 0));
		driver.turnTo(0.0, CoordinateSystem.HEADING_DEG);
		
		Button.waitForAnyPress();
		System.exit(0);
	}

}

