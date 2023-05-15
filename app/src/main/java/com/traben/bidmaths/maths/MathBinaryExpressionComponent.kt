package com.traben.bidmaths.maths

class MathBinaryExpressionComponent(
    var valueOne : IMathValue,
    var operator : MathOperator,
    var valueTwo : IMathValue
    ) : IMathValue   {

    override var isNegative = false

    override fun invert(){
        isNegative = ! isNegative
    }

    public fun isThisOperationReady() : Boolean{
        //todo possibly further actions here dont simplify
        if (valueOne is MathBinaryExpressionComponent || valueTwo is MathBinaryExpressionComponent)
            return false
        return true
    }


    override fun isValid(): Boolean {
        if(valueOne.isValid() && valueTwo.isValid()) {
            //check divide by 0
            return !(operator == MathOperator.DIVIDE && valueTwo.getValue() == 0f)
        }
        return false
    }

    var hasBrackets = false
    override fun setBrackets() {
        hasBrackets = true
    }

    override fun getValue(): Float {
        val result = operator.performOperation(valueOne,valueTwo)
        return if(isNegative) -result else result
    }



}