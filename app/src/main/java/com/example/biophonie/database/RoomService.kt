package com.example.biophonie.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SoundDao {
    @Query("select * from databasenewsound")
    fun getNewSoundsAsLiveData(): LiveData<List<DatabaseNewSound>>

    @Query("select * from databasenewsound")
    fun getNewSounds(): List<DatabaseNewSound>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newSound: DatabaseNewSound)

    @Delete
    fun delete(newSound: DatabaseNewSound)

    @Query("select * from databasenewsound where id like :id")
    fun getNewSound(id: String): DatabaseNewSound?
}

@Database(entities = [DatabaseNewSound::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NewSoundDatabase : RoomDatabase() {

    abstract val soundDao: SoundDao

    companion object {

        @Volatile
        private var INSTANCE: NewSoundDatabase? = null

        fun getInstance(context: Context): NewSoundDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NewSoundDatabase::class.java,
                        "new_sound_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}