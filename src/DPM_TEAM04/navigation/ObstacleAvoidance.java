package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;
import DPM_TEAM04.test.ObstacleAvoidTest;
import lejos.robotics.geometry.Point2D;

public class ObstacleAvoidance extends Thread {

	private DirectedCoordinate position;
	private Driver driver;
	public static Object lock;
	public static boolean isAvoiding = false;

	public ObstacleAvoidance() {
		lock = new Object();
	}

	public void run() {

		Driver driver = new Driver();
		position = Odometer.getOdometer().getPosition();
		
		searchPoint = new Point2D.Double(0.0, 0.0);

		while (true) {

			while (Resources.getFrontUSData() > 20) {

			}

			synchronized (lock) {
				isAvoiding = true;
			}

			leftMotor.stop(true);
			rightMotor.stop(false);

			driver.rotate(-90, CoordinateSystem.POLAR_DEG);

			// p controller takes control until it can go back to the desired
			// position

			leftMotor.setSpeed(150);
			rightMotor.setSpeed(150);

			double distance;
			double speed;

			while (true) {

				distance = Resources.getSideUSData();
				speed = (50.0 * (distance) - 700);
				if (speed > 300) {
					speed = 300;
				} else if (speed < 100) {
					speed = 0;
				}

				rightMotor.setSpeed((float) speed);

				leftMotor.forward();
				rightMotor.forward();

				if (Resources.getFrontUSData() < 20) {
					leftMotor.stop(true);
					rightMotor.stop(false);

					driver.rotate(-90, CoordinateSystem.POLAR_DEG);
				}
				
				if (thetaInRange(Math.PI/6.0, (position.getTheta(CoordinateSystem.HEADING_DEG)*(2.0*Math.PI)/360.0)) && Resources.getFrontUSData() > 20) {
					break;
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}

			}
			
			driver.travelTo(new Coordinate(CoordinateSystem.CARTESIAN, 2 * TILE_WIDTH, 0));

			/*
			 * synchronized (lock) { isAvoiding = false; }
			 */

		}

	}

	public boolean thetaInRange(double bandWidth, double trueTheta) {

		// Initialize variables
		double x_diff, y_diff, desiredTheta, thetaRangeLow, thetaRangeHigh, trueX, trueY;

		boolean inRange; // boolean to know if the heading is in range of the
							// desired theta
		trueX = position.getX();
		trueY = position.getY();
		x_diff = searchPoint.x - trueX; // Compute the X and Y deltas
										// (difference)
		y_diff = searchPoint.y - trueY;
		desiredTheta = getDesiredTheta(); // Get the desired theta

		// Compute the absolute distance to the desired point
		double distance = Math.sqrt((Math.pow(x_diff, 2) + Math.pow(y_diff, 2)));
		if (bandWidth >= Math.PI / 10) {

			// Unless the bandWidth is really small (when the method is called
			// to know if it goes straight to the point)
			// Adjust the bandWidth according to a linear function having a
			// small angle range when it is "far" from the position
			// and a bigger range when it is closer
			bandWidth = 4 * Math.PI / distance;
			if (bandWidth >= (2.0 * Math.PI)) {
				bandWidth = (2.0 * Math.PI);
			}
		}

		// Compute the range of theta with the bandWidth
		thetaRangeLow = desiredTheta - bandWidth;
		thetaRangeHigh = (desiredTheta + bandWidth) % (2.0 * Math.PI);
		if (thetaRangeLow < 0) {
			// If the lower bound is negative, add 2*PI
			thetaRangeLow += (2.0 * Math.PI);
		}
		// Condition to avoid issues when the range passes 2*PI to 0
		if (thetaRangeHigh < thetaRangeLow) {
			if (((trueTheta >= thetaRangeLow) && (trueTheta < (2.0 * Math.PI)))
					|| ((trueTheta <= thetaRangeHigh) && (trueTheta >= 0))) {
				// If the angle falls between the lower bound and 2PI or the
				// upper bound and 0
				inRange = true;
			} else {
				inRange = false;
			}
		} else if ((trueTheta >= thetaRangeLow) && ((trueTheta <= thetaRangeHigh))) {
			// If it is in the range
			inRange = true;
		} else {
			inRange = false;
		}
		return inRange;
	}

	public double getDesiredTheta() {

		double x_diff, y_diff, desiredTheta;
		// If the robot's heading is not facing the desired theta to go to the
		// position,
		// compute its desired theta and turn to this heading
		x_diff = searchPoint.x - position.getX(); // Compute the X and Y difference
											// (desired and actual values)
		y_diff = searchPoint.y - position.getY();
		desiredTheta = Math.atan2(x_diff, y_diff); // Get the angle from delta X
													// and Y
		if (desiredTheta < 0) {
			desiredTheta += (2.0*Math.PI); // If the angle is negative, add 2*PI
		}

		return desiredTheta; // Return the desired theta (towards the position)
	}

	public static boolean getIsAvoiding() {
		synchronized (lock) {
			return isAvoiding;
		}
	}

}
