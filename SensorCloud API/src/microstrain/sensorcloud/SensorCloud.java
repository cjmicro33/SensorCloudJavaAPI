package microstrain.sensorcloud;

import java.io.IOException;

import microstrain.sensorcloud.exception.InvalidAuthenticationCredentialsException;
import microstrain.sensorcloud.exception.InvalidUserInputException;
import microstrain.sensorcloud.exception.SCHTTPException;

/**
 * The parent to all objects.
 * 
 * @author Colin Cavanaugh
 *
 */
public class SensorCloud {
	private String authServer;
	
	/**
	 * Unless you know that you need to use another server you should authenticate with "sensorcloud.microstrain.com"
	 * 
	 * @param authServer  address of the server to authenticate with
	 */
	public SensorCloud (String authServer) {
		this.authServer = authServer;
	}
	
	/**
	 * Get an instance of your device
	 * 
	 * @param serial  device's serial
	 * @param authKey  from your authentication credentials
	 * @return An instance of the authenticated Device
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException 
	 */
	public Device getDevice (String serial, String authKey) throws IOException, InvalidUserInputException {
		try {
			return new Device( serial, new SCRequester(serial, authKey, authServer));
		} catch (SCHTTPException e) {
			switch (e.getStatusCode()) {
			case 401:
				throw new InvalidAuthenticationCredentialsException(serial);
			}
			throw e;
		}
	}
}
