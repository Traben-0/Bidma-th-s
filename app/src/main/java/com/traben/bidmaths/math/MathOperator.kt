package com.traben.bidmaths.math

import kotlin.math.pow

/**
 * this class represents all non number values in a valid math equation for this app
 * this is used for functionality in the BinaryExpressionComonents
 * but is also primarily used in the validation and creation steps of the math parsing
 *
 * the FUNCTIONAL values have use in the final math equation object
 * the NON FUNCTIONAL values are only utilized during equation parsing
 * */
enum class MathOperator : ParsedExpression.IMathComponent {
    //FUNCTIONAL
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    POWER,

    //NON FUNCTIONAL
    BRACKET_OPEN,
    BRACKET_CLOSED,
    NOT_VALID;

    //this method performs the given math Operation on the two given inputs
    fun performOperation(valueOne: IMathValue, valueTwo: IMathValue): Double {

        return when (this) {
            ADD -> valueOne.getValue() + valueTwo.getValue()
            SUBTRACT -> valueOne.getValue() - valueTwo.getValue()
            MULTIPLY -> valueOne.getValue() * valueTwo.getValue()
            DIVIDE -> valueOne.getValue() / valueTwo.getValue()
            POWER -> {
                //do a little bit of trickiness due to how -2^-2 needs to parse as -(2^-2) and not (-2)^-2 which is what my code does implicitly
                if (valueOne.isNegative) {
                    // technically this is -((-(-2))^-2) in the example of -2^-2
                    // the way I wanted to cheat and simplify negative numbers mean i need to do this
                    -((-valueOne.getValue()).pow(valueTwo.getValue()))
                } else {
                    valueOne.getValue().pow(valueTwo.getValue())
                }


            }
            //all other values are NON FUNCTIONAL
            else -> Double.NaN
        }

    }

    override fun toString(): String {
        return when (this) {
            ADD -> "+"
            SUBTRACT -> "-"
            MULTIPLY -> "*"
            DIVIDE -> "/"
            POWER -> "^"
            else -> "!!"
        }
    }

    // uses the common syntax for multiplication and division
    // used for display
    fun toStringPretty(): String {
        return when (this) {
            ADD -> "+"
            SUBTRACT -> "-"
            MULTIPLY -> "×"
            DIVIDE -> "÷"
            POWER -> "^"
            else -> "!!"
        }
    }


    companion object {

        fun getRandomFunctional(): MathOperator {
            return listOf(ADD,SUBTRACT,MULTIPLY,DIVIDE,POWER).random()
        }

        fun getFromChar(testChar: Char): MathOperator {
            return when (testChar) {
                '+' -> ADD
                '-' -> SUBTRACT
                '*' -> MULTIPLY
                '/' -> DIVIDE
                '^' -> POWER
                '(' -> BRACKET_OPEN
                ')' -> BRACKET_CLOSED
                else -> NOT_VALID
            }
        }
    }
}
