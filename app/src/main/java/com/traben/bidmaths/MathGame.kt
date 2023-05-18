package com.traben.bidmaths

import com.traben.bidmaths.maths.ParsedMathEquation

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

        return "failed $count times in ${equations.size} equations"
    }

    fun gameResultsStorageMap() :Map<String,Int>{
        val map = LinkedHashMap<String,Int>()
        for (equation in equations)
            map[equation.toStringPretty()] = equation.timesAnsweredWrong
        return map
    }


    companion object{
        fun loadEasyGame(){
            lastMode = GameMode.EASY
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..3) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/3))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadMediumGame(){
            lastMode = GameMode.MEDIUM
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..10) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/2))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadHardGame(){
            lastMode = GameMode.HARD
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..15) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        private var lastMode = GameMode.EASY

        fun loadNewGameInLastMode(){
            when(lastMode){
                GameMode.EASY -> loadEasyGame()
                GameMode.MEDIUM -> loadMediumGame()
                GameMode.HARD -> loadHardGame()
                GameMode.CUSTOM -> TODO()
            }
        }

        var currentMathGame : MathGame? = null
    }



    private enum class GameMode{
        EASY,
        MEDIUM,
        HARD,
        CUSTOM
    }

}