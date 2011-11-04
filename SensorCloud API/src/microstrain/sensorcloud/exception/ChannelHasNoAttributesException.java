package microstrain.sensorcloud.exception;

/**
 * The user requested all <b>Attributes</b> from a <b>Channel</b> that has none
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class ChannelHasNoAttributesException extends InvalidRequestException {
	/**
	 * @param name  the name of the <b>Channel</b>
	 */
	public ChannelHasNoAttributesException(String name) {
		super( "Channel " + name + " has no attributes" );
	}

}
