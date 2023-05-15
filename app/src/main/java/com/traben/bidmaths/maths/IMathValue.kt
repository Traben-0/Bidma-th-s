package com.traben.bidmaths.maths

import android.content.Context
import android.view.View
import android.widget.TextView

interface IMathValue : ParsedMathEquation.IMathComponent {

    var isNegative: Boolean
    fun invert()

    fun isValid() : Boolean {return false}

    fun setBrackets()

    fun getValue() : Float


    fun isResolved ():Boolean {return false}



    fun getAsView(context: Context) : View
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



        override fun getAsView(context: Context): View {
            return TextView(context)
        }


        override fun toString(): String {
            return "[InvalidValue: $why]"
        }

        override var isNegative: Boolean = false

        override fun invert() {
        }
    }
}