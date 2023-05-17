package com.traben.bidmaths.maths

import android.content.Context
import android.view.View
import android.widget.TextView

interface IMathValue : ParsedMathEquation.IMathComponent, java.io.Serializable {

    var isNegative: Boolean
    fun invert()

    fun isValid() : Boolean {return false}

    fun setBrackets()

    fun getValue() : Double


    fun isResolved ():Boolean {return false}




    fun getAsView(expressionObject : ParsedMathEquation, context: Context) : View
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

        override fun getValue(): Double {
            return  Double.NaN
        }




        override fun getAsView(expressionObject : ParsedMathEquation,context: Context): View {
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