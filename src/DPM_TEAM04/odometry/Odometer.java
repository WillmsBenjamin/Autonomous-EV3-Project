package DPM_TEAM04.odometry;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;

import static DPM_TEAM04.Resources.*;

public class Odometer extends Thread {
	// robot position
	private DirectedCoordinate position;

	private int leftMotorTachoCount, rightMotorTachoCount;

	private static Odometer theInstance;

	// default constructor
	private Odometer() {
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		position = new DirectedCoordinate(CoordinateSystem.CARTESIAN, 0, 0, 0,
				CoordinateSystem.POLAR_RAD);
	}

	public static Odometer getOdometer() {
		if (theInstance == null) {
			theInstance = new Odometer();
		}
		return theInstance;
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();

			// Get the tacho count
			int newLeftMotorTachoCount = leftMotor.getTachoCount();
			int newRightMotorTachoCount = rightMotor.getTachoCount();

			// Compute the distance traveled during the last iteration
			double distL = Math.PI * Resources.WHEEL_RADIUS
					* (newLeftMotorTachoCount - leftMotorTachoCount) / 180;
			double distR = Math.PI * Resources.WHEEL_RADIUS
					* (newRightMotorTachoCount - rightMotorTachoCount) / 180;

			// Store last tacho count for the next iteration
			leftMotorTachoCount = newLeftMotorTachoCount;
			rightMotorTachoCount = newRightMotorTachoCount;

			// Compute the distance the measured center moved
			double deltaD = 0.5 * (distL + distR);

			// Compute the change in the angle (in radians)
			double deltaT = (distR - distL) / Resources.TRACK;

			double currentHeading = position
					.getDirection(CoordinateSystem.POLAR_RAD);

			// Compute the x and y displacement of the movement
			double dx = deltaD * Math.cos(currentHeading);
			double dy = deltaD * Math.sin(currentHeading);

			position.incrementCoordinate(CoordinateSystem.CARTESIAN, dx, dy,
					deltaT, CoordinateSystem.POLAR_RAD);

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < Resources.ODOMETER_PERIOD) {
				try {
					Thread.sleep(Resources.ODOMETER_PERIOD
							- (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// Position accessor
	public DirectedCoordinate getPosition() {
		return position;
	}
}
