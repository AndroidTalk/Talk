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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredSendable;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.NotificationService;
import computer.schroeder.talk.util.Util;
import computer.schroeder.talk.util.sendable.Sendable;
import computer.schroeder.talk.util.sendable.SendableGroupOnAdd;
import computer.schroeder.talk.util.sendable.SendableTextMessage;

public class ScreenConversation extends Screen
{
    private long lastTime;
    private int localUser;
    private StoredConversation storedConversation;
    private long conversationID = -1;

    private LinearLayout messages;
    private ScrollView scrollView;

    private ArrayList<StoredSendable> selected = new ArrayList<>();

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
        for(StoredSendable storedMessage : getScreenManager().getMain().getComplexStorage().getComplexStorage()
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

                        SendableTextMessage sendableTextMessage = new SendableTextMessage(message);

                        StoredSendable storedSendable = Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), sendableTextMessage);

                        final View messageView = addMessage(storedSendable, false);

                        if(!storedSendable.isSent())
                        {
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
                getScreenManager().getMain().getWindow().getDecorView().setBackground(getScreenManager().getMain().getResources().getDrawable(R.drawable.backgroundxml));
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

    public View addMessage(final StoredSendable storedMessage, final boolean top)
    {
        final View messageView;

        if(storedMessage.getUser() == localUser)
        {
            messageView = getScreenManager().getInflater().inflate(R.layout.display_message_sent, messages, false);
            ImageView status = messageView.findViewById(R.id.status);
            if(storedMessage.isSent())
            {
                status.setImageDrawable(getScreenManager().getMain().getResources().getDrawable(R.drawable.ic_done_all));
            }
        }
        else
        {
            messageView = getScreenManager().getInflater().inflate(R.layout.display_message_received, messages, false);
            TextView textViewUsername = messageView.findViewById(R.id.username);
            textViewUsername.setText(getScreenManager().getMain().getString(R.string.username, storedMessage.getUser()));
            StoredUser user = getComplexStorage().getUser(storedMessage.getUser(), localUser);
            textViewUsername.setText(user.getUsername());
        }
        TextView textViewMessage = messageView.findViewById(R.id.message);

        Sendable sendable = storedMessage.getSendableObject();
        String text = "No text received.";
        if(sendable instanceof SendableTextMessage) text = ((SendableTextMessage) sendable).getText();
        else if(sendable instanceof SendableGroupOnAdd) text = "User #" + ((SendableGroupOnAdd) sendable).getUser() + " has been added to the group.";

        textViewMessage.setText(text);
        TextView textViewTime = messageView.findViewById(R.id.time);
        textViewTime.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(storedMessage.getTime())));

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
                    messageView.setBackgroundColor(Color.parseColor("#cce1f3fb"));
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
                    messageView.setBackgroundColor(Color.parseColor("#cce1f3fb"));
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
                if(update) addDateInfo(new Date(storedMessage.getTime()));
                if(top) messages.addView(messageView, 0);
                else messages.addView(messageView);
                scrollToView(messageView);
            }
        });
        return messageView;
    }

    private void addDateInfo(Date date)
    {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal2.setTime(new Date(System.currentTimeMillis()));
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        cal2.setTime(new Date(System.currentTimeMillis() - 86400000));
        boolean yesterday = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        if(sameDay) addInfo("TODAY", false);
        else if(yesterday) addInfo("YESTERDAY", false);
        else addInfo(new SimpleDateFormat("dd. MMMM yyyy", Locale.ENGLISH).format(date), false);
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
        if(selected.isEmpty()) getScreenManager().setActionBar(R.layout.actionbar_conversation, true, storedConversation.getTitle());
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
                                            for(StoredSendable storedMessage : selected)
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
