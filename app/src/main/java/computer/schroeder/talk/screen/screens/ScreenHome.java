package computer.schroeder.talk.screen.screens;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import computer.schroeder.chat.R;
import computer.schroeder.chat.screen.ScreenManager;
import computer.schroeder.chat.storage.ComplexStorage;
import computer.schroeder.chat.storage.SimpleStorage;
import computer.schroeder.chat.storage.entities.StoredConversation;
import computer.schroeder.chat.storage.entities.StoredMessage;
import computer.schroeder.chat.storage.entities.StoredUser;
import computer.schroeder.chat.util.TokenService;
import computer.schroeder.chat.util.Util;

public class ScreenHome extends Screen
{
    private HashMap<Long, View> conversationMap = new HashMap<>();

    private LinearLayout conversations;
    private boolean sync;
    private int localUser;

    private ArrayList<StoredConversation> selected = new ArrayList<>();

    public ScreenHome(ScreenManager screenManager, boolean sync)
    {
        super(screenManager, R.layout.screen_home);
        this.sync = sync;
    }

    @Override
    public void show() throws Exception
    {
        localUser = getScreenManager().getMain().getSimpleStorage().getUser();
        conversations = getContentView().findViewById(R.id.conversations);

        FloatingActionButton newChat = getContentView().findViewById(R.id.newChat);
        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getScreenManager().getMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        getScreenManager().getMain().invalidateOptionsMenu();
                        getScreenManager().getMain().setContentView(R.layout.screen_loading);

                        new Thread(new Runnable() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    int id = getScreenManager().getMain().getServerConnection().createConversation();
                                    StoredConversation conversation = getComplexStorage().getConversation(id);
                                    getScreenManager().showConversationScreen(conversation);

                                }
                                catch(Exception e)
                                {
                                    getScreenManager().showHomeScreen(false);
                                    getScreenManager().getMain().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getScreenManager().getMain(), "Could not create new conversation.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
            }
        });

        if(sync)
        {
            SimpleStorage simpleStorage = getScreenManager().getMain().getSimpleStorage();
            try
            {
                if(simpleStorage.getUser() == -1 || simpleStorage.getUserKey() == null)
                {
                    Object[] values = getScreenManager().getMain().getServerConnection().userRegister();
                    simpleStorage.setUserKey((String) values[0]);
                    simpleStorage.setUser((int) values[1]);
                    localUser = getScreenManager().getMain().getSimpleStorage().getUser();
                }
            }
            catch(Exception e)
            {
                throw new Exception("To start using Chat you need an internet connection.");
            }
            getScreenManager().getMain().getServerConnection().updatePublicKey(simpleStorage.getPublicKey());
            new TokenService().updateToken(getScreenManager().getMain());
            Util.sync(getScreenManager().getMain());
        }

        ArrayList<DisplayableConversation> conversations = new ArrayList<>();

        for(StoredConversation conversation : getScreenManager().getMain().getComplexStorage().getComplexStorage().conversationSelect()) conversations.add(new DisplayableConversation(conversation, getScreenManager().getMain().getComplexStorage().getComplexStorage().messageSelectLastMessageByConversation(conversation.getId())));

        Collections.sort(conversations, new Comparator<DisplayableConversation>() {
            @Override
            public int compare(DisplayableConversation d1, DisplayableConversation d2) {
                if(d1.storedMessage == null && d2.storedMessage == null) return 0;
                if(d1.storedMessage == null) return 1;
                if(d2.storedMessage == null) return -1;
                return Long.compare(d2.storedMessage.getTime(), d1.storedMessage.getTime());
            }
        });


        for(DisplayableConversation conversation : conversations) addConversation(conversation.conversation, false);


        getScreenManager().getMain().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                getScreenManager().getMain().setContentView(getContentView());
                updateActionBar();
            }
        });
    }

    @Override
    public boolean createOptionsMenu(Menu menu)
    {
        //getScreenManager().getMenuInflater().inflate(R.menu.menu_home, menu);
        return false;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void back() {
        getScreenManager().getMain().finish();
    }

    public void addConversation(final StoredConversation conversation, final boolean top)
    {
        String lastMessageFull = "No messages";
        String lastMessageSender = null;
        String lastMessageText = null;
        long lastMessageTime = 0;
        int unread = getScreenManager().getMain().getComplexStorage().getComplexStorage().messageSelectUnreadConversation(conversation.getId()).size();

        StoredMessage lastMessage = getScreenManager().getMain().getComplexStorage().getComplexStorage().messageSelectLastMessageByConversation(conversation.getId());
        if(lastMessage != null)
        {
            lastMessageText = lastMessage.getMessage();
            lastMessageTime = lastMessage.getTime();
            StoredUser user = getScreenManager().getMain().getComplexStorage().getUser(lastMessage.getUser(), localUser);
            lastMessageSender = user.getUsername();
        }

        if(lastMessageSender != null && lastMessageText != null) lastMessageFull = lastMessageSender + ": " + lastMessageText;

        final LinearLayout messageView;
        messageView = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_conversation, conversations, false);
        TextView textUsername = messageView.findViewById(R.id.username);
        textUsername.setText(conversation.getTitle());
        TextView textLastMessage = messageView.findViewById(R.id.lastMessage);
        textLastMessage.setText(lastMessageFull);
        TextView textViewTime = messageView.findViewById(R.id.time);
        textViewTime.setText(getTime(lastMessageTime));
        CardView unreadView = messageView.findViewById(R.id.unread);
        TextView unreadCountView = messageView.findViewById(R.id.unreadCount);

        messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(selected.isEmpty())
                {
                    getScreenManager().showConversationScreen(conversation);
                }
                else if(selected.contains(conversation))
                {
                    selected.remove(conversation);
                    messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
                else
                {
                    selected.add(conversation);
                    messageView.setBackgroundColor(Color.parseColor("#AFFEA02C"));
                }
                updateActionBar();
            }
        });

        messageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                if(selected.contains(conversation))
                {
                    selected.remove(conversation);
                    messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
                else
                {
                    selected.add(conversation);
                    messageView.setBackgroundColor(Color.parseColor("#AFFEA02C"));
                }
                updateActionBar();
                return true;
            }
        });


        if(unread == 0) unreadView.setVisibility(View.INVISIBLE);
        else if(unread > 9) unreadCountView.setText("+");
        else unreadCountView.setText(getScreenManager().getMain().getString(R.string.unread, unread));

        getScreenManager().getMain().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(top) conversations.addView(messageView, 0);
                else conversations.addView(messageView);

                conversationMap.put(conversation.getId(), messageView);
            }
        });
    }

    private void updateActionBar()
    {
        if(selected.isEmpty()) getScreenManager().setActionBar(null, false, "Your TalkTag: #" + localUser);
        else
        {
            getScreenManager().setActionBar(R.layout.actionbar_home_selected, false, "Your TalkTag: #" + localUser);
            ImageView delete = getScreenManager().getActionBar().getCustomView().findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    new AlertDialog.Builder(getScreenManager().getMain())
                            .setMessage("Do you really want to leave and delete those conversations?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for(StoredConversation conversation : selected)
                                            {
                                                getScreenManager().getMain().getServerConnection().conversationLeave(conversation.getId());
                                                ComplexStorage complexStorage = getScreenManager().getMain().getComplexStorage().getComplexStorage();
                                                complexStorage.conversationDelete(conversation);
                                                for(StoredMessage storedMessage : complexStorage.messageSelectConversation(conversation.getId())) complexStorage.messageDelete(storedMessage);
                                            }
                                            getScreenManager().showHomeScreen(false);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            });
        }
    }

    private String getTime(long time)
    {
        if(time == 0) return "";

        long julianDayNumber1 = System.currentTimeMillis() / 86400000;
        long julianDayNumber2 = time / 86400000;

        // If they now are equal then it is the same day.
        if(julianDayNumber1 == julianDayNumber2) return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(time));
        return new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(new Date(time));
    }

    public HashMap<Long, View> getConversationMap() {
        return conversationMap;
    }
}

class DisplayableConversation
{
    StoredConversation conversation;
    StoredMessage storedMessage;

    DisplayableConversation(StoredConversation storedConversation, StoredMessage storedMessage)
    {
        this.conversation = storedConversation;
        this.storedMessage = storedMessage;
    }
}