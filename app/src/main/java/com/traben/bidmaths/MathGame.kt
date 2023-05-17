package com.traben.bidmaths

import com.traben.bidmaths.maths.ParsedMathEquation

class MathGame(val equations : List<ParsedMathEquation>) : java.io.Serializable {


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

    companion object{
        fun loadEasyGame(respectLeftToRight : Boolean){
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..3) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/3, respectLeftToRight))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadMediumGame(respectLeftToRight : Boolean){
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..10) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i/2, respectLeftToRight))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadHardGame(respectLeftToRight : Boolean){
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for(i in 0..15) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i, respectLeftToRight))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        var currentMathGame : MathGame? = null
    }





}