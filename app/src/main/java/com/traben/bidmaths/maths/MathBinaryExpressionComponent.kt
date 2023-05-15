package com.traben.bidmaths.maths

import android.content.Context
import android.view.View
import android.view.ViewParent
import com.traben.bidmaths.maths.views.MathBinaryExpressionView
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

class MathBinaryExpressionComponent(
    var valueOne : IMathValue,
    var operator : MathOperator,
    var valueTwo : IMathValue
    ) : IMathValue {

    override var isNegative = false

    override fun invert() {
        isNegative = !isNegative
    }




    override fun isValid(): Boolean {
        if (valueOne.isValid() && valueTwo.isValid()) {
            //check divide by 0
            return !(operator == MathOperator.DIVIDE && (valueTwo.getValue() == 0f || valueTwo.getValue() == -0f ))
        }
        return false
    }


    var hasBrackets : Boolean = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun getValue(): Float {
        if(isResolved()) resolved
        val result = operator.performOperation(valueOne, valueTwo)
        return if (isNegative) -result else result
    }

    var resolved: Float? = null

    override fun isResolved(): Boolean {
        return resolved != null
    }

    fun canResolve() : Boolean{
        return valueOne.isResolved() && valueTwo.isResolved()
    }
    fun resolve(parentView : ViewParent?) {
        if(canResolve()) {
            resolved = getValue()
            if (parentView is MathBinaryExpressionView) {
                parentView.update()
            } else {
                println("done")
            }
        }else{
            println("nope")
        }
    }

    override fun toString(): String {
        return if (hasBrackets) "($valueOne$operator$valueTwo)" else "$valueOne$operator$valueTwo"
    }


    override fun getAsView(context: Context): View {
        if (isResolved()) {
            return MathNumber(resolved!!).getAsView(context)
        }

        return MathBinaryExpressionView(this,context)
    }

    companion object {

        fun getRandom(difficulty: Int, maxDepth: Int): MathBinaryExpressionComponent {

            val op = MathOperator.getRandom(difficulty)
            //simplify it to not get stupid big powers
            val second = if(op== MathOperator.POWER){
                genRandomValueSimple()
            }else{
                genRandomValue(difficulty, maxDepth)
            }
            val comp = MathBinaryExpressionComponent(
                genRandomValue(difficulty, maxDepth),
                op,
                second
            )

            val addBrackets: Boolean = Random.nextInt(2) == 1
            val negative: Boolean = Random.nextInt(25) == 1

            if(addBrackets) comp.setBrackets()
            if(negative) comp.invert()

            return comp
        }

        private fun genRandomValue(difficulty: Int, maxDepth: Int): IMathValue {
            //forcibly cut off the iteration at an upper limit
                if(maxDepth < -1) return MathNumber(genNumberByDifficulty(difficulty))
                if(maxDepth >0 ) return getRandom(difficulty, maxDepth-1)

            //determines whether to end the nesting with a value or continue, the one that is more likely is determined by max depth
                return if (Random.nextInt(6) == 1) {
                    //less likely
                    getRandom(difficulty, maxDepth-1)
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
                getRandom(difficulty, maxDepth-1)
            } else {
                //more likely
                MathNumber(genNumberByDifficulty(difficulty))

            }

        }

        private fun genNumberByDifficulty(difficulty: Int): Float {
            if(difficulty==0){
                return ((Random.nextFloat()*2-1) * 10).roundToInt().toFloat()
            }
            //get a number arbitrarily large set by difficulty
            var number : Float = ((Random.nextFloat()*2-1) * (difficulty*10)).toInt().toFloat()

            //set simple floating point values never more than 2 digits
            if(Random.nextBoolean() && Random.nextInt(11/difficulty.absoluteValue+1) < 1){
                number += if (difficulty > 10){
                    //two digits
                    Random.nextInt(100)/100
                }else{
                    //one digit
                    Random.nextInt(10)/10
                }
            }
            return number
        }

    }
}