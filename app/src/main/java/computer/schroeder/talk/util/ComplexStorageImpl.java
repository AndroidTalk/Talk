package computer.schroeder.talk.util;

import android.arch.persistence.room.Room;
import android.content.Context;

import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.Database;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredUser;

public class ComplexStorageImpl
{
    private ComplexStorage complexStorage;

    public ComplexStorageImpl(Context context)
    {
        this.complexStorage = Room.databaseBuilder(context,
                        Database.class, "chat").build().getDatabase();
    }

    public ComplexStorage getComplexStorage() {
        return complexStorage;
    }

    public StoredUser getUser(long id, long localUser)
    {
        StoredUser user = complexStorage.userSelect(id);
        if(user == null)
        {
            user = new StoredUser();
            user.setId(id);
            user.setUsername("#" + id);
            complexStorage.userInsert(user);
        }
        if(id == localUser && user.getUsername().equals("#" + id)) user.setUsername("You");
        return user;
    }

    public StoredConversation getConversation(long id)
    {
        StoredConversation conversation = complexStorage.conversationSelect(id);
        if(conversation == null)
        {
            conversation = new StoredConversation();
            conversation.setId(id);
            conversation.setBlocked(false);
            conversation.setSilent(0);
            conversation.setTitle("Conversation #" + id);
            complexStorage.conversationInsert(conversation);
        }
        return conversation;
    }
}
