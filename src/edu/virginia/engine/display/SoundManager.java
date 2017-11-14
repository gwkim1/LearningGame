package edu.virginia.engine.display;

import java.io.IOException;
import java.util.HashMap;
import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private HashMap<String,String> sounds;
    private Clip musicClip;

    public SoundManager() {
        this.sounds = new HashMap<String, String>();
    }

    // LoadMusic() not necessary as a separate function if we store the music in the same HashMap
    public void LoadSoundEffect(String id, String filename) {
        this.sounds.put(id, filename);
    }

    public void PlaySoundEffect(String id) {
        try {
            File file = new File("resources" + File.separator + "sounds" + File.separator + sounds.get(id));
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e){
            e.printStackTrace();
        }
    }

    public void PlayMusic(String id) {
        try {
            File file = new File("resources" + File.separator + "sounds" + File.separator + sounds.get(id));
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY); // loops music forever
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e){
            e.printStackTrace();
        }
    }
    public void StopMusic() {
        musicClip.stop();
    }
}
