package computer.schroeder.talk.screen.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import computer.schroeder.talk.R;
import computer.schroeder.talk.messages.MessageEventUserAdded;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.Util;
import top.defaults.colorpicker.ColorPickerPopup;

public class ScreenConversationInfo extends Screen
{
    private StoredConversation storedConversation;

    private LinearLayout member;

    public ScreenConversationInfo(ScreenManager screenManager, StoredConversation storedConversation)
    {
        super(screenManager, R.layout.screen_conversation_info);
        this.storedConversation = storedConversation;
    }

    @Override
    public void show() throws Exception
    {
        if(storedConversation == null) throw new Exception();
        member = getContentView().findViewById(R.id.member);
        String localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
        try
        {
            JSONObject object = getScreenManager().getMain().getRestService().conversationInfo(storedConversation.getId());
            JSONArray member = object.getJSONArray("member");
            String owner = object.getString("owner");
            for(int i = 0; i < member.length(); i++)
            {
                JSONObject o = (JSONObject) member.get(i);
                String id = o.getString("id");
                StoredUser user = getComplexStorage().getUser(id, localUserId);
                addMember(user, id.equals(owner), localUserId.equals(owner));
            }

            if(!localUserId.equals(owner)) getContentView().findViewById(R.id.addUser).setVisibility(View.GONE);
            else
            {
                getContentView().findViewById(R.id.addUser).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        final EditText input = new EditText(getScreenManager().getMain());
                        input.setHint("TalkTag");
                        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);

                        new AlertDialog.Builder(getScreenManager().getMain())
                                .setTitle("Who do you want to add?")
                                .setView(input)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run()
                                            {
                                                try
                                                {
                                                    getScreenManager().getMain().getRestService().conversationAdd(storedConversation.getId(), input.getText().toString());

                                                    MessageEventUserAdded sendableTextMessage = new MessageEventUserAdded(input.getText().toString());
                                                    Util.sendSendable(getScreenManager().getMain(), storedConversation.getId(), sendableTextMessage);
                                                }
                                                catch(Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                                getScreenManager().showConversationInfoScreen(storedConversation);
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton("Cancel", null).show();
                    }
                });
            }

            TextView idview = getContentView().findViewById(R.id.idview);
            idview.setText("Conversation: #" + storedConversation.getId());

            TextView textView = getContentView().findViewById(R.id.titleview);
            textView.setText(storedConversation.getTitle());

