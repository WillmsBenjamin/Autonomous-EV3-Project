package DPM_TEAM04.navigation;

import static DPM_TEAM04.Resources.*;
import DPM_TEAM04.geometry.Coordinate;
import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.navigation.Driver;

public class Search extends Thread {

	public Search() {
		
	}
	
	
	public void run() {
		
		Driver driver = new Driver();
		
		// Go to the search point
		driver.travelTo((new Coordinate(CoordinateSystem.CARTESIAN, searchPoint.x, searchPoint.y)));
		
		
		if (searchPoint.x < builderZone.getCenterX()) {
			if (searchPoint.y < builderZone.getCenterY()) {
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
			if (searchPoint.y < builderZone.getCenterY()) {
				// Bottom right quadrant
				driver.turnTo(180, CoordinateSystem.POLAR_DEG);
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.turnTo(0, CoordinateSystem.POLAR_DEG);
				driver.turnTo(90, CoordinateSystem.POLAR_DEG);
			} else {
				// Top right quadrant
				driver.turnTo(270, CoordinateSystem.POLAR_DEG);
				driver.rotate(180, CoordinateSystem.POLAR_DEG);
			}
		}
		
	}
}
