package com.voidsamuraj.lumbze


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.voidsamuraj.lumbze.db.UsersFirebaseDAO
import com.voidsamuraj.lumbze.ui.theme.LumbzeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private  val mazeViewModel: MazeViewModel by viewModels()
    private var width:Int=0
    private lateinit var auth:MyAuthentication

    @SuppressLint("SourceLockedOrientationActivity")
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                mazeViewModel.isLoading.value
            }
        }
        super.onCreate(savedInstanceState)
        auth=MyAuthentication(getString(R.string.firebase_auth_key),this)
        this.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            LumbzeTheme() {
                initAdd(stringResource(id = R.string.game_services_add_identity_id_test),LocalContext.current)

                width=with(LocalDensity.current){   LocalConfiguration.current.screenWidthDp.dp.toPx()}.toInt()
                mazeViewModel.maze.apply {
                    postData(
                        cellSize = mazeViewModel.cellSize,
                        canvasSideSize = width,
                        updateCellSize =mazeViewModel::setCellSize,
                        mazeViewModel=mazeViewModel,
                        screenWidth =with(LocalDensity.current){   LocalConfiguration.current.screenWidthDp.dp.toPx()}.toInt()
                    )

                    createMaze(mazeViewModel.rowsAmount.value)
                    mazeViewModel.setMazeBitmap(getMaze())
                    mazeViewModel.setBallBitmap(getBall())
                }
                Navigation(mazeViewModel = mazeViewModel,
                    navController = rememberAnimatedNavController( ),
                    width = width,
                    auth = auth,
                    resources = resources)
            }
        }
        MobileAds.initialize(this)
    }

    override fun onStart() {
        super.onStart()
        mazeViewModel.setRepositoryAndSharedPreferences(application)
        mazeViewModel.isUserSignedIn.value=false

        auth.firebaseOnStartSetup(){
            CoroutineScope(Dispatchers.Default).launch {
                mazeViewModel.setFirebaseInRepository(
                    usersFirebaseDAO = UsersFirebaseDAO()
                )
                mazeViewModel.saveIdLocally(auth.getUser()?.uid)
            }
            mazeViewModel.isUserSignedIn.value=true
        }
    }

    override fun onDestroy() {
        mazeViewModel.detachFirebaseListener()
        super.onDestroy()
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        auth.onFirebaseResult(requestCode,data){

            CoroutineScope(Dispatchers.Default).launch {
                mazeViewModel.setRepositoryAndSharedPreferences(application)
                mazeViewModel.setFirebaseInRepository(
                    usersFirebaseDAO = UsersFirebaseDAO()
                )
                mazeViewModel.saveIdLocally(auth.getUser()?.uid)
                mazeViewModel.isUserSignedIn.value=true
                delay(1000)
                auth.getUser()?.let {
                    mazeViewModel.repository?.synchronizeDatabase(it.uid)
                }
            }

        }
    }
    fun initAdd(addId:String,context: Context/*, onSuccess:()->Unit*/){
        val adRequest: AdRequest = AdRequest.Builder().build()
        RewardedAd.load(context,
            addId,
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("ADDS", loadAdError.toString())
                    mazeViewModel.mRewardedAd=null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d("ADDS", "Ad was loaded.")
                    mazeViewModel.mRewardedAd=rewardedAd
                    //  onSuccess()
                }
            })
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LumbzeTheme() {
    }
}
