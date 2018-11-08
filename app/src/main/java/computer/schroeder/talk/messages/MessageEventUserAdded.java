package computer.schroeder.talk.messages;

import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class MessageEventUserAdded extends Message
{
    public MessageEventUserAdded() {}
    /**
     * User which had been added to the group.
     */
    private String user;

    public MessageEventUserAdded(String user)
    {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String asString() {
        return "User #" + user + " has been added to the group.";
    }

    @Override
    public View asView(ScreenManager screenManager, ViewGroup parent, String localUserId, StoredMessage storedMessage)
    {
        View messageView = screenManager.getInflater().inflate(R.layout.display_message_text, parent, false);

        CardView card = messageView.findViewById(R.id.card);
        ImageView status = messageView.findViewById(R.id.status);
        TextView username = messageView.findViewById(R.id.username);

        TextView message = messageView.findViewById(R.id.message);
        message.setText("User #" + user + " has been added to the group.");

        TextView time = messageView.findViewById(R.id.time);
        time.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(storedMessage.getTime())));

        if(storedMessage.getUser().equals(localUserId))
        {
            if(storedMessage.isSent()) status.setImageDrawable(screenManager.getMain().getResources().getDrawable(R.drawable.ic_done_all));
            username.setVisibility(View.GONE);
            card.setCardBackgroundColor(screenManager.getMain().getResources().getColor(R.color.colorBubble));
        }
        else
        {
            status.setVisibility(View.GONE);
            StoredUser user = screenManager.getMain().getComplexStorage().getUser(storedMessage.getUser(), localUserId);
            username.setText(user.getUsername());
        }

        return messageView;
    }

    @Override
    void toJsonChild(JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("user", user);
    }

    @Override
    void fromJsonChild(JSONObject jsonObject) throws JSONException
    {
        this.user = jsonObject.getString("user");
    }


}
