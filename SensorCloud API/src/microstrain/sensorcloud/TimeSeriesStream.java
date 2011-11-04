package microstrain.sensorcloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import microstrain.sensorcloud.exception.InvalidRequestException;
import microstrain.sensorcloud.exception.InvalidUserInputException;
import microstrain.sensorcloud.exception.SCHTTPException;
import microstrain.sensorcloud.exception.SensorCloudRuntimeException;
import microstrain.sensorcloud.exception.VersionNotSupportedException;
import microstrain.sensorcloud.xdr.XDRInStream;
import microstrain.sensorcloud.xdr.XDROutStream;

/**
 * A stream of data points. Each point has a timestamp and sample rate.
 * No two points can share the same timestamp.
 * 
 * @author Colin Cavanaugh
 *
 */
public class TimeSeriesStream {
	private String channelName, sensorName;
	private Requester requester;
	private long startTime, endTime;
	private BlockingQueue<BlockingQueue<Point>> buffer;
	private BlockingQueue<Point> curBuff;
	private Map<Queue<Point>, SampleRate> rateMap;
	private AsyncUpload sendThread;
	private SampleRate curRate = null;
	private Semaphore bufferChange;
	private int minBuffSize;
	
	private TimeSeriesStream (long startTime, long endTime, String channelName, String sensorName, Requester requester) {
		this.channelName = channelName;
		this.sensorName = sensorName;
		this.requester = requester;
		this.endTime = endTime;
		this.startTime = startTime;
		this.minBuffSize = 1000;
		
		buffer = new LinkedBlockingQueue<BlockingQueue<Point>>();
		curBuff = new LinkedBlockingQueue<Point>();
		rateMap = new HashMap<Queue<Point>, SampleRate>();
		bufferChange = new Semaphore(5);
		
		sendThread = new AsyncUpload(channelName, sensorName);
		sendThread.start();
	}

	/**
	 * Add a single point of data to the buffer 
	 * flushes the buffer to SensorCloud if it is full or if
	 * the <b>SampleRate</b> provided is different from the current <b>SampleRate</b>
	 * 
	 * @param point  point of data added to the buffer
	 * @param samplerate  <b>SampleRate</b> at the given point
	 * @throws InvalidUserInputException 
	 * @throws IOException 
	 */
	public void addData(Point point, SampleRate samplerate) throws InvalidUserInputException, IOException {		
		if (sendThread.e != null) {
			if (sendThread.e instanceof SCHTTPException) {
				SCHTTPException except = (SCHTTPException)sendThread.e;
				
				if (except.getStatusCode() == 409) {
					sendThread.e = null;
					sendThread.interrupt();
				} else {
					sendThread.kill = true;
					sendThread.interrupt();
					throw except;
				}
			} else {
				sendThread.kill = true;
				sendThread.interrupt();
				throw new SensorCloudRuntimeException(sendThread.e.getMessage());
			}
		}
		
		if (curRate == null) {
			curRate = samplerate;
			rateMap.put(curBuff, curRate);
			buffer.offer(curBuff);
		} else if (!curRate.equals(samplerate)) {
			curBuff = new LinkedBlockingQueue<Point>();
			curRate = samplerate;
			rateMap.put(curBuff, curRate);
			buffer.offer(curBuff);
		}
		
		curBuff.add( point );
	}

