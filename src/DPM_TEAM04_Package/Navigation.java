package DPM_TEAM04_Package;

import DPM_TEAM04_Package.Odometer;
import lejos.hardware.Audio;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class contains all navigation methods.
 * 
 * @author Alexis Giguère-Joannette & Tristan Saumure-Toupin
 * @version 1.0
 */
public class Navigation {
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	private boolean navigating;
	private double x, y;
	
	public Navigation(Odometer odometer) {
		this.odometer = odometer;
		
		this.leftMotor = Resources.leftMotor;
		this.rightMotor = Resources.rightMotor;
		
		// set acceleration
		this.leftMotor.setAcceleration(Resources.ACCELERATION_SMOOTH);
		this.rightMotor.setAcceleration(Resources.ACCELERATION_SMOOTH);
		
		// set speed
		this.leftMotor.setSpeed(Resources.SPEED_FORWARD);
		this.rightMotor.setSpeed(Resources.SPEED_FORWARD);
		
		
		this.navigating = false;
	}
	
	
	
	
	/**
	 * This method makes the robot navigate to the desired coordinates, within a range of error.
	 * @param x Position desired in x
	 * @param y Position desired in y
	 */
	public void travelTo(double x, double y) {
		
		this.navigating = true;									// The robot is navigating
		
		this.x = x;
		this.y = y;
		
		double trueX, trueY;		// Initialize variables
		
		while(true) {
			this.navigating = true;								// If the path is now clear, the robot is navigating again
			trueX = odometer.getX();							// Get the X and Y values from the odometer
			trueY = odometer.getY();
			if ((trueX <= (this.x+Resources.NAVIGATION_POSITION_BANDWIDTH) && trueX >= (this.x-Resources.NAVIGATION_POSITION_BANDWIDTH))
					&& ((trueY <= (this.y+Resources.NAVIGATION_POSITION_BANDWIDTH)) && (trueY >= (this.y-Resources.NAVIGATION_POSITION_BANDWIDTH)))) {
				
				// If the robot is close enough to the position it needs to be (position with a range of Resources.NAVIGATION_POSITION_BANDWIDTH)
				this.leftMotor.stop(true);
				this.rightMotor.stop(false);
				
				// Make EV3 beep each time it gets to a position
				Audio audio = LocalEV3.get().getAudio();
			    audio.systemSound(0);
			    
				break;											// Break the first while loop to exit the travelTo method
			}
			
			if(thetaInRange(Resources.NAVIGATION_HEADING_BANDWIDTH, this.odometer.getTheta())) {
				
				// If the robot's heading is going in the right direction (within a Resources.NAVIGATION_HEADING_BANDWIDTH range)
				this.leftMotor.setSpeed(Resources.SPEED_FORWARD);			// Reset speeds to go straight to the position (same speed)
				this.rightMotor.setSpeed(Resources.SPEED_FORWARD);
				this.leftMotor.forward();
				this.rightMotor.forward();
			} else {
				
				// If the robot's heading is not facing the desired theta to go to the position,
				// compute its desired theta and turn to this heading
				turnToDesiredTheta();							// Turn to the desired angle
			}
		}
		this.navigating = false;								// When the while loop is exited, the robot is not navigating
		return;													// return void
	}
	
