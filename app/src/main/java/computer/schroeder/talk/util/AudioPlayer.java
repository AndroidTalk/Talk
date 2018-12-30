package computer.schroeder.talk.util;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import computer.schroeder.talk.screen.ScreenManager;

public class AudioPlayer
{
    private MediaPlayer player = null;

    public void startPlaying(ScreenManager screenManager, String audio) throws IOException
    {
        stopPlaying();
        byte[] decodedString = Base64.decode(audio, Base64.DEFAULT);

        File file = File.createTempFile("audio_", ".mpg4", screenManager.getMain().getExternalFilesDir("Audio"));
        file.deleteOnExit();
        file.mkdir();
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(decodedString);
        fos.close();

        player = new MediaPlayer();
        player.setDataSource(file.getPath());
        player.prepare();
        player.start();
    }

    public static int getDuration(ScreenManager screenManager, String audio)
    {
        try
        {
            byte[] decodedString = Base64.decode(audio, Base64.DEFAULT);

            File file = File.createTempFile("audio_", ".mpg4", screenManager.getMain().getExternalFilesDir("Audio"));
            file.deleteOnExit();
            file.mkdir();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(decodedString);
            fos.close();

            MediaPlayer player = new MediaPlayer();
            player.setDataSource(file.getPath());
            player.prepare();
            int d = player.getDuration();
            return d;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public void stopPlaying()
    {
        if(player == null) return;
        player.stop();
        player.release();
        player = null;
    }
}
