package microstrain.sensorcloud.exception;

/**
 * The user requested all attributes from a <b>Device</b> that had none.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class DeviceHasNoAttributesException extends InvalidRequestException {
	/**
	 * @param serial  serial number of the device
	 */
	public DeviceHasNoAttributesException(String serial) {
		super( "Device " + serial + " has no attributes" );
	}

}
