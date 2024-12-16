package etc;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BgmManager {
    private static ArrayList<Clip> clips = new ArrayList<>();
    private static String[] audioNames = {
            "bgm/게임종료.wav", "bgm/로비.wav", "bgm/메인화면.wav", "bgm/오답.wav", "bgm/인게임.wav", "bgm/입장.wav", "bgm/정답.wav"
    };

    static {
        for (String audioName : audioNames) {
            loadAudio(audioName);
            // 모든 오디오의 볼륨을 10%로 줄이기
            setVolume(clips.size() - 1, 0.1f);
        }
    }

    private static void loadAudio(String pathName) {
        try {
            Clip clip = AudioSystem.getClip();
            File audioFile = new File(pathName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip.open(audioStream);
            clips.add(clip);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void playAudio(int index) {
        Clip clip = clips.get(index);
        clip.setFramePosition(0); // 오디오 재사용을 위해 처음위치로 복귀
        clip.start();
    }

    public static void stopAudio(int index) {
        Clip clip = clips.get(index);
        clip.setFramePosition(0); // 오디오 재사용을 위해 처음위치로 복귀
        clip.stop();
    }

    public static void loopAudio(int index) {
        Clip clip = clips.get(index);
        clip.loop(Clip.LOOP_CONTINUOUSLY); // 무한 반복 재생
    }

    // 50% 볼륨으로 설정 -> BgmManager.setVolume(0, 0.5f);
    // 데시벨 단위로 볼륨 조절
    // AudioClip은 FloatControl를 통해서 오디오 제어 기능을 제공함
    public static void setVolume(int index, float volume) {
        Clip clip = clips.get(index);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }

    // 0번 bgm 음소거 -> BgmManager.mute(0);
    public static void mute(int index) {
        setVolume(index, 0.0f);
    }
}