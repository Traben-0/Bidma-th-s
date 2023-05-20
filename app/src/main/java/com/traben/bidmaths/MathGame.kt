package com.traben.bidmaths

import android.widget.TextView
import com.traben.bidmaths.maths.MathBinaryExpressionComponent
import com.traben.bidmaths.maths.MathOperator
import com.traben.bidmaths.maths.ParsedMathEquation
import kotlin.math.roundToInt
import kotlin.random.Random

class MathGame(val equations: List<ParsedMathEquation>) {

    // used to prevent repeated hints, feels unresponsive to end user
    private var currentHint = GameHint.GENERAL

    //was easier then data binding, sue me
    var hintSetter : (String)->Unit = {}

    //updates the current game hint given certain context
    fun updateHint(operatorClicked: MathOperator) {
        currentHint = GameHint.getFromEquationDifferentTo(
            lastEquation?.getNextOperation(),
            operatorClicked,
            currentHint
        )
        hintSetter.invoke(currentHint.getMessage())
    }

    var lastEquation: ParsedMathEquation? = null
    fun getEquation(index: Int): ParsedMathEquation {
        lastEquation = equations[index]
        return equations[index]
    }


    fun isLastGame(iteration: Int): Boolean {
        return equations.size - 1 == iteration
    }


    fun gameScore(): Int {
        val averageIncorrectPerEquation = averageScore()
        // division number is an arbitrary choice it allows the users
        // to get that many attempts at each question and still get a score between 0-100
        return 100 - (averageIncorrectPerEquation * 100.0 / 2.25).toInt().coerceAtLeast(0)
            .coerceAtMost(100)

    }

    private fun averageScore(): Double {
        var totalTimesWrong = 0
        for (equation in equations)
            totalTimesWrong += equation.timesAnsweredWrong

        return totalTimesWrong.toDouble() / equations.size.toDouble()
    }

    fun scoreGrade(): String {
        return scoreToGrade(gameScore())
    }

    fun gameResultsDetailedInfo(): String {
        val allEquations = LinkedHashMap<String, Int>()
        var best = Int.MAX_VALUE
        var bestEquation = ""

        var worst = 0
        var worstEquation = ""

        for (equation in equations) {
            val wrongAnswers = equation.timesAnsweredWrong
            val equationString = equation.toStringPretty()
            if (wrongAnswers < best) {
                best = wrongAnswers
                bestEquation = equationString
            }
            if (wrongAnswers > worst) {
                worst = wrongAnswers
                worstEquation = equationString
            }
            allEquations[equationString] = wrongAnswers


        }
        val output = java.lang.StringBuilder(
            """
                Best round:
                 - equation:    $bestEquation
                 - timesWrong:  $best
                
                Worst round:
                 - equation:    $worstEquation
                 - timesWrong:  $worst
                 
                Average:
                 - timesWrong:  ${averageScore()}
                 
                All rounds:
            """.trimIndent()
        )
        var i = 0
        for (entry in allEquations) {
            i++
            output.append(
                """
                
                > Round #$i:
                   - equation:    ${entry.key}
                   - timesWrong:  ${entry.value}
                 
            """.trimIndent()
            )
        }
        return output.toString()
    }


