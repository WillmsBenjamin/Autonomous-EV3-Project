/*
 * DPM
 * ECSE 211
 * DESIGN PROJECT
 * 
 * 
 * 2016
 * 
 * 
 * 
 * 
 * 
 * VERSION NUMBER WRITTEN AS A CONSTANT
 * 
 */


package DPM_TEAM04_Package;

import DPM_TEAM04_Package.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Main {
	
	public static final int VERSION_NB = 1;
	
	
	//private static final Port usPortLeft = LocalEV3.get().getPort("S4");
	private static final Port usPortFront = LocalEV3.get().getPort("S3");		
	private static final Port colorPortDown = LocalEV3.get().getPort("S1");
	//private static final Port colorPortFront = LocalEV3.get().getPort("S2");
	
	
	/**
	 * DOCUMENTATION COMMENTS
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		// Ultrasonic sensors
		/*
		@SuppressWarnings("resource")							    			// Because we don't bother to close this resource
		SensorModes usSensorLeft_Mode = new EV3UltrasonicSensor(usPortLeft);	// usSensor is the instance
		SampleProvider usSensorLeft = usSensorLeft_Mode.getMode("Distance");					// usDistance provides samples from this instance
		float[] usDataLeft = new float[usSensorLeft.sampleSize()];						// usData is the buffer in which data are returned
		*/
		
		@SuppressWarnings("resource")
		SensorModes usSensorFront_Mode = new EV3UltrasonicSensor(usPortFront);
		SampleProvider usSensorFront = usSensorFront_Mode.getMode("Distance");
		float[] usDataFront = new float[usSensorFront.sampleSize()];
		
		SensorModes colorSensorDown_Mode = new EV3ColorSensor(colorPortDown);
		SampleProvider colorSensorDown = colorSensorDown_Mode.getMode("Red");			// colorValue provides samples from this instance
		float[] colorDataDown = new float[colorSensorDown.sampleSize()];			// colorData is the buffer in which data are returned
		
		/*
		SensorModes colorSensorFront_Mode = new EV3ColorSensor(colorPortFront);
		SampleProvider colorSensorFront = colorSensorFront_Mode.getMode("Red");			// colorValue provides samples from this instance
		float[] colorDataFront = new float[colorSensorFront.sampleSize()];			// colorData is the buffer in which data are returned
		*/
		
		
		// LCD Display
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		// Initialize the odometer
		Odometer odometer = new Odometer();
		
		// Initialize the display
		Display display = new Display(odometer, t);
		
		
		Resources resources = new Resources(usSensorFront, usDataFront, colorSensorDown, colorDataDown);
		
		
		// Initialize the resources (constants and sensors)
		//Resources resources = new Resources();
		
		// Initialize the localization thread
		Localization localization = new Localization(odometer, resources);
		
		
		
		odometer.start();
		display.start();
		
		
		// Wait 4 seconds for everything to be set up (sensors)
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
		
		localization.start();
		
		/*
		 * 
		 * 
		 * START OTHER THREADS
		 * 
		 * START THE WIFI CONNECTION AND WAIT TO RECEIVE THE PARAMETERS
		 * 
		 * PASS THE PARAMETERS TO THE RESOURCES
		 * 
		 * 
		 */
		
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
