package computer.schroeder.talk.screen.screens;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.TokenService;
import computer.schroeder.talk.util.Util;
import computer.schroeder.talk.messages.Message;

public class ScreenHome extends Screen
{
    private HashMap<String, View> conversationMap = new HashMap<>();

    private LinearLayout conversations;
    private boolean sync;
    private String localUserId;

    private ArrayList<StoredConversation> selected = new ArrayList<>();

    public ScreenHome(ScreenManager screenManager, boolean sync)
    {
        super(screenManager, R.layout.screen_home);
        this.sync = sync;
    }

    @Override
    public void show() throws Exception
    {
        localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
        conversations = getContentView().findViewById(R.id.conversations);

        FloatingActionButton newGroup = getContentView().findViewById(R.id.newGroup);
        newGroup.setOnClickListener(new View.OnClickListener() {
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
                                    String id = getScreenManager().getMain().getRestService().createConversation();
                                    StoredConversation conversation = getComplexStorage().getConversation(id, "GROUP");
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

        FloatingActionButton newDialog = getContentView().findViewById(R.id.newDialog);
        newDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                final EditText input = new EditText(getScreenManager().getMain());
                input.setHint("TalkTag");
                input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);

                new AlertDialog.Builder(getScreenManager().getMain())
                        .setTitle("Who do you want to talk to?")
                        .setView(input)
                        .setPositiveButton("Start dialog", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            String id = getScreenManager().getMain().getRestService().getDialog(input.getText().toString());
                                            StoredConversation conversation = getComplexStorage().getConversation(id, "DIALOG");
                                            getScreenManager().showConversationScreen(conversation);
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        if(sync)
        {
            final SimpleStorage simpleStorage = getScreenManager().getMain().getSimpleStorage();
            try
            {
                if(localUserId == null || simpleStorage.getUserKey() == null)
                {
                    Object[] values = getScreenManager().getMain().getRestService().userRegister();
                    simpleStorage.setUserId((String) values[0]);
                    simpleStorage.setUserKey((String) values[1]);
                    localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            getScreenManager().getMain().getRestService().updatePublicKey(simpleStorage.getPublicKey());
                            new TokenService().updateToken(getScreenManager().getMain());
                            Util.sync(getScreenManager().getMain());
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Exception("To start using Chat you need an internet connection.");
            }
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
        getScreenManager().getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.contacts) getScreenManager().showContactsScreen();
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
            Message message = lastMessage.getSendableObject();
            lastMessageText = message.asString();
            lastMessageTime = lastMessage.getTime();
            StoredUser user = getScreenManager().getMain().getComplexStorage().getUser(lastMessage.getUser(), localUserId);
            lastMessageSender = user.getUsername();
        }

        if(lastMessageSender != null && lastMessageText != null) lastMessageFull = lastMessageSender + ": " + lastMessageText;

        final LinearLayout messageView;
        messageView = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_conversation, conversations, false);
        TextView textUsername = messageView.findViewById(R.id.username);
        textUsername.setText(conversation.getTitle());
        TextView textName = messageView.findViewById(R.id.name);
        if(conversation.getTitle().length() >= 1) textName.setText(conversation.getTitle());

        textName.getBackground().setColorFilter(conversation.getColor(), PorterDuff.Mode.SRC_ATOP);


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
        if(selected.isEmpty()) getScreenManager().setActionBar(null, false, "Your TalkTag: #" + localUserId, ContextCompat.getColor(getScreenManager().getMain(), R.color.standard));
        else
        {
            getScreenManager().setActionBar(R.layout.actionbar_home_selected, false, "Your TalkTag: #" + localUserId, ContextCompat.getColor(getScreenManager().getMain(), R.color.standard));
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
                                                getScreenManager().getMain().getRestService().conversationLeave(conversation.getId());
                                                ComplexStorage complexStorage = getScreenManager().getMain().getComplexStorage().getComplexStorage();
                                                complexStorage.conversationDelete(conversation);
                                                for(StoredMessage storedMessage : complexStorage.messageSelectConversation(conversation.getId())) complexStorage.messageDelete(storedMessage);
                                            }
                                            getScreenManager().getMain().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getScreenManager().showHomeScreen(false);
                                                }
                                            });
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
        long julianDayNumber2 = julianDayNumber1 - 1;
        long julianDayNumber = time / 86400000;

        // If they now are equal then it is the same day.
        if(julianDayNumber1 == julianDayNumber) return new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(time));
        if(julianDayNumber == julianDayNumber2) return "YESTERDAY";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(new Date(time));
    }

    public HashMap<String, View> getConversationMap() {
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