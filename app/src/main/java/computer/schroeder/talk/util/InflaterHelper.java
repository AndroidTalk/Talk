package computer.schroeder.talk.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredUser;
import top.defaults.colorpicker.ColorPickerPopup;

public class InflaterHelper
{
    ScreenManager screenManager;

    public InflaterHelper(ScreenManager screenManager)
    {
        this.screenManager = screenManager;
    }

    public View getUserDisplay(ViewGroup parent, final StoredUser storedUser, final StoredConversation storedConversation)
    {
        final LinearLayout view = (LinearLayout) getScreenManager().getInflater().inflate(R.layout.display_user, parent, false);

        TextView logo = view.findViewById(R.id.logo);
        if(storedUser.getUsername().length() >= 1) logo.setText(storedUser.getUsername());
        logo.getBackground().setColorFilter(storedUser.getColor(), PorterDuff.Mode.SRC_ATOP);

        TextView username = view.findViewById(R.id.username);
        username.setText(storedUser.getUsername() + " (#" + storedUser.getId() +")");

        view.findViewById(R.id.rename).setOnClickListener(new View.OnClickListener() {
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
                                            getScreenManager().getMain().getComplexStorage().getComplexStorage().userInsert(storedUser);
                                        }
                                        catch(Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                        getScreenManager().showContactsScreen();
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        view.findViewById(R.id.changeColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ColorPickerPopup.Builder(getScreenManager().getMain())
                        .initialColor(storedUser.getColor()) // Set initial color
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
                                        storedUser.setColor(color);
                                        getScreenManager().getMain().getComplexStorage().getComplexStorage().userInsert(storedUser);
                                        getScreenManager().showContactsScreen();
                                    }
                                }).start();
                            }

                            @Override
                            public void onColor(int color, boolean fromUser) {

                            }
                        });

            }
        });

        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getScreenManager().getMain())
                        .setMessage("Do you really want to delete this user from your contacts?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getScreenManager().getMain().getComplexStorage().getComplexStorage().delete(storedUser);
                                        getScreenManager().showContactsScreen();
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        if(storedConversation == null)
        {
            view.findViewById(R.id.group).setVisibility(View.GONE);
        }
        else
        {

        }

        return view;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }
}
