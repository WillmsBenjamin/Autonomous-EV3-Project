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

			while (!isSearching && !getIsAvoiding()) {
				if (driver.getIsTravelling()) {
					isThereObstacle();
				}
			}

			if (getIsAvoiding()) {
				position = Odometer.getOdometer().getPosition();
				
				synchronized (driver) {
					driver.interrupt();
					driver.setIsTravelling(false);
				}
				Sound.beep();
				double firstAng = position.getDirection(CoordinateSystem.POLAR_DEG);

				leftMotor.stop(true);
				rightMotor.stop(false);
				/*
				leftMotor.setAcceleration(ACCELERATION_FAST);
				rightMotor.setAcceleration(ACCELERATION_FAST);
				*/

				driver.rotate(-90, CoordinateSystem.POLAR_DEG);
				
				while (Math.abs((firstAng + 360) - position.getDirection(CoordinateSystem.POLAR_DEG)) % 360 > 25) {
					avoidBlock();
					if (getFrontUSData() < 10) {
						leftMotor.stop(true);
						rightMotor.stop(false);
						driver.rotate(-90, CoordinateSystem.POLAR_DEG);
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
		double USDistance = getFrontUSData();
		
		/*
		 * START OF NEW CODE : Compute the dangerous circle of the red zone and if the robot is inside, start the obstacle avoidance
		 
		Point2D topRightCornerPt = new Point2D.Double(collectorZone.getMaxX(), collectorZone.getMaxY());
		Point2D centerOfCollector = new Point2D.Double(collectorZone.getCenterX(), collectorZone.getCenterY());
		double minimalDistanceFromCenterOfColleter = Math.abs(topRightCornerPt.distance(centerOfCollector)) + TILE_WIDTH;
		Point2D actualPosition = new Point2D.Double(position.getX(), position.getY());
		
		if (Math.abs(actualPosition.distance(centerOfCollector)) < minimalDistanceFromCenterOfColleter) {
			setIsAvoiding(true);
			return;
		}

		 * END OF NEW CODE
		 */
		
		if (USDistance > 10) {
			setIsAvoiding(false);
			return;
		} else {
			leftMotor.stop(true);
			rightMotor.stop(false);
			
			if (USDistance > 10) {
				USDistance = 10;
			}
			driver.travelDistance(USDistance - 4);
			float[] colorRGB = getColorRGB();
			if (colorRGB[1] > colorRGB[0] && colorRGB[1] > colorRGB[2]) {
				driver.rotate(-30, CoordinateSystem.POLAR_DEG);
				driver.travelDistance(8);
				driver.rotate(60, CoordinateSystem.POLAR_DEG);
				
				
				leftMotor.stop(true);
				rightMotor.stop(false);
				leftMotor.setAcceleration(ACCELERATION_SMOOTH);
				rightMotor.setAcceleration(ACCELERATION_SMOOTH);
				
				driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, driver.destination.getX(), driver.destination.getY())), true);
				
				setIsAvoiding(false);
				return;
			} else {
				setIsAvoiding(true);
				return;
			}

		}
	}

	//STEP PCONTROLLER FCT
	private void avoidBlock() {
		double distance;
		double speed;
		leftMotor.setSpeed(SPEED_AVOIDING_INBETWEEN);
		distance = getSideUSData();
		
		/*
		 * START OF NEW CODE : Compute the dangerous circle of the red zone
		 * 
		 * If the robot is inside the zone, avoid it
		 * If the distance to the zone is smaller than 10, avoid it
		 
		
		Point2D topRightCornerPt = new Point2D.Double(collectorZone.getMaxX(), collectorZone.getMaxY());
		Point2D centerOfCollector = new Point2D.Double(collectorZone.getCenterX(), collectorZone.getCenterY());
		double minimalDistanceFromCenterOfColleter = Math.abs(topRightCornerPt.distance(centerOfCollector)) + TILE_WIDTH;
		Point2D actualPosition = new Point2D.Double(position.getX(), position.getY());
		double distanceFromZoneRadially = Math.abs(minimalDistanceFromCenterOfColleter - Math.abs(actualPosition.distance(centerOfCollector)));
		
		if (Math.abs(actualPosition.distance(centerOfCollector)) < minimalDistanceFromCenterOfColleter) {
			distance = distanceFromZoneRadially;
		} else if (distanceFromZoneRadially < 10 && distance >= 10) {
			distance = distanceFromZoneRadially;
		}
		
		
		 * END OF NEW CODE
		 */
		
		
		//change distance max and min
		if (distance > 30){				//max distance
			speed = SPEED_AVOIDING_MAX;
		} else if (distance < 10){		//min distance
			speed = SPEED_AVOIDING_MIN;
		}else {							//in between
			speed = SPEED_AVOIDING_INBETWEEN;
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

	public static void stopObsAvoid() throws InterruptedException {
		Thread.sleep(50000000);
	}
	
	
}
