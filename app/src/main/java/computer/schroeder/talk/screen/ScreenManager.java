package computer.schroeder.talk.screen;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.screens.Screen;
import computer.schroeder.talk.screen.screens.ScreenContacts;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenConversationInfo;
import computer.schroeder.talk.screen.screens.ScreenError;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.entities.StoredConversation;

public class ScreenManager
{
    private Main main;
    private LayoutInflater inflater;
    private MenuInflater menuInflater;
    private Screen currentScreen;

    public ScreenManager(Main main)
    {
        this.main = main;
        this.inflater = main.getLayoutInflater();
        this.menuInflater = main.getMenuInflater();
    }

    public Main getMain()
    {
        return main;
    }

    public LayoutInflater getInflater()
    {
        return inflater;
    }

    public MenuInflater getMenuInflater() {
        return menuInflater;
    }

    private void showScreen(final Screen screen)
    {
        main.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showLoadingScreen();

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            currentScreen = screen;
                            currentScreen.show();
                            currentScreen.setStatus(Screen.Status.Done);
                            main.invalidateOptionsMenu();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            if(screen instanceof ScreenError) main.finish();
                            else showErrorScreen(e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }

    public void showHomeScreen(boolean sync)
    {
        showScreen(new ScreenHome(ScreenManager.this, sync));
    }

    public void showConversationScreen(StoredConversation conversation)
    {
        showScreen(new ScreenConversation(ScreenManager.this, conversation));
    }

    public void showConversationScreen(String conversation)
    {
        showScreen(new ScreenConversation(ScreenManager.this, conversation));
    }

    public void showConversationInfoScreen(StoredConversation conversation)
    {
        showScreen(new ScreenConversationInfo(ScreenManager.this, conversation));
    }

    public void showContactsScreen()
    {
        showScreen(new ScreenContacts(ScreenManager.this));
    }

    private void showErrorScreen(String error)
    {
        showScreen(new ScreenError(ScreenManager.this, error));
    }

    private void showLoadingScreen()
    {
        currentScreen = null;
        main.invalidateOptionsMenu();
        main.setContentView(R.layout.screen_loading);
        setActionBar(null, false, "Loading...");
        getMain().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }

    public void setActionBar(Integer id, boolean home)
    {
        setActionBar(id, home, "Talk");
    }

    public void setActionBar(final Integer id, final boolean home, final String title)
    {
        getMain().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                ActionBar actionBar = getMain().getSupportActionBar();
                if(actionBar == null) return;
                if(id != null)
                {
                    actionBar.setCustomView(id);
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayShowCustomEnabled(true);
                }
                else
                {
                    actionBar.setDisplayShowCustomEnabled(false);
                    actionBar.setDisplayShowTitleEnabled(true);
                    actionBar.setTitle(title);
                }
                actionBar.setDisplayHomeAsUpEnabled(home);
                actionBar.setDisplayShowHomeEnabled(home);
                actionBar.setHomeButtonEnabled(home);
            }
        });
    }

    public ActionBar getActionBar()
    {
        return getMain().getSupportActionBar();
    }

    public Screen getCurrentScreen()
    {
        return currentScreen;
    }
}
