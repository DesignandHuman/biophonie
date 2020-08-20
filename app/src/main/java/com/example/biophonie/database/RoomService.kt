package com.example.biophonie.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SoundDao {
    @Query("select * from databasenewsound")
    fun getSounds(): LiveData<List<DatabaseNewSound>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sound: DatabaseNewSound)
}

@Database(entities = [DatabaseNewSound::class], version = 1, exportSchema = false)
abstract class NewSoundDatabase : RoomDatabase() {

    abstract val sleepDatabaseDao: SoundDao

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