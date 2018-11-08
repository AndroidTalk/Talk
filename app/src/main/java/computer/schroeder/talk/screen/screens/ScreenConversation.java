package computer.schroeder.talk.screen.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import computer.schroeder.talk.R;
import computer.schroeder.talk.messages.Message;
import computer.schroeder.talk.messages.MessageAudio;
import computer.schroeder.talk.messages.MessageImage;
import computer.schroeder.talk.messages.MessageText;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.util.NotificationService;
import computer.schroeder.talk.util.Util;

public class ScreenConversation extends Screen
{
    private long lastTime;
    private String localUserId;
    private StoredConversation storedConversation;
    private String conversationID = null;

    private LinearLayout messages;
    private ScrollView scrollView;

    private ArrayList<StoredMessage> selected = new ArrayList<>();

    public ScreenConversation(ScreenManager screenManager, StoredConversation storedConversation)
    {
        super(screenManager, R.layout.screen_conversation);
        this.storedConversation = storedConversation;
    }

    public ScreenConversation(ScreenManager screenManager, String storedConversation)
    {
        super(screenManager, R.layout.screen_conversation);
        conversationID = storedConversation;
    }

    @Override
    public void show() throws Exception
    {
        if(storedConversation == null && conversationID != null) storedConversation = getComplexStorage().getConversation(conversationID);
        if(storedConversation == null) throw new Exception();
        localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
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
                final String textMessage = text.getText().toString();
                text.setText("");
                if(textMessage.trim().isEmpty()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Message message = new MessageText(textMessage);
                        if(textMessage.equalsIgnoreCase("bild")) message = new MessageImage("Test");
                        else if(textMessage.equalsIgnoreCase("audio")) message = new MessageAudio();

                        Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), message);
                    }
                }).start();
            }
        });

        final EditText input = getContentView().findViewById(R.id.messageText);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(final Editable s)
            {
                getScreenManager().getMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        ImageView icon = getContentView().findViewById(R.id.icon);
                        ImageView attachment = getContentView().findViewById(R.id.attatchment);
                        ImageView camera = getContentView().findViewById(R.id.camera);
                        if(s.toString().equals(""))
                        {
                            icon.setImageResource(R.drawable.ic_mic);
                            attachment.setVisibility(View.VISIBLE);
                            camera.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            icon.setImageResource(R.drawable.ic_send);
                            attachment.setVisibility(View.GONE);
                            camera.setVisibility(View.GONE);
                        }
                    }
                });
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

    public View addMessage(final StoredMessage storedMessage, final boolean top)
    {
        final View messageView = storedMessage.getSendableObject().asView(getScreenManager(), messages, localUserId, storedMessage);

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

        if(!storedMessage.isSent()) ((CardView) messageView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#FFFF4444"));

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
