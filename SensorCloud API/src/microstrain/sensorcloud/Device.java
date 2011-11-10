package microstrain.sensorcloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import microstrain.sensorcloud.exception.*;
import microstrain.sensorcloud.json.JSONException;
import microstrain.sensorcloud.json.JSONObject;
import microstrain.sensorcloud.xdr.XDROutStream;

/**
 * A Device represents a single point of aggregation on SensorCloud. A device may be a physical device that is collecting data from 
 * multiple sensors or a device could be a virtual device that is pulling data from multiple streams. A MicroStrain WSDA(Wireless Sensor Data 
 * Aggregator) is one example of a device. The device is a container for sensors.
 * 
 * @author Colin Cavanaugh
 *
 */

public class Device {
	
	/** serial number of the device */
	private String serial;
	
	/** Requester for SensorCloud Communication */
	private Requester requester;
	
	/**
	 * Class Constructor
	 * 
	 * @param serial 	the serial number for the device
	 * @param requester the SensorCloud requester
	 */
	public Device (String serial, Requester requester) {
		//TODO: Make this method protected
		this.serial = serial;
		this.requester = requester;
	}
	
	/**
	 * Creates a sensor on SensorCloud
	 * 
	 * @param name  the name of the sensor
	 * @param label  a string for the sensors label
	 * @param type  a string for the sensors type
	 * @param description  a string for the sensors description
	 * @return sensor  a sensor object from the created sensor
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException
	 */
	public Sensor createSensor (String name, String label, String type, String description) throws IOException, InvalidUserInputException, InvalidRequestException {
		// Verify Paramaters 		
		if (name.length() > 50) { throw new InvalidNameException( name, "Name has over 50 characters" ); }
		else if (name.contains(" ")) { throw new InvalidNameException( name, "Name must be made up of: A-Z a-z 0-9 _" );	}
		
		if (label.length() > 50) { throw new InvalidLabelException( label, "Label has over 50 characters" );	}
		
		if (type.length() > 50) { throw new InvalidTypeException( type, "Type has over 50 characters" ); }
		
		if (description.length() > 50) { throw new InvalidDescriptionException(description, "Description has over 50 characters" ); }
		
		String url = "sensors/" + name + "/";
		
		// build xdr data
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream(byteStream);
		
		xdrStream.writeInt(1);
		xdrStream.writeString(type);
		xdrStream.writeString(label);
		xdrStream.writeString(description);
		
		byte [] data = byteStream.toByteArray();
		xdrStream.close();
		
		try {
			requester.put(url, data);
		} catch (SCHTTPException e) {
			List<String> params = new ArrayList<String>();
			params.add(name);
			throw parseException(e, params);
		}
		return Sensor.getInstanceOf(name, data, requester);
	}
	
