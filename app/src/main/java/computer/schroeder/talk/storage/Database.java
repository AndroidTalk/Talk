package computer.schroeder.talk.storage;

import android.arch.persistence.room.RoomDatabase;

import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredSendable;
import computer.schroeder.talk.storage.entities.StoredUser;

@android.arch.persistence.room.Database(entities = {StoredConversation.class, StoredSendable.class, StoredUser.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase
{
    public abstract ComplexStorage getDatabase();
}
