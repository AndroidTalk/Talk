package computer.schroeder.talk.messages;

import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredMessage;

public abstract class Message
{
    public Message() {}

    /**
     * Converts a sendable to a json string
     * @return the sendable as a jsonString (not encrypted)
     */
    public String asJsonString()
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

    /**
     *
     * @return returns the sendable as a displayable string
     */
    public abstract String asString();

    public abstract View asView(ScreenManager screenManager, ViewGroup parent, String localUserId, StoredMessage storedMessage);

    /**
     * Called by the superclass, allowing the child to fill the json with needed information
     * @param jsonObject the final json sendable
     * @throws JSONException if the convert fails
     */
    abstract void toJsonChild(JSONObject jsonObject) throws JSONException;

    /**
     * Called by the superclass, allowing the child to build up on the given information
     * @param jsonObject
     * @throws JSONException
     */
    abstract void fromJsonChild(JSONObject jsonObject) throws JSONException;

    /**
     * Creates a sendable object from a json string
     * @param type the type of the sendable object
     * @param json the sendable json string
     * @return the sendable object
     */
    public static Message fromJson(String type, String json)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(json);
            Class c = Class.forName("computer.schroeder.talk.messages." + type);
            Message message = (Message) c.newInstance();
            if(message != null)
            {
                message.fromJsonChild(jsonObject);
                return message;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