    companion object {

        private val gradesOrdered = listOf(
            "F-", "F", "F+",
            "E-", "E", "E+",
            "D-", "D", "D",
            "D+", "C-", "C",
            "C", "C+", "C+",
            "B-", "B", "B+",
            "A-", "A", "A+"
        )

        public fun scoreToGrade(score: Int): String {
            val gradeIndex: Double = score.coerceAtLeast(0).coerceAtMost(100) / 5.0
            return gradesOrdered[gradeIndex.roundToInt()]
        }

        private fun loadEasyGame() {
            lastMode = GameMode.EASY
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for (i in 0..1) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i / 3))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        private fun loadMediumGame() {
            lastMode = GameMode.MEDIUM
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for (i in 0..9) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i / 2))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        fun loadHardGame() {
            lastMode = GameMode.HARD
            val equationsForGame = mutableListOf<ParsedMathEquation>()
            for (i in 0..14) {
                equationsForGame.add(ParsedMathEquation.createRandomExpression(i))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        suspend fun loadGameMode(mode: GameMode) {
            when (mode) {
                GameMode.EASY -> loadEasyGame()
                GameMode.MEDIUM -> loadMediumGame()
                GameMode.HARD -> loadHardGame()
            }
        }

        private var lastMode = GameMode.EASY

        fun loadNewGameInLastMode() {
            when (lastMode) {
                GameMode.EASY -> loadEasyGame()
                GameMode.MEDIUM -> loadMediumGame()
                GameMode.HARD -> loadHardGame()
            }
        }

        // the current game as a resettable singleton format
        var currentMathGame: MathGame? = null
    }

    // a simple utility enum
    enum class GameMode {
        EASY,
        MEDIUM,
        HARD
    }

    // an enum containing various possible hints
    // the variations allow for a context driven hint system
    enum class GameHint(private val message: String) {
        BRACKET_HINT("Are there any brackets that need to be completed first?"),
        LEFT_TO_RIGHT_HINT("Are you solving from the left to the right?"),
        NEXT_IS_POWER("Indices (^) go before Division, Multiplication, Addition, & Subtraction."),
        NEXT_IS_MULTIPLY("Multiplication and Division must always be done before adding and subtracting values."),
        NOT_PLUS("Remember you cannot add or subtract numbers if they need to be Multiplied or Divided first, you must also resolve any (Brackets) & ^Indeces."),
        NOT_MULTIPLY("Remember (Brackets) & ^Indeces go before Multiplication & Division"),
        NOT_POWER("Something is preventing this Index (^) from resolving, perhaps the index value itself isn't resolved, or there are brackets needing resolution first."),
        GENERAL("Remember the order of operations: Brackets -> Orders -> Division & Multiplication -> Addition & Subtraction");

        fun getMessage() : String{
            return "Hint: ${this.message}"
        }
        companion object {

            // this function is basically a cheap method of ensuring the hint is different every click
            // even if every other click might repeat randomly
            fun getFromEquationDifferentTo(
                equation: MathBinaryExpressionComponent?,
                clickedOperator: MathOperator,
                currentHint : GameHint
            ): GameHint {
                val hint = getFromEquation(equation, clickedOperator)
                if(hint == currentHint) return GENERAL
                return hint
            }

            // a method to return a GameHint enum both from some random selection and also
            // contextual information such as the next required move and the wrong move the
            // player just made
            private fun getFromEquation(
                equation: MathBinaryExpressionComponent?,
                clickedOperator: MathOperator
            ): GameHint {
                if (equation == null) return getHintFromClicked(clickedOperator)

                //random 1/4 chance to give left right hint as its usually relevant
                if (SettingsFragment.respectLeftRight && Random.nextInt(3) ==1) {
                    return LEFT_TO_RIGHT_HINT
                }

                //random 1/3 chance to give bracket hint as its not detected
                if (Random.nextInt(2) ==1) {
                    return BRACKET_HINT
                }

//                //random 1/6 chance to give the general hint
//                if (Random.nextInt(5) ==1) {
//                    return GENERAL
//                }

                //50% chance to give hint based on the incorrectly clicked operation or the actual next one
                if (Random.nextBoolean()) {
                    return getHintFromClicked(clickedOperator)
                }
                return getHintFromNext(equation)

            }

            //gets a context driven GameHint based on the actual next correct move to be made
            private fun getHintFromNext(equation: MathBinaryExpressionComponent): GameHint {
                return when (equation.operator) {
                    MathOperator.MULTIPLY, MathOperator.DIVIDE -> NEXT_IS_MULTIPLY
                    MathOperator.POWER -> NEXT_IS_POWER
                    else -> GENERAL
                }
            }

            //gets a context driven GameHint based on the incorrect move made by the player
            private fun getHintFromClicked(clickedOperator: MathOperator): GameHint {
                return when (clickedOperator) {
                    MathOperator.ADD, MathOperator.SUBTRACT -> NOT_PLUS
                    MathOperator.MULTIPLY, MathOperator.DIVIDE -> NOT_MULTIPLY
                    MathOperator.POWER -> NOT_POWER
                    else -> GENERAL
                }
            }
        }
    }
}