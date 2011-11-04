package microstrain.sensorcloud.exception;

/**
 * The user provided an invalid name string.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidNameException extends InvalidUserInputException {
	/**
	 * @param name  invalid name
	 * @param reason  why the name is invalid
	 */
	public InvalidNameException (String name, String reason) {
		super( "Invalid Name: " + name + "\n" + reason );
	}

}
