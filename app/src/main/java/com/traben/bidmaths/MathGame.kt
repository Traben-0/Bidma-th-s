package com.traben.bidmaths

import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.maths.ParsedMathEquation
import kotlinx.coroutines.Dispatchers

class MathGame(val equations : List<ParsedMathEquation>) {


    fun getEquation(index: Int) : ParsedMathEquation{
        return equations[index]
    }

    fun isGameFinished(iteration: Int) : Boolean{
        return equations.size <= iteration
    }

    fun isLastGame(iteration: Int) : Boolean{
        return equations.size-1 == iteration
    }

    fun gameResults() : String{
        var count = 0
        for (equation in equations)
            count += equation.timesAnsweredWrong

        return "Wrong $count times in ${equations.size} rounds"
    }
    fun gameScore() : Int{
        var count = 0
        for (equation in equations)
            count += equation.timesAnsweredWrong
        return (100 - count / (equations.size * 3) *100).coerceAtLeast(0)
    }

    fun gameResultsDetailedInfo() :String{
        val allEquations = LinkedHashMap<String,Int>()
        var best = Int.MAX_VALUE;
        var bestEquation = ""

        var worst = Int.MIN_VALUE;
        var worstEquation = ""

        for (equation in equations) {
            val wrongAnswers = equation.timesAnsweredWrong
            val equationString = equation.toStringPretty()
            if(wrongAnswers < best){
                best = wrongAnswers
                bestEquation = equationString
            }else if (wrongAnswers > worst){
                worst = wrongAnswers
                worstEquation = equationString
            }
            allEquations[equationString] = wrongAnswers


        }
        val output = java.lang.StringBuilder( """
                Best round:
                 - equation:    $bestEquation
                 - timesWrong:  $best
                
                Worst round:
                 - equation:    $worstEquation
                 - timesWrong:  $worst
                 
                All rounds:
            """.trimIndent())
        var i = 0
        for(entry in allEquations){
            i++
            output.append("""
                
                Round #$i:
                 - equation:    ${entry.key}
                 - timesWrong:  ${entry.value}
                 
            """.trimIndent())
        }
        return output.toString()
    }


    companion object{
        private fun loadEasyGame(){
            lastMode = GameMode.EASY
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..1) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/3))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        private fun loadMediumGame(){
            lastMode = GameMode.MEDIUM
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..9) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/2))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadHardGame(){
            lastMode = GameMode.HARD
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..14) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        suspend fun  loadGameMode(mode: GameMode){
            when (mode){
                GameMode.EASY -> loadEasyGame()
                GameMode.MEDIUM -> loadMediumGame()
                GameMode.HARD -> loadHardGame()
            }
        }

        private var lastMode = GameMode.EASY

        fun loadNewGameInLastMode(){
            when(lastMode){
                GameMode.EASY -> loadEasyGame()
                GameMode.MEDIUM -> loadMediumGame()
                GameMode.HARD -> loadHardGame()
            }
        }

        var currentMathGame : MathGame? = null
    }



    enum class GameMode{
        EASY,
        MEDIUM,
        HARD
    }

}