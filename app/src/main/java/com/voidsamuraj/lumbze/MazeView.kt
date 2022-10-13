package com.voidsamuraj.maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import androidx.compose.ui.graphics.Color
import com.voidsamuraj.lumbze.MazeViewModel
import java.lang.Math.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.roundToInt

class MazeView(context: Context) : View(context) {
    enum class MazeDirections{
        OUT,RIGHT,CENTER,LEFT
    }
    enum class CellState{
        UNVISITED,VISITED,BLOCKED
    }
    private var cellHeight:Int=10
    private var cellWidth:Int=10
    private var centerX:Float=0f
    private var centerY:Float=0f
    private var canvas: Canvas?=null
    private var paint: Paint?=null
    /***
     * @param cellWidth width of circuit for one cell
     * @param cellHeight height of circle radius for one cell
     */
    fun postData(cellHeight:Int,cellWidth:Int){
        this.cellWidth=cellWidth
        this.cellHeight=cellHeight
    }
    /*
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas=canvas
        centerX=width/2f
        centerY=height/2f
        paint=Paint()
        paint!!.color= Color(255,0,0).hashCode()
        drawMaze(18)

    }*/

    /**
     *  function calculate width and cellsAmount in row
     *  @return Pair<width,cellsAmount>
     */
    private fun calculateWidthAndAmount(row:Int):Pair<Double,Int>{
        var ret:Double=cellWidth.toDouble()
        val smallRadius=cellHeight
        val smallLength=2*PI*smallRadius
        var cellsAmount:Int=(smallLength/ret).toInt()
        ret=smallLength/cellsAmount
        for (i in 1 until row){

            val bigRadius=cellHeight*(i+1)
            val bigLength=2*PI*bigRadius
            val splitedCells=bigLength/cellsAmount
            ret=splitedCells
            if(splitedCells/2>=cellWidth)
                ret/=2
            cellsAmount=(bigLength/ret).toInt()
        }
        return Pair(ret,cellsAmount)
    }

