package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.xml.ws.http.HTTPException;

import microstrain.sensorcloud.exception.SCHTTPException;
import microstrain.sensorcloud.exception.SensorCloudRuntimeException;
import microstrain.sensorcloud.xdr.XDRInStream;

/**
 * The standard <b>Requester</b> for making API calls to SensorCloud
 * 
 * @author Colin Cavanaugh
 *
 */
public class SCRequester implements Requester{
	private String serial, authKey, authServer, authToken, baseURL, authURL;
	private Semaphore unAuth;

	/**
	 * @param serial  device serial 
	 * @param authKey  authentication key associated with the serial
	 * @param authServer  server to authorize with
	 * @throws IOException
	 */
	protected SCRequester (String serial, String authKey, String authServer) throws IOException {
		this.serial = serial;
		this.authKey = authKey;
		this.authServer = authServer;
		unAuth = new Semaphore(1000);
		authenticate(authToken);
	}

	@Override
	public String getSerial() {
		return serial;
	}
	
	private void authenticate (String auth_token) throws IOException {
		try {
			unAuth.acquire();
		} catch (InterruptedException e1) {
			return;
		}
		if (auth_token != this.authToken) {
			unAuth.release();
			return;
		}
		
		byte [] data = null;
		String url = "https://" + authServer + "/SensorCloud/devices/" + serial 
		+ "/authenticate/?version=1&key=" + authKey;
		
		URL urlObj = null;
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e) {
			// this exception cannot be recovered from and is indicitave of outdated code
			throw new SensorCloudRuntimeException( "Malformed Url, please update your API" );
		}
		
		HttpURLConnection conn = null;
		try {
			// Open up a connection to the URL and prepare a buffered reader to read data back
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setRequestProperty( "Accept", "application/xdr");
			
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			byte [] buf = new byte [1024]; // buffer for reading in from server	
			int read; // bytes read
			
			while ((read = is.read(buf, 0, buf.length)) != -1) {
				buffer.write(buf, 0, read);
			}
			
			buffer.flush();
			data = buffer.toByteArray();
			
			// check the response code for success
			if (conn.getResponseCode() != 200) {
				throw new SCHTTPException( conn.getResponseCode(), conn.getResponseMessage() );
			}
			
			unAuth.release();
		} catch (IOException e) {
			unAuth.release();
			if (conn.getResponseCode() != -1) {
				throw new SCHTTPException(conn.getResponseCode(), conn.getResponseMessage() + "\n" + e.getMessage());
			} else {
				throw e; // exception is not a sensorcloud exception
			}
		}
				
		XDRInStream xdrStream = new XDRInStream( new ByteArrayInputStream( data ));
		
