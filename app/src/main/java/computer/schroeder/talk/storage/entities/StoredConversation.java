package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class StoredConversation
{
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private long silent;
    private boolean blocked;

    public String getId() {
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

    public void setId(String id) {
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
