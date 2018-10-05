package computer.schroeder.talk.util.sendable;

import org.json.JSONException;
import org.json.JSONObject;

public class SendableGroupOnAdd extends Sendable
{
    /**
     * User which had been added to the group.
     */
    private long user;

    public SendableGroupOnAdd(long user)
    {
        this.user = user;
    }

    protected SendableGroupOnAdd() {}

    public long getUser() {
        return user;
    }

    @Override
    void toJsonChild(JSONObject jsonObject) throws JSONException
    {
        jsonObject.put("user", user);
    }

    @Override
    void fromJsonChild(JSONObject jsonObject) throws JSONException
    {
        this.user = jsonObject.getLong("user");
    }
}
