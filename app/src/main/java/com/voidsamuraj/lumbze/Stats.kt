package com.voidsamuraj.lumbze

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidsamuraj.lumbze.ui.theme.LumbzeTheme

@Composable
fun DrawStatScreen(mazeViewModel: MazeViewModel){

    Box( ){

        Image(painter = painterResource(id = R.drawable.bg),
            contentDescription ="bg",
            contentScale = ContentScale.FillBounds)
        mazeViewModel.getUsersStats().value?.let { list->


            LazyColumn( verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier
                .fillMaxSize(0.9f)
                .align(Alignment.Center)
                .background(colorResource(id = R.color.trunk1), shape = RoundedCornerShape(20.dp))
                .padding(10.dp)
            ){
                items( list){ user->
                    if(user.first<=20)
                        Box(modifier = Modifier.fillMaxWidth()){
                            Image(painter = painterResource(id =
                           if(user.first<4) R.drawable.gold
                           else if(user.first<11) R.drawable.silver
                           else  R.drawable.bronze

                            ), contentDescription ="medal" ,
                            modifier = Modifier.width(45.dp).height(45.dp).align(Alignment.CenterStart).padding(15.dp,0.dp,0.dp,0.dp))
                        Row(horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.align(Alignment.Center)) {
                            Text(text = user.first.toString()+'.',
                                fontSize = 24.sp )
                            Text(text = user.second.name, modifier = Modifier.padding(10.dp,0.dp),
                                fontSize = 24.sp)
                            Text(text = user.second.points.toString(), textAlign = TextAlign.Center,
                                fontSize = 24.sp)
                        }
                        }
                    else
                        Row(horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(text = user.first.toString()+'.',
                                fontSize = 24.sp )
                            Text(text = user.second.name, modifier = Modifier.padding(10.dp,0.dp),
                                fontSize = 24.sp)
                            Text(text = user.second.points.toString(), textAlign = TextAlign.Center,
                                fontSize = 24.sp)
                        }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun StatPreview() {
    LumbzeTheme {
        //  DrawStatScreen()
    }
}