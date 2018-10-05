package computer.schroeder.talk.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredSendable;
import computer.schroeder.talk.storage.entities.StoredUser;

@Dao
public interface ComplexStorage
{
    // USER


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void userInsert(StoredUser user);

    @Query("SELECT * FROM storeduser WHERE id = :id")
    StoredUser userSelect(long id);

    @Query("SELECT * FROM storedconversation")
    List<StoredConversation> conversationSelect();

    @Query("SELECT * FROM storedconversation WHERE id = :id")
    StoredConversation conversationSelect(long id);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation ORDER BY time DESC LIMIT 1")
    StoredSendable messageSelectLastMessageByConversation(long conversation);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation ORDER BY time ASC")
    List<StoredSendable> messageSelectConversation(long conversation);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation and read = 0")
    List<StoredSendable> messageSelectUnreadConversation(long conversation);

    @Query("SELECT * FROM storedsendable WHERE read = 0 ORDER BY time DESC")
    List<StoredSendable> messageSelectUnread();

    //@Query("SELECT * FROM WHERE id = :id")
    //StoredMessage messageSelect(long id);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void conversationInsert(StoredConversation conversation);

    @Insert
    long messageInsert(StoredSendable message);

    @Update
    void messageUpdate(StoredSendable message);

    @Delete
    void messageDelete(StoredSendable... messages);

    @Delete
    void conversationDelete(StoredConversation... conversations);
    // MESSAGE


    /*@Insert
    void messageCreate(Message... messages);

    //@Query("SELECT * FROM message WHERE messageID = :id")
    //Message messageLoad(int id);

    @Query("SELECT * FROM message WHERE conversation = :conversation")
    List<Message> messageLoadByConversation(int conversation);

    @Query("SELECT * FROM message WHERE conversation = :conversation AND read = 0")
    List<Message> messageLoadUnreadByConversation(int conversation);

    @Query("SELECT * FROM message WHERE conversation = :conversation ORDER BY time DESC LIMIT 1")
    Message messageLoadLastMessageByConversation(int conversation);

    @Update
    void messageSave(Message... messages);

    @Delete
    void messageDelete(Message... messages);

    // Conversation


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveConversation(Conversation... conversations);

    //@Query("SELECT * FROM conversation")
    //List<Conversation> conversationLoad();

    @Query("SELECT * FROM conversation where conversationID = :conversationID")
    Conversation conversationLoad(int conversationID);

    //@Query("SELECT * FROM conversation JOIN user ON user.userID = conversation.conversationID")
    //List<ConversationWithUserAndMessages> conversationLoadWithUserAndMessages();

    @Query("SELECT conversation.*, user.* FROM conversation JOIN user ON user.userID = conversation.conversationID WHERE conversation.conversationID = :conversation")
    ConversationWithUserAndMessages conversationLoadWithUserAndMessages(int conversation);

    @Query("SELECT conversation.*, user.* FROM conversation JOIN user ON user.userID = conversation.conversationID")
    List<ConversationWithUser> conversationLoadWithUser();

    @Delete
    void conversationDelete(Conversation... conversations);*/
}
