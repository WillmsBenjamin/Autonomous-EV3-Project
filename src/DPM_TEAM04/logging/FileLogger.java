package DPM_TEAM04.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.crypto.Data;

import lejos.robotics.SampleProvider;


/**
 * Class for saving various data to a CSV file
 * @author KareemHalabi
 *
 */
public class FileLogger extends Thread {
	
	//Member variables
	private String fileName;
	private int refreshPeriod;
	
	private ArrayList<DataEntryProvider> entryProviders;
	
	
	public FileLogger(String fileName, int refreshPeriod) {
		this.fileName = fileName;
		this.refreshPeriod = refreshPeriod;
		this.entryProviders = new ArrayList<DataEntryProvider>();
	}
	
	/**
	 * Adds a DataEntryProvider to this data logger.
	 * Only adds if this DataLogger is not running
	 * @param entryProvider the DataEntryProvider to add
	 * @return true if entryProvider added successfully, false if this DataLogger is already running
	 */
	public boolean addToLogger(DataEntryProvider entryProvider) {
		if(!isAlive()) {
			entryProviders.add(entryProvider);
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		
		File file = new File(fileName);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			
			writeHeadings(writer);
			
			//Repeat until closed
			while(!isInterrupted()) {
				long start = System.currentTimeMillis();
				String dataLine = "";
				
				for(DataEntryProvider data : entryProviders)
					dataLine += data.getEntry() + ", ";
				
				writer.write(dataLine + "\n");
				
				long end = System.currentTimeMillis();
				
				if(refreshPeriod - (end - start) > 0) //ensure minimum delay is met, otherwise sleeps for remainder
					Thread.sleep(refreshPeriod - (end - start));
			}
			
			writer.close();
		}
		catch (InterruptedException e) { // close file if interrupted
			try {
				writer.close();
			} catch (IOException e1) {}
		}
		catch (IOException e) {
			System.out.println("Error Writing file");
		}
	}
	
	public void writeHeadings(BufferedWriter writer) throws IOException {
		String heading = "";
		for(DataEntryProvider data : entryProviders)
			heading += data.HEADING + ", ";
			
		writer.write(heading + "\n");
	}
}
