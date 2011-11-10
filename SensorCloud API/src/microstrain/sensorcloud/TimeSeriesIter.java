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
import microstrain.sensorcloud.exception.SensorContainsChannelsException;
import microstrain.sensorcloud.json.JSONException;
import microstrain.sensorcloud.json.JSONObject;

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
	protected TimeSeriesIter (long startTime, long endTime, SampleRate samplerate, String channelName, String sensorName, Requester requester) throws IOException, InvalidUserInputException, InvalidRequestException {
		String url = "sensors/" + sensorName + "/channels/" + channelName + "/streams/timeseries/data/";
		Map <String, String> params = new TreeMap<String, String>();
		
		if (startTime > endTime) {
			throw new EndTimeBeforeStartTimeException(startTime, endTime);
		}else if (startTime < 0) {
			startTime = 0;
			params.put( "starttime", "0" );
		} else {
			params.put( "starttime", Long.toString(startTime) );
		}
		
		if (endTime < 0) {
			endTime = Long.MAX_VALUE;
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
			
			try {
				JSONObject json = new JSONObject( e.getMessage() );
				
				if (json.has( "errorcode" )) {
					String code = json.getString( "errorcode" );
					String [] codes = code.split("-");
					int x = Integer.parseInt( codes[0] );
					int y = Integer.parseInt( codes[1] );
					
					switch (x) {
					case 404:
						switch (y) {
						case 3:
							throw new DataDoesNotExistException( json.getString( "message" ) );
						}
					}
				}
			} catch (JSONException excep) {
				
			}
			
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
