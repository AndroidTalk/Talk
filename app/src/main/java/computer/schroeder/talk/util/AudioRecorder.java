package computer.schroeder.talk.util;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

import computer.schroeder.talk.screen.ScreenManager;

public class AudioRecorder
{
    private MediaRecorder recorder = null;
    private File file = null;

    public void startRecording(ScreenManager screenManager) throws IOException {

        stopRecording();

        file = File.createTempFile("audio_", ".mpg4", screenManager.getMain().getExternalFilesDir("Audio"));
        file.mkdir();
        file.createNewFile();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(file.getPath());
        recorder.setAudioEncodingBitRate(16);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording()
    {
        if(recorder == null) return;
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public File getFile() {
        return file;
    }
}
