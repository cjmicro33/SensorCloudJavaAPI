package microstrain.sensorcloud.exception;

/**
 * The user provided an invalid labeln string.
 * 
 * @author c_cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidLabelException extends InvalidUserInputException {
	/**
	 * @param label  invalid label
	 * @param reason  why the label is invalid
	 */
	public InvalidLabelException (String label, String reason) {
		super( "Invalid Label: " + label + "\n" + reason);
	}
}
