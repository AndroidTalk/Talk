package computer.schroeder.talk.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SimpleStorage
{
    private SharedPreferences preferences;

    public SimpleStorage(Context context)
    {
        this.preferences = context.getSharedPreferences("chat", Context.MODE_PRIVATE);
    }

    public String getUserKey()
    {
        return preferences.getString("userKey", null);
    }

    public void setUserKey(String userKey)
    {
        preferences.edit().putString("userKey", userKey).apply();
    }

    public String getPrivateKey()
    {
        return preferences.getString("privateKey", null);
    }

    public void setPrivateKey(String privateKey)
    {
        preferences.edit().putString("privateKey", privateKey).apply();
    }

    public String getPublicKey()
    {
        return preferences.getString("publicKey", null);
    }

    public void setPublicKey(String publicKey)
    {
        preferences.edit().putString("publicKey", publicKey).apply();
    }

    public int getUser()
    {
        return preferences.getInt("user", -1);
    }

    public void setUser(int localUser)
    {
        preferences.edit().putInt("user", localUser).apply();
    }
}
