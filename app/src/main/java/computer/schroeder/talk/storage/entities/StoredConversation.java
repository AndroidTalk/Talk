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
    private String memberHash;
    private boolean silent;
    private boolean blocked;
    private String type;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMemberHash() {
        return memberHash;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMemberHash(String memberHash) {
        this.memberHash = memberHash;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setType(String type) {
        this.type = type;
    }
}