	/**
	 * Gets the object of a sensor that already exists on SensorCloud
	 * 
	 * @param name  the sensors name
	 * @return sensor  the sensor object requested
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public Sensor getSensor (String name) throws IOException, InvalidRequestException {
		String url = "sensors/" + name + "/";
		try {
			return Sensor.getInstanceOf( name, requester.get(url), requester );
		} catch (SCHTTPException e) {
			List<String> params = new ArrayList <String> ();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Deletes a sensor that exists on SensorCloud
	 * 
	 * @param name  the sensors name
	 * @throws InvalidRequestException
	 * @throws IOException
	 */
	public void deleteSensor (String name) throws InvalidRequestException, IOException {
		String url = "sensors/" + name + "/";
		
		try {
			requester.delete(url);
		}  catch (SCHTTPException e) {
			List<String> params = new ArrayList <String> ();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Deletes a Sensor and all of its underlying channels. Each time a channel is deleted an API call is made.
	 * 
	 * @param name  name of the sensor
	 * @return The number of Channels deleted and the number of API calls made
	 * 
	 * @throws InvalidRequestException
	 * @throws IOException
	 */
	public int deleteSensorAndChannels (String name) throws InvalidRequestException, IOException {
		int i = 0;
		try {
			deleteSensor( name );
		} catch (SensorContainsChannelsException e) {
			Sensor sensor = getSensor(name);
			List <Channel> channels = sensor.getAllChannels();
			
			for (Channel channel : channels) {
				sensor.deleteChannel( channel.getName() );
				i++;
			}
			deleteSensor( name );
		}
		
		return i;
	}
	/**
	 * Gets every sensor that is on the device
	 * 
	 * @return A List containing and instance of each sensor
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public List <Sensor> getAllSensors () throws IOException, InvalidRequestException {
		String url = "sensors/";
		try {
			return Sensor.getInstanceOfAll( requester.get(url), requester );
		} catch (SCHTTPException e) {
			List<String> params = new ArrayList <String> ();
			throw parseException(e, params);
		}
	}
	
	/** 
	 * @return Serial string
	 */
	public String getSerial() {
		return serial;
	}
	
	/**
	 * Get one of the <b>Device</b>'s <b>Attributes</b>
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * @return The <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public Attribute getAttribute (String name) throws IOException, InvalidRequestException {
		try {
			return Attribute.getInstanceOf( requester.get( "attributes/" + name + "/" ));
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Get all of the <b>Device</b>'s <b>Attributes</b>
	 * 
	 * @return Map of each name to its <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public AttributeMap getAllAttributes() throws IOException, InvalidRequestException {
		AttributeMap map = null;
		try {
			map = AttributeMap.getInstanceOf( requester.get( "attributes/" ) );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			throw parseException(e, params);
		}
		
		if (map.size() == 0) {
			throw new DeviceHasNoAttributesException(serial);
		}
		
		return map;
	}
	
	/**
	 * Add an <b>Attribute</b> to the <b>Device</b>
	 * 
	 * @param name  unique identifier for the <b>Attribute</b>
	 * @param attribute  value of the <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void addAttribute (String name, Attribute attribute) throws IOException, InvalidRequestException {
		try {
			requester.post( "attributes/" + name + "/", attribute.toXDR() );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add( name );
			throw parseException(e, params);
		}
	}
	
	/**
	 * Add several attributes to the <b>Device</b>
	 * 
	 * @param attributes  map of name to <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void bulkAddAttribute (AttributeMap attributes) throws IOException, InvalidRequestException {
		try {
			requester.post( "attributes/", attributes.toXDR() );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			throw parseException(e, params);
		}
	}
	
	/**
	 * Delete one of the <b>Device</b>'s <b>Attributes<b> 
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void deleteAttribute (String name) throws IOException, InvalidRequestException {
		try {
			requester.delete( "attributes/" + name + "/" );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add( name );
			throw parseException(e, params);
		}
	}

	/**
	 * Parses HTTP exceptions into specific <b>SensorCloudExceptions</b>.
	 * 404 errors are parsed as a missing <b>Channel</b> since a missing time series will be ignored.
	 * 
	 * @param e  exception from an HTTP request
	 * @param params  objects to be passed into the exception constructor, e.g. name
	 * @return SensorCloud exception
	 */
	private InvalidRequestException parseException (SCHTTPException e, List <String> params) {
		String message = e.getMessage();
		
		try {
			JSONObject json = new JSONObject(message);
			
			if (json.has( "errorcode" )) {
				String code = json.getString( "errorcode" );
				String [] codes = code.split("-");
				int x = Integer.parseInt( codes[0] );
				int y = Integer.parseInt( codes[1] );
				
				switch (x) {
				case 400:
					switch (y) {
					case 9:
						return new SensorContainsChannelsException( params.get(0) );
					}
				}
			}
		} catch (JSONException excep) {
			
		}
		
		switch (e.getStatusCode()) {
		case 400:
			
			if (e.getMessage().contains( "attribute" )) {
				return new AttributeAlreadyExistsException( params.get(0) );
			} else if (message.contains( "Version" )) {
				throw new VersionNotSupportedException("Please update your API");
			} else {
				return new SensorAlreadyExistsException( params.get(0) );
			}

		case 404:
			if (e.getMessage().contains( "attribute" )) {
				return new AttributeNotFoundException( params.get(0) );
			} else if ( e.getMessage().contains( "sensor" )) {
				return new SensorDoesNotExistException( params.get(0) );
			}
		}
		return new InvalidRequestException( e.getStatusCode() + ": " + e.getMessage() );
	}
}
	