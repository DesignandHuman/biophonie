package com.example.biophonie.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.biophonie.domain.Sound

@Dao
interface SoundDao {
    @Query("select * from databasenewsound")
    fun getNewSounds(): LiveData<List<DatabaseNewSound>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newSound: Sound)
}

@Database(entities = [Sound::class], version = 1, exportSchema = false)
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