package computer.schroeder.talk.util;

import org.json.JSONObject;

public class Message
{
    private String message;
    private long time;

    public Message(String message, long time)
    {
        this.message = message;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public String toJson() throws Exception
    {
        JSONObject object = new JSONObject();
        object.put("message", message);
        object.put("time", time);
        return object.toString();
    }

    public static Message fromJson(String json) throws Exception
    {
        JSONObject object = new JSONObject(json);
        return new Message(object.getString("message"), object.getLong("time"));
    }
}
