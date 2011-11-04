package microstrain.sensorcloud.exception;

/**
 * The user requested a <b>Channel</b> that does not exist
 * or tried to use a <b>Channel</b> that was deleted
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class ChannelDoesNotExistException extends InvalidRequestException {
	/**
	 * @param name  the channel requested
	 */
	public ChannelDoesNotExistException (String name) {
		super("Channel " + name + " does not exist.");
	}
}
