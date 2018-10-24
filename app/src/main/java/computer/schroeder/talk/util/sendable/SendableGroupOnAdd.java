package computer.schroeder.talk.util.sendable;

import org.json.JSONException;
import org.json.JSONObject;

public class SendableGroupOnAdd extends Sendable
{
    /**
     * User which had been added to the group.
     */
    private String user;

    public SendableGroupOnAdd(String user)
    {
        this.user = user;
    }

    protected SendableGroupOnAdd() {}

    public String getUser() {
        return user;
    }

    @Override
    String asString() {
        return "User #" + user + " has been added to the group.";
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
