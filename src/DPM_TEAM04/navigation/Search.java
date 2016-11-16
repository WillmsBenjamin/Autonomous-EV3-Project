package DPM_TEAM04.navigation;

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

public class Search extends Thread {

	public Search() {
		
	}
	
	
	public void run() {
		
		while (isLocalizing) {}
		
		Driver driver = new Driver();
		
		// Go to the search point
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		
		
		if (searchPoint.x < stackPoint.x) {
			if (searchPoint.y < stackPoint.y) {
				// Bottom left quadrant
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
			} else {
				// Top left quadrant
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
			}
		} else {
			if (searchPoint.y < stackPoint.y) {
				// Bottom right quadrant
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
			} else {
				// Top right quadrant
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
			}
		}
		
		
		
		
		
	}
}