		// Extract the authentication token and server from the response
		authToken = xdrStream.readString();
		String server = xdrStream.readString();
		baseURL = "https://" + server + "/SensorCloud/devices/" + serial + "/";
		authURL = "version=1&auth_token=" + authToken;
	}
	
	@Override
	public byte [] get (String url) throws IOException {
		// Get the URL from the url string
		String urlStr;
		if (url.endsWith("/")) {
			urlStr = baseURL + url + "?" + authURL; // the url does not contain any parameters
		} else {
			urlStr = baseURL + url + "&" + authURL; // the url has parameters 
		}
		
		URL urlObj = null;
		try {
			urlObj = new URL(urlStr);
		} catch (MalformedURLException e) {
			// this exception cannot be recovered from and is indicitave of outdated code
			throw new SensorCloudRuntimeException( "Malformed Url, please update your API" );
		}
		
		
		
		HttpURLConnection conn = null;
		try {
			// Open up a connection to the URL and prepare a buffered reader to read data back
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setDoInput(true);
			conn.setRequestProperty( "Accept", "application/xdr");
			
			// Read the servers response
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte [] buf = new byte [1024]; // buffer for reading in from server			
			int read; // bytes read
			
			// write bytes from input stream to buffer until stream is empty
			while ((read = is.read(buf, 0, buf.length)) != -1) {
				long time = System.currentTimeMillis();
				while (System.currentTimeMillis() < time + 100) {}
				buffer.write(buf, 0, read);
			}
			
			buffer.flush();
			
			// check the response code for success
			if (conn.getResponseCode() != 200) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					return get(url);
				} else {
					throw new SCHTTPException( conn.getResponseCode(), conn.getResponseMessage() );
				}
			}
			
			// return contents of the buffer
			return buffer.toByteArray();
			
		} catch (IOException e) {
			if (conn.getResponseCode() != -1) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					return get(url);
				} else {
					throw new SCHTTPException(conn.getResponseCode(), conn.getResponseMessage() + "\n" + e.getMessage());
				}
			} else {
				throw e; // exception is not a sensorcloud exception
			}
		} catch (HTTPException e) {
			throw (SCHTTPException)e;
		}
	}
	
	@Override
	public void post (String url, byte [] data) throws IOException {
		// Get the URL from the url string
		String urlStr;
		if (url.endsWith("/")) {
			urlStr = baseURL + url + "?" + authURL; // the url does not contain any parameters
		} else {
			urlStr = baseURL + url + "&" + authURL; // the url has parameters 
		}
		
		URL urlObj = null;
		try {
			urlObj = new URL(urlStr);
		} catch (MalformedURLException e) {
			// this exception cannot be recovered from and is indicitave of outdated code
			throw new SensorCloudRuntimeException( "Malformed Url, please update your API" );
		}
		
		HttpURLConnection conn = null;
		
		try {
			// Open up a connection to the URL and prepare a buffered reader to read data back
			conn = (HttpURLConnection)urlObj.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-type", "application/xdr");
			conn.connect();
			OutputStream writer = conn.getOutputStream();
			writer.write(data);
			writer.flush();
			int resp = conn.getResponseCode();
			if (resp != 201) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					post(url,data);
				} else {
					throw new SCHTTPException( resp, conn.getResponseMessage() + "/n");
				}
			}
		} catch (IOException e) {
			if (conn.getResponseCode() != -1) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					post(url, data);
				} else {
					throw new SCHTTPException(conn.getResponseCode(), e.getMessage());
				}
			} else {
				throw e;
			}
		}
	}

	@Override
	public void put(String url, byte [] data) throws IOException {
		// Get the URL from the url string
		String urlStr;
		if (url.endsWith("/")) {
			urlStr = baseURL + url + "?" + authURL; // the url does not contain any parameters
		} else {
			urlStr = baseURL + url + "&" + authURL; // the url has parameters 
		}
		
		
		URL urlObj = null;
		try {
			urlObj = new URL(urlStr);
		} catch (MalformedURLException e) {
			// this exception cannot be recovered from and is indicitave of outdated code
			throw new SensorCloudRuntimeException( "Malformed Url, please update your API" );
		}
		
		HttpURLConnection conn = null;
		
		try {
			// Open up a connection to the URL and prepare a buffered reader to read data back
			conn = (HttpURLConnection)urlObj.openConnection();
			conn.setRequestProperty("Content-Type", "application/xdr");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");

			// write the data out to the server
			OutputStream writer = conn.getOutputStream();
			writer.write(data);
			writer.close();
			
			// veryify server response
			int resp = conn.getResponseCode();
			if (resp != 201) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					put(url,data);
				} else {
					throw new SCHTTPException( resp, conn.getResponseMessage() );
				}
			}
			
		} catch (IOException e) {
			if (conn.getResponseCode() != -1) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					put(url, data);
				} else {
					throw new SCHTTPException(conn.getResponseCode(), e.getMessage());
				}
			} else {
				throw e;
			}
		}
	}

	public void delete (String url) throws IOException {
		// Get the URL from the url string
		String urlStr;
		if (url.endsWith("/")) {
			urlStr = baseURL + url + "?" + authURL; // the url does not contain any parameters
		} else {
			urlStr = baseURL + url + "&" + authURL; // the url has parameters already 
		}
		
		// Get the URL from the url string
		URL urlObj = null;
		try {
			urlObj = new URL(urlStr);
		} catch (MalformedURLException e) {
			// this exception cannot be recovered from and is indicitave of outdated code
			throw new SensorCloudRuntimeException( "Malformed Url, please update your API" );
		}
		
		HttpURLConnection conn = null;
		
		try {
			// Open up a connection to the URL and prepare a buffered reader to read data back
			conn = (HttpURLConnection)urlObj.openConnection();

			conn.setDoInput(true);
			conn.setRequestMethod("DELETE");
			conn.connect();
			
			// validate response code
			if (conn.getResponseCode() != 204) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					delete(url);
				} else {
					throw new SCHTTPException( conn.getResponseCode(), conn.getResponseMessage());
				}
			}
		} catch (IOException e) {
			if (conn.getResponseCode() != -1) {
				if (conn.getResponseCode() == 401) {
					authenticate(this.authToken);
					delete(url);
				} else {
					throw new SCHTTPException(conn.getResponseCode(), e.getMessage() + "\n" + conn.getResponseMessage());
				}
			} else {
				throw e;
			}
		}
	}

	@Override
	public byte [] get(String url, Map<String, String> params) throws IOException {
		return get( appendParams(url, params) );
	}

	@Override
	public void post(String url, Map<String, String> params, byte [] data) throws IOException {
		post( appendParams(url, params), data );
	}
	
	/* Append the given parameters to the url */
	private static String appendParams(String url, Map <String, String> params) {
		url += "?";
		
		Set<String> keys = params.keySet();
		boolean first = true;
		for (String key: keys) {
			if (first) {
				first = false;
			} else {
				url += "&";
			} 
			url += key + "=" + params.get(key);
		} 
		
		return url;
	}

	@Override
	public void put(String url, Map<String, String> params, byte [] data) throws IOException {
		put( appendParams(url, params), data );
	}
}
