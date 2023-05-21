package com.traben.bidmaths

import com.traben.bidmaths.math.BinaryExpressionComponent
import com.traben.bidmaths.math.MathOperator
import com.traben.bidmaths.math.ParsedExpression
import com.traben.bidmaths.screens.SettingsFragment
import kotlin.math.roundToInt
import kotlin.random.Random

/**
* this class acts primarily as a resettable singleton
* this class is tied to the current game loop and performs the loading and scoring functions
* of the game as well as most of it's non-maths logic
*/
class MathGame(val equations: List<ParsedExpression>) {

    // used to prevent repeated hints, feels unresponsive to end user
    private var currentHint = GameHint.GENERAL

    //was easier then data binding, sue me
    var hintSetter: (String) -> Unit = {}

    //updates the current game hint given certain context
    fun updateHint(operatorClicked: MathOperator) {
        currentHint = GameHint.getFromEquationDifferentTo(
            lastEquation?.getNextOperation(),
            operatorClicked,
            currentHint
        )
        hintSetter.invoke(currentHint.getMessage())
    }

    private var lastEquation: ParsedExpression? = null

    //used by the game loop to retrieve a parsed math object
    // this object is cached for future interactions with MathGame
    fun getEquation(index: Int): ParsedExpression {
        lastEquation = equations[index]
        return equations[index]
    }

    fun isLastGame(iteration: Int): Boolean {
        return equations.size - 1 == iteration
    }

    // calculates the players score between 0-100
    // 3 is an arbitrarily chosen number in the equation it decides how many incorrect inputs a
    // player can get on average to be pulled down to a 0 score
    // this would be a KEY value to be tweaked based on user feedback
    fun gameScore(): Int {
        val averageIncorrectPerEquation = averageScore()
        // division number is an arbitrary choice it allows the users
        // to get that many attempts at each question and still get a score between 0-100
        return 100 - (averageIncorrectPerEquation * 100.0 / 3).toInt().coerceAtLeast(0)
            .coerceAtMost(100)

    }

    private fun averageScore(): Double {
        var totalTimesWrong = 0
        for (equation in equations)
            totalTimesWrong += equation.timesAnsweredWrong
        return totalTimesWrong.toDouble() / equations.size.toDouble()
    }

    // parses the score 0-100 as a typical school grading score "F-" -> "A+"
    fun scoreGrade(): String {
        return scoreToGrade(gameScore())
    }

    // this collates the scores of an entire game into a report string that is stored in the
    // leaderboard database, in the use case of BidMaths being used in a classroom setting via a
    // teachers tablet it would allow the teacher a detailed account of the students abilities
    // key information is stored at the top for ease of use, including the best, worst, and average scores
    // this is not the most efficient storage method but this part of the app doesn't need to be pretty
    // it is purely designed for functional & concise feedback on the users games
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

        //listing these allows a simple index lookup to do the work for me
        private val gradesOrdered = listOf(
            "F-", "F", "F+",
            "E-", "E", "E+",
            "D-", "D", "D",
            "D+", "C-", "C",
            "C", "C+", "C+",
            "B-", "B", "B+",
            "A-", "A", "A+"
        )

        //a simple index lookup on a pre made list makes this easy
        fun scoreToGrade(score: Int): String {
            val gradeIndex: Double = score.coerceAtLeast(0).coerceAtMost(100) / 5.0
            return gradesOrdered[gradeIndex.roundToInt()]
        }


        // this method creates a new instance of MathGame and stores it to the singleton currentMathGame
        // this is intended to be run outside the main thread and simply directs to the relevant
        fun loadGameMode(difficultyMode: GameDifficultyMode) {
            lastMode = difficultyMode
            val equationsForGame = mutableListOf<ParsedExpression>()
            //length of game increases with difficulty
            for (i in 0..difficultyMode.length) {
                // equation complexity is divided by larger figures for lower difficulties
                // i is included so overall complexity increases each round no matter the difficulty
                val equationComplexity = i/difficultyMode.difficultyDivision
                equationsForGame.add(ParsedExpression.createRandomExpression(equationComplexity))
            }
            currentMathGame = MathGame(equationsForGame)
        }

        private var lastMode = GameDifficultyMode.EASY

        fun loadNewGameInLastMode() {
            loadGameMode(lastMode)
        }

        // the current game as a resettable singleton format
        var currentMathGame: MathGame? = null
    }

    // a simple utility enum
    enum class GameDifficultyMode(
        val length: Int,                //difficult games are longer
        val difficultyDivision : Int    //how much to divide the equation difficulty by
    ) {
        EASY(4,3),
        MEDIUM(9,2),
        HARD(14,1)
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

        fun getMessage(): String {
            return "Hint: ${this.message}"
        }

        companion object {

            // this function is basically a cheap method of ensuring the hint is different every click
            // even if every other click might repeat randomly
            fun getFromEquationDifferentTo(
                equation: BinaryExpressionComponent?,
                clickedOperator: MathOperator,
                currentHint: GameHint
            ): GameHint {
                val hint = getFromEquation(equation, clickedOperator)
                if (hint == currentHint) return GENERAL
                return hint
            }

            // a method to return a GameHint enum both from some random selection and also
            // contextual information such as the next required move and the wrong move the
            // player just made
            private fun getFromEquation(
                equation: BinaryExpressionComponent?,
                clickedOperator: MathOperator
            ): GameHint {
                if (equation == null) return getHintFromClicked(clickedOperator)

                //random 1/4 chance to give left right hint as its usually relevant
                if (SettingsFragment.respectLeftRight && Random.nextInt(3) == 1) {
                    return LEFT_TO_RIGHT_HINT
                }

                //random 1/3 chance to give bracket hint as its not detected
                if (Random.nextInt(2) == 1) {
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
            private fun getHintFromNext(equation: BinaryExpressionComponent): GameHint {
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