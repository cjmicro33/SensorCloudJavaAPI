package microstrain.sensorcloud.exception;

/**
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class InvalidAuthenticationCredentialsException extends
		InvalidUserInputException {

	/**
	 * @param serial  Device serial
	 */
	public InvalidAuthenticationCredentialsException(String serial) {
		super("The credentials for " + serial + " are invalid");
	}

}