	/**
	 * This method calls the travelTo method, but converting the inputs from a point coordinate on the map to (x,y) coordinates in cm.
	 * @param x X component of the point coordinate
	 * @param y Y component of the point coordinate
	 */
	public void travelToMapCoordinates(double x, double y) {
		travelTo(x*Resources.TILE_WIDTH, y*Resources.TILE_WIDTH);
	}
	
	
	/**
	 * Turns to a desired orientation by the minimum angle.
	 * @param theta The angle at which it needs to turn to. Theta is in radians.
	 * @param wait Boolean to set if the method waits until the rotation is complete before returning. True sets it to wait. False sets it to not wait.
	 */
	public void turnTo(double theta, boolean wait) {
		this.navigating = true;									// When turning, the robot is navigating
		this.leftMotor.stop(true);								// Stop the motors and wait until complete
		this.rightMotor.stop(false);
		
		// Compute both turns (left and right) to compare which one will be the smallest
		double thetaLeftTurn = 0.0;
		double thetaRightTurn = 0.0;
		if(theta <= this.odometer.getTheta()) {
			thetaLeftTurn = this.odometer.getTheta() - theta;
			thetaRightTurn = ((2.0*Math.PI) - this.odometer.getTheta()) + theta;
		} else {
			thetaLeftTurn = ((2.0*Math.PI) - theta) + this.odometer.getTheta();
			thetaRightTurn = theta - this.odometer.getTheta();
		}
		
		// Reset the motors speeds
		this.leftMotor.setSpeed(Resources.SPEED_TURNING);
		this.rightMotor.setSpeed(Resources.SPEED_TURNING);
		
		if(thetaLeftTurn <= thetaRightTurn) {
			// If the left turn is smaller than the right turn, turn left
			this.leftMotor.rotate(-convertAngle(Resources.LEFT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaLeftTurn)), true);
			if (wait) {
				this.rightMotor.rotate(convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaLeftTurn)), false);			// wait until comleted
			} else {
				this.rightMotor.rotate(convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaLeftTurn)), true);			// not wait until comleted
			}
			
		} else {
			// Turn right if theta is smaller
			this.leftMotor.rotate(convertAngle(Resources.LEFT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaRightTurn)), true);
			if (wait) {
				this.rightMotor.rotate(-convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaRightTurn)), false);		// wait until completed
			} else {
				this.rightMotor.rotate(-convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(thetaRightTurn)), true);		// not wait until completed
			}
			
		}
		
		this.navigating = false;								// When turning is done, it is not navigating
		return;
	}
	
	/**
	 * This method turns to the desired theta, which is the minimal angle to reach a destination.
	 */
	public void turnToDesiredTheta() {
		this.leftMotor.stop(true);								// Stop the motors and wait until complete
		this.rightMotor.stop(false);
		double desiredTheta = getDesiredTheta();				// Get the desired theta AFTER the motors are stopped
		turnTo(desiredTheta, true);								// turnTo the desired heading
	}
	
	
	/**
	 * Returns whether turnTo is running, or travelTo is running and isn't pausing until the path is clear.
	 * @return Returns true if navigating, false if not.
	 */
	public boolean isNavigating() {
		return navigating;
	}
	
	
	/**
	 * Returns true if trueTheta is within a range. The range is determined by the current position in relation to x and y, plus or minus the bandwidth.
	 * @param bandWidth Range of error in theta accepted.
	 * @param trueTheta Inputs the heading that needs to be checked if it is in range.
	 * @return Returns true if theta is in range, false otherwise.
	 */
	public boolean thetaInRange(double bandWidth, double trueTheta) {
		
		// Initialize variables
		double x_diff, y_diff, desiredTheta, thetaRangeLow, thetaRangeHigh, trueX, trueY;
		
		boolean inRange;										// boolean to know if the heading is in range of the desired theta
		trueX = odometer.getX();
		trueY = odometer.getY();
		x_diff = this.x - trueX;								// Compute the X and Y deltas (difference)
		y_diff = this.y - trueY;
		desiredTheta = getDesiredTheta();						// Get the desired theta
		
		// Compute the absolute distance to the desired point
		double distance = Math.sqrt((Math.pow(x_diff, 2)+Math.pow(y_diff, 2)));
		if (bandWidth >= Math.PI/10) {
			
			// Unless the bandWidth is really small (when the method is called to know if it goes straight to the point)
			// Adjust the bandWidth according to a linear function having a small angle range when it is "far" from the position
			// and a bigger range when it is closer
			bandWidth=4*Math.PI/distance;
			if(bandWidth >= (2.0*Math.PI)) {
				bandWidth = (2.0*Math.PI);
			}
		}
		
		// Compute the range of theta with the bandWidth
		thetaRangeLow = desiredTheta-bandWidth;
		thetaRangeHigh = (desiredTheta+bandWidth)%(2.0*Math.PI);
		if (thetaRangeLow < 0) {
			// If the lower bound is negative, add 2*PI
			thetaRangeLow += (2.0*Math.PI);
		}
		// Condition to avoid issues when the range passes 2*PI to 0
		if (thetaRangeHigh < thetaRangeLow) {
			if(((trueTheta >= thetaRangeLow) && (trueTheta < (2.0*Math.PI))) || ((trueTheta <= thetaRangeHigh) && (trueTheta >= 0))) {
				// If the angle falls between the lower bound and 2PI or the upper bound and 0
				inRange = true;
			} else {
				inRange = false;
			}
		} else if((trueTheta >= thetaRangeLow) && ((trueTheta <= thetaRangeHigh))) {
			// If it is in the range
			inRange = true;
		} else {
			inRange = false;
		}
		return inRange;
	}
	
	/**
	 * Compute the optimal angle to reach a point (x,y).
	 * @return Returns the desired theta.
	 */
	public double getDesiredTheta() {
		
		double x_diff, y_diff, desiredTheta;
		// If the robot's heading is not facing the desired theta to go to the position,
		// compute its desired theta and turn to this heading
		x_diff = this.x - odometer.getX();				// Compute the X and Y difference (desired and actual values)
		y_diff = this.y - odometer.getY();
		desiredTheta = Math.atan2(x_diff, y_diff);		// Get the angle from delta X and Y
		if(desiredTheta < 0) {
			desiredTheta += (2.0*Math.PI);					// If the angle is negative, add 2*PI
		}
		
		return desiredTheta;							// Return the desired theta (towards the position)
	}
	
	
	/**
	 * Method to turn around for 360 degrees.
	 * @param clockwise Turns clockwise if true. Turns counterclockwise if false.
	 */
	public void turnAround(boolean clockwise) {
		
		this.leftMotor.setSpeed(Resources.SPEED_TURNING);
		this.rightMotor.setSpeed(Resources.SPEED_TURNING);
		
		if (clockwise) {
			// Turn clockwise
			this.leftMotor.rotate(convertAngle(Resources.LEFT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(2*Math.PI)), true);
			this.rightMotor.rotate(-convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(2*Math.PI)), true);
		} else {
			// Turn counter clockwise
			this.leftMotor.rotate(-convertAngle(Resources.LEFT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(2*Math.PI)), true);
			this.rightMotor.rotate(+convertAngle(Resources.RIGHT_WHEEL_RADIUS, Resources.TRACK, radsToDegrees(2*Math.PI)), true);
		}
		
	}
	
	/**
	 * Set the acceleration of the motors.
	 * @param acceleration Set acceleration of the motors to this input value. Default acceleration if the input is -1.
	 */
	public void setAcceleration(int acceleration) {
		
		this.leftMotor.stop();
		this.rightMotor.stop();
		
		if (acceleration == -1) {
			// Set to the default acceleration
			acceleration = Resources.ACCELERATION_SMOOTH;
		} else if (acceleration > Resources.ACCELERATION_FAST) {
			// Cap to the maximal acceleration
			acceleration = Resources.ACCELERATION_FAST;
		}
		
		this.leftMotor.setAcceleration(acceleration);
		this.rightMotor.setAcceleration(acceleration);
		
	}
	
	/**
	 * Method to turn around at a custom speed (forever) until it is called to stop.
	 * @param clockwise Turns clockwise if true. Turns counterclockwise if false.
	 * @param start Starts turning if true, stops turning if false.
	 * @param speed Desired turning speed.
	 */
	public void turnAround(boolean clockwise, boolean start, int speed) {
		
		if (start) {
			// If start is true, start turning around
			
			// Set the custom speed (that is an input to the method)
			this.leftMotor.setSpeed(speed);
			this.rightMotor.setSpeed(speed);
			if (clockwise) {
				// If clockwise is true, turn clockwise
				this.leftMotor.forward();
				this.rightMotor.backward();
			} else {
				// Turn counter clockwise
				this.leftMotor.backward();
				this.rightMotor.forward();
			}
		} else {
			// Otherwise, stop turning
			this.leftMotor.stop(true);
			this.rightMotor.stop(false);
		}
		
		
	}
	
	/**
	 * Go forward for a specified distance.
	 * @param distance Distance to go forward in cm. Negative distance will go backwards.
	 */
	public void goForward(double distance) {
		
		this.leftMotor.setSpeed(Resources.SPEED_FORWARD);
		this.rightMotor.setSpeed(Resources.SPEED_FORWARD);

		this.leftMotor.rotate(convertDistance(Resources.LEFT_WHEEL_RADIUS, distance), true);
		this.rightMotor.rotate(convertDistance(Resources.RIGHT_WHEEL_RADIUS, distance), false);
		
	}
	
	/**
	 * Go backward (forever) until it is called again to stop.
	 * @param start Starts going backward if true, stops if false.
	 */
	public void goBackward(boolean start) {
		
		this.leftMotor.setSpeed(Resources.SPEED_FORWARD);
		this.rightMotor.setSpeed(Resources.SPEED_FORWARD);
		
		if (start) {
			this.leftMotor.backward();
			this.rightMotor.backward();
		} else {
			this.leftMotor.stop();
			this.rightMotor.stop();
		}
		
	}
	
	
	
	
	/**
	 * Convert a distance in cm to radians.
	 * @param radius Radius of the wheels in cm.
	 * @param distance Distance in cm.
	 * @return Converted distance in radians.
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	/**
	 * Convert an angle to turn to according to the track of the robot.
	 * @param radius Radius of the wheels in cm.
	 * @param width Width (track) of the robot in cm.
	 * @param angle Angle to turn in radians.
	 * @return Returns the angle to turn in radians.
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	
	/**
	 * Converts radians to degrees.
	 * @param angle Angle in radians.
	 * @return Returns the angle in degrees.
	 */
	private static double radsToDegrees(double angle) {
		return (angle/(2.0*Math.PI))*360.0;
	}

}

