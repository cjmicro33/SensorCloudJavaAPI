package microstrain.sensorcloud.exception;


/**
 * The user tried to add an attribute that already existed
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class AttributeAlreadyExistsException extends InvalidRequestException {

	/**
	 * @param name  the name of the attribute
	 */
	public AttributeAlreadyExistsException(String name) {
		super( "Attribute " + name + " already exists" );
	}

}
