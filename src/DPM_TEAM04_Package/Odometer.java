package DPM_TEAM04_Package;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		double tempTheta = 0.0;

		while (true) {
			updateStart = System.currentTimeMillis();
			

			// Get the tacho count
			int newLeftMotorTachoCount = leftMotor.getTachoCount();
			int newRightMotorTachoCount = rightMotor.getTachoCount();
			
			// Compute the distance traveled during the last iteration
			double distL = 3.14159*Resources.WHEEL_RADIUS*(newLeftMotorTachoCount-leftMotorTachoCount)/180;
			double distR = 3.14159*Resources.WHEEL_RADIUS*(newRightMotorTachoCount-rightMotorTachoCount)/180;
			
			// Store last tacho count for the next iteration
			leftMotorTachoCount=newLeftMotorTachoCount;
			rightMotorTachoCount=newRightMotorTachoCount;
			
			// Compute the distance the measured center moved
			double deltaD = 0.5*(distL+distR);
			
			// Compute the change in the angle
			double deltaT = (distL-distR)/Resources.TRACK;
			
			// Make sure theta is always positive and between 0 and 2PI
			// Update the value of theta
			// Computing math operations on a temporary value of theta instead of using the real variable
			tempTheta = getTheta() + deltaT;
			if (tempTheta < 0) {
				tempTheta += 2*3.14159;
			}
			tempTheta = tempTheta%(2*3.14159);
			
			// Compute the x and y displacement of the movement
		    double dx = deltaD * Math.sin(tempTheta);
			double dy = deltaD * Math.cos(tempTheta);
			
			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				x = x + dx;
				y = y + dy;
				theta = tempTheta;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < Resources.ODOMETER_PERIOD) {
				try {
					Thread.sleep(Resources.ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
	
	
	// accessors to motors
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}
	

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}
