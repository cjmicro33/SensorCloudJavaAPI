package microstrain.sensorcloud.exception;

/**
 * Thrown when an invalid timestamp is encountered
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidTimestampException extends InvalidUserInputException {
	/**
	 * @param timestamp  invalid timestamp
	 * @param reason  why the timestamp was invalid
	 */
	public InvalidTimestampException (long timestamp, String reason) {
		super( "Invalid Timestamp: " + timestamp + "\n" + reason );
	}

}
