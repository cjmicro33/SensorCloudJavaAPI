package microstrain.sensorcloud.exception;

/**
 * This exception is a runtime exception because the API is unable to recover from it.
 * It can indicate misinterpreted XDR data or an out of date API.
 * 
 * @author c_cavanaugh
 *
 */
@SuppressWarnings("serial")
public class VersionNotSupportedException extends SensorCloudRuntimeException {

	/**
	 * Used when a SensorCloud request returns a version not supported error
	 * 
	 * @param message  information about the exception
	 */
	public VersionNotSupportedException (String message) {
		super(message);
	}
	
	/**
	 * Used when a SensorCloud request returns a version not supported error
	 * 
	 * @param version  incorrect version number
	 */
	public VersionNotSupportedException (int version) {
		super("Version " + version + " not recognized");
	}

}
