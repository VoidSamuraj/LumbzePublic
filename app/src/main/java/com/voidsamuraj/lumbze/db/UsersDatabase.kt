package com.voidsamuraj.lumbze.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class],
    version = 1
)
abstract class UsersDatabase:RoomDatabase() {
    abstract fun userDao():UserDao
    companion object{
        @Volatile
        private var INSTANCE:UsersDatabase?=null

        fun getDatabase(context:Context):UsersDatabase{
            val tempInstance= INSTANCE
            tempInstance?.let {
                return it
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UsersDatabase::class.java,
                    "users"
                ).build()
                INSTANCE=instance
                return instance

            }
        }
        fun closeDatabase(){
            INSTANCE?.close()
        }
    }
}