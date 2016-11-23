package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;
import DPM_TEAM04.test.ObstacleAvoidTest;
import lejos.hardware.Sound;
import lejos.hardware.Audio;
import lejos.robotics.geometry.Point2D;


/**
 * 
 * 
 * @author Tristan Toupin & alexisgj
 *
 */

public class ObstacleAvoidance extends Thread {

	private DirectedCoordinate position;
	private Driver driver = Driver.getDriver();
	private Object Audio;
	public static Object lock;
	public static boolean isAvoiding = false;

	public ObstacleAvoidance() {
		lock = new Object();
	}

	public void run() {

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		while (true) {
			position = Odometer.getOdometer().getPosition();

			while (!isAvoiding) {
				isThereObstacle();
			}

			if (isAvoiding && driver.isTravelling) {
				synchronized (driver) {
					driver.interrupt();
				}
				Sound.beep();

				double firstAng = position
						.getDirection(CoordinateSystem.POLAR_DEG);

				leftMotor.stop(true);
				rightMotor.stop(false);
				leftMotor.setAcceleration(ACCELERATION_FAST);
				rightMotor.setAcceleration(ACCELERATION_FAST);

				driver.rotate(-90, CoordinateSystem.POLAR_DEG);
				leftMotor.stop(true);
				rightMotor.stop(false);
				if (getFrontUSData() > 1.0 * TILE_WIDTH) {
					while (Math.abs((firstAng + 360) - position.getDirection(CoordinateSystem.POLAR_DEG)) % 360 > 25) {
						avoidBlock();
					}
					leftMotor.stop(true);
					rightMotor.stop(false);
					driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN,
							searchPoint.x, searchPoint.y)));
				} else {
					driver.rotate(180, CoordinateSystem.POLAR_DEG);
					driver.travelDistance(getFrontUSData());
					leftMotor.stop(true);
					rightMotor.stop(false);
					driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN,
							searchPoint.x, searchPoint.y)));
				}
			}
		}
	}

	private void isThereObstacle() {
		if (getFrontUSData() > 10) {
			synchronized (lock) {
				isAvoiding = false;
			}
		} else {
			synchronized (lock) {
				isAvoiding = true;
			}
			leftMotor.stop(true);
			rightMotor.stop(false);
		}
	}

	private void avoidBlock() {
		double distance;
		double speed;
		leftMotor.setSpeed(SPEED_AVOIDING_INBETWEEN);
		distance = getSideUSData();
		speed = (35 * (distance) - 600);
		if (speed > SPEED_AVOIDING_MAX) {
			speed = SPEED_AVOIDING_MAX;
		} else if (speed < SPEED_AVOIDING_MIN) {
			speed = SPEED_AVOIDING_MIN;
		}

		rightMotor.setSpeed((float) speed);

		leftMotor.forward();
		rightMotor.forward();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}
}
