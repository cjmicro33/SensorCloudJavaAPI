package microstrain.sensorcloud.exception;

/**
 * Thrown when the user enters a samplerate type that is not defined by the <b>SampleRate</b> class
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidSampleRateTypeException extends InvalidUserInputException {
	/**
	 * @param type  invalid type
	 */
	public InvalidSampleRateTypeException (int type) {
		super( "SampleRate type " + type + " is invalid" );
	}
}
