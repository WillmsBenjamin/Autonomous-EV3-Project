package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import java.util.Queue;

import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;

/**
 * Coordinate system used in this class is compass heading
 * 
 * North ^ ^ +r |- / | \ / +theta (deg only) | / | / |/ +-------------> East (0
 * angle is y axis)
 * 
 */

public class Driver extends Thread {

	public Queue<Coordinate> waypoints;
	public Coordinate destination;
	private Object lock;
	public boolean isTravelling;
	private static Driver driverInstance;

	public static Driver getDriver() {
		if (driverInstance == null) {
			driverInstance = new Driver();
		}
		return driverInstance;
	}

	private Driver() {
		lock = new Object();
		destination = new Coordinate(CoordinateSystem.CARTESIAN, 0.0, 0.0);

		leftMotor.stop();
		leftMotor.setAcceleration(ACCELERATION_SMOOTH);

		rightMotor.stop();
		rightMotor.setAcceleration(ACCELERATION_SMOOTH);
	}

	@Override
	public void run() { // Unused for Lab 4

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// Not expected to be interrupted
		}

		while (!waypoints.isEmpty()) { // cycle through all waypoints

			travelTo(waypoints.poll());

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

	}

	/**
	 * Orient and travel to a new destination
	 * 
	 * @param dest
	 *            The Coordinate to travel to
	 */
	public void travelTo(Coordinate dest) {
		isTravelling = true;
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setAcceleration(ACCELERATION_SMOOTH);
		rightMotor.setAcceleration(ACCELERATION_SMOOTH);

		destination = dest;
		// destination.setCoordinate(CoordinateSystem.CARTESIAN,dest.getX(),dest.getY());

		// destination = dest.getCoordinates(CoordinateSystem.CARTESIAN);

		// clear lines
		/*
		 * LCD.drawString("                  ", 0, 5);
		 * LCD.drawString("                  ", 0, 6);
		 * 
		 * LCD.drawString("Travelling to (x,y):", 0, 5);
		 * 
		 * 
		 * 
		 * //String of coordinates (X.XX, Y.YY) String toPrint = "(" +
		 * LCDLogger.formattedDoubleToString(dest.getX(), 2) + ", " +
		 * LCDLogger.formattedDoubleToString(dest.getY(), 2) + ")";
		 * LCD.drawString(toPrint, 0, 6);
		 */

		// get distance and heading changes
		double distanceToC = Odometer.getOdometer().getPosition()
				.distanceTo(dest);
		double changeInHeading = Odometer.getOdometer().getPosition()
				.angleTo(dest, CoordinateSystem.HEADING_DEG);

		// Orient to destination
		rotate(changeInHeading, CoordinateSystem.HEADING_DEG);

		// Travel to destination
		travelDistance(distanceToC);

		leftMotor.stop();
		rightMotor.stop();
		isTravelling = false;

	}
	public void travelTo(Coordinate dest, boolean immediateReturn) {
		isTravelling = true;
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setAcceleration(ACCELERATION_SMOOTH);
		rightMotor.setAcceleration(ACCELERATION_SMOOTH);

		destination = dest;

		// get distance and heading changes
		double distanceToC = Odometer.getOdometer().getPosition()
				.distanceTo(dest);
		double changeInHeading = Odometer.getOdometer().getPosition()
				.angleTo(dest, CoordinateSystem.HEADING_DEG);

		// Orient to destination
		rotate(changeInHeading, CoordinateSystem.HEADING_DEG);

		// Travel to destination
		travelDistance(distanceToC, immediateReturn);

		//isTravelling = false;

	}
	public void travelToWithoutSavingDestination(Coordinate dest) {
		isTravelling = true;
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setAcceleration(ACCELERATION_SMOOTH);
		rightMotor.setAcceleration(ACCELERATION_SMOOTH);

		// get distance and heading changes
		double distanceToC = Odometer.getOdometer().getPosition()
				.distanceTo(dest);
		double changeInHeading = Odometer.getOdometer().getPosition()
				.angleTo(dest, CoordinateSystem.HEADING_DEG);

		// Orient to destination
		rotate(changeInHeading, CoordinateSystem.HEADING_DEG);

		// Travel to destination
		travelDistance(distanceToC);

		//isTravelling = false;

	}

	public void rotate(double angle, CoordinateSystem angleUnit) {
		rotate(angle, angleUnit, false);
	}

	/**
	 * Rotates robot in place
	 * 
	 * @param angle
	 *            The amount of rotation
	 * @param angleUnit
	 *            Units of angle
	 * @param immediateReturn
	 *            true if to return right away, false if otherwise
	 */

	@SuppressWarnings("incomplete-switch")
	public void rotate(double angle, CoordinateSystem angleUnit,
			boolean immediateReturn) {
		leftMotor.setSpeed(SPEED_TURNING_FAST);
		rightMotor.setSpeed(SPEED_TURNING_FAST);

		double angleHeadingDeg = angle;
		// normalize angle to Heading Deg
		switch (angleUnit) {
		case CARTESIAN:
		case POLAR_RAD:
			angleHeadingDeg = -Math.toDegrees(angle);
			break;
		case POLAR_DEG:
			angleHeadingDeg = -angle;
			break;
		}

		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, angleHeadingDeg),
				true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, angleHeadingDeg),
				immediateReturn);
	}

	public void turnTo(double angle, CoordinateSystem angleUnit) {
		turnTo(angle, angleUnit, false);
	}

	public void turnTo(double angle, CoordinateSystem angleUnit,
			boolean immediateReturn) {
		DirectedCoordinate position = Odometer.getOdometer().getPosition();
		double changeInAngle = position.directionTo(angle, angleUnit);
		rotate(changeInAngle, angleUnit, immediateReturn);
	}

	public void setIsTravelling(boolean boolValue) {
		synchronized (lock) {
			isTravelling = boolValue;
		}
	}
	public boolean getIsTravelling() {
		synchronized (lock) {
			return isTravelling;
		}
	}
	
	/**
	 * Travels a distance in cm
	 * 
	 * @param distance
	 *            How far to travel in cm
	 */
	public void travelDistance(double distance) {
		travelDistance(distance, false);
	}

	public void travelDistance(double distance, boolean immediateReturn) {
		leftMotor.setSpeed(SPEED_FORWARD);
		rightMotor.setSpeed(SPEED_FORWARD);

		leftMotor.rotate(convertDistance(WHEEL_RADIUS, distance), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, distance),
				immediateReturn);
	}

	// converts a traveled distance to an wheel angular displacement in degrees
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// converts a turning circle HEADING_DEG displacement into a motor rotation
	// angluar displacement
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	public Coordinate getDestination() {
		return destination;
	}

}