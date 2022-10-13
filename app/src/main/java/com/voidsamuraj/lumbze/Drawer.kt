package com.voidsamuraj.lumbze

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voidsamuraj.lumbze.ui.theme.LumbzeTheme
import com.voidsamuraj.lumbze.ui.theme.mazeFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun DrawDrawer(mazeViewModel: MazeViewModel, navigate:()->Unit, logout:()->Unit){

    val drawerVisibility = remember{ mutableStateOf(if(mazeViewModel.isDrawerOpen.value)100f else 0f)}
    val sWidth = LocalConfiguration.current.screenWidthDp

    Row(modifier = Modifier
        .absoluteOffset(
            x =
            animateDpAsState(
                if (mazeViewModel.isDrawerOpen.value) 0.dp else (-sWidth - 1).dp,
                animationSpec = tween(
                    easing = LinearEasing,
                    durationMillis = integerResource(id = R.integer.drawer_duration),
                    delayMillis = integerResource(id = R.integer.drawer_delay)
                )
            ).value
        )
        .alpha(drawerVisibility.value)

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight()
                .background(
                    color = colorResource(id = R.color.trunk)
                )
                .padding(10.dp)
        ) {
            DrawDrawerContent(mazeViewModel = mazeViewModel,navigate=navigate,logout)

        }

    }
    IconButton(
        onClick = {
            if(!mazeViewModel.isDrawerOpen.value)
                drawerVisibility.value=100f
            mazeViewModel.setDrawerOpen(!mazeViewModel.isDrawerOpen.value)
        }, modifier = Modifier
            .layoutId("button_settings")
            .absoluteOffset(
                x = animateDpAsState(
                    if (!mazeViewModel.isDrawerOpen.value) 0.dp else (sWidth - 48).dp - 40.dp,
                    animationSpec = tween(
                        easing = LinearEasing,
                        durationMillis = integerResource(id = R.integer.drawer_duration),
                        delayMillis = integerResource(id = R.integer.drawer_delay)
                    )
                ).value

            )
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_settings_24),
            colorFilter = ColorFilter.tint(
                animateColorAsState(
                    targetValue = if(mazeViewModel.isDrawerOpen.value) colorResource(id = R.color.green) else colorResource(id = R.color.trunk2),
                    animationSpec = tween(
                        easing = LinearEasing,
                        durationMillis = integerResource(id = R.integer.drawer_duration),
                        delayMillis = integerResource(id = R.integer.drawer_delay)
                    )
                ).value
            ),
            contentDescription = "Settings",
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.icon_size))
                .height(dimensionResource(id = R.dimen.icon_size))

        )

    }
}

@Composable
fun DrawDrawerContent(mazeViewModel: MazeViewModel, navigate: () -> Unit, logout:()->Unit) {
    mazeViewModel.getUserNameFromDB()
    //val name:String?=
    val isDialogShown: MutableState<Boolean> = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (mazeViewModel.currentUserName.value != "") {
            Box(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = mazeViewModel.currentUserName.value,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(5.dp, 0.dp)
                        .background(colorResource(id = R.color.trunk1), CircleShape),
                    fontFamily = mazeFont,
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center

                    //      .align(Alignment.Center)
                )

                IconButton(
                    onClick = {
                        isDialogShown.value = true

                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = colorResource(id = R.color.menuFrontColor),
                            shape = CircleShape
                        )
                        .width(24.dp)
                        .height(24.dp)
                        .padding(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                        contentDescription = "Edit"
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(60.dp))
        }

        DrawSliderWithText(
            state = mazeViewModel.cellSize,
            minValue = 10f,
            maxValue = 60f,
            setterFunction = mazeViewModel::setCellSize, stringResource(id = R.string.cellSize)
        )
        DrawSliderWithText(
            state = mazeViewModel.rowsAmount,
            minValue = 2f,
            maxValue = 20f,
            setterFunction = mazeViewModel::setRowsAmount, stringResource(id = R.string.rows)
        )

        Button(
            onClick = {
                navigate()
            },
            shape = CutCornerShape(dimensionResource(id = R.dimen.square_button_clip)),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(
                text = stringResource(id = R.string.stats),
                fontFamily = mazeFont
            )
        }
        Spacer(Modifier.weight(1f))
        if (mazeViewModel.isUserSignedIn.value)
            Button(
                onClick = {
                    logout()
                    mazeViewModel.saveIdLocally("")
                    mazeViewModel.isUserSignedIn.value = false
                    mazeViewModel.currentUserName.value=""
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(10.dp),
                shape = CutCornerShape(dimensionResource(id = R.dimen.square_button_clip)),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.menuFrontColor))
            ) {

                Text(
                    text = stringResource(id = R.string.signOut),
                    fontFamily = mazeFont,
                    style = MaterialTheme.typography.h6
                )

            }
    }
    if (isDialogShown.value)
        showDialog(isDialogShown,mazeViewModel)

}
@Composable
fun showDialog(isDialogShown: MutableState<Boolean>,mazeViewModel: MazeViewModel){
    val newName:MutableState<String> = remember {mutableStateOf(mazeViewModel.currentUserName.value) }
    AlertDialog(title = {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.set_username))
            Divider(modifier = Modifier.padding(bottom = 8.dp))
        }
    },
        onDismissRequest = { isDialogShown.value = false },
        confirmButton = {
            Button(onClick = {
                CoroutineScope(Dispatchers.Default)
                mazeViewModel.editCurrentUserName(newName.value)
              //  mazeViewModel.currentUserName.value = newName.value
                isDialogShown.value = false
            }) {
                Text(text = stringResource(id = R.string.change))
            }
        },
        dismissButton = {
            Button(onClick = { isDialogShown.value = false }) {
                Text(text = stringResource(id = R.string.close))
            }
        },
        text = {    Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            Spacer(modifier = Modifier.height(10.dp))
            BasicTextField(value =newName.value , onValueChange = { changed ->
                newName.value=changed
            } )
            Divider()

        } }
    )
}

@Composable
fun DrawSliderWithText(state: State<Int>, minValue:Float, maxValue:Float, setterFunction:(Int)->Unit, titleText:String){
    Box(modifier = Modifier.fillMaxWidth()){
        Text(text = titleText,
            modifier = Modifier.align(Alignment.TopStart),
            fontFamily = mazeFont,
            style = MaterialTheme.typography.h6)
        Text(text = state.value.toString(),
            modifier = Modifier.align(Alignment.Center),
            fontFamily = mazeFont,
            style = MaterialTheme.typography.h6)
    }
    Slider(value = state.value.toFloat(),
        onValueChange ={value->
            setterFunction(value.toInt())
        },
        steps = (maxValue-minValue-2).toInt(),
        valueRange = minValue..maxValue

    )
}
@Preview(showBackground = true)
@Composable
fun DrawerPreview() {
    LumbzeTheme() {
        //  DrawDrawerContent(mazeModel = mazeModel)
    }
}