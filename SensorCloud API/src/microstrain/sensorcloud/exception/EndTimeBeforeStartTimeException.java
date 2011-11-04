package microstrain.sensorcloud.exception;

/**
 * @author c_cavanaugh
 *
 */
@SuppressWarnings("serial")
public class EndTimeBeforeStartTimeException extends InvalidUserInputException {
	/**
	 * @param startTime  start timestamp
	 * @param endTime  end timestamp
	 */
	public EndTimeBeforeStartTimeException (long startTime, long endTime) {
		super( "The end timestamp(" + endTime + ") comes before the start timestamp(" + startTime + ")" );
	}
}
