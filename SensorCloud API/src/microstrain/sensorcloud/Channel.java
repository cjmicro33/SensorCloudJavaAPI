package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import microstrain.sensorcloud.exception.AttributeAlreadyExistsException;
import microstrain.sensorcloud.exception.AttributeNotFoundException;
import microstrain.sensorcloud.exception.ChannelDoesNotExistException;
import microstrain.sensorcloud.exception.ChannelHasNoAttributesException;
import microstrain.sensorcloud.exception.InvalidRequestException;
import microstrain.sensorcloud.exception.SCHTTPException;
import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;
import microstrain.sensorcloud.xdr.XDROutStream;

/**
 * A Channel on SensorCloud is a container of data streams. 
 * Right now the only stream implemented is the <b>TimeSeriesStream</b>.
 * 
 * 
 * @author Colin Cavanaugh
 *
 */
public class Channel {
	/**
	 * Unique identifier of the <b>Channel</b>
	 */
	private String name;
	
	/**
	 * Human readable name for the <b>Channel</b>
	 */
	private String label;
	
	/**
	 * Details about the <b>Channel</b>
	 */
	private String description;
	
	/**
	 * Parent <b>Sensor</b>'s name
	 */
	private String sensorName;
	
	/** 
	 * <b>Requester</b> authorized with the parent <b>Device</b> 
	 */
	private Requester requester;
	
	/**
	 * Private class constructor to prevent orphan <b>Channels</b>.
	 * 
	 * @param name  unique identifier of the <b>Channel</b>
	 * @param label  human readable name of the <b>Channel</b>
	 * @param description  details about the <b>Channel</b>
	 * @param sensorName  parent <b>Sensor</b>'s name
	 * @param requester  authorized <b>Requester</b>
	 */
	private Channel (String name, String label, String description, String sensorName, Requester requester) {
		this.name = name;
		this.label = label;
		this.description = description;
		this.sensorName = sensorName;
		this.requester = requester;
	}
	
