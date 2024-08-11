package com.example.noteapp3.models

import android.content.Context
import android.provider.ContactsContract
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.noteapp3.models.Comment
import com.example.noteapp3.notificationinapp.MessageNotification
import com.example.noteapp3.polls.Poll
import retrofit2.http.PUT

@Database(entities = [profile::class, Comment::class, Post::class,Poll::class,MessageNotification::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commentDao(): CommentDao
    abstract fun postDao(): PostDao
    abstract fun pollDao(): PollDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE message_notifications (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, senderPicture TEXT NOT NULL, senderId TEXT NOT NULL, senderName TEXT NOT NULL, messageContent TEXT NOT NULL)")
            }
        }


        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app-database"
                    )

                        .addMigrations(MIGRATION_1_2)
                        .build()

                INSTANCE = instance
                instance
            }
        }
    }
}



@Dao
interface CommentDao {
    @Query("SELECT * FROM comments")
    fun getAllComments(): List<Comment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(comments: List<Comment>)

    @Query("DELETE FROM comments WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)

    @Query("SELECT * FROM comments WHERE id IN (:commentIds)")
    fun getCommentsByIds(commentIds: List<String>): List<Comment>

    @Insert
    fun addComment(comment: Comment)
}



@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<Post>)


    @Delete
    fun deletePost(post: Post)

    @Update
    fun updatePost(post:Post)

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Post?

    @Query("DELETE FROM posts WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)

    @Insert
    fun addPost(post: Post)

}

@Dao
interface PollDao {

    @Query("SELECT * FROM poll")
    fun getAllPolls():List<Poll>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<Poll>)

    @Delete
    fun deletePoll(poll: Poll)

    @Update
    fun updatePoll(poll:Poll)

    @Query("SELECT * FROM poll WHERE id = :pollId")
    fun getPollById(pollId: String): Poll?

    @Query("DELETE FROM poll WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)

    @Insert
    fun addPoll(poll: Poll)

}

@Dao
interface NotificationDao{
    @Insert
    suspend fun insert(notification: MessageNotification)

    @Query("SELECT * FROM message_notifications")
    suspend fun getAllNotifications(): List<MessageNotification>

    @Query("DELETE FROM message_notifications")
    suspend fun deleteAllNotifications()

}
