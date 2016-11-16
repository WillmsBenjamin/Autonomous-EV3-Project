package DPM_TEAM04.odometry;

import java.util.ArrayList;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.Resources;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.geometry.DirectedCoordinate;
import DPM_TEAM04.geometry.Distance;
import DPM_TEAM04.navigation.Driver;
import DPM_TEAM04.navigation.Navigation;
import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Delay;
import lejos.robotics.geometry.*;

/**
 * This class localizes the robot using the ultrasonic sensor.
 * 
 * It is started by the Main class and extends Thread. It scans its environment
 * and bumps into the closest wall (the minimal distance seen). It after bumps
 * into the other wall and sets the x, y and theta values of the odometer.
 * 
 * @author Alexis Giguere-Joannette & Tristan Saumure-Toupin
 * @version 1.0
 */
public class Localization extends Thread {

	private double minDistance, minDistAngle;
	private ArrayList<Distance> listOfDistances;
	private boolean isLeftWall;
	
	private DirectedCoordinate position;

	public Localization() {
		listOfDistances = new ArrayList<Distance>();
	}

	/**
	 * Code executed when the Thread is started.
	 */
	public void run() {
		
		
		
		
		Navigation navigator = new Navigation(Odometer.getOdometer());
		
		position = Odometer.getOdometer().getPosition();
		
		Driver driver = new Driver();
		
		// set the smooth acceleration (default)
		navigator.setAcceleration(-1);

		this.minDistance = Resources.getFrontUSData();
		this.minDistAngle = Odometer.getOdometer().getPosition().getDirection(CoordinateSystem.POLAR_RAD);

		//navigator.turnAround(true);
		driver.rotate(360, CoordinateSystem.POLAR_DEG, true);
		
		//wait a little to get motors started
		Delay.msDelay(100);
		
		while (leftMotor.isMoving() && rightMotor.isMoving()) {
			saveDistance();
		}

		int index = getMinimalDistance();
		isLeftWall = determineWallSeen(index);

		this.minDistAngle = (this.minDistAngle + Math.PI) % (2.0 * Math.PI);

		driver.turnTo(this.minDistAngle, CoordinateSystem.POLAR_RAD);
		
		driver.travelDistance(-(this.minDistance - Resources.BUMPER_TO_CENTER + Resources.US_TO_CENTER + 12.0));

		if (getSideUSData() < TILE_WIDTH) {
			isLeftWall = false;
		} else {
			isLeftWall = true;
		}

		setNewCoordinates();
		
		if (isLeftWall) {
			
			
			driver.travelDistance(6.0);

			driver.rotate(Math.PI/2.0, CoordinateSystem.POLAR_RAD);

		} else {
			


			driver.travelDistance(6.0);

			driver.rotate(-Math.PI/2.0, CoordinateSystem.POLAR_RAD);

		}

		// Go forward to the next wall
		//navigator.goForward(-(Resources.TILE_WIDTH - Resources.BUMPER_TO_CENTER + 6.0));
		driver.travelDistance(-(this.minDistance - Resources.BUMPER_TO_CENTER + Resources.US_TO_CENTER + 12.0));
		
		if(isLeftWall){
			isLeftWall = false;
		}else {
			isLeftWall = true;
		}
		
		setNewCoordinates();

		/*if (isLeftWall) {
			// Now it bumped the right wall
			position.setY(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			position.setDirection(Math.PI/2.0, CoordinateSystem.POLAR_RAD);
		} else {
			// Now it bumped the left wall
			position.setX(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			position.setDirection(0.0, CoordinateSystem.POLAR_RAD);
		}*/

		/*
		 * 
		 * END OF LOCALIZATION
		 * 
		 */

		// Make EV3 beep when it stops following the wall
		Audio audio = LocalEV3.get().getAudio();
		audio.systemSound(2);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		//navigator.goForward(10.0);
		driver.travelDistance(10.0);
		
		//navigator.travelToMapCoordinates(Resources.wifiData.get("LGZx"), Resources.wifiData.get("LGZy"));
		//driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, 0, 0)));
		
		
		isLocalizing = false;


	}
	
