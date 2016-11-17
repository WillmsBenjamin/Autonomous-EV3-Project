package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;

public class Search extends Thread {
	
	private DirectedCoordinate position;

	public Search() {
		
	}
	
	
	public void run() {
		
		Driver driver = new Driver();
		position = Odometer.getOdometer().getPosition();
		
		// Go to the search point
//		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		
		boolean blockSeen = false;
		int blockDistanceCap = 5;
		int searchCap = 50;
		double lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
		boolean clockwise = true; //if true will rotate clockwise, else counter Clockwise
		double angleDifference;
		
		
		while(true){
			double USDistance = Resources.getFrontUSData();
			
			if (!blockSeen || USDistance > searchCap) {
				scanning(clockwise);
			}
			
			if (USDistance < blockDistanceCap) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				break;
			} else if (USDistance <= searchCap) {
				blockSeen = true;
				// move forward
				leftMotor.setSpeed(SPEED_TURNING_MEDIUM);
				rightMotor.setSpeed(SPEED_TURNING_MEDIUM);
				leftMotor.forward();
				rightMotor.forward();
				lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
			} else if (blockSeen) {
				//we went too far, re-initialize values so its at initial state (no block seen) and turn the other way around.
				System.out.println("\n\n\n\n\n" + lastAngle + "\n" + position.getDirection(CoordinateSystem.POLAR_DEG));
				double actualAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
				angleDifference = Math.abs(actualAngle-lastAngle);
				if (angleDifference >= 180) {
					// Doesn't make sense to have such a big difference, so one angle must have been cap
					// add 360 deg to "uncap" and compute the real difference between the angles
					if (actualAngle > lastAngle) {
						lastAngle += 360;
					} else {
						actualAngle += 360;
					}
					angleDifference = Math.abs(actualAngle-lastAngle);
				}
				if (angleDifference > 25) {
					
					blockSeen = false;
					if (clockwise){
						clockwise = false;
					}else {
						clockwise = true;
					}
				}
			}
		}
		
		
		// Close to block at this point
		
		//purposely collide into block
		if (clockwise) {
			driver.rotate(7, CoordinateSystem.POLAR_DEG);
		} else {
			driver.rotate(-7, CoordinateSystem.POLAR_DEG);
		}
		driver.travelDistance(blockDistanceCap);
		int colorID = (int) getColorID();
		if(colorID == 6 || colorID == 7) {
			System.out.println("Block!");
		}
		else {
			System.out.println("Not Block");
		}
		
		/*
		if (searchPoint.x < builderZone.getCenterX()) {
			if (searchPoint.y < builderZone.getCenterY()) {
				// Bottom left quadrant
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
			} else {
				// Top left quadrant
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
			}
		} else {
			if (searchPoint.y < builderZone.getCenterY()) {
				// Bottom right quadrant
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
			} else {
				// Top right quadrant
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.rotate(180, CoordinateSystem.POLAR_DEG);
			}
		}*/
	}
	
	private void scanning(boolean isClockwise){
		if (isClockwise){
			leftMotor.setSpeed(SPEED_SCANNING);
			rightMotor.setSpeed(SPEED_SCANNING);
			leftMotor.backward();
			rightMotor.forward();
		}else {
			leftMotor.setSpeed(SPEED_SCANNING);
			rightMotor.setSpeed(SPEED_SCANNING);
			leftMotor.forward();
			rightMotor.backward();
		}
		return;
	}
	
	
	
}