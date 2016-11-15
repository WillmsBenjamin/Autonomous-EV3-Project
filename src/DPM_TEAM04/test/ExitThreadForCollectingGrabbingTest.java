package DPM_TEAM04.test;

import lejos.hardware.Button;

public class ExitThreadForCollectingGrabbingTest extends Thread {

	public ExitThreadForCollectingGrabbingTest() {
		
	}
	
	public void run() {
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
