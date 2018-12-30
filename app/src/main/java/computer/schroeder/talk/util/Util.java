package computer.schroeder.talk.util;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.screen.screens.Screen;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.messages.Message;

public class Util
{
    /**
     * Used to sync internal message storage with the backend
     * @param context the context used to access the backend
     * @throws Exception thrown if the decryption service fails
     */
    public static void sync(final Context context) throws Exception
    {
        final ComplexStorageWrapper complexStorage = new ComplexStorageWrapper(context);
        ArrayList<StoredMessage> storedMessages = new RestService(context).messageSync(new EncryptionService(context), complexStorage);

        if(storedMessages.isEmpty()) return;

        if(Main.isClosed())
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        new RestService(context).messageSync(new EncryptionService(context), complexStorage);
                        NotificationService.update(context, complexStorage);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else if(Main.getScreenManager() != null && Main.getScreenManager().getCurrentScreen() != null)
        {
            Screen screen = Main.getScreenManager().getCurrentScreen();
            if(screen.getStatus() == Screen.Status.Done)
            {
                if(screen instanceof ScreenConversation)
                {
                    ScreenConversation screenConversation = (ScreenConversation) screen;
                    for(StoredMessage storedMessage : storedMessages)
                    {
                        if(screenConversation.getStoredConversation().getId().equals(storedMessage.getConversation()))
                        {
                            storedMessage.setRead(true);
                            complexStorage.getComplexStorage().messageUpdate(storedMessage);
                            screenConversation.loadMessages(0, true);
                        }
                    }
                }
                else if(screen instanceof ScreenHome)
                {
                    ScreenHome screenHome = (ScreenHome) screen;
                    for(StoredMessage storedMessage : storedMessages)
                    {
                        StoredConversation conversation = complexStorage.getConversation(storedMessage.getConversation());
                        if(screenHome.getConversationMap().containsKey(conversation.getId()))
                        {
                            final View view = screenHome.getConversationMap().get(conversation.getId());
                            Main.getScreenManager().getMain().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((LinearLayout) view.getParent()).removeView(view);
                                }
                            });
                            screenHome.getConversationMap().remove(conversation.getId());
                        }
                        screenHome.addConversation(conversation, true);
                    }
                }
            }
        }
    }

    public static void sendSendable(final Main main, final String conversation, final Message message)
    {
        final String localUserId = main.getSimpleStorage().getUserId();
        final StoredMessage storedMessage = new StoredMessage();
        storedMessage.setTime(System.currentTimeMillis());
        storedMessage.setConversation(conversation);
        storedMessage.setUser(localUserId);
        storedMessage.setRead(true);
        storedMessage.setSent(true);
        storedMessage.setType(message.getClass().getSimpleName());
        storedMessage.setSendable(message.asJsonString());
        storedMessage.setId(UUID.randomUUID().toString());
        main.getComplexStorage().getComplexStorage().messageInsert(storedMessage);

        if(Main.getScreenManager().getCurrentScreen() instanceof ScreenConversation)
        {
            ScreenConversation screenConversation = (ScreenConversation) Main.getScreenManager().getCurrentScreen();
            if(screenConversation.getStoredConversation().getId().equals(conversation)) screenConversation.loadMessages(0, true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                try
                {
                    final StoredConversation storedConversation = main.getComplexStorage().getConversation(conversation);
                    JSONObject object = main.getRestService().conversationInfo(conversation);
                    final JSONArray member = object.getJSONArray("member");

                    if(storedConversation.getMemberHash() == null || !storedConversation.getMemberHash().equals(member.toString()))
                    {
                        storedConversation.setMemberHash(member.toString());
                        main.getComplexStorage().getComplexStorage().conversationInsert(storedConversation);
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(main, "The memberlist for conversation #" + storedConversation.getId() + " has changed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    for(int i = 0; i < member.length(); i++)
                    {
                        JSONObject o = (JSONObject) member.get(i);
                        String id = o.getString("id");
                        if(id.equals(localUserId)) continue;
                        String messageId = main.getRestService().messageSend(main.getEncryptionService(), message, id, conversation, main);
                        storedMessage.setId(messageId);
                    }
                }
                catch(Exception e)
                {
                    storedMessage.setSent(false);
                    e.printStackTrace();
                }
                main.getComplexStorage().getComplexStorage().messageUpdate(storedMessage);
            }
        }).start();
    }
}
