package microstrain.sensorcloud.exception;

/**
 * The user provided an invalid description string.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidDescriptionException extends InvalidUserInputException {
	/**
	 * @param description  invalid description
	 * @param reason  why the description is invalid
	 */
	public InvalidDescriptionException (String description, String reason) {
		super( "Invalid Description: " + description + "\n" + reason);
	}
}