	/**
	 * Adds a collection of data to the stream.
	 * This method is intended for adding previously collected data.
	 * The buffer is synchronously flushed when this method is called.
	 * The user can assume that the data is on sensorcloud when this method finishes.
	 * 
	 * @param points  list of points to be added
	 * @param samplerate  rate for all points in the list
	 * @throws InvalidUserInputException 
	 */
	public void bulkAddData(List <Point> points, SampleRate samplerate) throws InvalidUserInputException {
		if (curRate == null) {
			curRate = samplerate;
			rateMap.put(curBuff, curRate);
			buffer.offer(curBuff);
		} else if (!curRate.equals(samplerate)) {
			try {
				bufferChange.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			curBuff = new LinkedBlockingQueue<Point>();
			curRate = samplerate;
			rateMap.put(curBuff, samplerate);
			bufferChange.release();
		}
		
		curBuff.addAll(points);
		
	}
	
	/**
	 * Get all of the data in the stream
	 * 
	 * @return Iterator for all of the data
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException 
	 */
	public TimeSeriesIter getData() throws IOException, InvalidUserInputException, InvalidRequestException {
		return getData((long)-1, (long)-1, null);
	}
	
	/**
	 * Get all of the data in the stream with the given <b>SampleRate</b>
	 * 
	 * @param samplerate  sample rate of the data requested
	 * @return Iterator for the requested data
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException 
	 */
	public TimeSeriesIter getData (SampleRate samplerate) throws IOException, InvalidUserInputException, InvalidRequestException {
		return getData((long)-1, (long)-1, samplerate);
	}
	
	/**
	 * Get all of the data between two timestamps
	 * 
	 * @param startTime  timestamp of the data's starting point
	 * @param endTime  timestamp of the data's ending point
	 * @return Iterator for the requested data
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException 
	 */
	public TimeSeriesIter getData (long startTime, long endTime) throws IOException, InvalidUserInputException, InvalidRequestException {
		return getData(startTime, endTime, null);
	}
	
	/**
	 * Get the data between two timestamps at the given sample rate
	 * 
	 * @param startTime
	 * @param endTime
	 * @param samplerate
	 * @return Iterator for the requested data
	 * 
	 * @throws IOException
	 * @throws InvalidUserInputException
	 * @throws InvalidRequestException 
	 */
	public TimeSeriesIter getData (long startTime, long endTime, SampleRate samplerate) throws IOException, InvalidUserInputException, InvalidRequestException {
		/*if (endTime < startTime) {
			throw new EndTimeBeforeStartTimeException(startTime, endTime);
		}*/
		return new TimeSeriesIter(startTime, endTime, samplerate, channelName, sensorName, requester);
	}
	
	/**
	 * @param bufferSize  number of <b>Points</b> collected before data is uploaded to SensorCloud
	 */
	public void setMinBufferSize (int bufferSize) {
		this.minBuffSize = bufferSize;
	}
	
	/**
	 * @return Number of <b>Points</b> collected before data is uploaded to SensorCloud
	 */
	public int getBufferSize() {
		return sendThread.buffSize;
	}
	
	/**
	 * @return Starting time of the data
	 * 
	 * @throws IOException
	 */
	public long getStartTime() throws IOException {
		try {
			return getInstanceOf(requester.get("sensors/" + sensorName + "/channels/" + channelName + "/streams/timeseries/"), sensorName, channelName, requester).startTime;
		} catch (SCHTTPException e) {
			switch (e.getStatusCode()) {
			case 404:
				return TimeSeriesStream.getEmptyInstanceOf(sensorName, channelName, requester).startTime;
			}
				throw e;
		}
	}
	
	/**
	 * @return The last timestamp in the data
	 * 
	 * @throws IOException
	 */
	public long getEndTime() throws IOException {
		try {
			return getInstanceOf(requester.get("sensors/" + sensorName + "/channels/" + channelName + "/streams/timeseries/"), sensorName, channelName, requester).endTime;
		} catch (SCHTTPException e) {
			switch (e.getStatusCode()) {
			case 404:
				return TimeSeriesStream.getEmptyInstanceOf(sensorName, channelName, requester).startTime;
			}
			throw e;
		}
	}
	
	/**
	 * Perform a synchronous flush of added data to SensorCloud
	 */
	public void flush() {
		sendThread.flush = true;
		while(sendThread.flush) {
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() < time + 10){}
		}
	}

	/**
	 * Get an instance of a <b>TimeSeriesStream</b> from XDR data
	 * 
	 * @param xdr  XDR data
	 * @param sensorName  parent <b>Sensor</b>'s name
	 * @param channelName  parent <b>Channel</b>'s name
	 * @param requester  authorized <b>Requester</b>
	 * @return  <b>TimeSeriesStream</b> from the data
	 * 
	 * @throws IOException
	 */
	public static TimeSeriesStream getInstanceOf (byte [] xdr, String sensorName, String channelName, Requester requester) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(xdr);
		XDRInStream xdrStream = new XDRInStream(inStream);
		
