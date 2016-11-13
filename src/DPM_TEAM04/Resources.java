
package DPM_TEAM04;

import java.util.HashMap;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.filter.MedianFilter;

/**
 * This class contains all resources (Motors, Sensors, Filters, Display) and all constants
 * required for operation. Resources are initialized once this class is loaded by the JVM
 * 
 * Sensor data is also acquired through this class
 * @author Kareem Halabi
 * @version 2.0
 */
public class Resources {
	
	//Wifi
	public static final String SERVER_IP = "192.168.2.5";
	public static final int TEAM_NUMBER = 4;
	public static boolean isBuilder;
	public static HashMap<String, Integer> wifiData;
	
	//Motor Constants
	public static final int SPEED_FORWARD = 200, SPEED_TURNING = 200, SPEED_ROTATING = 110, ACCELERATION_FAST = 4000;
	public static final int ACCELERATION_SMOOTH = 500;
	
	//Distance Constants
	public static final double TILE_WIDTH = 30.48, CS_TO_CENTER = 14.7, US_TO_CENTER = 6.7, BUMPER_TO_CENTER = 17.6;
	public static final double TRACK = 10.25, WHEEL_RADIUS = 2.127, LEFT_WHEEL_RADIUS = WHEEL_RADIUS, RIGHT_WHEEL_RADIUS = WHEEL_RADIUS;

	public static final long ODOMETER_PERIOD = 25;		// odometer update period, in ms
	public static final long DISPLAY_PERIOD = 250;
	public static final double BAND_CENTER = 20.0;
	public static final double NAVIGATION_POSITION_BANDWIDTH = 2.5, NAVIGATION_HEADING_BANDWIDTH = 0.14;

	
	private static final String LEFT_MOTOR_PORT = "D";
	public static final EV3LargeRegulatedMotor leftMotor;
	private static final String RIGHT_MOTOR_PORT = "C";
	public static final EV3LargeRegulatedMotor rightMotor;

	//public static final EV3LargeRegulatedMotor grabMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	//public static final EV3LargeRegulatedMotor liftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	//Ultrasonic sensors
	private static final String US_FRONT_PORT = "S4";
	private static final SampleProvider usFront;
	private static final float[] usDataFront;
	private static final MedianFilter usFrontFilter;
	private static final int US_FRONT_NUM_SAMPLES = 10;
	private static final float US_FRONT_CLIP = 50;
	
	private static final String US_SIDE_PORT = "S2";
	private static final SampleProvider usSide;
	private static final float[] usDataSide;
	private static final MedianFilter usSideFilter;
	private static final int US_SIDE_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;
	private static final float US_SIDE_CLIP = US_FRONT_CLIP;
	
	//Color sensors
	private static final String CS_FRONT_PORT = "S3";
	private static final SampleProvider csFront;
	private static final float[] csDataFront;
	private static final MedianFilter csFrontFilter;
	private static final int CS_FRONT_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;
	
	private static final String CS_DOWN_PORT = "S1";
	private static final SampleProvider csDown;
	private static final float[] csDataDown;
	private static final MeanFilter csDownFilter;
	private static final int CS_DOWN_NUM_SAMPLES = US_FRONT_NUM_SAMPLES;
	private static final float DIFF_THRESH = -0.02f;
	private static float previousValue;
	
	//LCD
	public static final TextLCD lcd;
	
	//Dummy variable to force initialization
	public static boolean initialize;
	//Initializes resources
	static {
		//--------------------------Motors-------------------------
		
		System.out.println("L Motor-" + LEFT_MOTOR_PORT);
		leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(LEFT_MOTOR_PORT));
		
		System.out.println("R Motor-" + RIGHT_MOTOR_PORT);
		rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort(RIGHT_MOTOR_PORT));
		
		//------------------------US Sensors------------------------
		
		System.out.println("US Front-" + US_FRONT_PORT);
		usFront = (new EV3UltrasonicSensor(LocalEV3.get().getPort(US_FRONT_PORT))).getDistanceMode();
		usDataFront = new float[usFront.sampleSize()];
		usFrontFilter = new MedianFilter(usFront, US_FRONT_NUM_SAMPLES);
		
		System.out.println("US Side-" + US_SIDE_PORT);
		usSide = (new EV3UltrasonicSensor(LocalEV3.get().getPort(US_SIDE_PORT))).getDistanceMode();
		usDataSide = new float[usSide.sampleSize()];
		usSideFilter = new MedianFilter(usSide, US_SIDE_NUM_SAMPLES);
		
		//-----------------------Color Sensors-----------------------
		
		System.out.println("CS Front-" + CS_FRONT_PORT);
		csFront = (new EV3ColorSensor(LocalEV3.get().getPort(CS_FRONT_PORT))).getColorIDMode();
		csDataFront = new float[csFront.sampleSize()];
		csFrontFilter = new MedianFilter(csFront, CS_FRONT_NUM_SAMPLES);
		
		System.out.println("CS Down-" + CS_DOWN_PORT);
		csDown = (new EV3ColorSensor(LocalEV3.get().getPort(CS_DOWN_PORT))).getRedMode();
		csDataDown = new float[csDown.sampleSize()];
		csDownFilter = new MeanFilter(csDown, CS_DOWN_NUM_SAMPLES);
		
		csDownFilter.fetchSample(csDataDown, 0);
		
		//---------------------------LCD---------------------------
		lcd = LocalEV3.get().getTextLCD();
		
		lcd.clear();
		lcd.drawString("Ready", 0, 0);
		Sound.beep();
	}
	
	
	public static float getFrontUSData() {
		usFrontFilter.fetchSample(usDataFront, 0);
		
		if (usDataFront[0] > US_FRONT_CLIP)
			usDataFront[0] = US_FRONT_CLIP;
		
		return usDataFront[0];
	}
	
	public static float getSideUSData() {
		usSideFilter.fetchSample(usDataSide, 0);
		
		if (usDataSide[0] > US_SIDE_CLIP)
			usDataSide[0] = US_SIDE_CLIP;
		
		return usDataSide[0];
	}
	
	public static boolean isBlueBlock() {
		//Since the front color sensor is not polled continuously
		//we always need a fresh set of samples
		for(int i = 0; i < CS_FRONT_NUM_SAMPLES; i++)
			csFrontFilter.fetchSample(csDataFront, 0);
		
		if(csDataFront[0] == 6f || csDataFront[0] == 7f)
			return true;
		
		return false;
	}
	
	/**
	 * Determines if the robot has crossed a line.
	 * Uses a Mean filter for previous and current values, comparing
	 * their difference to determine a line
	 * @return true if a line is detected, false if otherwise
	 * @version 1.0
	 */
	public static boolean isLinePresent() {
		previousValue = csDataDown[0];
		csDownFilter.fetchSample(csDataDown, 0);
		
		if((csDataDown[0] - previousValue) < DIFF_THRESH)
			return true;
		
		return false;
	}
}