package microstrain.sensorcloud.exception;


/**
 * The user tried to add a <b>Channel</b> that already exists on the <b>Sensor</b>
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class ChannelAlreadyExistsException extends InvalidRequestException {

	/**
	 * @param name  the name of the <b>Channel</b>
	 */
	public ChannelAlreadyExistsException(String name) {
		super( "Channel " + name + " already exists" );
	}

}
