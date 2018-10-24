package computer.schroeder.talk.storage.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import computer.schroeder.talk.util.sendable.Sendable;

@Entity
public class StoredSendable
{
    @PrimaryKey
    @NonNull
    private String id;
    private String conversation;
    private String user;
    private long time;
    private boolean read;
    private boolean sent;
    private String type;
    private String sendable;

    public String getId() {
        return id;
    }

    public String getConversation() {
        return conversation;
    }

    public String getUser() {
        return user;
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

    public String getType() {
        return type;
    }

    public String getSendable() {
        return sendable;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public void setUser(String user) {
        this.user = user;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setSendable(String sendable) {
        this.sendable = sendable;
    }

    public Sendable getSendableObject()
    {
        return Sendable.fromJson(type, sendable);
    }
}