		// create an empty timeseries if the channel does not have one
		
		// check the version number, also a weak check for improperly formatted data
		int version = xdrStream.readInt();
		if (version != 1) {
			throw new VersionNotSupportedException( version );
		}
		
		long startTime = xdrStream.readHyper();
		long endTime = xdrStream.readHyper();
		
		return new TimeSeriesStream( startTime, endTime, channelName, sensorName, requester);
	}
	
	/**
	 * Returns an empty instance of a <b>TimeSeriesStream</b>.
	 * Allows the user to add data and create one for the <b>Channel</b>.
	 * 
	 * @param sensorName  parent <b>Sensor</b>'s name
	 * @param channelName  parent <b>Channel</b>'s name
	 * @param requester  authorized <b>Requester</b>
	 * @return Empty <b>TimeSeriesStream</b>
	 */
	public static TimeSeriesStream getEmptyInstanceOf (String sensorName, String channelName, Requester requester) {
		return new TimeSeriesStream(0, 0, channelName, sensorName, requester);
	}
	
	/*protected void finalize() {
		sendThread.kill = true;
	}*/
	
	/**
	 * Runnable for performing multi-threaded data uploading
	 * 
	 * @author Colin Cavanaugh
	 *
	 */
	private class AsyncUpload extends Thread{
		String channelName, sensorName;
		private SampleRate curSamplerate;
		private BlockingQueue<Point> sendBuff; 
		private boolean flushing = false;
		private int buffSize;
		
		public Exception e;
		public boolean flush = false;
		public boolean kill = false;

		public AsyncUpload (String channelName, String sensorName) {
			this.channelName = channelName;
			this.sensorName = sensorName;
			e = null;
			buffSize = minBuffSize;
		}
		
		private void packPoint(Point point, XDROutStream xdrStream) throws IOException {
			xdrStream.writeHyper( point.getTimestamp() );
			xdrStream.writeFloat( point.getValue() );
		}
		
		private byte [] parseHeader (int pointCount) throws IOException {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			XDROutStream xdrStream = new XDROutStream( outStream );
			
			xdrStream.writeInt(1);
			xdrStream.writeInt( curSamplerate.getType() );
			xdrStream.writeInt( curSamplerate.getRate() );
			xdrStream.writeInt( pointCount );
			
			return outStream.toByteArray();
		}
		
		@Override
		public void run() {
			boolean newSendBuff = true;
			try {
				while (!kill) {
					if (newSendBuff) {
						// take the next buffer of points
						try {
							sendBuff = buffer.poll(100, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e2) {
							break;
						}
					}
					
					if (sendBuff != null) {
						if (newSendBuff) {
							curSamplerate = rateMap.get(sendBuff);
						}
						
						// reset the xdr stream
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						XDROutStream xdrStream = new XDROutStream( outStream );
						Point point;
						
						newSendBuff = false;
						int pointCount = 0; // number of points parsed
						while (!kill && pointCount < buffSize) {
							try {
								point = sendBuff.poll(10, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								break;
							}
							
							// if there was no timeout
							if (point != null) {
								packPoint(point, xdrStream);
								pointCount++;
							} else {
								if (buffer.size() > 0) {
									newSendBuff = true;
									break;
								}
								
								if (flush) {
									flushing = true;
									break;
								}
							}
						}
		
						byte [] data = outStream.toByteArray();
						byte [] head = parseHeader(pointCount);
						byte [] xdr = new byte [data.length + head.length];
						System.arraycopy(head, 0, xdr, 0, head.length);
						System.arraycopy(data, 0, xdr, head.length, data.length);
						
						
						boolean success = false;
						
						while (!success & !kill) {
							success = true;
							try {
								requester.post("sensors/" + sensorName + "/channels/" + channelName + "/streams/timeseries/data/", xdr);
							} catch (SCHTTPException e) {
								this.e = e;
								try {
									sleep(1500);
								} catch (InterruptedException e1) {
									e.printStackTrace();
									success = false;
								}
							}
						}
						if (flushing) {
							flushing = false;
							flush = false;
						}
					} else {
						if (flush) {
							flush = false;
						}
					}
				}
			} catch (IOException e) {
				this.e = e;
			}
		}
	}
}
