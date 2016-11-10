/*
 * 
 * 
 * CONTAINS ALL THE RESOURCES
 * 
 * CONSTANTS
 * MOTORS
 * SENSORS GETTER
 * 
 * 
 */


package DPM_TEAM04_Package;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Resources {
	
	public static final int SPEED_FORWARD = 200, SPEED_TURNING = 200, SPEED_ROTATING = 110, ACCELERATION_FAST = 4000;
	public static final int ACCELERATION_SMOOTH = 500;
	public static final double TILE_WIDTH = 30.48, CS_TO_CENTER = 14.7, US_TO_CENTER = 0.0, BUMPER_TO_CENTER = 18.1;
	public static final double TRACK = 11.2, WHEEL_RADIUS = 2.127, LEFT_WHEEL_RADIUS = WHEEL_RADIUS, RIGHT_WHEEL_RADIUS = WHEEL_RADIUS;
	public static final long ODOMETER_PERIOD = 25;		// odometer update period, in ms
	public static final long DISPLAY_PERIOD = 250;
	public static final double BAND_CENTER = 20.0;
	public static final double NAVIGATION_POSITION_BANDWIDTH = 2.5, NAVIGATION_HEADING_BANDWIDTH = 0.14;
	
	
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor grabMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	public static final EV3LargeRegulatedMotor liftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	
	private SampleProvider usSensorFront;
	private float[] usDataFront;
	private SampleProvider colorSensorDown;
	private float[] colorDataDown;
	
	public Resources(SampleProvider usSensorFront, float[] usDataFront, SampleProvider colorSensorDown, float[] colorDataDown) {
		this.usSensorFront = usSensorFront;
		this.usDataFront = usDataFront;
		this.colorSensorDown = colorSensorDown;
		this.colorDataDown = colorDataDown;
		
	}
	
	
	
	public float getFrontUSData() {
		this.usSensorFront.fetchSample(this.usDataFront, 0);
		float distance = this.usDataFront[0] * 100;
		
		if (distance > 50) {
			distance = 50;
		}
		
		return distance;
	}
	public float getDownColorData() {
		// Fetch the data from the color sensor
		this.colorSensorDown.fetchSample(this.colorDataDown,0);
		return this.colorDataDown[0]*1000;
		
	}
	
	
	
	
	/*
	public float getLeftUSData() {
		this.usSensorLeft.fetchSample(this.usDataLeft, 0);
		float distance = this.usDataLeft[0] * 100;
		
		if (distance > 50) {
			distance = 50;
		}
		
		return distance;
	}
	public float getFrontColorData() {
		// Fetch the data from the color sensor
		this.colorSensorFront.fetchSample(this.colorDataFront,0);
		return this.colorDataFront[0]*1000;
		
	}
	*/
	

}
