package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class StoredMessage
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long conversation;
    private long user;
    private String message;
    private long time;
    private boolean read;
    private boolean sent;

    public long getId() {
        return id;
    }

    public long getConversation() {
        return conversation;
    }

    public long getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isSent() {
        return sent;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setConversation(long conversation) {
        this.conversation = conversation;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
