package com.traben.bidmaths.math

import android.content.Context
import android.view.View
import com.traben.bidmaths.math.views.MathNumberView

/**
 *  An instance of IMathValue that simply holds a number for use in math logic
 *  the number can be negative and or surrounded by brackets
 * */
class MathNumber(private var number: Double) : IMathValue {


    override fun isValid(): Boolean {
        return !number.isNaN()
    }


    override fun getValue(): Double {
        return if (isNegative) -number else number
    }

    var hasBrackets = false
    override fun setBrackets() {
        hasBrackets = true
    }

    //presents the number for display
    // cannot use typical number formatting classes due to API level
    override fun toString(): String {
        val num = getValue()
        val string = if ((num * 100) % 1.0 != 0.0)
            //if number has more than 2 decimal places shorten it to 2 and add ".."
            String.format("%.2f..", num)
        else if ((num * 10) % 1.0 != 0.0)
            //if number has exactly 2 decimal places display them only
            String.format("%.2f", num)
        else if (num % 1.0 != 0.0)
            //if number has exactly 1 decimal place display that only
            String.format("%.1f", num)
        else
            //if number has no decimal places display none
            String.format("%.0f", num)
        return if (hasBrackets) "($string)" else string
    }


    override var isNegative = false

    override fun invert() {
        isNegative = !isNegative
    }

    //returns a MathNumberView representing this object
    override fun getAsView(expressionObject: ParsedExpression, context: Context): View {
        return MathNumberView(this, context)
    }

    //simplified method used when parsed equation not needed or relevant
    // used in binary expression component resolution
    fun getAsView(context: Context): View {
        return MathNumberView(this, context)
    }

}