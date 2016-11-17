package DPM_TEAM04.test;

import DPM_TEAM04.Resources;
import DPM_TEAM04.navigation.Search;
import lejos.hardware.Button;

public class SearchTest {

	public static void main(String[] args) {
		
		Resources.initialize = true;
		
		(new Search()).start();
		
		Button.waitForAnyPress();
		System.exit(0);
	}

}
