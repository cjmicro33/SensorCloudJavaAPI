package microstrain.sensorcloud.exception;

/**
 * This exception type is thrown when the user causes a bad API request. 
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidRequestException extends Exception {
	/**
	 * @param message  information about what cause the exception
	 */
	public InvalidRequestException(String message) {
		super(message);
	}
}
