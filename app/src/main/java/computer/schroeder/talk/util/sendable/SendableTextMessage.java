package computer.schroeder.talk.util.sendable;

import org.json.JSONException;
import org.json.JSONObject;

public class SendableTextMessage extends Sendable
{
    private String text;

    public SendableTextMessage(String text)
    {
        this.text = text;
    }

    protected SendableTextMessage() {}

    public String getText() {
        return text;
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
