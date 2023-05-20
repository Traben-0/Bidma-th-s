package com.traben.bidmaths

import android.widget.TextView
import com.traben.bidmaths.maths.MathBinaryExpressionComponent
import com.traben.bidmaths.maths.MathOperator
import com.traben.bidmaths.maths.ParsedMathEquation
import kotlin.math.roundToInt
import kotlin.random.Random

class MathGame(val equations: List<ParsedMathEquation>) {

    var currentHint = GameHint.GENERAL

    public var hintDisplay: TextView? = null

    fun updateHint(operatorClicked: MathOperator) {
        currentHint = GameHint.getFromEquation(lastEquation?.getNextOperation(), operatorClicked)
        hintDisplay?.text = currentHint.message
        println("hint")
    }

    var lastEquation: ParsedMathEquation? = null
    fun getEquation(index: Int): ParsedMathEquation {
        lastEquation = equations[index]
        return equations[index]
    }

    fun isGameFinished(iteration: Int): Boolean {
        return equations.size <= iteration
    }

    fun isLastGame(iteration: Int): Boolean {
        return equations.size - 1 == iteration
    }

    fun gameResults(): String {
        var count = 0
        for (equation in equations)
            count += equation.timesAnsweredWrong

        return "Wrong $count times in ${equations.size} rounds"
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
            "D-", "D", "D+",
            "C-", "C", "C+",
            "B-", "B", "B+",
            "A-", "A", "A+",
            "S", "S+", "S++"
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

        var currentMathGame: MathGame? = null
    }


    enum class GameMode {
        EASY,
        MEDIUM,
        HARD
    }

    enum class GameHint(val message: String) {
        BRACKET_HINT("brackets?"),
        NEXT_IS_POWER("NEXT_IS_POWER"),
        NEXT_IS_MULTIPLY("NEXT_IS_MULTIPLY"),
        NOT_PLUS("NOT_PLUS"),
        NOT_MULTIPLY("NOT_MULTIPLY"),
        NOT_POWER("NOT_POWER"),
        GENERAL("GENERAL");

        companion object {
            fun getFromEquation(
                equation: MathBinaryExpressionComponent?,
                clickedOperator: MathOperator
            ): GameHint {
                if (equation == null) return getHintFromClicked(clickedOperator)

                //random 25% chance to give bracket hint as its not detected
                if (Random.nextBoolean() && Random.nextBoolean()) {
                    return BRACKET_HINT
                }

                //50% chance to give hint based on the incorrectly clicked operation or the actual next one
                if (Random.nextBoolean()) {
                    return getHintFromClicked(clickedOperator)
                }
                return getHintFromNext(equation)

            }

            private fun getHintFromNext(equation: MathBinaryExpressionComponent): GameHint {

                return when (equation.operator) {
                    MathOperator.MULTIPLY, MathOperator.DIVIDE -> NEXT_IS_MULTIPLY
                    MathOperator.POWER -> NEXT_IS_POWER
                    else -> GENERAL
                }
            }

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