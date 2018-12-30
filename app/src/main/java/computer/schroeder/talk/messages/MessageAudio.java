package computer.schroeder.talk.messages;

import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.AudioPlayer;

public class MessageAudio extends Message
{
    public MessageAudio() {}

    public MessageAudio(String audio)
    {
        this.audio = audio;
    }

    private String audio;

    @Override
    public String asString() {
        return "\uD83D\uDD0A";
    }

    @Override
    public View asView(final ScreenManager screenManager, ViewGroup parent, String localUserId, final StoredMessage storedMessage)
    {
        View messageView = screenManager.getInflater().inflate(R.layout.display_message_audio, parent, false);

        CardView card = messageView.findViewById(R.id.card);
        ImageView status = messageView.findViewById(R.id.status);
        TextView username = messageView.findViewById(R.id.username);
        ImageView play = messageView.findViewById(R.id.play);
        TextView duration = messageView.findViewById(R.id.duration);
        Date date = new Date(AudioPlayer.getDuration(screenManager, audio));
        SimpleDateFormat df = new SimpleDateFormat("mm:ss");
        duration.setText(df.format(date));

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    screenManager.getMain().getAudioPlayer().startPlaying(screenManager, audio);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        TextView time = messageView.findViewById(R.id.time);
        time.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(storedMessage.getTime())));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();

        if(storedMessage.getUser().equals(localUserId))
        {
            if(storedMessage.isSent()) status.setImageDrawable(screenManager.getMain().getResources().getDrawable(R.drawable.ic_done_all));
            username.setVisibility(View.GONE);
            card.setCardBackgroundColor(screenManager.getMain().getResources().getColor(R.color.colorBubble));
            params.setMargins(0, (int) screenManager.getMain().getResources().getDimension(R.dimen.sent_top), 0, (int) screenManager.getMain().getResources().getDimension(R.dimen.sent_bottom));
            params.setMarginStart((int) screenManager.getMain().getResources().getDimension(R.dimen.sent_start));
            params.setMarginEnd((int) screenManager.getMain().getResources().getDimension(R.dimen.sent_end));
            params.gravity = Gravity.END;
        }
        else
        {
            status.setVisibility(View.GONE);
            StoredUser user = screenManager.getMain().getComplexStorage().getUser(storedMessage.getUser(), localUserId);
            username.setText(user.getUsername());
            params.setMargins(0, (int) screenManager.getMain().getResources().getDimension(R.dimen.received_top), 0, (int) screenManager.getMain().getResources().getDimension(R.dimen.received_bottom));
            params.setMarginStart((int) screenManager.getMain().getResources().getDimension(R.dimen.received_start));
            params.setMarginEnd((int) screenManager.getMain().getResources().getDimension(R.dimen.received_end));
            params.gravity = Gravity.START;
        }

        return messageView;
    }

    @Override
    void toJsonChild(JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("audio", audio);
    }

    @Override
    void fromJsonChild(JSONObject jsonObject) throws JSONException
    {
        this.audio = jsonObject.getString("audio");
    }
}
