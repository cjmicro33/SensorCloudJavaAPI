package microstrain.sensorcloud;

import microstrain.sensorcloud.exception.InvalidTimestampException;
import microstrain.sensorcloud.exception.InvalidUserInputException;

/**
 * A <b>Point</b> is a single point of data on SensorCloud. Each <b>Point</b> contains a UTC timestamp in nanoseconds
 * and a float value.
 * 
 * @author Colin Cavanaugh
 *
 */
public class Point {
	private long timestamp;
	private float value;
	
	/**
	 * Constructor method. The timestamp is the number of nanoseconds that have passed since
	 * January 1, 1970.
	 * 
	 * @param timestamp  unix time in nanoseconds
	 * @param value  data value at the given timestamp
	 * 
	 * @throws InvalidUserInputException
	 */
	public Point (long timestamp, float value) throws InvalidUserInputException {
		// validate timestamp
		if (timestamp < 0) {
			throw new InvalidTimestampException( timestamp, "Timestamps cannot be less than zero" );
		}
		
		this.timestamp = timestamp;
		this.value = value;
	}
	
	/**
	 * @return Float value
	 */
	public float getValue() {
		return value;
	}
	
	/**
	 * @return Unix time in nanoseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}
}
