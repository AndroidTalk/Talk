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
        View messageView = screenManager.getInflater().inflate(R.layout.display_info, parent, false);

        TextView message = messageView.findViewById(R.id.message);
        message.setText("User #" + user + " has been added to the group.");

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
