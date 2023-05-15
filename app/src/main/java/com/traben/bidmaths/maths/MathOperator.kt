package com.traben.bidmaths.maths

import kotlin.math.pow

enum class MathOperator : ParsedMathEquation.IMathComponent {
    ADD,
    SUBTRACT,
    START,
    MULTIPLY,
    DIVIDE,
    BRACKET_OPEN,
    BRACKET_CLOSED,
    POWER,
    NOT_VALID;

    fun performOperation(valueOne : IMathValue, valueTwo: IMathValue) : Float{

        return when(this){
                ADD -> valueOne.getValue() + valueTwo.getValue()
                SUBTRACT -> valueOne.getValue() - valueTwo.getValue()
                MULTIPLY -> valueOne.getValue() * valueTwo.getValue()
                DIVIDE -> valueOne.getValue() / valueTwo.getValue()
                POWER -> {
                    //do a little bit of trickiness due to how -2^-2 needs to parse as -(2^-2) and not (-2)^-2 which is what my code does implicitly
                    if(valueOne.isNegative){
                        // technically this is -((-(-2))^-2) in the example of -2^-2
                        // the way I wanted to cheat and simplify negative numbers mean i need to do this
                        -((-valueOne.getValue()).pow(valueTwo.getValue()))
                    }else{
                        valueOne.getValue().pow(valueTwo.getValue())
                    }


                }
                else -> Float.NaN
        }

    }


    companion object {
        fun get(testChar: Char) : MathOperator{
            return when(testChar){
                '+'-> ADD
                '-'-> SUBTRACT
                '*'-> MULTIPLY
                '/'-> DIVIDE
                '('-> BRACKET_OPEN
                ')'-> BRACKET_CLOSED
                '^'-> POWER
                else -> NOT_VALID
            }
        }
    }
}
