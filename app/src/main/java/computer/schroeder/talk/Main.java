package computer.schroeder.talk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenConversationInfo;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.util.ComplexStorageImpl;
import computer.schroeder.talk.util.EncryptionService;
import computer.schroeder.talk.util.ServerConnection;

public class Main extends AppCompatActivity
{
    private static boolean open;
    private static ScreenManager screenManager;
    private SimpleStorage simpleStorage;
    private ComplexStorageImpl complexStorage;
    private EncryptionService encryptionService;
    private ServerConnection serverConnection;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundle = savedInstanceState;
        init();
    }

    private void init()
    {
        screenManager = new ScreenManager(this);
        simpleStorage = new SimpleStorage(this);
        complexStorage = new ComplexStorageImpl(this);
        try
        {
            encryptionService = new EncryptionService(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }
        serverConnection = new ServerConnection(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        startWithState(getIntent().getExtras());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        open = true;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        startWithState(intent.getExtras());
    }

    @Override
    protected void onPause() {
        super.onPause();
        open = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        if(screenManager.getCurrentScreen() == null) return super.onOptionsItemSelected(item);
        return screenManager.getCurrentScreen().optionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(screenManager.getCurrentScreen() == null) return super.onCreateOptionsMenu(menu);
        return screenManager.getCurrentScreen().createOptionsMenu(menu);
    }

    @Override
    public void onBackPressed()
    {
        if(screenManager.getCurrentScreen() == null) finish();
        else screenManager.getCurrentScreen().back();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(getScreenManager().getCurrentScreen() != null)
        {
            if(getScreenManager().getCurrentScreen() instanceof ScreenHome) outState.putString("screen", "HOME");
            else if(getScreenManager().getCurrentScreen() instanceof ScreenConversation)
            {
                outState.putString("screen", "CONVERSATION");
                outState.putLong("conversation", ((ScreenConversation) getScreenManager().getCurrentScreen()).getStoredConversation().getId());
            }
            else if(getScreenManager().getCurrentScreen() instanceof ScreenConversationInfo)
            {
                outState.putString("screen", "CONVERSATION_INFO");
                outState.putLong("conversation", ((ScreenConversationInfo) getScreenManager().getCurrentScreen()).getStoredConversation().getId());
            }
        }
    }

    public void startWithState(Bundle bundle)
    {
        if(bundle == null) bundle = this.bundle;
        if(bundle != null)
        {
            String screen = bundle.getString("screen", "HOME");
            switch (screen) {
                case "CONVERSATION": {
                    long conversation = bundle.getLong("conversation", 0);
                    if (conversation > 0) getScreenManager().showConversationScreen(conversation);
                    else getScreenManager().showHomeScreen(true);
                    break;
                }
                case "CONVERSATION_INFO": {
                    long conversation = bundle.getLong("conversation", 0);
                    if (conversation > 0)
                        getScreenManager().showConversationInfoScreen(conversation);
                    else getScreenManager().showHomeScreen(true);
                    break;
                }
                default:
                    screenManager.showHomeScreen(true);
                    break;
            }
        }
        else getScreenManager().showHomeScreen(true);
        this.bundle = null;
    }

    public static boolean isClosed() {
        return !open;
    }

    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    public ComplexStorageImpl getComplexStorage() {
        return complexStorage;
    }

    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static ScreenManager getScreenManager() {
        return screenManager;
    }
}
