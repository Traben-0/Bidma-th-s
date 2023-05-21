package com.traben.bidmaths.math

import android.content.Context
import android.view.View
import android.view.ViewParent
import android.widget.LinearLayout
import com.traben.bidmaths.math.views.MathBinaryExpressionView
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 *  An instance of IMathValue that holds 2 IMathValue's and the math operation that is applied to them
 *  This object is the basic for a nested binary tree detailing a maths equation and the tree is
 *  ordered according to the order of operations with the left most lowest unresolved branch being
 *  first in that order.
 *
 *  Illustration of the resultant binary tree for the expression  "2 * 4 - 8 / (4 + 2)"
 *
 *                    (-)
 *                  /     \
 *                (*)     (/)
 *               /  \    /   \
 *              2   4   8   (+)
 *                         /   \
 *                        4    2
 *
 *  Reading the tree shows the implicit order of operations as you start at the top and solve the
 *  (-) subtraction expression, you can see the left hand (*) expression isn't resolved so we do that first
 *  which is (2 * 4) and is first in the order of operations.
 *  next we solve the right hand side of the (-) which is a division (/) but before the division can
 *  resolve we need to solve the addition (+), because it was in brackets (4+2).
 *
 *  We also utilise this nested binary tree approach to construct the views for this expression in
 *  the game
 * */
