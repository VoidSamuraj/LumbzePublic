package com.voidsamuraj.lumbze.db

interface LumbzeRepository {
    suspend fun addUser(user:User)
    suspend fun getUser(id:String):User?
    suspend fun setFirebase(usersFirebaseDAO: UsersFirebaseDAO)
    suspend fun getFirst50AndUser(uId:String):List<Pair<Int,User>>?
    suspend fun closeDatabases()

}