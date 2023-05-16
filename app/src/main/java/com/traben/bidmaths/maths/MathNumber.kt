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
        val string = if (num % 1.0 != 0.0)
             String.format("%.2f..", num)
        else
             String.format("%.0f", num)
        return if(hasBrackets) "($string)" else string
    }
    fun toStringPretty(): String {
        val num = getValue()
        val stringParts = num.toString().split(".")

        var decimal = stringParts[1]
        if(decimal.isNotBlank()){
            decimal = when{
                num % 1.0 == 0.0 -> ""
                decimal.length > 2 -> "${decimal[0]}${decimal[1]}.."
                decimal.length == 1 -> "${decimal[0]}"
                else -> decimal
            }
        }

        var number = stringParts[0]
        val groupsOfThree: Double = number.removePrefix("-").length/3.0
        if(groupsOfThree > 1.3){
            number = number.replace(Regex("\\\\?([\\d]{3}\$)"),",")
            for (i in 0.. number.toInt()){
                "[^,]\\\\?([0-9]{3},)"
            }
        }

        if(decimal.isNotEmpty()){
            number = "$number.$decimal"
        }

        return if(hasBrackets) "($number)" else number
    }



    override var isNegative = false

    override fun invert(){
        isNegative = ! isNegative
    }


    override fun getAsView(context: Context): View {
        return MathNumberView(this, context)
    }

}