package DPM_TEAM04.logging;

public abstract class DataEntryProvider {
	
	//heading for data
	public final String HEADING;
	
	public DataEntryProvider(String heading) {
		HEADING = heading;
	}
	
	/**
	 * Getter for data entry, implementation is sensor/data provider specific
	 * @return an entry representing the current value of the DataEntryProvider
	 */
	public abstract double getEntry();
}
