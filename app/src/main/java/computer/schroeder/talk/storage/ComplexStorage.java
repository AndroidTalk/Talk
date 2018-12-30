package computer.schroeder.talk.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;
import computer.schroeder.talk.storage.entities.StoredUser;

@Dao
public interface ComplexStorage
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void userInsert(StoredUser user);

    @Query("SELECT * FROM storeduser WHERE id = :id")
    StoredUser userSelect(String id);

    @Query("SELECT * FROM storedconversation")
    List<StoredConversation> conversationSelect();

    @Query("SELECT * FROM storedconversation WHERE id = :id")
    StoredConversation conversationSelect(String id);

    @Query("SELECT * FROM storedmessage WHERE conversation = :conversation ORDER BY time DESC LIMIT 1")
    StoredMessage messageSelectLastMessageByConversation(String conversation);

    @Query("SELECT * FROM storedmessage WHERE conversation = :conversation ORDER BY time ASC")
    List<StoredMessage> messageSelectConversation(String conversation);

    @Query("SELECT * FROM storedmessage WHERE conversation = :conversation and read = 0")
    List<StoredMessage> messageSelectUnreadConversation(String conversation);

    @Query("SELECT * FROM storedmessage WHERE read = 0 ORDER BY time DESC")
    List<StoredMessage> messageSelectUnread();

    @Query("SELECT * FROM storeduser ORDER BY id DESC")
    List<StoredUser> selectAllUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void conversationInsert(StoredConversation conversation);

    @Insert
    void messageInsert(StoredMessage message);

    @Update
    void messageUpdate(StoredMessage message);

    @Delete
    void messageDelete(StoredMessage... messages);

    @Delete
    void conversationDelete(StoredConversation... conversations);

    @Delete
    void delete(StoredUser... storedUsers);
}
