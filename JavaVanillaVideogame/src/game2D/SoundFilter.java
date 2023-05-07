package game2D;

import java.io.*;
import javax.sound.sampled.*;

public class SoundFilter extends Thread{

	String filename;	// The name of the file to play
	boolean finished;	// A flag showing that the thread has finished
	
	public SoundFilter(String fname) {
		filename = fname;
		finished = false;
	}
	
	/**
	 * run will play the actual sound but you should not call it directly.
	 * You need to call the 'start' method of your sound object (inherited
	 * from Thread, you do not need to declare your own). 'run' will
	 * eventually be called by 'start' when it has been scheduled by
	 * the process scheduler.
	 */
	public void run()
	{
		try {
			File file = new File(filename);
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			AudioFormat	format = stream.getFormat();
//			FadeFilterStream filtered = new FadeFilterStream(stream);
//			AudioInputStream f = new AudioInputStream(filtered,format,stream.getFrameLength());
			
			FilteredSound theFilter = new FilteredSound(stream);
			AudioInputStream r = new AudioInputStream(theFilter,format,stream.getFrameLength());
//			
//			EchoFilterStream echo = new EchoFilterStream(stream);
//			AudioInputStream e = new AudioInputStream(echo, format, stream.getFrameLength());
			
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			Clip clip = (Clip)AudioSystem.getLine(info);
//			clip.open(f);
			clip.open(r);
//			clip.open(e);
			clip.start();
			Thread.sleep(100);
			while (clip.isRunning()) { Thread.sleep(100); }
			clip.close();
		}
		catch (Exception e) {}
	}
}
