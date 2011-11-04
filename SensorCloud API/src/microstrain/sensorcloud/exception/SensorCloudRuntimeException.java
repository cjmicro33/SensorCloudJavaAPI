package microstrain.sensorcloud.exception;

/**
 * This is the super class for all SensorCloud runtime exceptions.
 * These are exceptions that the API cannot recover from and usually indicate
 * an outdated API
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class SensorCloudRuntimeException extends RuntimeException {
	/**
	 * @param message  description of what cause the exception
	 */
	public SensorCloudRuntimeException (String message) {
		super(message);
	}
}
