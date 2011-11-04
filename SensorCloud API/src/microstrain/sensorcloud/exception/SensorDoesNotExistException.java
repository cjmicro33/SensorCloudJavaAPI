package microstrain.sensorcloud.exception;

/**
 * The user tried to access a <b>Sensor</b> that does not exist on the <b>Device</b>
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class SensorDoesNotExistException extends InvalidRequestException {
	/**
	 * @param name  missing sensors name
	 */
	public SensorDoesNotExistException (String name) {
		super("Sensor " + name + " does not exist.");
	}
}
