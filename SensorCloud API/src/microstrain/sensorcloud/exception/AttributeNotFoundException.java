package microstrain.sensorcloud.exception;

/**
 * The user requested an <b>Attribute</b> that does not exist.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class AttributeNotFoundException extends InvalidRequestException {

	/**
	 * @param name  the attribute requested
	 */
	public AttributeNotFoundException(String name) {
		super( "Attribute " + name + " does not exist" );
	}

}
