package com.traben.bidmaths.maths

import android.content.Context
import android.view.View
import com.traben.bidmaths.maths.views.MathNumberView

class MathNumber(var number : Double) : IMathValue {


    override fun isValid(): Boolean {
        return !number.isNaN()
    }

    override fun getValue(): Double {
        return if (isNegative) -number else number
    }

    public var hasBrackets = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun isResolved(): Boolean {
        return true
    }


    override fun toString(): String {
        val num = getValue()
        val string = if ((num *100)% 1.0 != 0.0)
            String.format("%.2f..", num)
        else  if (num % 1.0 != 0.0)
             String.format("%.2f", num)
        else if ((num * 10) % 1.0 != 0.0)
            String.format("%.1f", num)
        else
             String.format("%.0f", num)
        return if(hasBrackets) "($string)" else string
    }




    override var isNegative = false

    override fun invert(){
        isNegative = ! isNegative
    }


    override fun getAsView(expressionObject : ParsedMathEquation, context: Context): View {
        return MathNumberView(this, context)
    }
    fun getAsView( context: Context): View {
        return MathNumberView(this, context)
    }

}