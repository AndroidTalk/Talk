package computer.schroeder.talk.screen.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.Message;
import computer.schroeder.talk.util.NotificationService;

public class ScreenConversation extends Screen
{
    private long lastTime;
    private int localUser;
    private StoredConversation storedConversation;
    private long conversationID = -1;

    private LinearLayout messages;
    private ScrollView scrollView;

    private ArrayList<StoredMessage> selected = new ArrayList<>();

    public ScreenConversation(ScreenManager screenManager, StoredConversation storedConversation)
    {
        super(screenManager, R.layout.screen_conversation);
        this.storedConversation = storedConversation;
    }

    public ScreenConversation(ScreenManager screenManager, long storedConversation)
    {
        super(screenManager, R.layout.screen_conversation);
        conversationID = storedConversation;
    }

    @Override
    public void show() throws Exception
    {
        if(storedConversation == null && conversationID != -1) storedConversation = getComplexStorage().getConversation(conversationID);
        if(storedConversation == null) throw new Exception();
        localUser = getScreenManager().getMain().getSimpleStorage().getUser();
        messages = getContentView().findViewById(R.id.messages);
        scrollView = getContentView().findViewById(R.id.scroll);

        addInfo("Messages are End-To-End encrypted", true);
        for(StoredMessage storedMessage : getScreenManager().getMain().getComplexStorage().getComplexStorage()
                .messageSelectConversation(storedConversation.getId()))
        {
            storedMessage.setRead(true);
            getScreenManager().getMain().getComplexStorage().getComplexStorage().messageUpdate(storedMessage);
            addMessage(storedMessage, false);
        }
        NotificationService.clear(getScreenManager().getMain(), storedConversation.getId());
        NotificationService.update(getScreenManager().getMain(), getScreenManager().getMain().getComplexStorage());


        final View send = getContentView().findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = getContentView().findViewById(R.id.messageText);
                final String message = text.getText().toString();
                text.setText("");
                if(message.trim().isEmpty()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final StoredMessage storedMessage = new StoredMessage();
                        storedMessage.setTime(System.currentTimeMillis());
                        storedMessage.setMessage(message);
                        storedMessage.setConversation(storedConversation.getId());
                        storedMessage.setUser(localUser);
                        storedMessage.setRead(true);
                        storedMessage.setSent(true);
                        getScreenManager().getMain().getComplexStorage().getComplexStorage().messageInsert(storedMessage);
                        final View messageView = addMessage(storedMessage, false);
                        try
                        {
                            JSONObject object = getScreenManager().getMain().getServerConnection().conversationInfo(storedConversation.getId());
                            JSONArray member = object.getJSONArray("member");
                            Message msg = new Message(storedMessage.getMessage(), storedMessage.getTime());
                            for(int i = 0; i < member.length(); i++)
                            {
                                JSONObject o = (JSONObject) member.get(i);
                                int id = o.getInt("id");
                                if(id == localUser) continue;
                                getScreenManager().getMain().getServerConnection().messageSend(getScreenManager().getMain().getEncryptionService(), msg, id, storedConversation.getId());
                            }
                        }
                        catch(Exception e)
                        {
                            storedMessage.setSent(false);
                            getComplexStorage().getComplexStorage().messageInsert(storedMessage);
                            getScreenManager().getMain().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((CardView) messageView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#FFFF4444"));
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        getScreenManager().getMain().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                getScreenManager().getMain().setContentView(getContentView());
                updateActionBar();

                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    @Override
    public boolean createOptionsMenu(Menu menu)
    {
        getScreenManager().getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.info) getScreenManager().showConversationInfoScreen(storedConversation);
        else if(item.getItemId() == R.id.delete)
        {
            getScreenManager().showConversationScreen(storedConversation);
        }
        return false;
    }

    @Override
    public void back() {
        getScreenManager().showHomeScreen(false);
    }

    public View addMessage(final StoredMessage storedMessage, final boolean top)
    {
        final View messageView;

        if(storedMessage.getUser() == localUser) messageView = getScreenManager().getInflater().inflate(R.layout.display_message_sent, messages, false);
        else
        {
            messageView = getScreenManager().getInflater().inflate(R.layout.display_message_received, messages, false);
            TextView textViewUsername = messageView.findViewById(R.id.username);
            textViewUsername.setText(getScreenManager().getMain().getString(R.string.username, storedMessage.getUser()));
            StoredUser user = getComplexStorage().getUser(storedMessage.getUser(), localUser);
            textViewUsername.setText(user.getUsername());
        }
        TextView textViewMessage = messageView.findViewById(R.id.message);
        textViewMessage.setText(storedMessage.getMessage());
        TextView textViewTime = messageView.findViewById(R.id.time);
        textViewTime.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(storedMessage.getTime())));

        if(!storedMessage.isSent())
            ((CardView) messageView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#FFFF4444"));

        messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(selected.contains(storedMessage))
                {
                    selected.remove(storedMessage);
                    messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
                else if(!selected.isEmpty())
                {
                    selected.add(storedMessage);
                    messageView.setBackgroundColor(Color.parseColor("#AFFEA02C"));
                }
                updateActionBar();
            }
        });

        messageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                if(selected.contains(storedMessage))
                {
                    selected.remove(storedMessage);
                    messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
                else
                {
                    selected.add(storedMessage);
                    messageView.setBackgroundColor(Color.parseColor("#AFFEA02C"));
                }
                updateActionBar();
                return true;
            }
        });


        long julianDayNumber1 = lastTime / 86400000;
        long julianDayNumber2 = storedMessage.getTime() / 86400000;

        final boolean update = julianDayNumber1 != julianDayNumber2;
        lastTime = storedMessage.getTime();

        getScreenManager().getMain().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(update) addInfo(new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(new Date(storedMessage.getTime())), false);
                if(top) messages.addView(messageView, 0);
                else messages.addView(messageView);
                scrollToView(messageView);
            }
        });
        return messageView;
    }

    private void addInfo(String message, boolean top)
    {
        final LinearLayout messageView;
        messageView = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_info, messages, false);
        TextView textViewMessage = messageView.findViewById(R.id.message);
        textViewMessage.setText(message);
        if(top) messages.addView(messageView, 0);
        else messages.addView(messageView);
    }


    private void updateActionBar()
    {
        if(selected.isEmpty()) getScreenManager().setActionBar(null, true, storedConversation.getTitle());
        else
        {
            getScreenManager().setActionBar(R.layout.actionbar_conversation_selected, true, storedConversation.getTitle());
            ImageView delete = getScreenManager().getActionBar().getCustomView().findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    new AlertDialog.Builder(getScreenManager().getMain())
                            .setMessage("Do you really want to remove those messages?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for(StoredMessage storedMessage : selected)
                                            {
                                                getScreenManager().getMain().getComplexStorage().getComplexStorage().messageDelete(storedMessage);
                                            }
                                            getScreenManager().showConversationScreen(storedConversation);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            });
        }
    }

    public void scrollToView(final View view)
    {
        scrollView.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollView.smoothScrollTo(0, view.getTop());
            }
        });
    }

    public StoredConversation getStoredConversation() {
        return storedConversation;
    }
}
