package computer.schroeder.talk.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.storage.entities.StoredMessage;

public class ServerConnection
{
    private SimpleStorage simpleStorage;
    public ServerConnection(Context context)
    {
        this.simpleStorage = new SimpleStorage(context);
    }

    public Object[] userRegister() throws Exception
    {
        JSONObject json = request("userRegister", "");
        Object[] response = new Object[2];
        response[0] = json.getString("userKey");
        response[1] = json.getInt("user");
        return response;
    }

    public JSONObject conversationInfo(long conversation) throws Exception
    {
        return request("conversationInfo", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8"));
    }

    public void conversationRemove(long conversation, long user)
    {
        try
        {
            request("conversationRemove", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&user=" + URLEncoder.encode("" + user, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationAdd(long conversation, long user)
    {
        try
        {
            request("conversationAdd", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&user=" + URLEncoder.encode("" + user, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationLeave(long conversation)
    {
        try
        {
            request("conversationLeave", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationOwner(long conversation, long user)
    {
        try
        {
            request("conversationOwner", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&user=" + URLEncoder.encode("" + user, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public int createConversation() throws Exception
    {
        JSONObject json = request("conversationCreate", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8"));
        return json.getInt("id");
    }

    public String getPublicKey(long user) throws Exception
    {
        JSONObject json = request("userPublicKey", "user=" + URLEncoder.encode("" + user, "UTF-8"));
        return json.getString("publicKey");
    }

    public void updateFCMToken(String token)
    {
        try
        {
            request("userUpdateFCMToken", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&fcmToken=" + URLEncoder.encode(token, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updatePublicKey(String publicKey)
    {
        try
        {
            request("userUpdatePublicKey", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&publicKey=" + URLEncoder.encode(publicKey, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void messageSend(EncryptionService encryptionService, Message message, long user, long conversation)
    {
        try
        {
            String msg = encryptionService.encryptMessage(this, message, user);
            request("messageSend", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&receiver=" + user + "&message=" + URLEncoder.encode(msg, "UTF-8") + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ArrayList<StoredMessage> messageSync(EncryptionService encryptionService, ComplexStorage complexStorage)
    {
        ArrayList<StoredMessage> storedMessages = new ArrayList<>();
        try
        {
            JSONObject object = request("messageSync", "userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8"));
            JSONArray messages = object.getJSONArray("messages");
            for(int i = 0; i < messages.length(); i++)
            {
                JSONObject msg = messages.getJSONObject(i);
                int sender = msg.getInt("sender");
                int conversation = msg.getInt("conversation");
                String encrypted = msg.getString("message");

                Message message = encryptionService.decryptMesage(encrypted);

                StoredMessage storedMessage = new StoredMessage();
                storedMessage.setUser(sender);
                storedMessage.setSent(true);
                storedMessage.setRead(false);
                storedMessage.setConversation(conversation);
                storedMessage.setMessage(message.getMessage());
                storedMessage.setTime(message.getTime());

                long newId = complexStorage.messageInsert(storedMessage);
                storedMessage.setId(newId);
                storedMessages.add(storedMessage);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return storedMessages;
    }

    private JSONObject request(String type, String request) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://chat.schroeder.computer/api/" + type + ".php?" + request).openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return new JSONObject(br.readLine());
    }

    /*public boolean isReachable()
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://chat.schroeder.computer/api/").openConnection();
            return connection.getResponseCode() == 200;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }*/
}
