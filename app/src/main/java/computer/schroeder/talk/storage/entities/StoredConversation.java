package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class StoredConversation
{
    @PrimaryKey
    private long id;
    private String title;
    private long silent;
    private boolean blocked;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getSilent() {
        return silent;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSilent(long silent) {
        this.silent = silent;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
