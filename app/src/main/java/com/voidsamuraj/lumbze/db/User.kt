package com.voidsamuraj.lumbze.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id:String,
    @ColumnInfo(name = "name")val name:String,
    @ColumnInfo(name = "points")val points:Int
    )
