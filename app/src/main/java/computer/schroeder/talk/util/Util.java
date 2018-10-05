package computer.schroeder.talk.util;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.screens.Screen;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredSendable;
import computer.schroeder.talk.util.sendable.Sendable;

public class Util
{
    public static void sync(final Context context) throws Exception
    {
        final ComplexStorageImpl complexStorage = new ComplexStorageImpl(context);
        ArrayList<StoredSendable> storedMessages = new ServerConnection(context).messageSync(new EncryptionService(context), complexStorage.getComplexStorage());

        if(storedMessages.isEmpty()) return;

        if(Main.isClosed())
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        new ServerConnection(context).messageSync(new EncryptionService(context), complexStorage.getComplexStorage());
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
                    for(StoredSendable storedMessage : storedMessages)
                    {
                        if(screenConversation.getStoredConversation().getId() == storedMessage.getConversation())
                        {
                            storedMessage.setRead(true);
                            complexStorage.getComplexStorage().messageUpdate(storedMessage);
                            View view = screenConversation.addMessage(storedMessage, false);
                            screenConversation.scrollToView(view);
                        }
                    }
                }
                else if(screen instanceof ScreenHome)
                {
                    ScreenHome screenHome = (ScreenHome) screen;
                    for(StoredSendable storedMessage : storedMessages)
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

    public static StoredSendable sendSendable(Main main, long conversation, Sendable sendable)
    {
        int localUser = main.getSimpleStorage().getUser();
        final StoredSendable storedSendable = new StoredSendable();
        storedSendable.setTime(System.currentTimeMillis());
        storedSendable.setConversation(conversation);
        storedSendable.setUser(localUser);
        storedSendable.setRead(true);
        storedSendable.setType(sendable.getClass().getSimpleName());
        storedSendable.setSendable(sendable.toString());

        boolean sent = false;

        try
        {
            JSONObject object = main.getServerConnection().conversationInfo(conversation);
            JSONArray member = object.getJSONArray("member");
            for(int i = 0; i < member.length(); i++)
            {
                JSONObject o = (JSONObject) member.get(i);
                int id = o.getInt("id");
                if(id == localUser) continue;
                main.getServerConnection().messageSend(main.getEncryptionService(), sendable, id, conversation);
            }
            sent = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        storedSendable.setSent(sent);
        main.getComplexStorage().getComplexStorage().messageInsert(storedSendable);
        return storedSendable;
    }

}
