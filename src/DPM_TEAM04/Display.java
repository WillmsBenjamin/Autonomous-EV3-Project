package DPM_TEAM04;

import static DPM_TEAM04.Resources.lcd;

import DPM_TEAM04.geometry.CoordinateSystem;
import DPM_TEAM04.odometry.Odometer;

/**
 * Class to Display Odometry data on LCD
 * 
 * @deprecated Use LCDLogger instead
 */
@Deprecated
public class Display extends Thread {
	private Odometer odometer;

	// constructor
	public Display(Odometer odometer) {
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		lcd.clear();

		while (true) {
			displayStart = System.currentTimeMillis();

			// clear the lines for displaying odometry information
			lcd.drawString("VERSION:        ", 0, 0);
			lcd.drawInt((int) Main.VERSION_NB, 9, 0);
			lcd.drawString("X:              ", 0, 1);
			lcd.drawString("Y:              ", 0, 2);
			lcd.drawString("T:              ", 0, 3);
			lcd.drawString("Dist:           ", 0, 4);
			lcd.drawInt((int) Resources.getSideUSData(), 5, 4);

			// get the odometry information
			position = odometer.getPosition().getCoordinates(
					CoordinateSystem.CARTESIAN, CoordinateSystem.HEADING_DEG);

			// display odometry information
			for (int i = 0; i < 3; i++) {
				lcd.drawString(formattedDoubleToString(position[i], 2), 3,
						i + 1);
			}

			// throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < Resources.DISPLAY_PERIOD) {
				try {
					Thread.sleep(Resources.DISPLAY_PERIOD
							- (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}

	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;

		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";

		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long) x;
			if (t < 0)
				t = -t;

			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}

			result += stack;
		}

		// put the decimal, if needed
		if (places > 0) {
			result += ".";

			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long) x);
			}
		}

		return result;
	}

}