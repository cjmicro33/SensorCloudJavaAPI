package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import microstrain.sensorcloud.exception.AttributeAlreadyExistsException;
import microstrain.sensorcloud.exception.AttributeNotFoundException;
import microstrain.sensorcloud.exception.ChannelAlreadyExistsException;
import microstrain.sensorcloud.exception.ChannelDoesNotExistException;
import microstrain.sensorcloud.exception.ChannelHasNoAttributesException;
import microstrain.sensorcloud.exception.InvalidDescriptionException;
import microstrain.sensorcloud.exception.InvalidLabelException;
import microstrain.sensorcloud.exception.InvalidNameException;
import microstrain.sensorcloud.exception.InvalidRequestException;
import microstrain.sensorcloud.exception.InvalidUserInputException;
import microstrain.sensorcloud.exception.SCHTTPException;
import microstrain.sensorcloud.exception.SensorDoesNotExistException;
import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;
import microstrain.sensorcloud.xdr.XDROutStream;

/**
 * A Sensor represents a collection of related channels.  A sensor may be a physical sensor like a G-Link or 
 * 3DM-GX3-35, or it could be a virtual sensor like stock info.  Each sensor may contain one or more 
 * channels, and in turn each channel may contain one or more data-streams.  A Data-stream represents 
 * the data that the sensor sampled.  SensorCloud supports the concept of multiple stream types, but 
 * currently the only data-stream that is available is the time-series data-stream.  In the future additional 
 * data-stream types may be added to SensorCloud.
 * 
 * @author Colin Cavanaugh
 *
 */
public class Sensor {
	
	private String name, label, type, description;
	
	/**
	 * A <b>Requester</b> authorized for the <b>Sensor</b>'s parent device
	 */
	private Requester requester;
	
	/**
	 * Class Constructor 
	 * only accessible through the getInstanceOfMethod to prevent orphan sensors
	 * 
	 * @param name  the sensor's name
	 * @param label  the sensor's label
	 * @param type  the sensor's type
	 * @param description  the sensor's description
	 * @param requester  authorized requester
	 */
	private Sensor (String name, String label, String type, String description, Requester requester) {
		this.name = name;
		this.label = label;
		this.type = type;
		this.description = description;
		this.requester = requester;
	}
	
