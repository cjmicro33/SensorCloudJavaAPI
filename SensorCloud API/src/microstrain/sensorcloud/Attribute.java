package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;
import microstrain.sensorcloud.xdr.XDROutStream;
/**
 * An attribute is a mapping between a name and a value of some type.
 * The attribute value can be text, numeric, boolean, or custom.
 * The <b>Attribute</b> class is a wrapper for this value.
 * The number that corresponds to each type is defined by static variables.
 * A Text Attribute is a UTF-8 formated 
 * string.  Numeric is a text representation of a number in either fixed point notation or 
 * scientific notation ^[-+]?[0-9]+((.[0-9]+)?([eE][0-9]+)?)?$ .  Boolean is 0 or 1 in text from.  
 * Custom is an arbitrary string of bytes. Each channel can have up to 25 custom attributes.
 * 
 * @author Colin Cavanaugh
 *
 */
public class Attribute {
	/**
	 * Attribute value
	 */
	private byte [] value;
	
	/**
	 * Defines how the value will be treated
	 */
	private int type;
	
	/**
	 * A text value
	 */
	public final static int TEXT = 0;
	
	/**
	 * A numeric value
	 */
	public final static int NUMBER = 1;
	
	/**
	 * A custom value
	 */
	public final static int CUSTOM = 2;
	
	/**
	 * A boolean value
	 */
	public final static int BOOL = 3;
	
	/**
	 * Constructor for user to add arbitrary data.
	 * This method allows the user to control the serialization and deserialization of their attribute value.
	 * 
	 * @param value  string representation of the attribute
	 * @param type  type defined by static values
	 */
	public Attribute (byte [] value, int type) {
		this.value = value;
		this.type = type;
	}
	
	/**
	 * Constructor for a text attribute
	 * 
	 * @param value  string of text
	 */
	public Attribute (String value) {
		this.value = value.getBytes();
		this.type = TEXT;
	}
	
	/**
	 * Constructor for a numerical attribute
	 * 
	 * @param value  the numerical value
	 */
	public Attribute (double value) { 
		this.value = Double.toString( value ).getBytes();
		this.type = NUMBER;
	}
	
	/**
	 * Constructor for a custom attribute.
	 * 
	 * 
	 * @param value  object that can be serialized to an opaque data string
	 * 
	 * @throws IOException
	 */
	public Attribute (Serializable value) throws IOException {
		// serialize the object
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(value);
		this.value = byteStream.toByteArray();
		this.type = CUSTOM;
	}
	
	/**
	 * Constructor for a boolean attribute
	 * 
	 * @param value  boolean
	 */
	public Attribute (Boolean value) {
		// boolean values are represented as a 1 or 0
		if (value.booleanValue()) {
			this.value = "true".getBytes();
		} else {
			this.value = "false".getBytes();
		}
		
		this.type = BOOL;
	}
	
	/**
	 * Get the raw string value.
	 * This method is used for parsing the attribute map.
	 * 
	 * @return Value
	 */
	public String getValueString() {
		return new String(value);
	}
	
	/**
	 * @return Value in a byte array
	 */
	public byte [] getValueBytes() {
		return value;
	}
	/**
	 * If the attribute is a text or numeric type then the return object will be a string.
	 * For numeric this string will be a text representation of the number.
	 * Boolean attributes will return a boolean object.
	 * For custom types the attribute will attempt to deserialize an object from the string.
	 * If you did not provide a serializable object in the constructor the raw string will be returned.
	 * 
	 * @return String representation of the value
	 * 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public Object getValue() throws IOException {
		switch (type) {
		case BOOL:
			return new Boolean( new String(value) );
		case CUSTOM:
			// try to deserialize an object from the string
			try {
				ObjectInputStream in = new ObjectInputStream( 
						new ByteArrayInputStream( value ));
				return in.readObject();
			} catch (Exception e) {
				// if its not a known class then return the raw string
				return value;
			}
		case NUMBER:
			return Double.parseDouble( new String(value) );
		}
		// text values should be returned as is
		return new String(value);
	}
	
	/**
	 * @return Type value defined by static members
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Returns XDR data for the attribute to be added to SensorCloud
	 * 
	 * @return SensorCloud representation of the <b>Attribute</b>
	 * 
	 * @throws IOException
	 */
	public byte [] toXDR() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream(byteStream);
		
		xdrStream.writeInt(1);
		xdrStream.writeInt( type );
		xdrStream.writeOpaque(value);
			
		return byteStream.toByteArray();
	}
	
	 /**
	  * Returns an instance of an attribute from its xdr data
	  * 
	  * @param xdr  data from a get attribute request
	  * @return Attribute
	  * 
	  * @throws IOException
	  */
	public static Attribute getInstanceOf (byte [] xdr) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream( xdr );
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException(version);
		}
		
		int type = xdrStream.readInt();
		int bytes = xdrStream.readInt();
		byte [] buff = new byte [bytes];
		inStream.read(buff);
		byte [] value = buff;
		
		return new Attribute( value, type );
	}
	
}