	/**
	 * Get the <b>TimeSeriesStream</b> of the <b>Channel</b>.
	 * If the <b>Channel</b> does not have one an empty <b>TimeSeriesStream</b> object is returned for the user to add data.
	 * An empty <b>TimeSeriesStream</b> can be identified by an end time of 0.
	 * 
	 * @return An instance of the <b>Channel</b>'s data stream
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public TimeSeriesStream getTimeSeriesStream() throws IOException, InvalidRequestException {
		try {
			return TimeSeriesStream.getInstanceOf(requester.get("sensors/" + sensorName + "/channels/" + name + "/streams/timeseries/"), sensorName, name, requester);
		} catch (SCHTTPException e) {
			if (e.getStatusCode() == 404) {
				return TimeSeriesStream.getEmptyInstanceOf(sensorName, name, requester);
			}
		List<String> params = new LinkedList<String>();
		params.add(name);
		throw parseException(e, params);
		}
		
	}
	
	/**
	 * @return Unique identifier of the <b>Channel</b>
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Human readable name of the <b>Channel</b>
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return Details about the <b>Channel</b>
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param label  human readable name of the <b>Channel</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public void setLabel (String label) throws IOException, InvalidRequestException {
		this.label = label;
		update();
	}
	
	/**
	 * @param description  details about the <b>Channel</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public void setDescription (String description) throws IOException, InvalidRequestException {
		this.description = description;
		update();
	}
	
	/**
	 * Get one of the <b>Channel</b>'s <b>Attributes</b>
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * @return The <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException 
	 */
	public Attribute getAttribute (String name) throws IOException, InvalidRequestException {
		try {
			return Attribute.getInstanceOf( requester.get( "sensors/" + sensorName + "/channels/" + this.name + "/attributes/" + name + "/" ));
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
	}
	
	/**
	 * Get all of the <b>Channel</b>'s <b>Attributes</b>
	 * 
	 * @return Map of each name to its <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public AttributeMap getAllAttributes() throws IOException, InvalidRequestException {
		AttributeMap map = null;
		try {
			map = AttributeMap.getInstanceOf( requester.get( "sensors/" + sensorName + "/channels/" + this.name + "/attributes/" ) );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add(name);
			throw parseException(e, params);
		}
		
		// does not return 404
		if (map.size() == 0) {
			throw new ChannelHasNoAttributesException(name);
		}
		
		return map;
	}
	
	/**
	 * Add an <b>Attribute</b> to the <b>Channel</b>
	 * 
	 * @param name  unique identifier for the <b>Attribute</b>
	 * @param attribute  value of the <b>Attribute</b>
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void addAttribute (String name, Attribute attribute) throws IOException, InvalidRequestException {
		try {
			requester.post( "sensors/" + sensorName + "/channels/" + this.name + "/attributes/" + name + "/", attribute.toXDR() );
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
			requester.post( "sensors/" + sensorName + "/channels/" + this.name + "/attributes/", attributes.toXDR() );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			throw parseException(e, params);
		}
	}
	
	/**
	 * Delete one of the <b>Channel</b>'s <b>Attributes<b> 
	 * 
	 * @param name  the <b>Attribute</b>'s name
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	public void deleteAttribute (String name) throws IOException, InvalidRequestException {
		try {
			requester.delete( "sensors/" + sensorName + "/channels/" + this.name + "/attributes/" + name + "/" );
		} catch (SCHTTPException e) {
			List<String> params = new LinkedList<String>();
			params.add( name );
			throw parseException(e, params);
		}
	}
	
	/**
	 * Updates SensorCloud with the most recent data for the <b>Channel</b>.
	 * This method is called after each set method.
	 * 
	 * @throws IOException
	 * @throws InvalidRequestException
	 */
	private void update() throws IOException, InvalidRequestException{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		XDROutStream xdrStream = new XDROutStream(byteStream);
		
		xdrStream.writeInt(1);
		xdrStream.writeString(label);
		xdrStream.writeString(description);

		try {
			requester.post( "sensors/" + sensorName + "/channels/" + name + "/", byteStream.toByteArray() );
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
		switch (e.getStatusCode()) {
		case 400:
			String message = e.getMessage();
			if (message.contains( "attribute " )) {
				return new AttributeAlreadyExistsException( params.get(0) );
			} else if (message.contains( "Version" )) {
				throw new VersionNotSupportedException("Please update your API");
			}
			break;
		case 404:
			if (e.getMessage().contains( "attributes" )) {
				return new AttributeNotFoundException( params.get(0) );
			} else if ( e.getMessage().contains( "channel" )) {
				return new ChannelDoesNotExistException( name );
			}
		}
		return new InvalidRequestException( e.getStatusCode() + ": " + e.getMessage() );
	}
	
	/**
	 * Get an instance of a <b>Channel</b>.
	 * Called after a get channel API request.
	 * 
	 * @param xdr  data from a get channel api request
	 * @param name  <b>Channel</b> name (not included in xdr data)
	 * @param sensorName  parent sensor of the <b>Channel</b>'s name
	 * @param requester  authorized <b>Requester</b>
	 * 
	 * @return An instance of the <b>Channel</b>
	 * 
	 * @throws IOException
	 */
	public static Channel getInstanceOf (byte [] xdr, String name, String sensorName, Requester requester) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(xdr);
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		// check the version number, also a weak check for improperly formatted data
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException( version );
		}
		
		String label = xdrStream.readString();
		String description = xdrStream.readString();
		
		return new Channel(name, label, description, sensorName, requester);		
	}
	
	/**
	 * Get an instance of a sensor's <b>Channels</b>.
	 * Called after a get channels API request.
	 * 
	 * @param xdr  data from a get channels api request
	 * @param sensorName  parent sensor of the <b>Channels</b>
	 * @param requester  authorized <b>Requester</b>
	 * 
	 * @return A list of the <b>Channels</b>
	 * 
	 * @throws IOException
	 * @throws VersionNotSupportedException
	 */
	public static List<Channel> getInstanceOfAll (byte [] xdr, String sensorName, Requester requester) throws IOException, VersionNotSupportedException {
		List <Channel> channelList = new LinkedList<Channel>();
		
		ByteArrayInputStream inStream = new ByteArrayInputStream(xdr);
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		// check the version number, also a weak check for improperly formatted data
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException( version );
		}
		
		int cnt = xdrStream.readInt();
		
		for (int i = 0; i < cnt; i++) {
			String name = null;
			String label = null;
			String description = null;
				name = xdrStream.readString();
				label = xdrStream.readString();
				description = xdrStream.readString();
				int ct = xdrStream.readInt();
				for (int j = 0; j < ct; j++) {
					xdrStream.readString();
					int bytes = xdrStream.readInt();
					xdrStream.skip(bytes);
				}
				channelList.add( new Channel(name, label, description, sensorName, requester) );
			}
		
		return channelList;
	}
}
