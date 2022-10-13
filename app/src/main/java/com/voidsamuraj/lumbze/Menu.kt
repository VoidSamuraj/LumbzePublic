package com.voidsamuraj.lumbze

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.voidsamuraj.lumbze.ui.theme.LumbzeTheme
import com.voidsamuraj.lumbze.ui.theme.mazeFont


@Composable
fun DrawMainMenu(firstDestination:String="", secondDestination:String="", navigator:NavController?=null, loginCode:()->Unit,mazeViewModel: MazeViewModel){
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Start Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ){

            Spacer(modifier = Modifier.weight(2f))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.h1,
                fontFamily = mazeFont,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.menuFrontColor)

            )

            Spacer(modifier = Modifier.weight(2f))
            val modifier= Modifier
                .fillMaxWidth(0.6f)
                .height(dimensionResource(id = R.dimen.button_height))
            DrawMenuButton(
                onClick = {navigator?.navigate(firstDestination)
                },
                text = stringResource(id = R.string.play),
                modifier = modifier
            )
            Spacer(modifier = Modifier.weight(1f))

            Column {
                val contextForMessage= LocalContext.current
                DrawMenuButton(
                    onClick = {
                        if(Firebase.auth.currentUser!=null)
                        navigator?.navigate(secondDestination)
                        else
                            Toast.makeText(contextForMessage, R.string.please_login,Toast.LENGTH_SHORT).show()
                              },
                    text = stringResource(id = R.string.stats),
                    modifier = modifier

                )
                Row(modifier= Modifier
                    .fillMaxWidth(0.6f)
                    .height(dimensionResource(id = R.dimen.button_height)),
                verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(
                        id = if(mazeViewModel.isUserSignedIn.value)
                            R.string.you_are_signed
                        else
                            R.string.you_are_not_signed),
                        Modifier.weight(2f),//.wrapContentHeight(align = Alignment.CenterVertically),
                        textAlign = TextAlign.Center,
                        fontFamily = mazeFont,
                        color = colorResource(id = R.color.menuFrontColor)
                    )
                    Button(
                        onClick = {
                            loginCode()
                           // mazeViewModel.repository?.deleteRoom()
                        },
                        modifier = Modifier
                            //.align(alignment = Alignment.End)
                            .padding(0.dp, 10.dp, 0.dp, 0.dp)
                            .weight(1f),
                        shape = CutCornerShape(dimensionResource(id = R.dimen.square_button_clip)),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.menuFrontColor))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Settings"
                        )

                    }
                }
            }


            Spacer(modifier = Modifier.weight(2f))
        }
    }

}
@Composable
fun DrawMenuButton(text:String, modifier: Modifier=Modifier, textColor: Color =Color.White, onClick:()->Unit){

    Button(onClick = { onClick()},
        modifier = modifier,
        shape = CutCornerShape(dimensionResource(id = R.dimen.square_button_clip)),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.menuFrontColor))
    ) {
            Text(text = text,
                fontFamily = mazeFont,
            style = MaterialTheme.typography.h4,
            color = textColor)
    }
}
@Preview(showBackground = true)
@Composable
fun MenuPreview() {
    LumbzeTheme() {
        //DrawMainMenu()
    }
}