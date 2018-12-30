package computer.schroeder.talk.screen.screens;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredUser;
import computer.schroeder.talk.util.InflaterHelper;
import top.defaults.colorpicker.ColorPickerPopup;

public class ScreenContacts extends Screen
{
    private HashMap<String, View> contactViews = new HashMap<>();

    private LinearLayout contacts;
    private String localUserId;

    private ArrayList<StoredConversation> selected = new ArrayList<>();

    public ScreenContacts(ScreenManager screenManager)
    {
        super(screenManager, R.layout.screen_contacts);
    }

    @Override
    public void show()
    {
        localUserId = getScreenManager().getMain().getSimpleStorage().getUserId();
        contacts = getContentView().findViewById(R.id.contacts);

        InflaterHelper inflaterHelper = new InflaterHelper(getScreenManager());

        for(StoredUser user : getComplexStorage().getComplexStorage().selectAllUser()) contacts.addView(inflaterHelper.getUserDisplay(contacts, user, null));


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
        return false;
    }

    @Override
    public boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void back() {
        getScreenManager().showHomeScreen(false);
    }

    private void updateActionBar()
    {
        if(selected.isEmpty()) getScreenManager().setActionBar(null, true, "Your local contacts", ContextCompat.getColor(getScreenManager().getMain(), R.color.standard));
        else
        {
            getScreenManager().setActionBar(R.layout.actionbar_home_selected, true, "Your local contacts", ContextCompat.getColor(getScreenManager().getMain(), R.color.standard));
        }
    }
}