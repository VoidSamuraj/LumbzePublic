package com.voidsamuraj.lumbze.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
* TODO
*  add EXECUTOR
* */
class LumbzeRepositoryImplementation(
    private val roomUserDao: UserDao
):LumbzeRepository {
    private var _firebaseDAO: UsersFirebaseDAO?=null

    override suspend fun addUser(user:User){
        withContext(Dispatchers.IO) {
            if (user.name != "") {
                addUserToRoom(user)
                _firebaseDAO!!.add(user)
            }
        }
    }
    fun addUserToRoom(user:User){
        roomUserDao.addUser(user)
    }

    /**
     * adding online database
     * */
    override suspend fun setFirebase(usersFirebaseDAO: UsersFirebaseDAO){
        _firebaseDAO = usersFirebaseDAO
        _firebaseDAO!!.addListeners()
    }
    override suspend fun closeDatabases(){
        _firebaseDAO?.removeListeners()
        UsersDatabase.closeDatabase()
    }
    override suspend fun getFirst50AndUser(uId:String): List<Pair<Int,User>>? {
        var userPos:Int=-1
        var index=1
        val takeFirst=20
        val usersAround=10
        _firebaseDAO!!.dataFlow?.collect{
            if (it.id==uId) {
                userPos = index
            }
            ++index
        }
        index=1
        if(userPos!=-1){
            val users:ArrayList<Pair<Int,User>> = arrayListOf()
            val first=(if(userPos>takeFirst+usersAround)takeFirst else takeFirst+usersAround)
            _firebaseDAO!!.dataFlow?.collect{
                if(index<=first)
                    users.add(Pair(index,it))
                if(first==takeFirst&&index>userPos-usersAround&&index<userPos+usersAround)
                    users.add(Pair(index,it))
                ++index

            }
            return users
        }

        return null
    }

    override suspend fun getUser(id: String):User?  {
        getUserFromRoom(id)?.let { return it }

        getUserFromFirebase(id)?.let {
            roomUserDao.addUser(it)
            return it
        }

        return null
    }


    fun synchronizeDatabase(id: String){
        if(_firebaseDAO!=null)
            CoroutineScope(Dispatchers.IO).launch {
                val roomUser = getUserFromRoom("2137")
                val firebaseUser = getUserFromFirebase(id)
                if (roomUser != null) {
                    //compare users if not null

                    roomUserDao.deleteUser("2137")

                    var user =
                        if (firebaseUser != null)
                            User(id, firebaseUser.name, firebaseUser.points + roomUser.points)
                        else
                            User(id, roomUser.name, roomUser.points)
                    //second line of checking
                    _firebaseDAO!!.addIfNotExists(user)?.let { firebasePrevUser->
                        if(firebaseUser!=null){
                            if(firebasePrevUser.points>firebaseUser.points)
                                user= User(id, firebasePrevUser.name, firebasePrevUser.points + roomUser.points)
                            _firebaseDAO!!.add(user)

                        }
                    }
                    roomUserDao.addUser(user)

                } else if (firebaseUser != null) {
                    //first time user logged
                    roomUserDao.addUser(firebaseUser)
                }
            }
    }

    private suspend fun getUserFromFirebase(id: String):User?{
        var userRet:User?=null
        _firebaseDAO!!.dataFlow?.filter {user-> id==user.id }?.collect{ userRet=it }
        return userRet
    }
    private fun getUserFromRoom(id: String):User?{
        return roomUserDao.getUser(id)

    }

}
