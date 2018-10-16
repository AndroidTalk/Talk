package computer.schroeder.talk.screen.screens;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import computer.schroeder.talk.screen.ScreenManager;
import computer.schroeder.talk.util.ComplexStorageWrapper;

public abstract class Screen
{
    private ScreenManager screenManager;
    private ComplexStorageWrapper complexStorage;

    private ViewGroup content;
    private View contentView;
    private Status status = Status.Preparation;

    Screen(ScreenManager screenManager, int view)
    {
        this.screenManager = screenManager;
        this.content = screenManager.getMain().findViewById(android.R.id.content);
        this.contentView = getScreenManager().getInflater().inflate(view, getContent(), false);
        this.complexStorage = getScreenManager().getMain().getComplexStorage();
    }

    ScreenManager getScreenManager() {
        return screenManager;
    }

    ComplexStorageWrapper getComplexStorage() {
        return complexStorage;
    }

    public ViewGroup getContent() {
        return content;
    }

    View getContentView() {
        return contentView;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public abstract void show() throws Exception;

    public abstract boolean createOptionsMenu(Menu menu);
    public abstract boolean optionsItemSelected(MenuItem item);

    public abstract void back();

    public enum Status
    {
        Preparation,
        Done
    }
}
