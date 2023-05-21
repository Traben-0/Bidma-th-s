package com.traben.bidmaths.math

import android.content.Context
import android.view.View
import android.widget.TextView

/**
 * interface refering to any object that is valid to be held within a BinaryExpressionComponent
 *  these being, MathNumber & BinaryExpressionComponent itself
 * */
interface IMathValue : ParsedExpression.IMathComponent {


    //negative number handling logic
    var isNegative: Boolean
    fun invert()


    fun isValid(): Boolean {
        return false
    }

    //sets the value to be encased by brackets
    fun setBrackets()

    // returns the result if this value including any nested expression results
    // the way these are built implicitly gives the result correct ordering
    fun getValue(): Double

    // checks if this component has already been resolved in this game round
    // only applicable to the binary component but saves a cast if the method is valid for both
    fun isResolved(): Boolean {
        return true
    }

    //returns this object as a valid View
    fun getAsView(expressionObject: ParsedExpression, context: Context): View

    companion object {

        //see below
        fun getInvalid(why: String): InvalidValue {
            return InvalidValue(why)
        }

    }

    // this invalid value instance is used in equation validation where a valid IMathValue is expected,
    // it is then caught later in the process and has it's why parameter printed to give the cause
    class InvalidValue(val why: String) : IMathValue {

        override fun isValid(): Boolean {
            return false
        }

        override fun setBrackets() {
        }

        override fun getValue(): Double {
            return Double.NaN
        }


        override fun getAsView(expressionObject: ParsedExpression, context: Context): View {
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