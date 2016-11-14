package DPM_TEAM04.odometry;

import java.util.ArrayList;

import DPM_TEAM04.Distance;
import DPM_TEAM04.Resources;
import DPM_TEAM04.navigation.Navigation;
import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;

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

	private Odometer odometer;

	private double minDistance, minDistAngle;
	private ArrayList<Distance> listOfDistances;
	private boolean isLeftWall;

	public Localization(Odometer odometer) {
		this.odometer = odometer;

		listOfDistances = new ArrayList<Distance>();
	}

	/**
	 * Code executed when the Thread is started.
	 */
	public void run() {
		
		Navigation navigator = new Navigation(this.odometer);
		
		// set the smooth acceleration (default)
		navigator.setAcceleration(-1);

		this.minDistance = Resources.getSideUSData();
		this.minDistAngle = odometer.getTheta();

		
		navigator.turnAround(true);
		while (odometer.getTheta() >= 0.0 && odometer.getTheta() < 6.26) {
			// wait until it turns a little

			saveDistance();
		}

		int index = getMinimalDistance();
		isLeftWall = determineWallSeen(index);

		this.minDistAngle = (this.minDistAngle + Math.PI) % (2.0 * Math.PI);

		// Turn to the minimal distance seen
		navigator.turnTo(this.minDistAngle, true);
		// Go to this distance and more than the distance to "bump" into it
		navigator.goForward(-(this.minDistance - Resources.BUMPER_TO_CENTER + Resources.US_TO_CENTER + 12.0));


		if (isLeftWall) {
			// If it bumped into the left wall, set position accordingly
			odometer.setX(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			odometer.setTheta(Math.PI / 2.0);

			// Go back to turn and bump the other wall
			navigator.goForward(6.0);
			navigator.turnTo(0.0, true);
		} else {
			// If it bumped into the right wall, set position accordingly
			odometer.setY(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			odometer.setTheta(0.0);

			// Go back to turn and bump the other wall
			navigator.goForward(6.0);
			navigator.turnTo(Math.PI / 2.0, true);
		}

		// Go forward to the next wall
		navigator.goForward(-(Resources.TILE_WIDTH - Resources.BUMPER_TO_CENTER + 6.0));

		if (isLeftWall) {
			// Now it bumped the right wall
			odometer.setY(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			odometer.setTheta(0.0);
		} else {
			// Now it bumped the left wall
			odometer.setX(-Resources.TILE_WIDTH + Resources.BUMPER_TO_CENTER);
			odometer.setTheta(Math.PI / 2.0);
		}

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

		navigator.goForward(10.0);
		navigator.travelToMapCoordinates(Resources.wifiData.get("LGZx"), Resources.wifiData.get("LGZy"));
		navigator.turnTo(0.0, true);

	}

	/**
	 * Saves the distance seen by the ultrasonic sensor and the angle at which
	 * the distance has been seen. It saves a Distance object into an ArrayList.
	 */
	private void saveDistance() {

		float actualDist = Resources.getSideUSData();
		if (actualDist > 1) {
			Distance d = new Distance(actualDist, odometer.getTheta());
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