package microstrain.sensorcloud.exception;

/**
 * The user attempted to access a <b>Sensor</b> that already exists on the <b>Device</b>
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class SensorAlreadyExistsException extends InvalidRequestException {
	/**
	 * @param name  sensor that already exists
	 */
	public SensorAlreadyExistsException (String name) {
		super("Sensor " + name + " already exists.");
	}
}
