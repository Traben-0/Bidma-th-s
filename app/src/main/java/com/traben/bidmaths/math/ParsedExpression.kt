package com.traben.bidmaths.math

import android.content.Context
import android.view.View
import android.widget.TextView
import com.traben.bidmaths.screens.SettingsFragment
import java.util.*

/**
 *
 * This object holds a fully completed math expression and can be considered the top level/container
 * of the Binary Expression Tree as outline in BinaryExpressionComponent
 *
 * this object also handles interactions with the game loop being the primary face of the expression
 * */
class ParsedExpression(val validExpression: BinaryExpressionComponent?) {

    //count of times this equations received a wrong answer from the player
    var timesAnsweredWrong: Int = 0

    //top level solving method for the equation
    fun getAnswer(): Double {
        return validExpression?.getValue() ?: Double.NaN
    }

    // checks whether the expression had been solved from a game loop context
    fun isCompleted(): Boolean {
        return validExpression?.isResolved() ?: false
    }

    //holds an action to be invoked when the expression is completed
    // this holds the potential risk of a memory leak when the held action refers to a now destroyed
    // View however this actions is only held and used while the view is valid and is blanked once
    // used
    var completeAction: () -> Unit = {}

    fun isValid(): Boolean {
        return !getAnswer().isNaN()
    }

    // if the setting is enabled to enforce left to right solving this will check if the chosen
    // operator is the next correct unresolved choice with regards to left to right solving of the game loop
    fun isNextOperationThisConsideringLeftToRight(operation: BinaryExpressionComponent): Boolean {
        if (!SettingsFragment.respectLeftRight) return true
        return operation == getNextOperation()
    }

    // gets the next operation that can be resolved correctly in the game loop
    fun getNextOperation(): BinaryExpressionComponent? {
        return validExpression?.getNextOperation()
    }

    override fun toString(): String {
        //apply common notation for multiplying with brackets
        return validExpression?.toString()?.replace("*(", "(") ?: "NaN"
    }

    fun toStringPretty(): String {
        //apply common notation for multiplying with brackets
        return validExpression?.toString()?.replace("*", "×")?.replace("/", "÷")?.replace("×(", "(")
            ?: "NaN"
    }

    fun getAsView(context: Context): View {
        return validExpression?.getAsView(this, context) ?: TextView(context)
    }


    companion object {

        // performs some validation and limitations on these settings
        // they are arbitrarily chosen and have been tweaked by what feels right
        fun createRandomExpression(difficulty: Int): ParsedExpression {
            val diff = difficulty.coerceAtLeast(1).coerceAtMost(20)
            val depth = 1 + diff / 5
            return createRandomExpression(diff, depth, 1)
        }

        //creates a random valid ParsedEquation
        private fun createRandomExpression(
            complexity: Int,
            depth: Int,
            attempts: Int
        ): ParsedExpression {
            //max 10 attempts at generating a valid random expression
            if (attempts > 10) {
                // i'm not perfect lets pick from some known good examples as this is hopefully a rare occurrence
                //println("Failed to create random expression 10 times, defaulting to known expressions")
                return parseExpressionString(listOfGoodBackupExpressions.random())
            }

            //create a random expression
            val generatedButNotValid = BinaryExpressionComponent.createRandom(complexity, depth)
            // now ew simply abandon it as it is not 'order of operations' valid
            // extract its string expression value and then validate that into a correct binary tree
            generatedButNotValid.hasBrackets = false
            val stringExpression = generatedButNotValid.toString()

            val possiblyValidExpression = parseExpressionString(stringExpression)
            return if (possiblyValidExpression.isValid()) {
                //return valid expression
                possiblyValidExpression
            } else {
                //loop this if it was invalid
                //failures are expected as we construct them lazily and could easily have a divide
                // by 0 result in the equation or some other niche case
                createRandomExpression(complexity, depth, attempts + 1)
            }
        }

        //parses a string expression, ParsedExpression(null) if invalid
        fun parseExpressionString(expression: String): ParsedExpression {
            if (expression.isBlank()) {
                return ParsedExpression(null)
            }
            try {//just in case
                val parsedResult = parseExpression(expression, false)
                if (parsedResult.isValid() && parsedResult is BinaryExpressionComponent) {
                    return ParsedExpression(parsedResult)
                }
            } catch (_: java.lang.Exception) { }
            return ParsedExpression(null)
        }




    }

    // a blank interface used for the list when parsing expression strings
    interface IMathComponent
}

private val listOfGoodBackupExpressions = listOf(
    "6/2(1+2)",
    "10*4-2*(4^2/4)/2/0.5+9",
    "-10/(20/4*5/5)*8-2",
    "8/2(2+2)",
    "30/29-99*(-120^116)",
    "33*-131*-80-(96*-98)",
    "-115/20*-113/(-25/50)",
    "(37+-56)/(106-129)/147",
    "(-16^2)+-67-74",
    "35--76^43*26",
    "(8/3)*(19^-13)",
    "((-3^9^3)--2)+(-7+8)",
    "7*(-10/9)/3-(-12-16)"
)