    fun drawMaze(rows:Int){
        val cellsData:ArrayList<Pair<Double,Int>> = arrayListOf()
        val mazeArray:ArrayList<ArrayList<Triple<CellState,List<MazeDirections>,List<MazeDirections>>>> = arrayListOf()
        val fullList=listOf(MazeDirections.OUT,MazeDirections.CENTER,MazeDirections.LEFT,MazeDirections.RIGHT)
        var counter=0
        var currentRow=0
        var currentColumn= 0
        val stos:Stack<Pair<Int,Int>> = Stack()
        fun getCellState(row:Int,col:Int):CellState=mazeArray[row][col].first
       fun removeCellWall(row: Int,column: Int,wall:MazeDirections){
            mazeArray[row].apply{
                set(column,
                    Triple(
                        CellState.VISITED,
                        get(column).second.toMutableList()
                            .apply {  remove(wall)}
                            .toList(),
                        get(column).third
                    )
                )
            }
        }
        fun changeCellState(row: Int,column: Int,state:CellState){
            mazeArray[row].apply{
                set(column,
                    Triple(
                        state,
                        get(column).second,
                        get(column).third
                    )
                )
            }
        }
        fun removeMovement(row: Int,column: Int,direction:MazeDirections){
            mazeArray[row].apply{
                set(column,
                    Triple(
                        get(column).first,
                        get(column).second,
                        get(column).third.toMutableList()
                            .apply {  remove(direction)}
                            .toList()
                    )
                )
            }
        }

        //calculate number of cells, cells in row and widths
        for (i in 1..rows){
            val data=calculateWidthAndAmount(i)
            cellsData.add(data)
            counter+=data.second
            mazeArray.add(
                ArrayList(
                    (1..data.second).map { Triple(CellState.UNVISITED,fullList, fullList) }
                )
            )
        }
        paint?.color=Color.Green.hashCode()


            //first cell
            currentColumn=(random()*cellsData[0].second).toInt()
            removeCellWall(0,currentColumn,MazeDirections.CENTER)
            changeCellState(0,currentColumn,CellState.VISITED)
            removeMovement(0,currentColumn,MazeDirections.CENTER)

            //change cells in maze
            do{
                lateinit var dir:MazeDirections
                val options:Int = mazeArray[currentRow][currentColumn].third.size
                if (options!=0){
                dir= mazeArray[currentRow][currentColumn].third[(random()*options).toInt()]

                if(dir==MazeDirections.LEFT){
                    var nextPos=currentColumn-1
                    if(nextPos<0)
                        nextPos=cellsData[currentRow].second-1
                    if(getCellState(currentRow,nextPos)==CellState.UNVISITED){

                        removeCellWall(currentRow,currentColumn,MazeDirections.LEFT)
                        removeCellWall(currentRow,nextPos,MazeDirections.RIGHT)

                        changeCellState(currentRow,nextPos,CellState.VISITED)
                        stos.add(Pair(currentRow,currentColumn))
                        currentColumn=nextPos
                        --counter


                    }else
                        removeMovement(currentRow,currentColumn,MazeDirections.LEFT)


                }else if(dir==MazeDirections.RIGHT){
                    var nextPos=currentColumn+1
                    if(nextPos>=cellsData[currentRow].second)
                        nextPos=0

                    if(getCellState(currentRow,nextPos)==CellState.UNVISITED){

                        removeCellWall(currentRow,currentColumn,MazeDirections.RIGHT)
                        removeCellWall(currentRow,nextPos,MazeDirections.LEFT)

                        changeCellState(currentRow,nextPos,CellState.VISITED)
                        stos.add(Pair(currentRow,currentColumn))
                        currentColumn=nextPos
                        --counter

                    }else
                        removeMovement(currentRow,currentColumn,MazeDirections.RIGHT)

                }else if(dir==MazeDirections.CENTER&&currentRow!=0){
                    val nextRow=currentRow-1
                    var nextColumn=currentColumn
                    val currentAmount:Int=cellsData[currentRow].second
                    val nextAmount:Int=cellsData[nextRow].second
                    if(nextAmount!=currentAmount)
                        nextColumn= kotlin.math.floor(currentColumn.toDouble() / 2).toInt()
                    if(getCellState(nextRow,nextColumn)==CellState.UNVISITED){

                        removeCellWall(currentRow,currentColumn,MazeDirections.CENTER)
                        removeCellWall(nextRow,nextColumn,MazeDirections.OUT)

                        changeCellState(nextRow,nextColumn,CellState.VISITED)
                        stos.add(Pair(currentRow,currentColumn))
                        --counter
                        currentRow=nextRow
                        currentColumn=nextColumn
                    }else
                        removeMovement(currentRow,currentColumn,MazeDirections.CENTER)


                }else if(dir==MazeDirections.CENTER&&currentRow!=0){
                    removeMovement(currentRow,currentColumn,MazeDirections.CENTER)

                }else if(dir==MazeDirections.OUT&&currentRow!=rows-1){
                    val nextRow=currentRow+1
                    var nextColumn=currentColumn
                    val currentAmount:Int=cellsData[currentRow].second
                    val nextAmount:Int=cellsData[nextRow].second
                    if(nextAmount==2*currentAmount)
                        nextColumn= currentColumn*2- random().roundToInt()
                    if(nextColumn<0)
                        nextColumn=0
                    if(getCellState(nextRow,nextColumn)==CellState.UNVISITED){
                        removeCellWall(currentRow,currentColumn,MazeDirections.OUT)
                        removeCellWall(nextRow,nextColumn,MazeDirections.CENTER)
                        changeCellState(nextRow,nextColumn,CellState.VISITED)
                        stos.add(Pair(currentRow,currentColumn))
                        --counter
                        currentRow=nextRow
                        currentColumn=nextColumn

                    }else
                        removeMovement(currentRow,currentColumn,MazeDirections.OUT)


                } else if(dir==MazeDirections.OUT&&currentRow==rows-1){
                    removeMovement(currentRow,currentColumn,MazeDirections.OUT)

                }else if(mazeArray[currentRow][currentColumn].third.isEmpty()){
                    //turn back
                    if(stos.isNotEmpty()){
                        val last=stos.pop()
                        currentRow=last.first
                        currentColumn=last.second
                    }
                }
                }else{
                    if(stos.isNotEmpty()){
                        val last=stos.pop()
                        currentRow=last.first
                        currentColumn=last.second
                        if((currentRow+currentColumn)==0)
                            break
                    }
                }
      } while(counter!=0&&stos.isNotEmpty())



            //display maze

                for (i in 1..rows){

                    for (j in 1..cellsData[i-1].second){
                        drawCell(i,j,
                            mazeArray[i-1][j-1].second,
                            cellsData[i-1].first
                        )
                  }
            }

    }

      fun drawCell(row:Int,column:Int,directions:List<MazeDirections>,width:Double){

        fun drawArc(radius:Int,startAngle:Float,endAngle:Float){
            canvas?.drawArc(centerX-radius,centerY-radius,centerX+radius,centerY+radius,startAngle,endAngle-startAngle,false,paint!!)
        }
        fun drawLine(angle:Float){
            val myAngle=toRadians(angle.toDouble())
            val oX1=row*cellHeight
            val oX2=oX1+cellHeight
            val myCos=cos(myAngle).toFloat()
            val mySin=sin(myAngle).toFloat()
            val x1=oX1*myCos+centerX
            val x2=oX2*myCos+centerX
            val y1=oX1*mySin+centerY
            val y2=oX2*mySin+centerY
            paint?.let { canvas?.drawLine(x1,y1,x2,y2,paint!!)}
        }
        if (paint==null){

            paint=Paint()
            paint!!.color= Color(255,0,0).hashCode()
        }
        paint!!.style= Paint.Style.STROKE
        paint!!.strokeWidth=10f

        val smallRadius=cellHeight*row
        val circleLength=2*PI*smallRadius
        val cellsAmount:Float=(circleLength/width).toFloat()

        if(column<=cellsAmount){
            val cellAngle=360f/cellsAmount
            val endAngle=cellAngle*column
            val startAngle=endAngle-cellAngle
            for(direction in directions){
                when(direction){
                    MazeDirections.CENTER->
                        drawArc(smallRadius,startAngle,endAngle)
                    MazeDirections.OUT->
                        drawArc(smallRadius+cellHeight,startAngle,endAngle)
                    MazeDirections.LEFT->
                        drawLine(startAngle)

                    MazeDirections.RIGHT->
                        drawLine(endAngle)
                }

            }

        }
    }

}