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
	public static Object lock;
	private static boolean isAvoiding = false;
	private Coordinate pointBefore;

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

			while (!getIsAvoiding()) {
				if (driver.getIsTravelling()) {
					isThereObstacle();
				}
			}

			if (getIsAvoiding()) {
				synchronized (driver) {
					driver.interrupt();
					driver.setIsTravelling(false);
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
				if (getFrontUSData() > (2.0 * TILE_WIDTH)) {
					
					while (Math.abs((firstAng + 360) - position.getDirection(CoordinateSystem.POLAR_DEG)) % 360 > 25) {
						avoidBlock();
						if (getFrontUSData() < 10) {
							leftMotor.stop(true);
							rightMotor.stop(false);
							driver.rotate(-90, CoordinateSystem.POLAR_DEG);
						} 
					}
					
				} else {
					
					leftMotor.stop(true);
					rightMotor.stop(false);
					leftMotor.setAcceleration(ACCELERATION_SMOOTH);
					rightMotor.setAcceleration(ACCELERATION_SMOOTH);
					pointBefore = new Coordinate(CoordinateSystem.CARTESIAN, position.getX(), position.getY());
					if (!isHoldingBlock) {
						leftMotor.forward();
						rightMotor.forward();
						while(getFrontUSData() > 4.0) {
							
						}
						leftMotor.stop(true);
						rightMotor.stop(false);
						
					}
					float[] colorRGB = getColorRGB();
					if (colorRGB[1] > colorRGB[0] /*&& colorRGB[1] > colorRGB[2]*/ && !isHoldingBlock) {
						Search.captureBlockWhileAvoiding();
						leftMotor.stop(true);
						rightMotor.stop(false);
						driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
					} else {
						leftMotor.setAcceleration(ACCELERATION_SMOOTH);
						rightMotor.setAcceleration(ACCELERATION_SMOOTH);
						
						driver.rotate(180, CoordinateSystem.POLAR_DEG);
						
						driver.travelToWithoutSavingDestination(pointBefore);
						double USDistance = getFrontUSData();
						if (USDistance > (2.0*TILE_WIDTH)) {
							USDistance = 2.0*TILE_WIDTH;
						}
						USDistance -= (US_TO_CENTER+5.0);
						driver.travelDistance(USDistance);
					}
					
				}
				
				Sound.beep();
				
				leftMotor.stop(true);
				rightMotor.stop(false);
				leftMotor.setAcceleration(ACCELERATION_SMOOTH);
				rightMotor.setAcceleration(ACCELERATION_SMOOTH);
				
				driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
				
				setIsAvoiding(false);
			}
		}
	}

	private void isThereObstacle() {
		if (getFrontUSData() > 10) {
			setIsAvoiding(false);
		} else {
			leftMotor.stop(true);
			rightMotor.stop(false);
			leftMotor.setAcceleration(ACCELERATION_SMOOTH);
			rightMotor.setAcceleration(ACCELERATION_SMOOTH);
			driver.travelDistance(getFrontUSData() - 1.0);
			float[] colorRGB = getColorRGB();
			if (colorRGB[1] > colorRGB[0] /*&& colorRGB[1] > colorRGB[2]*/ && !isHoldingBlock) {
				Search.captureBlockWhileAvoiding();
				leftMotor.stop(true);
				rightMotor.stop(false);
				driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
			} else {
				setIsAvoiding(true);
			}

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
	
	public static void setIsAvoiding(boolean boolValue) {
		synchronized (lock) {
			isAvoiding = boolValue;
		}
	}
	public static boolean getIsAvoiding() {
		synchronized (lock) {
			return isAvoiding;
		}
	}
	
	
}
