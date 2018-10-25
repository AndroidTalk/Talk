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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void userInsert(StoredUser user);

    @Query("SELECT * FROM storeduser WHERE id = :id")
    StoredUser userSelect(String id);

    @Query("SELECT * FROM storedconversation")
    List<StoredConversation> conversationSelect();

    @Query("SELECT * FROM storedconversation WHERE id = :id")
    StoredConversation conversationSelect(String id);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation ORDER BY time DESC LIMIT 1")
    StoredSendable messageSelectLastMessageByConversation(String conversation);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation ORDER BY time ASC")
    List<StoredSendable> messageSelectConversation(String conversation);

    @Query("SELECT * FROM storedsendable WHERE conversation = :conversation and read = 0")
    List<StoredSendable> messageSelectUnreadConversation(String conversation);

    @Query("SELECT * FROM storedsendable WHERE read = 0 ORDER BY time DESC")
    List<StoredSendable> messageSelectUnread();

    @Query("SELECT * FROM storeduser")
    List<StoredUser> selectAllUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void conversationInsert(StoredConversation conversation);

    @Insert
    void messageInsert(StoredSendable message);

    @Update
    void messageUpdate(StoredSendable message);

    @Delete
    void messageDelete(StoredSendable... messages);

    @Delete
    void conversationDelete(StoredConversation... conversations);
}
