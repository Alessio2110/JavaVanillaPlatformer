package game2D;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilteredSound extends FilterInputStream {

	FilteredSound(InputStream in) { super(in); }

	// Get a value from the array 'buffer' at the given 'position'
	// and convert it into short big-endian format
	public short getSample(byte[] buffer, int position)
	{
		return (short) (((buffer[position+1] & 0xff) << 8) |
					     (buffer[position] & 0xff));
	}

	// Set a short value 'sample' in the array 'buffer' at the
	// given 'position' in little-endian format
	public void setSample(byte[] buffer, int position, short sample)
	{
		buffer[position] = (byte)(sample & 0xFF);
		buffer[position+1] = (byte)((sample >> 8) & 0xFF);
	}

	public int read(byte [] sample, int offset, int length) throws IOException
	{
		// Get the number of bytes in the data stream
		int bytesRead = super.read(sample,offset,length);
		// Create a new buffer the same size as the old buffer
		byte[] buffer = new byte [bytesRead];
		short amp;
		int	p;
		float change = 4.0f * (1.0f / (float)bytesRead);
		// Start off at full volume
//		float volume = 1.f;
		float volume = 1f;
		amp=0;
		for (p=0; p<bytesRead; p = p + 2)
		{
			// Read the current amplitutude (volume)
			amp = getSample(sample,p);
			// Reduce it by the relevant volume factor
			amp = (short)((float)amp * volume);
			// Set the new amplitude value
			setSample(sample,p,amp);
			// Increase the volume
			volume = volume + change;
		}
		// Copy the sample to the buffer
		for (p =0; p < bytesRead ; p++) {
			buffer[p] = sample[p];}
		//	Loop through the sample 2 bytes at a time
		for ( p = 0; p < bytesRead ; p += 2) {
	         amp = getSample(buffer, p);
	        setSample(sample, bytesRead - 2 - p, amp);
	    }
		
	

		//	Loop through the sample 2 bytes at a time
		
//		for (p =0; p < bytesRead ; p++) {
//			buffer[p] = sample[p];}
//		for (int i = 0; i < bytesRead ; i = i + 2) {
//	         amp = getSample(buffer, i);
//	        sample[i] = sample[bytesRead - 2 - i];
//	        setSample(sample, bytesRead - 2 - i, amp);	        
//	    }
		
		return length;
	}
}
