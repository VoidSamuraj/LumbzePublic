package com.voidsamuraj.lumbze.db

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.*

@Keep
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user:User):Long
/*
    @Query("SELECT * FROM users ORDER BY points LIMIT 100")
    fun getTopUsers():LiveData<ArrayList<User>>
*/
    @Query("SELECT * FROM users WHERE id= :userId")
    fun getUser(userId:String):User?
/*
    @Query("SELECT * FROM users")
    fun getUsers():List<User>?*/

    @Query("DELETE FROM users WHERE id=:userId")
    fun deleteUser(userId:String)
/*
    @Query("SELECT * FROM users WHERE id = :userId UNION ALL (SELECT * FROM users WHERE points < (SELECT points FROM users where id = :userId) ORDER BY points DESC LIMIT 5)" +
            "UNION ALL (SELECT * FROM users WHERE points > (SELECT points FROM users where id = :userId) ORDER BY points DESC LIMIT 5) ORDER BY points")*/
   // @Query( "SELECT * FROM((SELECT * FROM users WHERE (points<(SELECT points FROM users WHERE id = :userId))  UNION SELECT * FROM users WHERE (points<(SELECT points FROM users WHERE id = :userId)) ORDER BY points LIMIT 5)) ")
  /*
    @Query( "SELECT * FROM (select *from users where id <> :userId order by abs(points - (select points from users where id = :userId)) limit 10) ORDER BY points")
    fun getNearUsers(userId: String):LiveData<ArrayList<User>>
    */
}