package microstrain.sensorcloud.exception;

/**
 * An exception caused by the user providing an invalid input parameter
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidUserInputException extends Exception {
	/**
	 * @param message  description of what caused the exception
	 */
	public InvalidUserInputException (String message) {
		super(message);
	}
}
