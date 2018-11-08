package computer.schroeder.talk.util;

import android.arch.persistence.room.Room;
import android.content.Context;

import computer.schroeder.talk.storage.ComplexStorage;
import computer.schroeder.talk.storage.Database;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredUser;

public class ComplexStorageWrapper
{
    private ComplexStorage complexStorage;

    /**
     * Creates a new complex storage object
     * @param context the context of the complex storage
     */
    public ComplexStorageWrapper(Context context)
    {
        this.complexStorage = Room.databaseBuilder(context,
                        Database.class, "chat").build().getDatabase();
    }

    /**
     * Returns the raw complex storage used to access the internal database
     * @return the complex storage
     */
    public ComplexStorage getComplexStorage() {
        return complexStorage;
    }

    /**
     * Returns a user, and creates one if the user does not exists
     * Nice to know: if the id is equal to the local user, the name is changed to "You"
     * @param id the requested id
     * @param localUserId
     * @return
     */
    public StoredUser getUser(String id, String localUserId)
    {
        StoredUser user = complexStorage.userSelect(id);
        if(user == null)
        {
            user = new StoredUser();
            user.setId(id);
            user.setUsername("#" + id);
            complexStorage.userInsert(user);
        }
        if(id == localUserId && user.getUsername().equals("#" + id)) user.setUsername("You");
        return user;
    }

    public StoredConversation getConversation(String id)
    {
        return getConversation(id, null);
    }

    /**
     * Returns a conversation, and creates one if there is non with the given id
     * @param id of the conversation
     * @return a stored conversation
     */
    public StoredConversation getConversation(String id, String type)
    {
        StoredConversation conversation = complexStorage.conversationSelect(id);
        if(conversation == null)
        {
            conversation = new StoredConversation();
            conversation.setId(id);
            conversation.setBlocked(false);
            conversation.setSilent(false);
            conversation.setType(type);
            if(type != null && type.equals("GROUP")) conversation.setTitle("Group #" + id);
            if(type != null && type.equals("DIALOG")) conversation.setTitle("Dialog #" + id);
            else conversation.setTitle("Group/Dialog #" + id);
            complexStorage.conversationInsert(conversation);
        }
        return conversation;
    }
}