            getContentView().findViewById(R.id.titleview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    final EditText input = new EditText(getScreenManager().getMain());
                    input.setHint("Conversation");
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    input.setText(storedConversation.getTitle());

                    new AlertDialog.Builder(getScreenManager().getMain())
                            .setTitle("Rename conversation #" + storedConversation.getId())
                            .setView(input)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                storedConversation.setTitle(input.getText().toString());
                                                getComplexStorage().getComplexStorage().conversationInsert(storedConversation);
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                            getScreenManager().showConversationInfoScreen(storedConversation);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            });

            final Switch mute = getContentView().findViewById(R.id.mute);
            mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    storedConversation.setSilent(b);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getComplexStorage().getComplexStorage().conversationInsert(storedConversation);
                        }
                    }).start();
                }
            });

            TextView leave = getContentView().findViewById(R.id.leave);
            leave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new AlertDialog.Builder(getScreenManager().getMain())
                            .setMessage("Do you really want to leave and delete this conversations?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getScreenManager().getMain().getRestService().conversationLeave(storedConversation.getId());
                                            ComplexStorage complexStorage = getScreenManager().getMain().getComplexStorage().getComplexStorage();
                                            complexStorage.conversationDelete(storedConversation);
                                            for(StoredMessage storedMessage : complexStorage.messageSelectConversation(storedConversation.getId())) complexStorage.messageDelete(storedMessage);
                                            getScreenManager().showHomeScreen(false);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            });

            getContentView().findViewById(R.id.changeColor).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new ColorPickerPopup.Builder(getScreenManager().getMain())
                            .initialColor(storedConversation.getColor()) // Set initial color
                            .enableBrightness(true) // Enable brightness slider or not
                            .enableAlpha(false) // Enable alpha slider or not
                            .okTitle("Choose")
                            .cancelTitle("Cancel")
                            .showIndicator(true)
                            .showValue(false)
                            .build()
                            .show(v, new ColorPickerPopup.ColorPickerObserver() {
                                @Override
                                public void onColorPicked(final int color) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            storedConversation.setColor(color);
                                            getComplexStorage().getComplexStorage().conversationInsert(storedConversation);
                                            getScreenManager().showConversationInfoScreen(storedConversation);
                                        }
                                    }).start();
                                }

                                @Override
                                public void onColor(int color, boolean fromUser) {

                                }
                            });

                }
            });

            getScreenManager().getMain().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    getScreenManager().getMain().setContentView(getContentView());
                    mute.setChecked(storedConversation.isSilent());
                    getScreenManager().setActionBar(null, true, storedConversation.getTitle(), storedConversation.getColor());
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
            getScreenManager().getMain().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getScreenManager().getMain(), "Could not load info. Maybe you are no longer a member of the group or you have no internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    getScreenManager().showConversationScreen(storedConversation);
                }
            }).start();
        }
    }

    @Override
    public boolean createOptionsMenu(Menu menu)
    {
        return false;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void back() {
        getScreenManager().showConversationScreen(storedConversation);
    }


    private void addMember(final StoredUser storedUser, boolean owner, boolean localOwner)
    {
        final LinearLayout messageView;
        messageView = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_user, member, false);
        TextView textUsername = messageView.findViewById(R.id.username);
        textUsername.setText(storedUser.getUsername());
        TextView remove = messageView.findViewById(R.id.remove);
        TextView ownerYes = messageView.findViewById(R.id.owner);
        TextView ownerNo = messageView.findViewById(R.id.nonOwner);

        messageView.findViewById(R.id.name).getBackground().setColorFilter(storedUser.getColor(), PorterDuff.Mode.SRC_ATOP);

        remove.setVisibility(View.GONE);
        ownerYes.setVisibility(View.GONE);
        ownerNo.setVisibility(View.GONE);

        if(localOwner && !owner) remove.setVisibility(View.VISIBLE);

        if(owner) ownerYes.setVisibility(View.VISIBLE);
        else if(localOwner) ownerNo.setVisibility(View.VISIBLE);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(getScreenManager().getMain())
                        .setMessage("Do you really want to remove this user from the group?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getScreenManager().getMain().getRestService().conversationRemove(storedConversation.getId(), storedUser.getId());
                                        getScreenManager().showConversationInfoScreen(storedConversation);
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        ownerNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(getScreenManager().getMain())
                        .setMessage("Do you really want to make this user the group owner?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getScreenManager().getMain().getRestService().conversationOwner(storedConversation.getId(), storedUser.getId());
                                        getScreenManager().showConversationInfoScreen(storedConversation);
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        messageView.findViewById(R.id.rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(getScreenManager().getMain())
                        .setMessage("Do you really want to remove this user from the group?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getScreenManager().getMain().getRestService().conversationRemove(storedConversation.getId(), storedUser.getId());
                                        getScreenManager().showConversationInfoScreen(storedConversation);
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        messageView.findViewById(R.id.rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final EditText input = new EditText(getScreenManager().getMain());
                input.setHint("Username");
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                input.setText(storedUser.getUsername());

                new AlertDialog.Builder(getScreenManager().getMain())
                        .setTitle("Rename user #" + storedUser.getId())
                        .setView(input)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            storedUser.setUsername(input.getText().toString());
                                            getComplexStorage().getComplexStorage().userInsert(storedUser);
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        getScreenManager().showConversationInfoScreen(storedConversation);
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });


        member.addView(messageView);
    }

    public StoredConversation getStoredConversation() {
        return storedConversation;
    }
}
