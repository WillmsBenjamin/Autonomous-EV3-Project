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

public class Main {
	
	public static final int VERSION_NB = 1;
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	
	
	
	/**
	 * DOCUMENTATION COMMENTS
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		// LCD Display
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		// Initialize the odometer
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		
		// Initialize the display
		Display display = new Display(odometer, t);
		
		/*
		Resources resources = new Resources(usDistanceLeft, usDataLeft, usDistanceFront, usDataFront, colorValueDown, colorDataDown, colorValueFront, colorDataFront);
		*/
		
		// Initialize the resources (constants and sensors)
		Resources resources = new Resources();
		
		// Initialize the localization thread
		Localization localization = new Localization(odometer);
		
		
		
		odometer.start();
		resources.start();
		display.start();
		
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