/**
 * This is a fun method to explain :)
 * this method accepts an expression string and if it is valid returns a valid IMathValue respresenting
 * it, this being either nested BinaryExpressionComponents or MathNumbers
 *
 * to summarise this greatly: ignoring explanations of expression validation
 *
 * this compiles a list of IMathComponent's from the expression string, these being numbers,
 * operators, and iterated returns from parseExpression() for parts of the expression inside brackets
 *
 * if the component list has only 1 value it returns that as a MathNumber
 *
 * if the component list is larger it does the following
 *
 * it resolves all negative numbers into the following MathNumber where appropriate,
 * i.e (2)(+)(-)(2) becomes  (2)(+)(-2)
 *
 * it resolves all instances of indeces (^)
 *
 * */
private fun parseExpression(expression: String, inBrackets: Boolean): IMathValue {
    //clear spaces
    val formattedExpression = expression.replace(" ", "")

    if (formattedExpression.matches(Regex.fromLiteral("[^0-9\\.\\+\\-\\*/)(]"))) {
        // expression has illegal characters
        return IMathValue.getInvalid("has illegal characters")
    }

    var components = LinkedList<ParsedExpression.IMathComponent>()

    //rolling read keeps track of the last few unused iterations of characters
    val rollingRead = java.lang.StringBuilder()

    val expressionStringIterator = formattedExpression.iterator()
    while (expressionStringIterator.hasNext()) {

        val currentCharacter = expressionStringIterator.nextChar()

        val operator = MathOperator.getFromChar(currentCharacter)
        if (operator == MathOperator.NOT_VALID) {
            //add to rolling read and continue
            rollingRead.append(currentCharacter)
        } else {

            //first catch any rolling read numbers
            if (rollingRead.isNotEmpty()) {
                val number: Double = rollingRead.toString().toDoubleOrNull()
                    ?: //invalid
                    return IMathValue.getInvalid("$rollingRead, is not a valid number")
                components.add(MathNumber(number))
                rollingRead.clear()
            }

            //rolling read always empty here
            when (operator) {
                MathOperator.BRACKET_OPEN -> {
                    // if the last component was not an operator like '+' it must be an implicit multiply like 2(2+2) = 2*(2+2) or invalid, which will get resolved later
                    if (!components.isEmpty() && components.last !is MathOperator) {
                        components.add(MathOperator.MULTIPLY)
                    }

                    //utilise rolling read to extract nested expression string
                    var nesting = 1
                    while (expressionStringIterator.hasNext()) {
                        val currentNestedCharacter = expressionStringIterator.nextChar()
                        val operatorNested = MathOperator.getFromChar(currentNestedCharacter)

                        if (operatorNested == MathOperator.BRACKET_OPEN) {
                            nesting++
                        } else if (operatorNested == MathOperator.BRACKET_CLOSED) {
                            nesting--
                            if (nesting == 0) {
                                break
                            }
                        }
                        rollingRead.append(currentNestedCharacter)

                    }
                    //is invalid
                    if (nesting != 0) return IMathValue.getInvalid("nesting did not synchronise with (")
                    //else rolling read has a fully contained nested expression
                    val nestedExpression = parseExpression(rollingRead.toString(), true)
                    if (nestedExpression !is IMathValue.InvalidValue) {
                        components.add(nestedExpression)
                        rollingRead.clear()
                    } else {
                        //invalid
                        return IMathValue.getInvalid("nested was invalid because: ${nestedExpression.why}")
                    }
                }
                MathOperator.BRACKET_CLOSED -> {
                    //should not happen this is invalid
                    return IMathValue.getInvalid("nesting did not synchronise with )")
                }

                else -> components.add(operator)
            }
        }
    }
    //catch any rolling read numbers left over for final component
    if (rollingRead.isNotEmpty()) {
        val number: Double = rollingRead.toString().toDoubleOrNull()
            ?: //invalid
            return IMathValue.getInvalid("$rollingRead, is not a valid number")
        components.add(MathNumber(number))
        rollingRead.clear()
    }

    //check components is not empty
    if (components.isEmpty()) return IMathValue.getInvalid("no components after initial object parse")


    //skip if only 1 component
    if (components.size > 1) {
        // here we should have a components object containing parsed operators numbers and nested expressions
        // now we do some tricky shit
        // lets check the formatting and see if we can settle this into a binary tree to make things easier in runtime
        // if we can resolve to binary expressions it will effectively have the bomdas ordering innately

        //todo add checks at each stage for if that step is even required, would cut down on iterations needed

        //parse out negative numbers, into inverted ImathValues
        val componentsNegative = LinkedList<ParsedExpression.IMathComponent>()
        val iteratorNegative = components.iterator()

        var nextOverride: ParsedExpression.IMathComponent? = null

        while (iteratorNegative.hasNext()) {
            val currentComponent = nextOverride ?: iteratorNegative.next()
            nextOverride = null

            if (currentComponent == MathOperator.SUBTRACT) {
                if (!iteratorNegative.hasNext()) return IMathValue.getInvalid("negative operator has no next value")
                if (componentsNegative.size == 0) {
                    val next = iteratorNegative.next()
                    if (next is IMathValue) {
                        next.invert()
                        componentsNegative.add(next)
                    } else return IMathValue.getInvalid("started with negative then followed by operator")
                } else {
                    val next = iteratorNegative.next()
                    val last = componentsNegative.last
                    if (next is IMathValue) {
                        if (last is MathOperator) {
                            next.invert()
                            componentsNegative.add(next)
                        } else {
                            componentsNegative.add(currentComponent)
                            componentsNegative.add(next)//skips a loop
                        }
                    } else {
                        componentsNegative.add(currentComponent)
                        nextOverride = next //overrides for next loop, catches "--" cases
                    }
                }
            } else {
                componentsNegative.add(currentComponent)
            }
        }
        //all negative numbers should be parsed

        // if the expression is valid the next stages should all succeed and respect negative numbers in the order of operations
        // unlike some other math libraries.. looking at you MXParser.........................................................................................................................................................................................................hi
        //MXParser thinks 2+1*-2 is (2+1)*-2 for some ungodly reason when it needs to be 2+(1*-2)


        //resolve powers into binary expression component
        //check these right to left as that is the ordering for powers when written with ^ notation
        val componentsPower = LinkedList<ParsedExpression.IMathComponent>()
        val iteratorPower = componentsNegative.reversed().iterator()
        while (iteratorPower.hasNext()) {
            val currentComponent = iteratorPower.next()
            if (currentComponent == MathOperator.POWER) {
                if (!iteratorPower.hasNext() || componentsPower.size < 1) return IMathValue.getInvalid(
                    "power doesnt have component both sides: [$components]:[$componentsNegative]"
                )
                val left = iteratorPower.next()
                val right = componentsPower.first
                if (!(left is IMathValue && right is IMathValue)) return IMathValue.getInvalid("power isnt surrounded by values: [$components]:[$componentsNegative]")
                componentsPower.removeFirst()
                componentsPower.addFirst(
                    BinaryExpressionComponent(
                        left,
                        currentComponent as MathOperator,
                        right
                    )
                )
            } else {
                componentsPower.addFirst(currentComponent)
            }
        }


        //resolve * / into binary expression component
        val componentsMD = LinkedList<ParsedExpression.IMathComponent>()
        val iteratorMD = componentsPower.iterator()
        while (iteratorMD.hasNext()) {
            val currentComponent = iteratorMD.next()
            if (currentComponent == MathOperator.MULTIPLY || currentComponent == MathOperator.DIVIDE) {
                if (!iteratorMD.hasNext() || componentsMD.size < 1) return IMathValue.getInvalid("$currentComponent, doesn't have components either side: [$components]:[$componentsPower]")
                val next = iteratorMD.next()
                val last = componentsMD.last
                if (!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only: [$components]:[$componentsPower]")
                componentsMD.removeLast()
                componentsMD.add(
                    BinaryExpressionComponent(
                        last,
                        currentComponent as MathOperator,
                        next
                    )
                )
            } else {
                componentsMD.add(currentComponent)
            }
        }

        //resolve + - into binary expression components
        val componentsFinal = LinkedList<ParsedExpression.IMathComponent>()
        val iteratorAS = componentsMD.iterator()
        while (iteratorAS.hasNext()) {
            val currentComponent = iteratorAS.next()
            if (currentComponent == MathOperator.ADD || currentComponent == MathOperator.SUBTRACT) {
                if (!iteratorAS.hasNext() || componentsFinal.size < 1) return IMathValue.getInvalid(
                    "$currentComponent, doesn't have componenets either side: [$components]:[$componentsMD]"
                )
                val next = iteratorAS.next()
                val last = componentsFinal.last
                if (!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only: [$components]:[$componentsMD]")
                componentsFinal.removeLast()
                componentsFinal.add(
                    BinaryExpressionComponent(
                        last,
                        currentComponent as MathOperator,
                        next
                    )
                )
            } else {
                componentsFinal.add(currentComponent)
            }
        }
        components = componentsFinal
    }

    //here componentsFinal should only be a list containing 1 single IMathValue
    if (components.size != 1) {
        return IMathValue.getInvalid("final didn't resolve to 1 component: $components")
    }

    // this is a IMathValue either a MathNumber or a binary tree of MathBinaryExpressionComponents
    // with each binary branch ending on a MathNumber

    return if (components.first is IMathValue) {
        if (inBrackets) (components.first as IMathValue).setBrackets()
        components.first as IMathValue
    } else {
        IMathValue.getInvalid("final wasn't a math value, likely invalid: $components")
    }
}
