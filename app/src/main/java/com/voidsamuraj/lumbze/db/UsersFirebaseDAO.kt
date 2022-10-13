package com.voidsamuraj.lumbze.db

import androidx.annotation.Keep
import com.google.firebase.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Keep
class UsersFirebaseDAO() {
    var data:ArrayList<User>  = arrayListOf()
    var dataFlow: Flow<User>?= null
    private var dbReference: DatabaseReference
    private  var vel: ValueEventListener
    init {
        val db=FirebaseDatabase.getInstance()
        dbReference=db.reference
        vel=object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    data = arrayListOf()
                    for (user in snapshot.children){
                        if(user.child("name").value!=null&&user.child("points").value!=null)
                            data.add(User(
                                user.key!!,
                                user.child("name").value.toString(),
                                (user.child("points").value as Long).toInt()
                            ))}

                    dataFlow= flow {
                        data.sortedByDescending { user: User -> user.points }.forEach {
                            emit(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }


    fun add(user:User) {
        val ref = dbReference.child(user.id)
        ref.child("name").setValue(user.name)
        ref.child("points").setValue(user.points)
        ref.push()

    }
    /**
    * @return User if id exist in db, else null
    * */
    fun addIfNotExists(user:User):User?{
        val ref = dbReference.child(user.id)
        data.forEach {
            if(it.id==user.id)
                return it
        }
        ref.child("name").setValue(user.name)
        ref.child("points").setValue(user.points)
        ref.push()
        return null
    }
    fun addListeners() {
        dbReference.addValueEventListener(vel)
    }

    fun removeListeners() {
        dbReference.removeEventListener(vel)
    }
}