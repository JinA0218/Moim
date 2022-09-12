package com.example.moim

import android.content.Context
import androidx.room.*

@Entity(tableName="liked_party")
data class LikedParty(
    @PrimaryKey
    val id: Int,
)

@Entity(tableName="my_party")
data class MyParty(
    @PrimaryKey
    val id: Int,
)

@Dao
interface LikedDao {
    @Insert
    fun insert(data: LikedParty)

    @Update
    fun update(data: LikedParty)

    @Delete
    fun delete(data: LikedParty)

    @Query("SELECT id FROM liked_party WHERE id=:id")
    fun getId(id: Int): Int?

    @Query("DELETE FROM liked_party")
    fun reset()
}

@Dao
interface MyDao {
    @Insert
    fun insert(data: MyParty)

    @Update
    fun update(data: MyParty)

    @Delete
    fun delete(data: MyParty)

    @Query("SELECT id FROM my_party WHERE id=:id")
    fun getId(id: Int): Int?

    @Query("DELETE FROM my_party")
    fun reset()
}


@Database(entities=[LikedParty::class, MyParty::class], version=1)
abstract class AppDB: RoomDatabase() {
    abstract fun LDao(): LikedDao
    abstract fun MDao(): MyDao

    companion object {
        private var instance: AppDB? = null

        @Synchronized
        fun getInstance(context: Context): AppDB? {
            if (instance == null) {
                synchronized(AppDB::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        "likedDB"
                    ).build()
                }
            }
            return instance
        }
    }
}