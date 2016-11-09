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
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Resources extends Thread {
	
	public static final int SPEED_FORWARD = 200, SPEED_TURNING = 150, SPEED_ROTATING = 90, ACCELERATION_FAST = 4000;
	public static final int ACCELERATION_SMOOTH = 400;
	public static final double TILE_WIDTH = 30.48, CS_TO_CENTER = 14.7, US_TO_CENTER = 0.0, BUMPER_TO_CENTER = 8.2;
	public static final double TRACK = 10.1, WHEEL_RADIUS = 2.127, LEFT_WHEEL_RADIUS = WHEEL_RADIUS, RIGHT_WHEEL_RADIUS = WHEEL_RADIUS;
	public static final long ODOMETER_PERIOD = 25;		// odometer update period, in ms
	public static final long DISPLAY_PERIOD = 250;
	public static final double BAND_CENTER = 20.0;
	public static final double NAVIGATION_POSITION_BANDWIDTH = 2.5, NAVIGATION_HEADING_BANDWIDTH = 0.14;
	
	private static final Port usPortLeft = LocalEV3.get().getPort("S1");
	private static final Port usPortFront = LocalEV3.get().getPort("S2");		
	private static final Port colorPortDown = LocalEV3.get().getPort("S3");
	private static final Port colorPortFront = LocalEV3.get().getPort("S4");
	
	private static SampleProvider usSensorLeft;
	private static float[] usDataLeft;
	private static SampleProvider usSensorFront;
	private static float[] usDataFront;
	private static SampleProvider colorSensorDown;
	private static float[] colorDataDown;
	private static SampleProvider colorSensorFront;
	private static float[] colorDataFront;
	private SensorModes colorSensorDown_Mode, colorSensorFront_Mode;
	
	/*
	public Resources(SampleProvider usSensorLeft, float[] usDataLeft, SampleProvider usSensorFront, float[] usDataFront, SampleProvider colorSensorDown, float[] colorDataDown, SampleProvider colorSensorFront, float[] colorDataFront) {
		this.usSensorLeft = usSensorLeft;
		this.usDataLeft = usDataLeft;
		this.usSensorFront = usSensorFront;
		this.usDataFront = usDataFront;
		this.colorSensorDown = colorSensorDown;
		this.colorDataDown = colorDataDown;
		this.colorSensorFront = colorSensorFront;
		this.colorDataFront = colorDataFront;
		
	}
	*/
	public Resources() {
		
	}
	
	public void run() {
		
		// Ultrasonic sensors
		@SuppressWarnings("resource")							    			// Because we don't bother to close this resource
		SensorModes usSensorLeft_Mode = new EV3UltrasonicSensor(usPortLeft);	// usSensor is the instance
		usSensorLeft = usSensorLeft_Mode.getMode("Distance");					// usDistance provides samples from this instance
		usDataLeft = new float[usSensorLeft.sampleSize()];						// usData is the buffer in which data are returned
		
		@SuppressWarnings("resource")
		SensorModes usSensorFront_Mode = new EV3UltrasonicSensor(usPortFront);
		usSensorFront = usSensorFront_Mode.getMode("Distance");
		usDataFront = new float[usSensorFront.sampleSize()];
		
		colorSensorDown_Mode = new EV3ColorSensor(colorPortDown);
		colorSensorDown = colorSensorDown_Mode.getMode("Red");			// colorValue provides samples from this instance
		colorDataDown = new float[colorSensorDown.sampleSize()];			// colorData is the buffer in which data are returned
		
		colorSensorFront_Mode = new EV3ColorSensor(colorPortFront);
		colorSensorFront = colorSensorFront_Mode.getMode("Red");			// colorValue provides samples from this instance
		colorDataFront = new float[colorSensorFront.sampleSize()];			// colorData is the buffer in which data are returned

		
		
		// Makes the run() method run forever, is it necessary?
		while(true) {}
		
	}
	
	
	public static float getLeftUSData() {
		usSensorLeft.fetchSample(usDataLeft, 0);
		float distance = usDataLeft[0] * 100;
		
		if (distance > 50) {
			distance = 50;
		}
		
		return distance;
	}
	public static float getFrontUSData() {
		usSensorFront.fetchSample(usDataFront, 0);
		float distance = usDataFront[0] * 100;
		
		if (distance > 50) {
			distance = 50;
		}
		
		return distance;
	}
	public static float getDownColorData() {
		// Fetch the data from the color sensor
		colorSensorDown.fetchSample(colorDataDown,0);
		return colorDataDown[0]*1000;
		
	}
	public static float getFrontColorData() {
		// Fetch the data from the color sensor
		colorSensorFront.fetchSample(colorDataFront,0);
		return colorDataFront[0]*1000;
		
	}

}
