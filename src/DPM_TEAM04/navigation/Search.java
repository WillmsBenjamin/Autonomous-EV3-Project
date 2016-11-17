package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.CoordinateSystem;

public class Search extends Thread {

	public Search() {
		
	}
	
	
	public void run() {
		
		Driver driver = new Driver();
		
		// Go to the search point
//		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		
		
		while(true){
			double USDistance = Resources.getFrontUSData();
			
			if (USDistance < 5) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				break;
			} else if (USDistance < US_FRONT_CLIP) {
				// move forward
				leftMotor.setSpeed(SPEED_TURNING_SLOW);
				rightMotor.setSpeed(SPEED_TURNING_SLOW);
				leftMotor.forward();
				rightMotor.forward();
			} else {
				leftMotor.setSpeed(SPEED_SCANNING);
				rightMotor.setSpeed(SPEED_SCANNING);
				leftMotor.backward();
				rightMotor.forward();
			}
		}
		
		// Close to block at this point
		
		//purposely collide into block
		driver.rotate(10, CoordinateSystem.POLAR_DEG);
		driver.travelDistance(getFrontUSData() + 2);
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
	
	
	
}