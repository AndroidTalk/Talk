package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class StoredUser
{
    @PrimaryKey
    private long id;
    private String username;

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
