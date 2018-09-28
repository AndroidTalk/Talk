package computer.schroeder.talk.screen.screens;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import computer.schroeder.talk.R;
import computer.schroeder.talk.screen.ScreenManager;

public class ScreenError extends Screen
{
    private String error;

    public ScreenError(ScreenManager screenManager, String error)
    {
        super(screenManager, R.layout.screen_error);
        this.error = error;
    }

    @Override
    public void show()
    {
        TextView errorViewMessage = getContentView().findViewById(R.id.error);
        errorViewMessage.setText(error);

        getContentView().findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getScreenManager().showHomeScreen(false);
            }
        });

        getScreenManager().getMain().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                getScreenManager().getMain().setContentView(getContentView());
                getScreenManager().setActionBar(null, true);
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
        getScreenManager().getMain().finish();
    }
}
