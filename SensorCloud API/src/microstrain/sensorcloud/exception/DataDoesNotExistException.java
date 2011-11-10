package microstrain.sensorcloud.exception;

import microstrain.sensorcloud.SampleRate;

/**
 * The user requested data that does not exist on the data stream
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class DataDoesNotExistException extends InvalidRequestException {
	/**
	 * Constructor if user did not specify a <b>SampleRate</b>
	 * 
	 * @param startTime  timestamp at the begining of requested data
	 * @param endTime  timestamp at the end of the requested data
	 */
	public DataDoesNotExistException (long startTime, long endTime) {
		super("No data exists between " + startTime + " and " + endTime + " at any sample rate");
	}
	
	/**
	 * Constructor if user specified a <b>SampleRate</b>
	 * 
	 * @param startTime  timestamp at the begining of requested data
	 * @param endTime  timestamp at the end of the requested data
	 * @param samplerate  sample rate of data requested
	 */
	public DataDoesNotExistException (long startTime, long endTime, SampleRate samplerate) {
		super("No data exists between " + startTime + " and " + endTime + " at " + samplerate.toString());
	}
	
	/**
	 * Uses message from JSON HTTP error 
	 * 
	 * @param message  errorcode message
	 */
	public DataDoesNotExistException (String message) {
		super( message );
	}
}
