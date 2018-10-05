package computer.schroeder.talk.util.sendable;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Sendable
{
    public String toString()
    {
        JSONObject json = new JSONObject();

        try
        {
            toJsonChild(json);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return json.toString();
    }

    abstract void toJsonChild(JSONObject jsonObject) throws JSONException;
    abstract void fromJsonChild(JSONObject jsonObject) throws JSONException;


    public static Sendable fromJson(String type, String json)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(json);
            Class c = Class.forName("computer.schroeder.talk.util.sendable." + type);
            Sendable sendable = (Sendable) c.newInstance();
            if(sendable != null)
            {
                sendable.fromJsonChild(jsonObject);
                return sendable;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
