package computer.schroeder.talk.screen.screens;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

public class ScreenConversation extends Screen {
    private long lastTime;
    private String localUserId;
    private StoredConversation storedConversation;
    private String conversationID = null;

    private LinearLayout views;
    private ScrollView scrollView;

    private ArrayList<StoredMessage> selected = new ArrayList<>();

    private File image;

    private boolean recording;
    private boolean shouldSend;

    private HashMap<View, Long> timeList = new HashMap<>();
    private ArrayList<Long> dateInfos = new ArrayList<>();

    private int index = 0;
    private int lastSize = 0;

    public ScreenConversation(ScreenManager screenManager, StoredConversation storedConversation) {
        super(screenManager, R.layout.screen_conversation);
        this.storedConversation = storedConversation;
    }

    public ScreenConversation(ScreenManager screenManager, String storedConversation) {
        super(screenManager, R.layout.screen_conversation);
        conversationID = storedConversation;
    }

    @Override
    public void show() throws Exception {
        if (storedConversation == null && conversationID != null)
            storedConversation = getComplexStorage().getConversation(conversationID);
        if (storedConversation == null) throw new Exception();
        localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
        views = getContentView().findViewById(R.id.messages);
        scrollView = getContentView().findViewById(R.id.scroll);

        //addInfo("Messages are End-To-End encrypted", true);

        loadMessages(10, false);

        NotificationService.clear(getScreenManager().getMain(), storedConversation.getId());
        NotificationService.update(getScreenManager().getMain(), getScreenManager().getMain().getComplexStorage());


        final View send = getContentView().findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = getContentView().findViewById(R.id.messageText);
                final String textMessage = text.getText().toString();
                text.setText("");
                if (textMessage.trim().isEmpty()) {
                    if (!recording) {
                        if (ActivityCompat.checkSelfPermission(getScreenManager().getMain(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(getScreenManager().getMain(), new String[]{Manifest.permission.RECORD_AUDIO},
                                    5000);

                        } else {
                            recording = true;
                            shouldSend = false;

                            getContentView().findViewById(R.id.audio).setVisibility(View.VISIBLE);
                            getContentView().findViewById(R.id.text).setVisibility(View.GONE);
                            final ImageView icon = getContentView().findViewById(R.id.icon);
                            icon.setImageResource(R.drawable.ic_send);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    long time = System.currentTimeMillis();

                                    try {
                                        getScreenManager().getMain().getAudioRecorder().startRecording(getScreenManager());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    while (recording) {
                                        long length = System.currentTimeMillis() - time;
                                        length /= 1000;

                                        final long seconds = length % 60;
                                        length -= seconds;
                                        final long minutes = length / 60;
                                        final TextView t = getContentView().findViewById(R.id.audioLength);

                                        getScreenManager().getMain().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                t.setText(minutes + ":" + (seconds < 10 ? ("0" + seconds) : ("" + seconds)));
                                            }
                                        });
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    getScreenManager().getMain().getAudioRecorder().stopRecording();

                                    getScreenManager().getMain().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getContentView().findViewById(R.id.audio).setVisibility(View.GONE);
                                            getContentView().findViewById(R.id.text).setVisibility(View.VISIBLE);
                                            icon.setImageResource(R.drawable.ic_mic);
                                        }
                                    });

                                    if (shouldSend) {

                                        try {
                                            byte[] byteArray = IOUtils.toByteArray(new FileInputStream(getScreenManager().getMain().getAudioRecorder().getFile()));
                                            getScreenManager().getMain().getAudioRecorder().getFile().delete();

                                            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                            Message message = new MessageAudio(encoded);
                                            Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), message);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        }
                    } else {
                        shouldSend = true;
                        recording = false;
                    }
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Message message = new MessageText(textMessage);
                            Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), message);
                        }
                    }).start();
                }
            }
        });

        getContentView().findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = false;
            }
        });

        final SwipeRefreshLayout refreshLayout = getContentView().findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getScreenManager().getMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadMessages(10, false);
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        final EditText input = getContentView().findViewById(R.id.messageText);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                getScreenManager().getMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView icon = getContentView().findViewById(R.id.icon);
                        ImageView attachment = getContentView().findViewById(R.id.attatchment);
                        ImageView camera = getContentView().findViewById(R.id.camera);
                        if (s.toString().equals("")) {
                            icon.setImageResource(R.drawable.ic_mic);
                            attachment.setVisibility(View.GONE);
                            camera.setVisibility(View.VISIBLE);
                        } else {
                            icon.setImageResource(R.drawable.ic_send);
                            attachment.setVisibility(View.GONE);
                            camera.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        getContentView().findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    image = File.createTempFile("picture_", ".png", getScreenManager().getMain().getExternalFilesDir("Pictures"));
                    Uri i = FileProvider.getUriForFile(getScreenManager().getMain(),
                            "computer.schroeder.talk.fileprovider",
                            image);
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, i);
                    if (takePictureIntent.resolveActivity(getScreenManager().getMain().getPackageManager()) != null) {
                        getScreenManager().getMain().startActivityForResult(takePictureIntent, 4000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        getContentView().findViewById(R.id.emoji).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getScreenManager().getMain().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                input.requestFocus();
            }
        });

        getScreenManager().getMain().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Drawable bg = getScreenManager().getMain().getResources().getDrawable(R.drawable.bg);
                //getScreenManager().getMain().getWindow().setBackgroundDrawable(bg);
                getScreenManager().getMain().setContentView(getContentView());
                updateActionBar();

                getContentView().findViewById(R.id.attatchment).setVisibility(View.GONE);

                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    public void loadMessages(final int old, final boolean allNew)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<StoredMessage> list = getScreenManager().getMain().getComplexStorage().getComplexStorage()
                        .messageSelectConversation(storedConversation.getId());

                int diff = list.size() - lastSize;
                lastSize = list.size();

                if(allNew)
                {
                    index += diff;
                    for (final StoredMessage storedMessage : list.subList(list.size() - diff, list.size())) {
                        storedMessage.setRead(true);
                        getScreenManager().getMain().getComplexStorage().getComplexStorage().messageUpdate(storedMessage);
                        getScreenManager().getMain().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addMessage(storedMessage);
                            }
                        });
                    }
                }

                if(old > 0)
                {
                    int max = list.size() - index;
                    index += old;
                    int min = list.size() - index;
                    if(min < 0) min = 0;
                    if(max < 0) max = 0;
                    for (final StoredMessage storedMessage : list.subList(min, max)) {
                        storedMessage.setRead(true);
                        getScreenManager().getMain().getComplexStorage().getComplexStorage().messageUpdate(storedMessage);
                        addMessage(storedMessage);
                    }
                    if(min == 0 && max == 0)
                    {
                        getScreenManager().getMain().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getScreenManager().getMain(), "No more messages", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private void addView(View view, long time, boolean scroll) {
        timeList.put(view, time);

        List<Map.Entry<View, Long>> sorted = entriesSortedByValues(timeList);

        int position = 0;
        for (Map.Entry<View, Long> v : sorted) {
            if (v.getKey() == view) break;
            position++;
        }
        views.addView(view, position);
        if(scroll) scrollToView(view);
    }

    List<Map.Entry<View, Long>> entriesSortedByValues(Map<View, Long> map) {

        List<Map.Entry<View, Long>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries, new Comparator<Map.Entry<View, Long>>() {
            @Override
            public int compare(Map.Entry<View, Long> e1, Map.Entry<View, Long> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        });

        return sortedEntries;
    }

    @Override
    public boolean createOptionsMenu(Menu menu) {
        getScreenManager().getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info)
            getScreenManager().showConversationInfoScreen(storedConversation);
        else if (item.getItemId() == R.id.delete) {
            getScreenManager().showConversationScreen(storedConversation);
        }
        return false;
    }

    @Override
    public void back() {
        getScreenManager().showHomeScreen(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 4000 && resultCode == AppCompatActivity.RESULT_OK) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), options);

                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.1), (int) (bitmap.getHeight() * 0.1), true);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    Message message = new MessageImage(encoded);

                    Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), message);

                    image.delete();
                }
            }).start();

        }
    }

    private View addMessage(final StoredMessage storedMessage) {
        final View messageView = storedMessage.getSendableObject().asView(getScreenManager(), views, localUserId, storedMessage);

        getScreenManager().getMain().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selected.contains(storedMessage)) {
                            selected.remove(storedMessage);
                            messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                        } else if (!selected.isEmpty()) {
                            selected.add(storedMessage);
                            messageView.setBackgroundColor(Color.parseColor("#cce1f3fb"));
                        }
                        updateActionBar();
                    }
                });

                messageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (selected.contains(storedMessage)) {
                            selected.remove(storedMessage);
                            messageView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                        } else {
                            selected.add(storedMessage);
                            messageView.setBackgroundColor(Color.parseColor("#cce1f3fb"));
                        }
                        updateActionBar();
                        return true;
                    }
                });

                if (!storedMessage.isSent())
                    ((CardView) messageView.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#FFFF4444"));



                getScreenManager().getMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDateInfo(new Date(storedMessage.getTime()));
                        addView(messageView, storedMessage.getTime(), true);
                    }
                });
            }
        });
        return messageView;
    }

    private void addDateInfo(Date date) {
        long day = date.getTime() / 86400000;
        if(dateInfos.contains(day)) return;
        else dateInfos.add(day);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, cal1.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal1.set(Calendar.MINUTE, cal1.getActualMinimum(Calendar.MINUTE));
        cal1.set(Calendar.SECOND, cal1.getActualMinimum(Calendar.SECOND));
        cal1.set(Calendar.MILLISECOND, cal1.getActualMinimum(Calendar.MILLISECOND));
        cal2.setTime(new Date(System.currentTimeMillis()));
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        cal2.setTime(new Date(System.currentTimeMillis() - 86400000));
        boolean yesterday = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        if (sameDay) addInfo("TODAY", cal1.getTimeInMillis());
        else if (yesterday) addInfo("YESTERDAY", cal1.getTimeInMillis());
        else
            addInfo(new SimpleDateFormat("dd. MMMM yyyy", Locale.ENGLISH).format(date), cal1.getTimeInMillis());
    }

    private void addInfo(String message, long time) {
        final LinearLayout messageView;
        messageView = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_info, views, false);
        TextView textViewMessage = messageView.findViewById(R.id.message);
        textViewMessage.setText(message);
        addView(messageView, time, false);
    }


    private void updateActionBar() {
        if (selected.isEmpty())
            getScreenManager().setActionBar(null, true, storedConversation.getTitle(), storedConversation.getColor());
        else {
            getScreenManager().setActionBar(R.layout.actionbar_conversation_selected, true, storedConversation.getTitle(), storedConversation.getColor());
            ImageView delete = getScreenManager().getActionBar().getCustomView().findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new AlertDialog.Builder(getScreenManager().getMain())
                            .setMessage("Do you really want to remove those messages?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (StoredMessage storedMessage : selected) {
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

    public void scrollToView(final View view) {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, view.getTop());
            }
        });
    }

    public StoredConversation getStoredConversation() {
        return storedConversation;
    }
}
