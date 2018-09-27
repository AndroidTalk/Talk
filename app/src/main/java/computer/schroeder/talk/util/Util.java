package computer.schroeder.talk.util;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.screen.screens.Screen;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;

public class Util
{
    public static void sync(final Context context) throws Exception
    {
        final ComplexStorageImpl complexStorage = new ComplexStorageImpl(context);
        ArrayList<StoredMessage> storedMessages = new ServerConnection(context).messageSync(new EncryptionService(context), complexStorage.getComplexStorage());

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
                    for(StoredMessage storedMessage : storedMessages)
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

}
