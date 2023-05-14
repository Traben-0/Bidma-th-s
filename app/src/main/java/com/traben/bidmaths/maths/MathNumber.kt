package com.traben.bidmaths.maths

class MathNumber(var number : Float) : IMathValue {




    override fun get(): Float {
        return number
    }

    override fun invert() {
        number = -number
    }
}