	/**
	 * Creates a channel on SensorCloud
	 * 
	 * @param name  a unique identifier for the <b>Channel</b>
	 * @param label  a human readable name for the new <b>Channel</b>
	 * @param description  details about the new <b>Channel</b>
	 * @return An instance of the created <b>Channel</b>
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException 
	 */
	public Channel createChannel (String name, String label, String description) throws IOException, InvalidUserInputException, InvalidRequestException {
		// check for invalid user input
		if (name.length() > 50) {
			throw new InvalidNameException(name,  "Name has over 50 characters" );
		} else if (name.contains(" ")) {
			throw new InvalidNameException(name, "Names must be made up of: A-Z a-z 0-9 _" );
		}
		
		if (label.length() > 50) {
			throw new InvalidLabelException(label, "Label has over 50 characters" );
		}
		
		if (description.length() > 500) {
			throw new InvalidDescriptionException(description, "Description has over 500 characters" );
		}
		
		// build the xdr data for the request
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream(byteStream);
		
		xdrStream.writeInt(1);
		xdrStream.writeString(label);
		xdrStream.writeString(description);
		
		byte [] xdr = byteStream.toByteArray();
		xdrStream.close();
		
		// add the channel to the sensor
		String url = "sensors/" + this.name + "/channels/" + name + "/";
		
		try { 
			requester.put(url, xdr); 
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
		
		// return an instance of the new channel
		return Channel.getInstanceOf(xdr, name, this.name, requester);
	}
	
	/**
	 * Returns all channels owned by the sensor
	 * 
	 * @return List of Channels
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public List <Channel> getAllChannels () throws IOException, InvalidRequestException {

		String url = "sensors/" + this.name + "/channels/";
		try {
			return Channel.getInstanceOfAll( requester.get(url), this.name, requester );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Returns an instance of a sensors channel, throws a ChannelDoesNotExistException 
	 * if the sensor is not found
	 * 
	 * @param name  the channel to be returned
	 * @return Channel
	 * 
	 * @throws InvalidRequestException
	 * @throws IOException
	 */
	public Channel getChannel (String name) throws InvalidRequestException, IOException {
		List <Channel> chans = getAllChannels();
		Iterator<Channel> itr = chans.iterator();
		
		// search for the requsted channel in the list
		while (itr.hasNext()) {
			Channel chan = itr.next();
			if (chan.getName().equals(name)) {
				return chan;
			}
		}
		throw new ChannelDoesNotExistException(name); /* the channel wasn't in the list */
	}

	/**
	 * Removes a <b>Channel</b> from SensorCloud
	 * 
	 * @param name  the <b>Channel</b> to be removed
	 * 
	 * @throws InvalidRequestException
	 * @throws IOException
	 */
	public void deleteChannel (String name) throws InvalidRequestException, IOException {
		
		String url = "sensors/" + this.name + "/channels/" + name + "/";
		
		try {
			requester.delete(url);
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * @param label  human readable name for the <b>Sensor</b>
	 * @throws IOException
	 */
	public void setLabel (String label) throws IOException {
		this.label = label;
		update();
	}
	
	/**
	 * @param type  identifier for groups of <b>Sensors</b>
	 * @throws IOException
	 */
	public void setType (String type) throws IOException {
		this.type = type;
		update();
	}
	
	/**
	 * @param description  details about the <b>Sensor</b>
	 * @throws IOException
	 */
	public void setDescription (String description) throws IOException {
		this.description = description;
		update();
	}
	
	/**
	 * @return Unique identifier for the <b>Sensor</b>
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The sensor's label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return The sensor's type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Details about the sensor
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get one of the <b>Sensor</b>'s <b>Attributes</b>
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * @return The <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public Attribute getAttribute (String name) throws IOException, InvalidRequestException {
		try {
			return Attribute.getInstanceOf( requester.get( "sensors/" + this.name + "/attributes/" + name + "/" ));
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Get all of the <b>Sensor</b>'s <b>Attributes</b>
	 * 
	 * @return Map of each name to its <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public AttributeMap getAllAttributes() throws IOException, InvalidRequestException {
		AttributeMap map = null;
		try {
			map = AttributeMap.getInstanceOf( requester.get( "sensors/" + this.name + "/attributes/" ) );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			throw parseException(e, params);
		}
		
		if (map.size() == 0) {
			throw new ChannelHasNoAttributesException(name);
		}
		
		return map;
	}
	
	/**
	 * Add an <b>Attribute</b> to the <b>Sensor</b>
	 * 
	 * @param name  unique identifier for the <b>Attribute</b>
	 * @param attribute  value of the <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void addAttribute (String name, Attribute attribute) throws IOException, InvalidRequestException {
		try {
			requester.post( "sensors/" + this.name + "/attributes/" + name + "/", attribute.toXDR() );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add( name );
			throw parseException(e, params);
		}
	}
	
	/**
	 * Add several attributes to the <b>Channel</b>.
	 * 
	 * @param attributes  map of name to <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void bulkAddAttribute (AttributeMap attributes) throws IOException, InvalidRequestException {
		try {
			requester.post( "sensors/" + this.name + "/attributes/", attributes.toXDR() );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			throw parseException(e, params);
		}
	}
	
	/**
	 * Delete one of the <b>Sensor</b>'s <b>Attributes<b> 
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void deleteAttribute (String name) throws IOException, InvalidRequestException {
		try {
			requester.delete( "sensors/" + this.name + "/attributes/" + name + "/" );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add( name );
			throw parseException(e, params);
		}
	}
	
	/**
	 * Updates SensorCloud with the current values of the sensor object
	 * called after every set method
	 * 
	 * @throws IOException
	 */
	private void update() throws IOException {
		String url = "sensors/" + name + "/";
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream(byteStream);
		
		xdrStream.writeInt(1);
		xdrStream.writeString(type);
		xdrStream.writeString(label);
		xdrStream.writeString(description);
		
		byte [] data = byteStream.toByteArray();
		xdrStream.close();
		requester.post(url, data);
	}
	
	/**
	 * Returns an instance of a sensor from its SensorCloud xdr data.
	 * 
	 * @param name  the sensors name, not included in the xdr data
	 * @param xdr  xdr representation of the sensor
	 * @param requester  authorized for that sensors device
	 * @return sensor  instance of the sensor
	 * 
	 * @throws IOException
	 */
	public static Sensor getInstanceOf (String name, byte [] xdr, Requester requester) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream( xdr );
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		// check the version number, also a weak check for improperly formatted data
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException( "Version number " + version + " is not recognized" );
		}
		
		String type = xdrStream.readString();
		String label = xdrStream.readString();
		String description = xdrStream.readString();
		
		return new Sensor(name, label, type, description, requester);
	}
	
	/**
	 * Returns a list of sensors from XDR data after a get sensors API request.
	 * 
	 * @param xdr  data from a get sensors api call
	 * @param requester  authorized for the sensors device
	 * @return list of sensors
	 * 
	 * @throws IOException
	 */
	public static List<Sensor> getInstanceOfAll (byte [] xdr, Requester requester) throws IOException {
		List <Sensor> sensors = new LinkedList<Sensor>();
		
		ByteArrayInputStream inStream = new ByteArrayInputStream(xdr);
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		// check the version number, also a weak check for improperly formatted data
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException( version );
		}

		int count = xdrStream.readInt(); // number of sensors
		for (int i = 0; i < count; i++) {
			String name = xdrStream.readString();
			String type = xdrStream.readString();
			String label = xdrStream.readString();
			String description = xdrStream.readString();
			
			/*
			 * channel data is not stored in the object so it is discarded here
			 * it will be generated dynamically when the get channel methods are called 
			 */
			int chanCount = xdrStream.readInt(); // number of channels
			for (int j = 0; j < chanCount; j++) {
				xdrStream.readString();
				xdrStream.readString();
				xdrStream.readString();
				
				int streamCount = xdrStream.readInt(); // number of data streams
				for (int k = 0; k < streamCount; k++) {
					xdrStream.readString();
					int skip = xdrStream.readInt(); // the size of the stream info in bytes
					xdrStream.skip(skip); // skip the stream info
				}
			}
			
			sensors.add( new Sensor(name, label, type, description, requester) );
		}
		return sensors;
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
		switch (e.getStatusCode()) {
		case 400:
			String message = e.getMessage();
			if (message.contains( "attribute" )) {
				return new AttributeAlreadyExistsException( params.get(0) );
			} else if (message.contains( "Version" )) {
				throw new VersionNotSupportedException("Please update your API");
			} else if (message.contains( "Channel " )){
				return new ChannelAlreadyExistsException( params.get(0) );
			}
			break;
		case 404:
			if (e.getMessage().contains( "attributes" )) {
				return new AttributeNotFoundException( params.get(0) );
			} else if ( e.getMessage().contains( "DNE" )) {
				return new ChannelDoesNotExistException( name );
			} else if ( e.getMessage().contains( "sensor" )) {
				return new SensorDoesNotExistException(name);
			}
		}
		return new InvalidRequestException( e.getStatusCode() + ": " + e.getMessage() );
	}
	
	@Override
	public boolean equals (Object o) {
		Sensor sensor;
		try {
			sensor = (Sensor)o;
		} catch (ClassCastException e) {
			return false;
		}
		
		if ( sensor.name.equals(name) && sensor.requester.getSerial().equals( requester.getSerial() )) {
			return true;
		}
		
		return false;
	}
}
