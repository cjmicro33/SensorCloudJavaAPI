package microstrain.sensorcloud;

import microstrain.sensorcloud.exception.InvalidSampleRateTypeException;

/**
 * A sample rate on SensorCloud is measured in either hertz or seconds. Every point in a time series
 * has a corresponding <b>SampleRate</b>
 * 
 * @author Colin Cavanaugh
 *
 */
public class SampleRate implements Comparable<SampleRate> {
	private int rate, type;
	
	/**
	 * Type when sampling at an interval that is less than one second
	 */
	public final static int HERTZ = 1;
	
	/**
	 * Type when sampling at an interval that is greater than one second
	 */
	public final static int SECONDS = 0;
	
	/**
	 * Class constructor. The type should be supplied using the static members.
	 * 
	 * @param rate  sample rate
	 * @param type  sample rate units
	 * 
	 * @throws InvalidSampleRateTypeException
	 */
	public SampleRate (int rate, int type) throws InvalidSampleRateTypeException {
		if (type != SampleRate.HERTZ && type != SampleRate.SECONDS) {
			throw new InvalidSampleRateTypeException(type);
		}
		
		this.rate = rate;
		this.type = type;
	}
	
	/**
	 * @return Sample rate
	 */
	public int getRate() {
		return rate;
	}
	
	/**
	 * @return Sample rate units
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Returns the <b>SampleRate</b> as a parameter for the Download Time-Series Data API request("hertz-10", "seconds-2")
	 * 
	 * @return Parameter
	 */
	public String toParam() {
		String value = "" + rate;
		
		
		if (type == HERTZ) {
			value += "-hertz";
		} else if (type == SECONDS) {
			value += "-seconds";
		}
		
		//value += rate;
		return value;
	}
	
	/**
	 * Returns true if the sample rate and type are the same.
	 * Also returns true if both have a sample rate of 1, since 1hz = 1sec
	 */
	@Override
	public boolean equals (Object o) {
		SampleRate samplerate;
		try {
			samplerate = (SampleRate)o;
		} catch (Exception e) {
			return false; // not the same class
		}
		
		return (this.rate == samplerate.rate) && (this.type == samplerate.type) || (samplerate.rate == 1 && rate == 1);
	}

	/**
	 * Faster sample rates are greater than slower ones.
	 */
	@Override
	public int compareTo(SampleRate o) {
		if (equals(o)) {
			return 0;
		}
		
		if (type == HERTZ) {
			if (o.type == SECONDS) {
				return 1;
			}
			
			if (rate > o.rate) {
				return 1;
			}
			return -1;
		} else {
			if (o.type == HERTZ) {
				return -1;
			}
			
			if (rate > o.rate) {
				return -1;
			}
			return 1;
		}
	}
	
}
