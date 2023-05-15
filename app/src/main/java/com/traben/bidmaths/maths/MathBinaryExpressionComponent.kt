package com.traben.bidmaths.maths

import kotlin.math.absoluteValue
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

    public fun isThisOperationReady(): Boolean {
        //todo possibly further actions here dont simplify
        if (valueOne is MathBinaryExpressionComponent || valueTwo is MathBinaryExpressionComponent)
            return false
        return true
    }


    override fun isValid(): Boolean {
        if (valueOne.isValid() && valueTwo.isValid()) {
            //check divide by 0
            return !(operator == MathOperator.DIVIDE && valueTwo.getValue() == 0f)
        }
        return false
    }

    var hasBrackets = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun getValue(): Float {
        val result = operator.performOperation(valueOne, valueTwo)
        return if (isNegative) -result else result
    }

    override fun toString(): String {
        return if (hasBrackets) "($valueOne$operator$valueTwo)" else "$valueOne$operator$valueTwo"
    }

    companion object {

        fun getRandom(difficulty: Int, maxDepth: Int): MathBinaryExpressionComponent {
            val comp = MathBinaryExpressionComponent(
                genRandomValue(difficulty, maxDepth),
                MathOperator.getRandom(difficulty),
                genRandomValue(difficulty, maxDepth)
            )

            val addBrackets: Boolean = Random.nextInt(2) == 1
            val negative: Boolean = Random.nextInt(25) == 1

            if(addBrackets) comp.setBrackets()
            if(negative) comp.invert()

            return comp
        }

        private fun genRandomValue(difficulty: Int, maxDepth: Int): IMathValue {
                //determines whether to end the nesting with a value or continue, the one that is more likely is determined by max depth
                return if (Random.nextInt(3) == 1) {
                    //less likely
                    if (maxDepth > 0) {
                        MathNumber(genNumberByDifficulty(difficulty))
                    }else{
                        getRandom(difficulty, maxDepth-1)
                    }
                } else {
                    //more likely
                    if (maxDepth > 0) {
                        getRandom(difficulty, maxDepth-1)
                    }else{
                        MathNumber(genNumberByDifficulty(difficulty))
                    }
                }

        }

        private fun genNumberByDifficulty(difficulty: Int): Float {
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