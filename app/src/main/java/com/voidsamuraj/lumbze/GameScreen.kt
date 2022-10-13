package com.voidsamuraj.lumbze

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.voidsamuraj.lumbze.ui.theme.mazeFont
import kotlinx.coroutines.*
import java.util.Stack
import kotlin.math.abs


@Composable
fun DrawMaze(mazeViewModel: MazeViewModel, resources: Resources,navigateStats: () -> Unit){

    val endTitles:MutableState<Boolean> = remember { mutableStateOf(false) }

    val alpha: Float by animateFloatAsState(
        if(mazeViewModel.isDrawerOpen.value)  0.3f else 1f ,
        animationSpec = tween(
            easing = LinearEasing,
            durationMillis = integerResource(id = R.integer.drawer_duration),
            delayMillis = integerResource(id = R.integer.drawer_delay)*2
        )
    )
    Box(modifier = Modifier.fillMaxSize()) {
        //background image
        Image(painter = painterResource(id = R.drawable.bg),
            contentDescription ="Background",
            modifier = Modifier
                .fillMaxSize()
                .blur(1.dp),
            contentScale = ContentScale.FillBounds
        )
        //maze image
        mazeViewModel.mazeBitmap.value?.let{
            Image(bitmap =it.asImageBitmap(),
                contentDescription ="Maze",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(mazeViewModel.rotation.value)
                    .alpha(alpha)
            )
        }
        //help balls bitmap
        mazeViewModel.maze.helpPathBitmap.value.let{

            Image(bitmap =it.asImageBitmap(), contentDescription = "help",      modifier = Modifier
                .fillMaxSize()
                .rotate(mazeViewModel.rotation.value))
        }

        //moving ball
        mazeViewModel.ballBitmap.value?.let{
            Image(bitmap =it.asImageBitmap() , contentDescription ="Ball",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .absoluteOffset(
                        y = pixelToDp(
                            mazeViewModel.translation.value.toInt(),
                            resources
                        )
                    )
                    .alpha(alpha)
            )
        }



        mazeViewModel.maze.ended.value.let {
            endTitles.value=it
        }
        drawEndScreen(endTitles,mazeViewModel,resources,navigateStats)

        // for touch
        // can capture or block touch in box area
        //val width=with(LocalDensity.current){   LocalConfiguration.current.screenWidthDp.dp.toPx()}.toInt()

        if(!mazeViewModel.maze.ended.value)
            Box(modifier = Modifier
                .fillMaxSize()

                .pointerInput(Unit) {

                    forEachGesture {

                        awaitPointerEventScope {

                            if ((!mazeViewModel.isDrawerOpen.value) && mazeViewModel.isScreenTouchable.value) {
                                val actionStart = awaitFirstDown().position

                                var endPos: Offset?
                                do {
                                    val event: PointerEvent = awaitPointerEvent()

                                    endPos = event.changes[event.changes.size - 1].position
                                    event.changes.forEach { pointerInputChange: PointerInputChange ->
                                        if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                                    }
                                } while (event.changes.any { it.pressed })


                                endPos?.let {


                                    CoroutineScope(Dispatchers.Default).launch {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            mazeViewModel.setIsScreenTouchable(false)
                                        }
                                        val xMove = endPos.x - actionStart.x
                                        val yMove = endPos.y - actionStart.y
                                        if (abs(xMove) > abs(yMove)) {
                                            if (xMove > 0) {
                                                move(Maze.CellDirections.RIGHT, mazeViewModel)
                                            } else {
                                                move(Maze.CellDirections.LEFT, mazeViewModel)
                                            }
                                        } else {
                                            if (yMove > 0) {
                                                move(Maze.CellDirections.OUT, mazeViewModel)
                                            } else {
                                                move(Maze.CellDirections.CENTER, mazeViewModel)
                                            }
                                        }

                                    }
                                    //CoroutineScope(Dispatchers.Main).launch {
                                    // do {

                                    //   awaitPointerEventScope {
                                    /*    val event: PointerEvent = awaitPointerEvent()
                                        event.changes.forEach { pointerInputChange: PointerInputChange ->
                                            if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                                            if (pointerInputChange.pressed != pointerInputChange.previousPressed) pointerInputChange.consume()
                                        }*/
                                    //    }
                                    //  } while (event.changes.any { it.pressed })
                                    //    (!mazeViewModel.isScreenTouchable.value)
                                    // }


                                }
                            } else {
                                do {
                                    val event: PointerEvent = awaitPointerEvent()
                                    event.changes.forEach { pointerInputChange: PointerInputChange ->
                                        if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                                        if (pointerInputChange.pressed != pointerInputChange.previousPressed) pointerInputChange.consume()
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    }
                }) {

            }
        IconButton(onClick = {
            mazeViewModel.maze.updateHelpDrawable()

        }, modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(20.dp),) {
            Icon(painter = painterResource(id = R.drawable.ic_twotone_help_24), contentDescription ="help" , tint =  colorResource(id = R.color.trunk2), modifier = Modifier
                .width(dimensionResource(id = R.dimen.icon_size))
                .height(dimensionResource(id = R.dimen.icon_size)))
        }
    }

}

@Composable
fun drawEndScreen(isScrollable: MutableState<Boolean>,mazeViewModel: MazeViewModel,resources:Resources,navigateStats:()->Unit){
    val scrollState = rememberScrollState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    if(isScrollable.value) {
        LaunchedEffect(keys = emptyArray()) {
            scrollState.scroll(MutatePriority.PreventUserInput){
                val aim= size.height * 4f
                val step=aim/333
                var now=scrollState.value.toFloat()
                if(step>0){
                    while(now<aim){
                        now+=step
                        if(now<aim)
                            scrollBy(step)
                        else
                            scrollBy(now-aim)
                        delay(1)

                    }
                }
            }
        }
    }
    else{
        LaunchedEffect(keys = emptyArray()){
            scrollState.scroll(MutatePriority.PreventUserInput) {
                val aim= 0f
                var now=scrollState.value.toFloat()
                val step=now/333
                if(step>0){
                    while(now>aim){
                        now-=step
                        if(now>aim)
                            scrollBy(-step)
                        else
                            scrollBy(-now)
                        delay(1)
                    }
                }
            }

        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
            size = it
        }

        .verticalScroll(scrollState)

    ) {
        Spacer(modifier = Modifier.then(
            with(LocalDensity.current) {
                Modifier.size(
                    width = pixelToDp(size.width, resources = resources),
                    height =  pixelToDp(size.height, resources = resources),
                )
            }
        ))
        Image(painter = rememberAsyncImagePainter(model =R.drawable.log3), contentDescription ="WOOD", modifier= Modifier
            .fillMaxWidth()
            .height(pixelToDp(size.height, resources = resources))
            , contentScale = ContentScale.FillBounds )
        Image(painter = rememberAsyncImagePainter(model =R.drawable.log2), contentDescription ="WOOD", modifier= Modifier
            .fillMaxWidth()
            .height(pixelToDp(size.height, resources = resources))
            , contentScale = ContentScale.FillBounds )

        Box (modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {

                forEachGesture {

                    awaitPointerEventScope {

                        do {
                            val event: PointerEvent = awaitPointerEvent()
                            event.changes.forEach { pointerInputChange: PointerInputChange ->
                                pointerInputChange.consume()
                            }
                        } while (event.changes.any { it.pressed })
                    }

                }
            },
            contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.log),
                contentDescription = "WOOD",
                modifier = Modifier
                    .fillMaxSize()
                    .height(pixelToDp(size.height, resources = resources)),
                contentScale = ContentScale.FillBounds
            )


            Box(modifier = Modifier
                .fillMaxWidth(0.7f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .clip(
                    CutCornerShape(20.dp, 10.dp, 20.dp, 10.dp)
                )){

                Image(modifier = Modifier
                    .fillMaxWidth()
                    .height(pixelToDp((size.height * 0.8).toInt(), resources = resources)), painter = painterResource(id = R.drawable.log_insidec), contentDescription ="Content", contentScale = ContentScale.Crop )

                Column(
                    modifier = Modifier.padding(0.dp,20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {

                    drawText(
                        text = "You got",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textStyle = MaterialTheme.typography.h5
                    )
                    drawText(
                        text = mazeViewModel.maze.allCells.toString(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    drawText(
                        text = "pieces of wood",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textStyle = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    drawText(
                        text = "Wood Thickness",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        drawTextButton("-") {
                            mazeViewModel.setRowsAmount(mazeViewModel.rowsAmount.value.dec())
                        }
                        drawText(text = mazeViewModel.rowsAmount.value.toString())
                        drawTextButton("+") {
                            mazeViewModel.setRowsAmount(mazeViewModel.rowsAmount.value.inc())
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    DrawMenuButton(text = stringResource(id = R.string.play), modifier = Modifier.padding(10.dp), textColor = colorResource(
                        id = R.color.trunkText
                    )) {
                        mazeViewModel.apply {
                            maze.createMaze(mazeViewModel.rowsAmount.value)
                            setMazeBitmap(maze.getMaze())
                            mazeViewModel.maze.ended.value=false
                        }

                        mazeViewModel.resetPositionAndRotation()
                    }
                    val contextForMessage= LocalContext.current
                    DrawMenuButton(text = stringResource(id = R.string.stats), modifier = Modifier.padding(10.dp), textColor = colorResource(
                        id = R.color.trunkText
                    )) {
                        if(mazeViewModel.isUserSignedIn.value)
                            navigateStats()
                        else
                            Toast.makeText(contextForMessage, R.string.please_login, Toast.LENGTH_SHORT).show()

                    }

                }
            }
        }
    }
}

fun pixelToDp(pixels:Int,resources: Resources): Dp {
    return (pixels / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).dp
}

suspend fun move(direction: Maze.CellDirections, mazeViewModel: MazeViewModel){
    Log.v("BALLPOSP",""+mazeViewModel.maze.getBallPos())
    mazeViewModel.maze.moveBall(direction)
    Log.v("BALLPOSA",""+mazeViewModel.maze.getBallPos()+" "+mazeViewModel.maze.ended+" "+mazeViewModel.isScreenTouchable+" "+mazeViewModel.isDrawerOpen)

}

@Preview
@Composable
fun gameScreenPreview(){
     val data:Stack<Pair<Int,Int>> = Stack()
    data.add(Pair(0,1))
    data.add(Pair(1,2))
    data.add(Pair(1,3))
    data.add(Pair(1,4))
    data.add(Pair(1,5))
    data.add(Pair(1,6))
}

@Composable
fun drawTextButton(text:String,
                   textStyle: TextStyle=MaterialTheme.typography.h4,
                   color:Color= colorResource(id = R.color.endScreenText),
                   onButtonClick:()->Unit={}){
    TextButton(onClick = {onButtonClick()}) {
        drawText(text=text,
            textStyle =textStyle,
            color = color)
    }

}
@Composable
fun drawText(text:String,
             modifier: Modifier=Modifier,
             textStyle: TextStyle=MaterialTheme.typography.h4,
             color:Color= colorResource(id = R.color.endScreenText )){
    Text(
        text = text,
        modifier= modifier,
        fontFamily = mazeFont,
        style = textStyle,
        color = color
    )


}