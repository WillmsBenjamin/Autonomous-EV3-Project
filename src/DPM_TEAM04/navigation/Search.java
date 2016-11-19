package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;

import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.odometry.Odometer;
import lejos.hardware.Audio;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.robotics.geometry.Point2D;
import lejos.robotics.geometry.Rectangle2D;

public class Search extends Thread {
	
	private DirectedCoordinate position;
	private Driver driver;
	private boolean blockSeen = false;
	private int blockDistanceCap = 5;
	private int searchCap = 70;
	private double lastAngle;
	private boolean clockwise = true; 				//if true will rotate clockwise, else counter Clockwise
	private double angleDifference, actualAngle;
	private double firstAngle, startSearchAngle, endSearchAngle;

	public Search() {
		
	}
	
	
	public void run() {
		
		driver = new Driver();
		position = Odometer.getOdometer().getPosition();
		
		grabMotor.setAcceleration(ACCELERATION_SMOOTH);
		liftMotor.setAcceleration(ACCELERATION_SMOOTH);
		grabMotor.setSpeed(SPEED_GRAB);
		liftMotor.setSpeed(SPEED_LIFT);
		
		searchPoint = new Point2D.Double(1.0*TILE_WIDTH, 1.0*TILE_WIDTH);
		mapCenter = new Point2D.Double(((MAP_DIMENSION/2.0)-1.0)*30.48, ((MAP_DIMENSION/2.0)-1.0)*30.48);
		builderZone = new Rectangle2D.Double(1.0*TILE_WIDTH, 1.0*TILE_WIDTH, TILE_WIDTH, TILE_WIDTH);
		
		startSearchAngle = 0;
		endSearchAngle = 90;
		
		
		// Go to the search point
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		
		
		if (searchPoint.x < mapCenter.x) {
			if (searchPoint.y < mapCenter.y) {
				// Bottom left quadrant
				driver.turnTo(0, CoordinateSystem.POLAR_DEG, false);
			} else {
				// Top left quadrant
				driver.turnTo(270, CoordinateSystem.POLAR_DEG, false);
			}
		} else {
			if (searchPoint.y < mapCenter.y) {
				// Bottom right quadrant
				driver.turnTo(90, CoordinateSystem.POLAR_DEG, false);
				
			} else {
				// Top right quadrant
				driver.turnTo(180, CoordinateSystem.POLAR_DEG, false);
			}
		}
		
		
		
		
		search();
		
		
		/*
		
		leftMotor.flt();
		rightMotor.flt();
		
		double distanceToTheWall;
		double theta;
		double x;
		double y;
		
		

		do {
			
			theta = position.getDirection(CoordinateSystem.POLAR_DEG);
			x = position.getX();
			y = position.getY();
			
			distanceToTheWall = -1;
			
			if ((theta > 7.0/4.0*Math.PI && theta <= 0) || (theta >= 0 && theta <= 1.0/4.0*Math.PI)) {
				// Facing front wall
				
				distanceToTheWall = (MAP_DIMENSION*TILE_WIDTH - y)/Math.cos(theta);
				
			} else if (theta > 1.0/4.0*Math.PI && theta <= 3.0/4.0*Math.PI) {
				// Facing right wall
				
				distanceToTheWall = (MAP_DIMENSION*TILE_WIDTH - x)/Math.cos(theta);
				
			} else if (theta > 3.0/4.0*Math.PI && theta <= 5.0/4.0*Math.PI) {
				// Facing back wall
				
				distanceToTheWall = y/Math.cos(theta);
				
			} else if (theta > 5.0/4.0*Math.PI && theta <= 7.0/4.0*Math.PI) {
				// Facing left wall
				
				distanceToTheWall = x/Math.cos(theta);
				
			}
			
			
			theta = theta*360/(2*Math.PI);
			
			
			
			// clear the display
			lcd.clear();
			
			lcd.drawString("Theta:  ", 0, 0);
			lcd.drawInt((int)theta, 7, 0);
			lcd.drawString("Wall distance:  ", 0, 1);
			lcd.drawInt((int)distanceToTheWall, 0, 2);
			lcd.drawString("cm              ", 0, 3);
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
			
			
			
			
		} while (true);
		
		*/
		
		// Close to block at this point
		
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
	
	
	private void search() {
		blockSeen = false;
		lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
		clockwise = true;
		boolean firstTime = true;
		
		while(true){
			double USDistance = Resources.getFrontUSData();
			
			if (!blockSeen || USDistance > searchCap) {
				scanning();
			}
			
			if (USDistance < blockDistanceCap) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				break;
			} else if (isObjectSeen(USDistance)) {
				blockSeen = true;
				// move forward
				leftMotor.setSpeed(SPEED_TURNING_MEDIUM);
				rightMotor.setSpeed(SPEED_TURNING_MEDIUM);
				leftMotor.forward();
				rightMotor.forward();
				lastAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
				
				if (firstTime) {
					firstAngle = lastAngle;
					firstTime = false;
				}
			} else if (blockSeen) {
				//we went too far, re-initialize values so its at initial state (no block seen) and turn the other way around.
				actualAngle = position.getDirection(CoordinateSystem.POLAR_DEG);
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
		
		scanBlock();
		
	}
	private boolean isObjectSeen(double USDistance) {
		
		if (USDistance <= searchCap) {
			
			double theta = position.getDirection(CoordinateSystem.CARTESIAN);
			Point2D.Double ObjectPoint = new Point2D.Double(USDistance*Math.cos(theta)+TILE_WIDTH, USDistance*Math.sin(theta)+TILE_WIDTH);
			Rectangle2D.Double goodMap = new Rectangle2D.Double(0.0, 0.0, (MAP_DIMENSION-2.0)*TILE_WIDTH, (MAP_DIMENSION-2.0)*TILE_WIDTH);
			
			//System.out.println("\n\n\n\n\n" + ObjectPoint.x + "\n" + ObjectPoint.y);
			System.out.println("\n\n\n\n\n" + theta);
			
			if (goodMap.contains(ObjectPoint)) {
				// good
				//System.out.println("\n\n\n\n\nIn the map!");
				return true;
			} else {
				// is too close to a wall
				//System.out.println("\n\n\n\n\nToo close to wall!");
				wallSeen();
				return false;
			}
			
		} else {
			return false;
		}
		
	}
	private void scanning(){
		if (clockwise){
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
	
	private void scanBlock() {
		//purposely collide into block
		if (clockwise) {
			driver.rotate(7, CoordinateSystem.POLAR_DEG);
		} else {
			driver.rotate(-7, CoordinateSystem.POLAR_DEG);
		}
		//driver.travelDistance(blockDistanceCap);
		float[] colorRGB = getColorRGB();
		if(colorRGB[1] > colorRGB[0] && colorRGB[1] > colorRGB[2]) {
			System.out.println("Block!");
			
			// Place the block in a good direction
			driver.travelDistance(-1.0);
			driver.rotate(Math.PI, CoordinateSystem.POLAR_RAD);
			driver.travelDistance(-Math.abs(BUMPER_TO_CENTER-US_TO_CENTER)-2.0);
			
			// orient the block to make it easier to grab
			if (clockwise) {
				rightMotor.rotate(-40, false);
			} else {
				leftMotor.rotate(-40, false);
			}
			driver.travelDistance(-4.0);
			
			// grab the block
			grabMotor.rotate(200, false);
			liftMotor.rotate(-450, false);
			
			
			// return to the center of the builder zone
			driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, (builderZone.getCenterX()+BUMPER_TO_CENTER+4.0), builderZone.getCenterY())));
			
			// turn to the 0 degrees to drop the block
			driver.turnTo(0, CoordinateSystem.POLAR_DEG, false);
			
			// drop the block
			liftMotor.rotate(450, false);
			grabMotor.rotate(-200, false);
			
			// turn to the next angle to search
			double nextAngle = (lastAngle + 25)%360.0;
			driver.turnTo(nextAngle, CoordinateSystem.POLAR_DEG);
			
			clockwise = true;
			search();
			
		}
		else {
			System.out.println("Not Block");
			notBlock();
		}
	}
	private void notBlock() {
		driver.travelDistance(-BUMPER_TO_CENTER);
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		double nextAngle = (lastAngle + 25)%360.0;
		driver.turnTo(nextAngle, CoordinateSystem.POLAR_DEG);
		clockwise = true;
		search();
	}
	private void wallSeen() {
		double theta = position.getDirection(CoordinateSystem.POLAR_DEG);
		theta = (theta + 25)%360.0;
		driver.turnTo(theta, CoordinateSystem.POLAR_DEG);
		clockwise = true;
		search();
	}
	
	
	
}