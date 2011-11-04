package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;
import microstrain.sensorcloud.xdr.XDROutStream;

/**
 * A map of attribute names to their corresponding <b>Attribute</b>.
 * This class adds the functionality of parsing the contents of the map
 * into XDR data.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class AttributeMap extends TreeMap<String, Attribute> {
	/**
	 * Returns the contents of the map as XDR data for a bulk add attribute API call.
	 * 
	 * @return XDR data
	 * 
	 * @throws IOException
	 */
	public byte [] toXDR() throws IOException {
		// iterating over the key set
		Set <String> keys = super.keySet();
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream( outStream );
		
		xdrStream.writeInt(1);
		xdrStream.writeInt(keys.size()); // number of attributes
		
		for (String key : keys) {
			xdrStream.writeInt( super.get(key).getType() );
			xdrStream.writeString( key );
			xdrStream.writeOpaque( super.get(key).getValueBytes() );
		}
		return outStream.toByteArray();
	}
	
	/**
	 * Parses an <b>AttributeMap</b> from XDR data.
	 * The XDR data should come from a get all attributes API request.
	 * 
	 * @param xdr  xdr data from request
	 * @return Map of the attributes
	 * 
	 * @throws IOException
	 */
	public static AttributeMap getInstanceOf (byte [] xdr) throws IOException {
		AttributeMap map = new AttributeMap();
		
		ByteArrayInputStream inStream = new ByteArrayInputStream( xdr );
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException(version);
		}
		
		int count = xdrStream.readInt();
		
		for (int i = 0; i < count; i++) {
			int type = xdrStream.readInt();
			String name = xdrStream.readString();
			byte [] value = xdrStream.readOpaque();
			
			map.put( name, new Attribute(value, type) );
		}
		
		return map;
	}
}
