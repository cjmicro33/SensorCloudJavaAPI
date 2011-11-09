package microstrain.sensorcloud;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import microstrain.sensorcloud.exception.DataDoesNotExistException;
import microstrain.sensorcloud.exception.EndTimeBeforeStartTimeException;
import microstrain.sensorcloud.exception.InvalidRequestException;
import microstrain.sensorcloud.exception.InvalidUserInputException;
import microstrain.sensorcloud.exception.SCHTTPException;

/**
 * An iterator for iterating over data from a <b>TimeSeriesStream</b>
 * 
 * @author Colin Cavanaugh
 *
 */
public class TimeSeriesIter implements Iterator<SampledPoint> {
	private List <SampledPoint> points;
	private Iterator<SampledPoint> itr;
	
	/**
	 * Class constructor.
	 * A negative start time will be set to zero while a negative
	 * end time will be set to the maximum value.
	 * 
	 * @param startTime  timestamp for the start of the data
	 * @param endTime  timestamp for the end of the data
	 * @param samplerate  <b>SampleRate</b> of the data to be retrieved
	 * @param channelName  name of the parent <b>Channel</b>
	 * @param sensorName  name of the parent <b>Sensor</b>
	 * @param requester  authorized <b>Requester</b>
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException
	 */
	public TimeSeriesIter (long startTime, long endTime, SampleRate samplerate, String channelName, String sensorName, Requester requester) throws IOException, InvalidUserInputException, InvalidRequestException {
		String url = "sensors/" + sensorName + "/channels/" + channelName + "/streams/timeseries/data/";
		Map <String, String> params = new TreeMap<String, String>();
		
		if (startTime > endTime) {
			throw new EndTimeBeforeStartTimeException(startTime, endTime);
		}else if (startTime < 0) {
			params.put( "starttime", "0" );
		} else {
			params.put( "starttime", Long.toString(startTime) );
		}
		
		if (endTime < 0) {
			params.put( "endtime", Long.toString( Long.MAX_VALUE ) );
		} else {
			params.put( "endtime", Long.toString(endTime) );
		}
		
		if (samplerate != null) {
			params.put( "specificsamplerate", samplerate.toParam() );
			params.put("showSampleRateBoundary", "false");
		}
		
		try {
			points = SampledPoint.getInstanceOfAll( requester.get(url, params) );
		} catch (SCHTTPException e) {			
			switch (e.getStatusCode()) {
			case 404:
				if (samplerate == null) {
					throw new DataDoesNotExistException(startTime, endTime);
				} else {
					throw new DataDoesNotExistException(startTime, endTime, samplerate);
				}
			}
		}
		itr = points.iterator();
	}
	
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return itr.hasNext();
	}

	@Override
	public SampledPoint next() {
		// TODO Auto-generated method stub
		return itr.next();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		itr.remove();
	}

}
