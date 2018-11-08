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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;

public class MessageText extends Message
{
    public MessageText() {}

    private String text;

    public MessageText(String text)
    {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String asString() {
        return text;
    }

    @Override
    public View asView(ScreenManager screenManager, ViewGroup parent, String localUserId, StoredMessage storedMessage)
    {
        View messageView = screenManager.getInflater().inflate(R.layout.display_message_text, parent, false);

        CardView card = messageView.findViewById(R.id.card);
        ImageView status = messageView.findViewById(R.id.status);
        TextView username = messageView.findViewById(R.id.username);

        TextView message = messageView.findViewById(R.id.message);
        message.setText(getText());

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
        jsonObject.put("text", text);
    }

    @Override
    void fromJsonChild(JSONObject jsonObject) throws JSONException
    {
        this.text = jsonObject.getString("text");
    }
}
