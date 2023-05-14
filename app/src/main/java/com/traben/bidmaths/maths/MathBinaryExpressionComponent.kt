package com.traben.bidmaths.maths

class MathBinaryExpressionComponent(
    var valueOne : IMathValue,
    var operator : MathOperator,
    var valueTwo : IMathValue
    ) : IMathValue   {

    var isNegative = false

    public override fun invert(){
        isNegative = ! isNegative
    }

    public fun isThisOperationReady() : Boolean{
        //todo possibly further actions here dont simplify
        if (valueOne is MathBinaryExpressionComponent || valueTwo is MathBinaryExpressionComponent)
            return false
        return true
    }


    override fun get(): Float {
        TODO("Not yet implemented")
    }
}