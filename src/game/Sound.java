package game;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * This class is responsible for handling the background sound and sound effects.
 * @author Abler, Andrew Kevin M.
 *
 */
public class Sound {
	
	Clip clip;
	URL soundURL[] = new URL[30];
	
	public Sound() {
		
		soundURL[0] = getClass().getResource("/sound/Drifting_abyss.wav");
		soundURL[1] = getClass().getResource("/sound/Lazer_Pew.wav");
	}
	
	public void setFile(int i) {
		
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
			clip = AudioSystem.getClip();
			clip.open(ais);
		}catch(Exception e){
			
		}
	}
	
	//Play audio clip
	public void play() {
		
		clip.start();
	}
	
	//Loop audio clip
	public void loop() {
		
		clip.loop(clip.LOOP_CONTINUOUSLY);
	}
	
	//Stop audio clip
	public void stop() {
		clip.stop();
	}

}
