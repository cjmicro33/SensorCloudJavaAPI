package microstrain.sensorcloud;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for connecting this API to the Web API Interface for SensorCloud
 * 
 * @author Colin Cavanaugh
 *
 */
public interface Requester {
	
	/**
	 * Performs an HTTP GET request at the given URL
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @return Data from the request
	 * 
	 * @throws IOException 
	 */
	public byte [] get (String url) throws IOException;
	
	
	/**
	 * Performs an HTTP GET request at the given URL with the included parameters
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @param params  key-value pairs
	 * @return Data from the request
	 * 
	 * @throws IOException
	 */
	public byte [] get (String url, Map <String,String> params) throws IOException;
	
	
	/**
	 * Performs an HTTP POST request at the given URL
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @param data  data to be posted to the URL
	 * 
	 * @throws IOException
	 */
	public void post (String url, byte [] data) throws IOException;
	
	
	/**
	 * Performs an HTTP POST request at the given URL with the included parameters
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @param params  key-value pairs
	 * @param data  data to be posted to the URL
	 * 
	 * @throws IOException
	 */
	public void post (String url, Map <String,String> params, byte [] data) throws IOException;
	
	/**
	 * Performs an HTTP PUT request at the given URL
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @param data  data to be put at the URL
	 * 
	 * @throws IOException
	 */
	public void put (String url, byte [] data) throws IOException;
	
	
	/**
	 * Performs an HTTP PUT request at the given URL with the included parameters
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * @param params  key-value pairs
	 * @param data  data to be put at the URL
	 * 
	 * @throws IOException
	 */
	public void put (String url, Map <String,String> params, byte [] data) throws IOException;
	
	
	/**
	 * Performs an HTTP DELETE request at the given URL
	 * 
	 * @param url  address for the request starting after /devices/<device_name>/
	 * 
	 * @throws IOException
	 */
	public void delete(String url) throws IOException;
	
	
	/**
	 * @return Serial of the authorized device
	 */
	String getSerial();
}
