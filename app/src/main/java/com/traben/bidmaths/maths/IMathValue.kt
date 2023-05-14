package com.traben.bidmaths.maths

interface IMathValue : IMathComponent {
    fun get(): Float
    fun invert()

    companion object{

        fun getInvalid(why: String) : InvalidValue {
            return InvalidValue(why)
        }

    }

    class InvalidValue(val why : String) : IMathValue{
        override fun get(): Float {
            return Float.NaN
        }

        override fun isValid(): Boolean {
            return false
        }

        override fun toString(): String {
            return "[InvalidValue: $why]"
        }
        override fun invert() {
        }
    }
}