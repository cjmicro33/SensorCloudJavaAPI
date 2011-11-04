package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import microstrain.sensorcloud.exception.InvalidUserInputException;
import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;

/**
 * A <b>Point</b> with its <b>SampleRate</b> included.
 * 
 * @author Colin Cavanaugh
 *
 */
public class SampledPoint extends Point {
	private SampleRate samplerate;
	
	/**
	 * @param timestamp  unix time stamp in nanoseconds
	 * @param value  data value at the given timestamp
	 * @param samplerate  the sample rate at the given timestamp
	 * @throws InvalidUserInputException
	 */
	public SampledPoint(long timestamp, float value, SampleRate samplerate) throws InvalidUserInputException {
		super(timestamp, value);
		this.samplerate = samplerate;
	}
	
	/**
	 * @return Sample rate at that point
	 */
	public SampleRate getSampleRate() {
		return samplerate;
	}

	/**
	 * Returns a list of <b>SampledPoints</b> from time series data.
	 * 
	 * @param xdr  data from a download time series stream API request
	 * @return List of points with their <b>SampleRates</b>
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws VersionNotSupportedException
	 */
	public static List <SampledPoint> getInstanceOfAll (byte [] xdr) throws IOException, InvalidUserInputException, VersionNotSupportedException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(xdr);
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		SampleRate samplerate = null;
		long timestamp;
		float value;
		
		List<SampledPoint> points = new LinkedList<SampledPoint>();
		
		try {
		while(true)
		{
			timestamp = xdrStream.readHyper();
			if (timestamp == 0) {
				//Sample rate info
				int type = xdrStream.readInt();
				int rate = xdrStream.readInt();
				samplerate = new SampleRate(rate, type);
			} else {
				value = xdrStream.readFloat();
				points.add( new SampledPoint(timestamp, value, samplerate) );
			}
		}
		} catch (EOFException e) {
			// no more points
		}
		return points;
	}
}
