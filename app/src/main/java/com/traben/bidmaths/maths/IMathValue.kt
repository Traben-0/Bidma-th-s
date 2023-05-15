package com.traben.bidmaths.maths

interface IMathValue : ParsedMathEquation.IMathComponent {

    var isNegative: Boolean
    fun invert()

    fun isValid() : Boolean {return false}

    fun setBrackets()

    fun getValue() : Float


    companion object{

        fun getInvalid(why: String) : InvalidValue {
            return InvalidValue(why)
        }

    }

    class InvalidValue(val why : String) : IMathValue{

        override fun isValid(): Boolean {
            return false
        }

        override fun setBrackets() {
        }

        override fun getValue(): Float {
            return  Float.NaN
        }

        override fun toString(): String {
            return "[InvalidValue: $why]"
        }

        override var isNegative: Boolean
            get() = false
            set(value) {}

        override fun invert() {
        }
    }
}