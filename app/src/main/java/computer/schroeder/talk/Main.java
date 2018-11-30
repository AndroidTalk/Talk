package computer.schroeder.talk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.screen.screens.Screen;
import computer.schroeder.talk.screen.screens.ScreenConversation;
import computer.schroeder.talk.screen.screens.ScreenConversationInfo;
import computer.schroeder.talk.screen.screens.ScreenHome;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.util.ComplexStorageWrapper;
import computer.schroeder.talk.util.EncryptionService;
import computer.schroeder.talk.util.RestService;

public class Main extends AppCompatActivity
{
    private static boolean open;
    private static ScreenManager screenManager;
    private SimpleStorage simpleStorage;
    private ComplexStorageWrapper complexStorage;
    private EncryptionService encryptionService;
    private RestService restService;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        startWithState(savedInstanceState);
    }

    /**
     * Setup for components
     * Called whenever the app is started without a stored state
     */
    private void init()
    {
        screenManager = new ScreenManager(this);
        simpleStorage = new SimpleStorage(this);
        complexStorage = new ComplexStorageWrapper(this);
        try
        {
            encryptionService = new EncryptionService(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }
        restService = new RestService(this);
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
        if(screenManager.getCurrentScreen() == null || screenManager.getCurrentScreen().getStatus() != Screen.Status.Done) return super.onCreateOptionsMenu(menu);
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
                outState.putString("conversation", ((ScreenConversation) getScreenManager().getCurrentScreen()).getStoredConversation().getId());
            }
            else if(getScreenManager().getCurrentScreen() instanceof ScreenConversationInfo)
            {
                outState.putString("screen", "CONVERSATION_INFO");
                outState.putString("conversation", ((ScreenConversationInfo) getScreenManager().getCurrentScreen()).getStoredConversation().getId());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(getScreenManager().getCurrentScreen() != null)
        {
            getScreenManager().getCurrentScreen().onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Starts the app with a previous stored state
     * @param bundle the state which should be opened again
     */
    public void startWithState(Bundle bundle)
    {
        init();

        if(bundle == null) bundle = new Bundle();

        String screen = bundle.getString("screen", "HOME");
        System.out.println("ccc" + bundle.getString("conversation"));
        if(screen.equals("CONVERSATION"))
        {
            String conversation = bundle.getString("conversation", null);
            if (conversation != null)
            {
                getScreenManager().showConversationScreen(conversation);
                return;
            }
        }
        else if(screen.equals("CONVERSATION_INFO"))
        {
            String conversation = bundle.getString("conversation", null);
            if (conversation != null)
            {
                getScreenManager().showConversationScreen(conversation);
                return;
            }
        }
        getScreenManager().showHomeScreen(true);
    }

    /**
     * Used for notifications
     * @return true if the app is in foreground
     */
    public static boolean isClosed() {
        return !open;
    }

    /**
     *
     * @return the simple storage used to store basic parameter
     */
    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    /**
     *
     * @return the complex storage used to store users, messages and sendables
     */
    public ComplexStorageWrapper getComplexStorage() {
        return complexStorage;
    }

    /**
     *
     * @return the encryption service which is used to encrypt and decrypt sendables
     */
    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    /**
     *
     * @return the Rest Client used to communicate with the backend
     */
    public RestService getRestService() {
        return restService;
    }

    /**
     *
     * @return the screen manager used to manage the shown screen
     */
    public static ScreenManager getScreenManager() {
        return screenManager;
    }
}
