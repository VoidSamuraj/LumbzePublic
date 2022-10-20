package com.voidsamuraj.lumbze

import android.animation.ValueAnimator
import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.rewarded.RewardedAd
import com.voidsamuraj.lumbze.db.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MazeViewModel():ViewModel() {
    //////////USER LOGIC
    val maze: Maze by lazy { Maze {
        CoroutineScope(Dispatchers.IO).launch {
            addPoints()

        }
    }
    }


    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    init {
        viewModelScope.launch()  {
            delay(1000L)
            _isLoading.value = false
        }
    }
    var mRewardedAd: RewardedAd? = null

    //Displayed in stats card
    private val userStats:MutableState<List<Pair<Int,User>>?> = mutableStateOf(null)
    val isUserSignedIn= mutableStateOf(false)
    val currentUserName:MutableState<String> = mutableStateOf("")
    var sharedPrefs:SharedPreferences?=null
    var repository:LumbzeRepositoryImplementation?=null


    private suspend fun addPoints(){
        val id= sharedPrefs?.getString("uId","")
        id?.let {
            if (it!=""&&isUserSignedIn.value){
                var user = repository!!.getUser(it)
                if (user == null)
                    user = User(it, "Name", 0)
                user.let {
                    repository!!.addUser(
                        User(
                            id = it.id,
                            name = it.name,
                            points = it.points + maze.allCells
                        )
                    )
                }
            }else{
                // if not logged create temporary user in local db, and delete this data when log in
                  val  user = User("2137", "Name", 0)
                user.let {
                    repository!!.addUserToRoom(
                        User(
                            id = it.id,
                            name = it.name,
                            points = it.points + maze.allCells
                        )
                    )

                }

            }
        }
    }

    fun saveIdLocally(uId:String?){
        uId?.let { sharedPrefs?.edit()!!.putString("uId",it).apply()}
    }

    fun getUserNameFromDB():String?{
        var name:String?=null
        CoroutineScope(Dispatchers.IO).launch {
            val id= sharedPrefs?.getString("uId","")
            id?.let {
                if (it!="")
                    repository?.getUser(it)?.name?.let {
                        currentUserName.value =it
                        name=it
                    }
            }
        }
        return name
    }
    fun editCurrentUserName(userName:String){
        CoroutineScope(Dispatchers.IO).launch {
            val id= sharedPrefs?.getString("uId","")
            id?.let {
                if (it!="") {
                    val user = repository?.getUser(it)
                    user?.let {
                        repository!!.addUser(
                            User(
                                id = it.id,
                                name = userName,
                                points = it.points
                            )
                        )
                    }
                }
            }
            currentUserName.value=userName
        }
    }
    fun getUsersStats():State<List<Pair<Int,User>>?>{
        CoroutineScope(Dispatchers.IO).launch {
            sharedPrefs?.let {
                userStats.value=repository?.getFirst50AndUser(it.getString("uId","")!!)
            }
        }
        return userStats
    }


    /**
     * VERY IMPORTANT TO CALL IT BEFORE  setFirebaseRepository
     */
    fun setRepositoryAndSharedPreferences(application: Application){
        val usersDao=UsersDatabase.getDatabase(application).userDao()
        repository=LumbzeRepositoryImplementation(
            roomUserDao =usersDao
        )
        sharedPrefs = application.getSharedPreferences("sharedPreferences",
            ComponentActivity.MODE_PRIVATE
        )
        _rowsAmount.value=sharedPrefs!!.getInt("rows_amount",10)
        _cellSize.value=sharedPrefs!!.getInt("cell_size",40)
    }

    /**
     * NEED TO CALL SET REPOSITORY BEFORE!!!!
     */
    fun setFirebaseInRepository(usersFirebaseDAO: UsersFirebaseDAO){
        CoroutineScope(Dispatchers.IO).launch {
            repository!!.setFirebase(usersFirebaseDAO = usersFirebaseDAO)
        }
    }
    fun detachFirebaseListener(){
        CoroutineScope(Dispatchers.IO).launch {
            repository?.closeDatabases()
        }
    }



    //////////APP LOGIC

    private val  _isDrawerOpen: MutableState<Boolean> = mutableStateOf(false)
    val isDrawerOpen:State<Boolean> = _isDrawerOpen
    fun setDrawerOpen(boolean: Boolean){_isDrawerOpen.value=boolean}

    private val  _isScreenTouchable: MutableState<Boolean> = mutableStateOf(true)
    val isScreenTouchable:State<Boolean> = _isScreenTouchable
    fun setIsScreenTouchable(boolean: Boolean){_isScreenTouchable.value=boolean}

    private val  _rotation:MutableState<Float> = mutableStateOf(0.0f)
    val rotation:State<Float> =_rotation
    fun setRotation(angle:Float){_rotation.value=angle}

    private val  _translation:MutableState<Float> = mutableStateOf(0.0f)
    val translation:State<Float> =_translation
    fun setTranslation(move:Float){_translation.value=move}

    private var _rowsAmount:MutableState<Int> = mutableStateOf(15)
    val rowsAmount:State<Int> = _rowsAmount
    fun setRowsAmount(amount: Int){
        _rowsAmount.value=amount
        sharedPrefs?.edit()?.putInt("rows_amount",amount)?.apply()
        resetPositionAndRotation()
    }

    /**
     * just visually
     */
    fun resetPositionAndRotation(){
        val rot = ValueAnimator.ofFloat( rotation.value,0f)
        rot.duration = 100 //in millis
        rot.addUpdateListener { animation ->
            MainScope().launch {
                setRotation(animation.animatedValue as Float)
            }
        }
        rot.start()

        val ballPos:Pair<Float,Float> = maze.getBallPosition()
        val va = ValueAnimator.ofFloat( translation.value,ballPos.first)
        va.duration = 100 //in millis
        va.addUpdateListener { animation ->
            MainScope().launch {
                setTranslation(animation.animatedValue as Float)
            }
        }
        va.start()
    }

    private val  _cellSize:MutableState<Int> = mutableStateOf(40)
    val cellSize:State<Int> =_cellSize
    fun setCellSize(size:Int){
        _cellSize.value=size
        sharedPrefs?.edit()?.putInt("cell_size",size)?.apply()
    }

    private val _mazeBitmap:MutableState<Bitmap?> = mutableStateOf(null)
    val mazeBitmap:State<Bitmap?> = _mazeBitmap
    fun setMazeBitmap(bitmap: Bitmap){_mazeBitmap.value=bitmap}

    private val _ballBitmap:MutableState<Bitmap?> = mutableStateOf(null)
    val ballBitmap:State<Bitmap?> = _ballBitmap
    fun setBallBitmap(bitmap: Bitmap){_ballBitmap.value=bitmap}


}