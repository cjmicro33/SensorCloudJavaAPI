package microstrain.sensorcloud.exception;

/**
 * Thrown when the user provides an invalid type string.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidTypeException extends InvalidUserInputException {
	/**
	 * @param type  invalid type
	 * @param reason  why the type is invalid
	 */
	public InvalidTypeException (String type, String reason) {
		super( "Invalid type: " + type + "\n" + reason );
	}
}