class BinaryExpressionComponent(
    val valueOne: IMathValue,   // the left value of the expression
    val operator: MathOperator, // the operation of this binary expression, to be applied to valueOne & valueTwo
    val valueTwo: IMathValue    // the right value of the expression
) : IMathValue {

    override var isNegative = false

    override fun invert() {
        isNegative = !isNegative
    }


    //check this is valid and also iterate down into our two values
    override fun isValid(): Boolean {
        if (valueOne.isValid() && valueTwo.isValid()) {
            //check we aren't trying to divide by 0
            return !(operator == MathOperator.DIVIDE && (valueTwo.getValue() == 0.0 || valueTwo.getValue() == -0.0))
        }
        return false
    }


    var hasBrackets: Boolean = false
    override fun setBrackets() {
        hasBrackets = true
    }

    // the resolving feature is used in the main game loop
    // allows us to skip future processing when not needed
    // and has functionality when updating the game display
    override fun getValue(): Double {
        if (isResolved()) resolved
        val result = operator.performOperation(valueOne, valueTwo)
        return if (isNegative) -result else result
    }

    private var resolved: Double? = null

    override fun isResolved(): Boolean {
        return resolved != null
    }

    //blocks this expression from resolving if both values aren't resolved yet
    // meaning we are not solving in the correct order and user has chosen wrong
    fun canResolve(): Boolean {
        return valueOne.isResolved() && valueTwo.isResolved()
    }

    // the main gameplay loop revolves around the user selecting operators in the correct order to
    // solve the equation.
    // this method is ONLY run after canResolve() = true && another optional condition is met
    fun resolve(thisView: MathBinaryExpressionView) {
        //find the upper container view for this expression component view
        val parentView: ViewParent? = thisView.parent?.parent?.parent

        //mark this expression as resolved by assigning it's final value
        resolved = getValue()

        if (parentView is MathBinaryExpressionView) {
            //if this expression is nested within another we need to tell that expression view to
            // update its views to reflect the now resolved value within it
            parentView.update()
        } else {
            //otherwise if this is the top level expression of the tree simply replace the
            //MathBinaryExpressionView with the final resolved number view
            val holder = thisView.parent
            if (holder is LinearLayout) {
                holder.removeAllViews()
                holder.addView(MathNumber(resolved!!).getAsView(holder.context))
            }
        }

    }

    override fun toString(): String {
        return if (hasBrackets) "($valueOne$operator$valueTwo)" else "$valueOne$operator$valueTwo"
    }


    //returns this expression component as a view, with possibly further nested expression views and
    // number views
    // notice that a resolved expression returns a numberView of its result, closing that branch of
    // the tree
    override fun getAsView(expressionObject: ParsedExpression, context: Context): View {
        if (isResolved()) {
            return MathNumber(resolved!!).getAsView(context)
        }
        return MathBinaryExpressionView(expressionObject, this, context)
    }

    // this is an iterating method that searches the binary tree for the first component that needs
    // to be solved if we are optionally enforcing "left to right" solving
    // returns the first component that is resolvable, checking in order of left, then right, then self
    fun getNextOperation(): BinaryExpressionComponent? {

        if (!valueOne.isResolved() && valueOne is BinaryExpressionComponent) {
            val oneOrNull = valueOne.getNextOperation()
            if (oneOrNull != null)
                return oneOrNull
        }
        if (!valueTwo.isResolved() && valueTwo is BinaryExpressionComponent) {
            val twoOrNull = valueTwo.getNextOperation()
            if (twoOrNull != null)
                return twoOrNull
        }
        if (canResolve())
            return this
        return null


    }

    companion object {

        //creates and returns a randomly generated binary expression component
        // this component is NOT valid, it is ordered randomly and is useless...
        // except for the string it can return :)
        // so to generate random expressions I make nonsense ones then extract the strings to be
        // parsed into validly ordered expression trees
        // this will be iterated over for high depth expressions to lengthen the final resulting tree
        fun createRandom(difficulty: Int, maxDepth: Int): BinaryExpressionComponent {

            val randomFunctionalOperator = MathOperator.getRandomFunctional()

            //simplify the number values so as to not get stupidly big powers and divisions
            val secondValue = if (randomFunctionalOperator == MathOperator.POWER || randomFunctionalOperator == MathOperator.DIVIDE) {
                createRandomMathValueSimplified()
            } else {
                createRandomMathValue(difficulty, maxDepth)
            }

            val comp = BinaryExpressionComponent(
                createRandomMathValue(difficulty, maxDepth),
                randomFunctionalOperator,
                secondValue
            )

            //odds of these are arbitrarily chosen, could be improved with feedback
            // brackets being common is highly desired
            val addBrackets: Boolean = Random.nextInt(2) == 1
            val makeNegative: Boolean = Random.nextInt(25) == 1

            if (addBrackets) comp.setBrackets()
            if (makeNegative) comp.invert()

            return comp
        }

        //create a random IMathValue for the binary expression
        // this can be either a number ending the nested tree or another binary extending it
        private fun createRandomMathValue(difficulty: Int, maxDepth: Int): IMathValue {
            //forcibly cut off the iteration at an upper limit
            if (maxDepth < -1) return MathNumber(createRandomNumberByDifficulty(difficulty))

            //force the equation to be larger at a certain depth threshold
            if (maxDepth > 0) return createRandom(difficulty, maxDepth - 1)

            //determines whether to end the nesting with a value or continue with expression binaries,
            return if (Random.nextInt(6) == 1) {
                //less likely
                createRandom(difficulty, maxDepth - 1)
            } else {
                //more likely
                MathNumber(createRandomNumberByDifficulty(difficulty))

            }

        }

        //as above but simplified numbers and limited depth continuation
        private fun createRandomMathValueSimplified(): IMathValue {
            //determines whether to end the nesting with a value or continue,
            return if (Random.nextInt(6) == 1) {
                //less likely
                createRandom(0, -1)
            } else {
                //more likely
                MathNumber(createRandomNumberByDifficulty(0))

            }

        }

        //create and return a random number based on the requested complexity
        private fun createRandomNumberByDifficulty(complexity: Int): Double {
            //return simple 0-10
            if (complexity <= 0 ) {
                return (Random.nextInt(11)).toDouble()
            }
            //get a number arbitrarily large set by complexity
            var number: Double =
                ((Random.nextFloat() * 2 - 1) * (complexity * 10)).roundToInt().toDouble()

            //set simple floating point values never more than 2 digits
            // more likely at higher complexity
            if (Random.nextBoolean() && Random.nextInt(11 / complexity + 1) < 1) {
                number += if (complexity > 10) {
                    //two digits
                    Random.nextInt(100) / 100.0
                } else {
                    //one digit
                    Random.nextInt(10) / 10.0
                }
            }
            return number
        }

    }
}