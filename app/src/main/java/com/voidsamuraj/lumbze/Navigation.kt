package com.voidsamuraj.lumbze

import android.content.res.Resources
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
@Composable
fun Navigation(mazeViewModel:MazeViewModel, navController: NavHostController, width:Int, auth:MyAuthentication, resources:Resources){

    val duration=300
    val slideIn= slideInHorizontally(
        initialOffsetX = {width},
        animationSpec = tween(
            durationMillis = duration
        )
    )
    val slideOut= slideOutHorizontally (
        targetOffsetX = {-width},
        animationSpec = tween(
            durationMillis = duration
        )
    )
    val popIn= slideOutHorizontally (
        targetOffsetX = {width},
        animationSpec = tween(
            durationMillis = duration
        )
    )
    val popOut= slideInHorizontally(
        initialOffsetX = {-width},
        animationSpec = tween(
            durationMillis = duration
        )
    )

    AnimatedNavHost(navController = navController, startDestination = Screen.StartScreen.route ){


        composable(route= Screen.StartScreen.route,
            enterTransition ={slideIn},
            exitTransition = {slideOut},
            popEnterTransition = {popOut},
            popExitTransition = {popIn}
        ){

            DrawMainMenu(
                firstDestination = Screen.MainScreen.route,
                secondDestination = Screen.StatsScreen.route,
                navigator = navController,
                mazeViewModel = mazeViewModel,
                loginCode = {

                    auth.login()
                })
        }


        composable(route= Screen.MainScreen.route,
            enterTransition ={slideIn},
            exitTransition = {slideOut},
            popEnterTransition = {popOut},
            popExitTransition = {popIn}){

            DrawMaze(mazeViewModel = mazeViewModel, resources) {
                navController.navigate(Screen.StatsScreen.route)
            }
            val contextForMessage= LocalContext.current
            DrawDrawer(mazeViewModel = mazeViewModel,
                logout = {
                    auth.signOut()
                    mazeViewModel.isUserSignedIn.value=false
                }, navigate = {
                    if(mazeViewModel.isUserSignedIn.value && auth.getUser()!=null)
                        navController.navigate(Screen.StatsScreen.route)
                    else
                        Toast.makeText(contextForMessage, R.string.please_login, Toast.LENGTH_SHORT).show()

                })
            BackHandler {
                if (mazeViewModel.isDrawerOpen.value)
                    mazeViewModel.setDrawerOpen(false)
                else
                    navController.navigateUp()
            }
        }

        composable(route= Screen.StatsScreen.route,
            enterTransition ={slideIn},
            exitTransition = {slideOut},
            popEnterTransition = {popOut},
            popExitTransition = {popIn}){
            DrawStatScreen(mazeViewModel = mazeViewModel)


        }
    }

}