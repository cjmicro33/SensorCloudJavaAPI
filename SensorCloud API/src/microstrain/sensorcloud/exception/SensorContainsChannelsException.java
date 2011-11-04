package microstrain.sensorcloud.exception;

/**
 * Thrown when you try to delete a Sensor that still has channels
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class SensorContainsChannelsException extends InvalidRequestException {

	/**
	 * @param name  name of the sensor
	 */
	public SensorContainsChannelsException(String name) {
		super( "Sensor " + name + " cannot be deleted because it still contains channels" );
	}

}