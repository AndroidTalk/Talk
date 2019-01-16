package computer.schroeder.talk.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.messages.Message;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.storage.entities.StoredMessage;

public class RestService
{
    private SimpleStorage simpleStorage;
    public RestService(Context context)
    {
        this.simpleStorage = new SimpleStorage(context);
    }

    public Object[] userRegister() throws Exception
    {
        JSONObject json = request("userRegister", "");
        Object[] response = new Object[2];
        response[0] = json.getString("userId");
        response[1] = json.getString("userKey");
        return response;
    }

    public JSONObject conversationInfo(String conversation) throws Exception
    {
        return request("conversationInfo", "conversation=" + URLEncoder.encode("" + conversation, "UTF-8"));
    }

    public void conversationRemove(String conversation, String target)
    {
        try
        {
            request("conversationRemove", "conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&target=" + URLEncoder.encode("" + target, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationAdd(String conversation, String target)
    {
        try
        {
            request("conversationAdd",  "conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&target=" + URLEncoder.encode("" + target, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationLeave(String conversation)
    {
        try
        {
            request("conversationLeave", "conversation=" + URLEncoder.encode("" + conversation, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void conversationOwner(String conversation, String target)
    {
        try
        {
            request("conversationOwner", "conversation=" + URLEncoder.encode("" + conversation, "UTF-8") + "&target=" + URLEncoder.encode("" + target, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public String createConversation() throws Exception
    {
        JSONObject json = request("conversationCreate", null);
        return json.getString("id");
    }

    public String getDialog(String target) throws Exception
    {
        JSONObject json = request("dialogGet",  "target=" + URLEncoder.encode(target, "UTF-8"));
        System.out.println(json);
        return json.getString("id");
    }

    public String getPublicKey(String target) throws Exception
    {
        JSONObject json = request("userPublicKey", "target=" + target);
        return json.getString("publicKey");
    }

    public void updateFCMToken(String token)
    {
        try
        {
            request("userUpdateFCMToken", "fcmToken=" + URLEncoder.encode(token, "UTF-8"));
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
            request("userUpdatePublicKey", "publicKey=" + URLEncoder.encode(publicKey, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String messageSend(EncryptionService encryptionService, Message message, String user, String conversation, Main main) throws Exception
    {
        String msg = encryptionService.encryptMessage(this, message.asJsonString(), user, main);
        String encodedMSG = URLEncoder.encode(msg, "UTF-8");

        HttpURLConnection connection = (HttpURLConnection) new URL("https://talk.schroeder.computer/api.php?api=messageSend&userId=" + URLEncoder.encode(simpleStorage.getUserId(), "UTF-8") + "&userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") + "&type=" + message.getClass().getSimpleName() + "&receiver=" + user + "&conversation=" + URLEncoder.encode("" + conversation, "UTF-8")).openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(msg);
        wr.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return new JSONObject(br.readLine()).getString("id");
    }

    public ArrayList<StoredMessage> messageSync(EncryptionService encryptionService, ComplexStorageWrapper complexStorage)
    {
        ArrayList<StoredMessage> storedMessages = new ArrayList<>();
        try
        {
            JSONObject object = request("messageSync", null);
            JSONArray messages = object.getJSONArray("messages");
            for(int i = 0; i < messages.length(); i++)
            {
                System.out.println("NEW MESSAGE");

                JSONObject msg = messages.getJSONObject(i);
                String sender = msg.getString("sender");
                String conversation = msg.getString("conversation");
                String type = msg.getString("type");
                String encrypted = msg.getString("message");
                String id = msg.getString("id");

                String message = encryptionService.decryptMesage(encrypted);

                StoredMessage storedMessage = new StoredMessage();
                storedMessage.setUser(sender);
                storedMessage.setSent(true);
                storedMessage.setRead(false);
                storedMessage.setConversation(conversation);
                storedMessage.setSendable(message);
                storedMessage.setType(type);
                storedMessage.setTime(System.currentTimeMillis());
                storedMessage.setId(id);
                complexStorage.getComplexStorage().messageInsert(storedMessage);
                storedMessages.add(storedMessage);

                System.out.println("NEU: " + type + " " + message);
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
        HttpURLConnection connection;
        if(simpleStorage.getUserId() != null && simpleStorage.getUserKey() != null)
            connection = (HttpURLConnection)
                new URL("https://talk.schroeder.computer/api.php?api=" + type +
                        "&userId=" + URLEncoder.encode(simpleStorage.getUserId(), "UTF-8")
                        + "&userKey=" + URLEncoder.encode(simpleStorage.getUserKey(), "UTF-8") +
                        (request != null ?  "&" + request : "")).openConnection();
        else connection = (HttpURLConnection) new URL("https://talk.schroeder.computer/api.php?api=" +
                type  + (request != null ?  "&" + request : "")).openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return new JSONObject(br.readLine());
    }
}
