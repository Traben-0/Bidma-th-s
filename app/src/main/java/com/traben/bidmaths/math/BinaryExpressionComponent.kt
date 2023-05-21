package com.traben.bidmaths.math

import android.content.Context
import android.view.View
import android.view.ViewParent
import android.widget.LinearLayout
import com.traben.bidmaths.math.views.MathBinaryExpressionView
import kotlin.math.absoluteValue
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
    val valueOne: IMathValue,
    val operator: MathOperator,
    val valueTwo: IMathValue
) : IMathValue {

    override var isNegative = false

    override fun invert() {
        isNegative = !isNegative
    }


    override fun isValid(): Boolean {
        if (valueOne.isValid() && valueTwo.isValid()) {
            //check divide by 0
            return !(operator == MathOperator.DIVIDE && (valueTwo.getValue() == 0.0 || valueTwo.getValue() == -0.0))
        }
        return false
    }


    var hasBrackets: Boolean = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun getValue(): Double {
        if (isResolved()) resolved
        val result = operator.performOperation(valueOne, valueTwo)
        return if (isNegative) -result else result
    }

    private var resolved: Double? = null

    override fun isResolved(): Boolean {
        return resolved != null
    }


    fun canResolve(): Boolean {
        return valueOne.isResolved() && valueTwo.isResolved()
    }

    fun resolve(thisView: MathBinaryExpressionView) {

        val parentView: ViewParent? = thisView.parent?.parent?.parent
        if (canResolve()) {
            resolved = getValue()
            if (parentView is MathBinaryExpressionView) {
                parentView.update()
            } else {
                val holder = thisView.parent
                if (holder is LinearLayout) {
                    holder.removeAllViews()
                    holder.addView(MathNumber(resolved!!).getAsView(holder.context))
                }
            }
        }
    }

    override fun toString(): String {
        return if (hasBrackets) "($valueOne$operator$valueTwo)" else "$valueOne$operator$valueTwo"
    }


    override fun getAsView(expressionObject: ParsedEquation, context: Context): View {
        if (isResolved()) {
            return MathNumber(resolved!!).getAsView(context)
        }

        return MathBinaryExpressionView(expressionObject, this, context)
    }

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

        fun getRandom(difficulty: Int, maxDepth: Int): BinaryExpressionComponent {

            val op = MathOperator.getRandom()
            //simplify it to not get stupidly big powers and divisions
            val second = if (op == MathOperator.POWER || op == MathOperator.DIVIDE) {
                genRandomValueSimple()
            } else {
                genRandomValue(difficulty, maxDepth)
            }
            val comp = BinaryExpressionComponent(
                genRandomValue(difficulty, maxDepth),
                op,
                second
            )

            val addBrackets: Boolean = Random.nextInt(2) == 1
            val negative: Boolean = Random.nextInt(25) == 1

            if (addBrackets) comp.setBrackets()
            if (negative) comp.invert()

            return comp
        }

        private fun genRandomValue(difficulty: Int, maxDepth: Int): IMathValue {
            //forcibly cut off the iteration at an upper limit
            if (maxDepth < -1) return MathNumber(genNumberByDifficulty(difficulty))
            if (maxDepth > 0) return getRandom(difficulty, maxDepth - 1)

            //determines whether to end the nesting with a value or continue, the one that is more likely is determined by max depth
            return if (Random.nextInt(6) == 1) {
                //less likely
                getRandom(difficulty, maxDepth - 1)
            } else {
                //more likely
                MathNumber(genNumberByDifficulty(difficulty))

            }

        }

        private fun genRandomValueSimple(): IMathValue {
            val maxDepth = 0
            val difficulty = 0

            //determines whether to end the nesting with a value or continue, the one that is more likely is determined by max depth
            return if (Random.nextInt(6) == 1) {
                //less likely
                getRandom(difficulty, maxDepth - 1)
            } else {
                //more likely
                MathNumber(genNumberByDifficulty(difficulty))

            }

        }

        private fun genNumberByDifficulty(difficulty: Int): Double {
            if (difficulty == 0) {
                return ((Random.nextFloat() * 2 - 1) * 10).roundToInt().toDouble()
            }
            //get a number arbitrarily large set by difficulty
            var number: Double =
                ((Random.nextFloat() * 2 - 1) * (difficulty * 10)).toInt().toDouble()

            //set simple floating point values never more than 2 digits
            if (Random.nextBoolean() && Random.nextInt(11 / difficulty.absoluteValue + 1) < 1) {
                number += if (difficulty > 10) {
                    //two digits
                    Random.nextInt(100) / 100
                } else {
                    //one digit
                    Random.nextInt(10) / 10
                }
            }
            return number
        }

    }
}