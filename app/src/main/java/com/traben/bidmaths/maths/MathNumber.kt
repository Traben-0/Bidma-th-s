package com.traben.bidmaths.maths

class MathNumber(var number : Float) : IMathValue {


    override fun isValid(): Boolean {
        return !number.isNaN()
    }

    override fun getValue(): Float {
        return if (isNegative) -number else number
    }

    var hasBrackets = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun toString(): String {
        return "[#:${getValue()}]"
    }
    override var isNegative = false

    override fun invert(){
        isNegative = ! isNegative
    }

}