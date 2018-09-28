package computer.schroeder.talk.screen.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;

public class ScreenConversationInfo extends Screen
{
    private StoredConversation storedConversation;
    private long conversationID;

    private LinearLayout member;

    public ScreenConversationInfo(ScreenManager screenManager, long storedConversation)
    {
        super(screenManager, R.layout.screen_conversation_info);
        this.conversationID = storedConversation;
    }

    public ScreenConversationInfo(ScreenManager screenManager, StoredConversation storedConversation)
    {
        super(screenManager, R.layout.screen_conversation_info);
        this.storedConversation = storedConversation;
    }

    @Override
    public void show() throws Exception
    {
        if(storedConversation == null && conversationID != -1) storedConversation = getComplexStorage().getConversation(conversationID);
        if(storedConversation == null) throw new Exception();
        member = getContentView().findViewById(R.id.member);
        long localUser = getScreenManager().getMain().getSimpleStorage().getUser();
        try
        {
            JSONObject object = getScreenManager().getMain().getServerConnection().conversationInfo(storedConversation.getId());
            JSONArray member = object.getJSONArray("member");
            int owner = object.getInt("owner");
            for(int i = 0; i < member.length(); i++)
            {
                JSONObject o = (JSONObject) member.get(i);
                int id = o.getInt("id");
                StoredUser user = getComplexStorage().getUser(id, localUser);
                addMember(user, id == owner, localUser == owner);
            }

            if(localUser != owner) getContentView().findViewById(R.id.addUser).setVisibility(View.GONE);
            else
            {
                getContentView().findViewById(R.id.addUser).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        final EditText input = new EditText(getScreenManager().getMain());
                        input.setHint("TalkTag");
                        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);

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
                                                    getScreenManager().getMain().getServerConnection().conversationAdd(storedConversation.getId(), Long.parseLong(input.getText().toString()));
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
                    if(b) storedConversation.setSilent(1);
                    else storedConversation.setSilent(0);
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
                                            getScreenManager().getMain().getServerConnection().conversationLeave(storedConversation.getId());
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

            getScreenManager().getMain().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    getScreenManager().getMain().setContentView(getContentView());
                    mute.setChecked(storedConversation.getSilent() > 0);
                    getScreenManager().setActionBar(null, true, storedConversation.getTitle());
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
            getScreenManager().showConversationScreen(storedConversation);
            getScreenManager().getMain().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getScreenManager().getMain(), "Could not load info. Maybe you are no longer a member of the group or you have no internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
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
                                        getScreenManager().getMain().getServerConnection().conversationRemove(storedConversation.getId(), storedUser.getId());
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
                                        getScreenManager().getMain().getServerConnection().conversationOwner(storedConversation.getId(), storedUser.getId());
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
                                        getScreenManager().getMain().getServerConnection().conversationRemove(storedConversation.getId(), storedUser.getId());
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