	/*
	 * |---------------|	
	 * | 4 + + + + + 3 |	
	 * | + + + + + + + |	
	 * | + + + + + + + |	^y
	 * | + + + + + + + |	|
	 * | + + + + + + + |	|
	 * | 1 + + + + + 2 |	|______>x
	 * |---------------|
	 * 
	 */
	private void setNewCoordinates(){
		
		if (isLeftWall){
			if (startingCorner == 1){
				position.setX(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(0.0, CoordinateSystem.POLAR_RAD);
			}else if (startingCorner == 2){
				position.setY(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection((Math.PI/2), CoordinateSystem.POLAR_RAD);

			}else if (startingCorner == 3){
				position.setX((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((Math.PI), CoordinateSystem.POLAR_RAD);

			}else {
				position.setY((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((3*Math.PI/2), CoordinateSystem.POLAR_RAD);

			}
		}else{	//right wall
			if (startingCorner == 1){
				position.setY(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(Math.PI/2, CoordinateSystem.POLAR_RAD);
			}else if (startingCorner == 2){
				position.setX((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((Math.PI), CoordinateSystem.POLAR_RAD);

			}else if (startingCorner == 3){
				position.setY((MAP_DIMENSION - 1) * TILE_WIDTH);
				position.setDirection((3*Math.PI/2), CoordinateSystem.POLAR_RAD);

			}else {
				position.setX(-TILE_WIDTH + BUMPER_TO_CENTER);
				position.setDirection(0.0, CoordinateSystem.POLAR_RAD);

			}
		}
		
		
	}
	
	

	/**
	 * Saves the distance seen by the ultrasonic sensor and the angle at which
	 * the distance has been seen. It saves a Distance object into an ArrayList.
	 */
	private void saveDistance() {

		float actualDist = Resources.getFrontUSData();
		if (actualDist > 1) {
			Distance d = new Distance(actualDist, position.getDirection(CoordinateSystem.POLAR_RAD));
			this.listOfDistances.add(d);
		}

	}

	/**
	 * This method is called to set the minimal distance and its angle.
	 * 
	 * @return Returns the index of the minimal distance in the ArrayList of
	 *         distances.
	 */
	private int getMinimalDistance() {

		int index = 0;

		// Initialize the first value
		this.minDistance = this.listOfDistances.get(0).getDistance();
		this.minDistAngle = this.listOfDistances.get(0).getAngle();

		// Search all the arraylist
		for (int i = 1; i < this.listOfDistances.size(); i++) {
			if (this.listOfDistances.get(i).getDistance() < this.minDistance) {

				// If the distance searched in the arraylist is smaller than the
				// previous minimum
				// save it as the minimal distance

				this.minDistance = this.listOfDistances.get(i).getDistance();
				this.minDistAngle = this.listOfDistances.get(i).getAngle();
				index = i;

			}
		}

		return index;

	}

	/**
	 * Determines if the wall seen is the left or the right wall. It basically
	 * checks the smallest distance at 90 degrees from the minimal distance.
	 * 
	 * @param index
	 *            Inputs the index of the minimal distance in the ArrayList of
	 *            distances.
	 * @return Returns true if it is the left wall. Returns false if it is the
	 *         right wall.
	 */
	private boolean determineWallSeen(int index) {

		// return true if facing the left wall, false if it is the "right" wall

		int quarterOfArraylist = (int) (this.listOfDistances.size() / 4.0);
		int choice1 = index - quarterOfArraylist;
		int choice2 = (index + quarterOfArraylist) % this.listOfDistances.size();

		if (choice1 < 0) {
			choice1 += this.listOfDistances.size();
		}

		// Check which distance is smaller, either 90 degrees from the smallest
		// distance or
		// -90 degrees from the smallest distance
		if (this.listOfDistances.get(choice1).getDistance() <= this.listOfDistances.get(choice2).getDistance()) {
			return true;
		}
		return false;

	}

}