package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class StoredUser
{
    @PrimaryKey
    @NonNull
    private String id;
    private String username;
    private String publicKey;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
