package DPM_TEAM04_Package;

import java.util.ArrayList;

import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;

public class Localization extends Thread {

	private Odometer odometer;
	private Resources resources;
	
	private double minDistance, minDistAngle;
	private ArrayList<Distance> listOfDistances;
	private boolean isLeftWall;
	
	public Localization(Odometer odometer, Resources resources) {
		this.odometer = odometer;
		this.resources = resources;
		
		listOfDistances = new ArrayList<Distance>();
	}
	
	public void run() {
		
		// Wait 1 second for everything to be set up (sensors)
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
		
		Navigation navigator = new Navigation(this.odometer);
		
		this.minDistance = resources.getFrontUSData();
		this.minDistAngle = odometer.getTheta();
		
		
		navigator.setAcceleration(4000);
		navigator.turnAround(true);
		while (odometer.getTheta() >= 0.0 && odometer.getTheta() < 6.26) {
			// wait until it turns a little
			
			//System.out.println(odometer.getTheta());
			saveDistance();
		}
		
		
		// Make EV3 beep when it stops following the wall
		Audio audio = LocalEV3.get().getAudio();
		audio.systemSound(2);
		
		
		int index = getMinimalDistance();
		isLeftWall = determineWallSeen(index);
		
		// Turn to the minimal distance seen
		navigator.turnTo(this.minDistAngle, true);
		// Go to this distance and more than the distance to "bump" into it
		navigator.goForward(this.minDistance + 6.0);
		
		if (isLeftWall) {
			// If it bumped into the left wall, set position accordingly
			odometer.setX(-Resources.TILE_WIDTH+Resources.BUMPER_TO_CENTER);
			odometer.setTheta(3.0/2.0*Math.PI);
			
			// Go back to turn and bump the other wall
			navigator.goForward(-20.0);
			navigator.turnTo(Math.PI, true);
		} else {
			// If it bumped into the right wall, set position accordingly
			odometer.setY(-Resources.TILE_WIDTH+Resources.BUMPER_TO_CENTER);
			odometer.setTheta(Math.PI);
			
			// Go back to turn and bump the other wall
			navigator.goForward(-20.0);
			navigator.turnTo(3.0/2.0*Math.PI, true);
		}
		
		
		
		// Go forward to the next wall
		navigator.goForward(resources.getFrontUSData() + 6.0);
		
		// reset the smooth acceleration (default)
		navigator.setAcceleration(-1);
		
		
		if (isLeftWall) {
			// Now it bumped the right wall
			odometer.setY(-Resources.TILE_WIDTH+Resources.BUMPER_TO_CENTER);
			odometer.setTheta(Math.PI);
		} else {
			// Now it bumped the left wall
			odometer.setX(-Resources.TILE_WIDTH+Resources.BUMPER_TO_CENTER);
			odometer.setTheta(3.0/2.0*Math.PI);
		}
		
		
		resources.getFrontUSData();
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		
		
		navigator.goForward(-10.0);
		navigator.travelTo(0.0, 0.0);
		navigator.turnTo(0.0, true);
		
		
		
	}
	
	
	private void saveDistance() {
		
		float actualDist = resources.getFrontUSData();
		Distance d = new Distance(actualDist, odometer.getTheta());
		this.listOfDistances.add(d);
		
	}
	
	private int getMinimalDistance() {
		
		int index = 0;
		
		// Initialize the first value
		this.minDistance = this.listOfDistances.get(0).getDistance();
		this.minDistAngle  = this.listOfDistances.get(0).getAngle();
		
		// Search all the arraylist
		for (int i=1; i<this.listOfDistances.size(); i++) {
			if (this.listOfDistances.get(i).getDistance() < this.minDistance) {
				
				// If the distance searched in the arraylist is smaller than the previous minimum
				// save it as the minimal distance
				
				this.minDistance = this.listOfDistances.get(i).getDistance();
				this.minDistAngle  = this.listOfDistances.get(i).getAngle();
				index = i;
				
			}
		}
		
		return index;
		
	}
	
	private boolean determineWallSeen(int index) {
		
		// return true if facing the left wall, false if it is the "right" wall
		
		int quarterOfArraylist = (int) (this.listOfDistances.size() / 4.0);
		int choice1 = index - quarterOfArraylist;
		int choice2 = (index + quarterOfArraylist)%this.listOfDistances.size();
		
		if (choice1 < 0) {
			choice1 += this.listOfDistances.size();
		}
		
		
		// Check which distance is smaller, either 90 degrees from the smallest distance or
		// -90 degrees from the smallest distance
		if (this.listOfDistances.get(choice1).getDistance() <= this.listOfDistances.get(choice2).getDistance()) {
			return true;
		}
		return false;
		
	}
	
	
	

}