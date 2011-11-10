package microstrain.sensorcloud.exception;

import javax.xml.ws.http.HTTPException;

/**
 * Wrapper for HTTPException that contains an additional message about the HTTP error.
 * Thrown after an HTTPException or HTTP related IOException is encountered.
 * 
 * @author Colin Cavanaugh
 *
 */
@SuppressWarnings("serial")
public class SCHTTPException extends HTTPException {

	String message;
	
	/**
	 * @param statusCode  status code of the http error
	 * @param message
	 */
	public SCHTTPException(int statusCode, String message) {
		super(statusCode);
		this.message = message;
		if (message.startsWith( "{" )) {
			System.out.println( message );
		}
	}

	public String getMessage() {
		return message;
	}
	
}
