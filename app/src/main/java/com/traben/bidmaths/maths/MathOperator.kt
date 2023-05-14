package com.traben.bidmaths.maths

enum class MathOperator : IMathComponent{
    ADD,
    SUBTRACT,
    START,
    MULTIPLY,
    DIVIDE,
    BRACKET_OPEN,
    BRACKET_CLOSED,
    POWER,
    NOT_VALID;

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
