package com.traben.bidmaths.maths

import java.util.*


class ParsedMathEquation( val validExpression: IMathValue?){


    fun getAnswer() : Float{
        return validExpression?.getValue() ?: Float.NaN
    }

    fun isValid() : Boolean {
        return !getAnswer().isNaN()
    }


    companion object{

        fun createRandomExpression( difficulty : Int,size : Int) : ParsedMathEquation{
            return  createRandomExpression(difficulty,size,1)
        }
        private fun createRandomExpression( difficulty : Int,size : Int, iterations : Int) : ParsedMathEquation{
            //max 10 attempts at generating a valid random expression
            if(iterations > 10) return ParsedMathEquation(null)

            val diff = difficulty.coerceAtLeast(1).coerceAtMost(20)
            val depth = size.coerceAtLeast(0).coerceAtMost(3)
            //create a random structures expression
            val generatedButNotValid = MathBinaryExpressionComponent.getRandom(diff,depth)
            //now simply abandon it as it is likely not valid
            // extract its string expression value and then validate that
            generatedButNotValid.hasBrackets=false
            val stringExpression = generatedButNotValid.toString()

            val possiblyValidExpression = parseExpressionAndPrepare(stringExpression)
            return if(possiblyValidExpression.isValid()){
                possiblyValidExpression
            }else{
                //loop if was invalid
                println("Failed #$iterations: $stringExpression")
                createRandomExpression(difficulty,size,iterations+1)
            }
        }

        fun parseExpressionAndPrepare(expression : String) : ParsedMathEquation{
            if(expression.isBlank()){
                return ParsedMathEquation(null)
            }
            try {//just in case
                val parsedResult = parseExpression(expression, false)
                if(parsedResult.isValid()){
                    return ParsedMathEquation(parsedResult)
                }else {
                    println("FAILED: $parsedResult")
                }
            }catch(e: java.lang.Exception){
                println("FAILED: ${e.cause}")
            }
            return ParsedMathEquation(null)
        }



        //parses a string expression, returning null if invalid

    }
    interface IMathComponent
}
private fun parseExpression(expression : String, inBrackets : Boolean) : IMathValue{
    //clear spaces
    val formattedExpression = expression.replace(" ","")

    if(formattedExpression.matches(Regex.fromLiteral("[^0-9\\.\\+\\-\\*/)(]"))){
        // expression has illegal characters
        return IMathValue.getInvalid("has illegal characters")
    }

    var components = LinkedList<ParsedMathEquation.IMathComponent>()

    //var isValid = true
    val rollingRead = java.lang.StringBuilder()

    val stringIterator = formattedExpression.iterator()
    while(stringIterator.hasNext()){

        val currentCharacter = stringIterator.nextChar()

        val operator = MathOperator.get(currentCharacter)
        if(operator == MathOperator.NOT_VALID){
            //add to rolling read and continue
            rollingRead.append(currentCharacter)
        }else{

            //first catch any rolling read numbers
            if(rollingRead.isNotEmpty()){
                val number : Float = rollingRead.toString().toFloatOrNull()
                    ?: //invalid
                    return IMathValue.getInvalid("${rollingRead.toString()}, is not a valid number")
                components.add(MathNumber(number))
                rollingRead.clear()
            }

            //rolling read always empty here
            when(operator){
                MathOperator.BRACKET_OPEN -> {
                    // if the last component was not an operator like '+' it must be an implicit multiply like 2(2+2) = 2*(2+2) or invalid, which will get resolved later
                    if(components.last !is MathOperator){
                        components.add(MathOperator.MULTIPLY)
                    }

                    //utilise rolling read to extract nested expression string
                    var nesting = 1;
                    while(stringIterator.hasNext()){
                        val currentNestedCharacter = stringIterator.nextChar()
                        val operatorNested = MathOperator.get(currentNestedCharacter)

                        if(operatorNested == MathOperator.BRACKET_OPEN ) {
                            nesting++
                        }else if (operatorNested == MathOperator.BRACKET_CLOSED ){
                            nesting--
                            if(nesting == 0){
                                break
                            }
                        }
                        rollingRead.append(currentNestedCharacter)

                    }
                    //is invalid
                    if(nesting != 0) return IMathValue.getInvalid("nesting did not synchronise with (")
                    //else rolling read has a fully contained nested expression
                    val nestedExpression  = parseExpression(rollingRead.toString(),true)
                    if(nestedExpression !is IMathValue.InvalidValue){
                        components.add(nestedExpression)
                        rollingRead.clear()
                    } else{
                        //invalid
                        return IMathValue.getInvalid("nested was invalid because: ${nestedExpression.why}")
                    }
                }
                MathOperator.BRACKET_CLOSED -> {
                    //should not happen this is invalid
                    return IMathValue.getInvalid("nesting did not synchronise with )")
                }

                else -> components.add(operator)
            }
        }
    }
    //catch any rolling read numbers left over for final component
    if(rollingRead.isNotEmpty()){
        val number : Float = rollingRead.toString().toFloatOrNull()
            ?: //invalid
            return IMathValue.getInvalid("${rollingRead.toString()}, is not a valid number")
        components.add(MathNumber(number))
        rollingRead.clear()
    }

    //check components is not empty
    if(components.isEmpty()) return IMathValue.getInvalid("no components after initial object parse")


    //skip if only 1 component
    if(components.size > 1) {
        // here we should have a components object containing parsed operators numbers and nested expressions
        // now we do some tricky shit
        // lets check the formatting and see if we can settle this into a binary tree to make things easier in runtime
        // if we can resolve to binary expressions it will effectively have the bomdas ordering innately

        //todo add checks at each stage for if that step is even required, would cut down on iterations needed

        //parse out negative numbers, into inverted ImathValues
        val componentsNegative = LinkedList<ParsedMathEquation.IMathComponent>()
        val iteratorNegative = components.iterator()

        var nextOverride: ParsedMathEquation.IMathComponent? = null

        while (iteratorNegative.hasNext()) {
            val currentComponent = nextOverride ?: iteratorNegative.next()
            nextOverride = null

            if (currentComponent == MathOperator.SUBTRACT) {
                if (!iteratorNegative.hasNext()) return IMathValue.getInvalid("negative operator has no next value")
                if (componentsNegative.size == 0) {
                    val next = iteratorNegative.next()
                    if (next is IMathValue) {
                        next.invert()
                        componentsNegative.add(next)
                    } else return IMathValue.getInvalid("started with negative then followed by operator")
                } else {
                    val next = iteratorNegative.next()
                    val last = componentsNegative.last
                    if (next is IMathValue) {
                        if (last is MathOperator) {
                            next.invert()
                            componentsNegative.add(next)
                        } else {
                            componentsNegative.add(currentComponent)
                            componentsNegative.add(next)//skips a loop
                        }
                    } else {
                        componentsNegative.add(currentComponent)
                        nextOverride = next //overrides for next loop, catches "--" cases
                    }
                }
            } else {
                componentsNegative.add(currentComponent)
            }
        }
        //all negative numbers should be parsed

        // if the expression is valid the next stages should all succeed and respect negative numbers in the order of operations
        // unlike some other math libraries.. looking at you MXParser.........................................................................................................................................................................................................hi
        //MXParser thinks 2+1*-2 is (2+1)*-2 for some ungodly reason when it needs to be 2+(1*-2)


        //resolve powers into binary expression component
        val componentsPower = LinkedList<ParsedMathEquation.IMathComponent>()
        val iteratorPower = componentsNegative.iterator()
        while (iteratorPower.hasNext()) {
            val currentComponent = iteratorPower.next()
            if (currentComponent == MathOperator.POWER) {
                if (!iteratorPower.hasNext() || componentsPower.size < 1) return IMathValue.getInvalid(
                    "power doesnt have component both sides: [$components]:[$componentsNegative]"
                )
                val next = iteratorPower.next()
                val last = componentsPower.last
                if (!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("power isnt surrounded by values: [$components]:[$componentsNegative]")
                componentsPower.removeLast()
                componentsPower.add(
                    MathBinaryExpressionComponent(
                        last,
                        currentComponent as MathOperator,
                        next
                    )
                )
            } else {
                componentsPower.add(currentComponent)
            }
        }

        //resolve * / into binary expression component
        val componentsMD = LinkedList<ParsedMathEquation.IMathComponent>()
        val iteratorMD = componentsPower.iterator()
        while (iteratorMD.hasNext()) {
            val currentComponent = iteratorMD.next()
            if (currentComponent == MathOperator.MULTIPLY || currentComponent == MathOperator.DIVIDE) {
                if (!iteratorMD.hasNext() || componentsMD.size < 1) return IMathValue.getInvalid("$currentComponent, doesn't have components either side: [$components]:[$componentsPower]")
                val next = iteratorMD.next()
                val last = componentsMD.last
                if (!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only: [$components]:[$componentsPower]")
                componentsMD.removeLast()
                componentsMD.add(
                    MathBinaryExpressionComponent(
                        last,
                        currentComponent as MathOperator,
                        next
                    )
                )
            } else {
                componentsMD.add(currentComponent)
            }
        }

        //resolve + - into binary expression components
        val componentsFinal = LinkedList<ParsedMathEquation.IMathComponent>()
        val iteratorAS = componentsMD.iterator()
        while (iteratorAS.hasNext()) {
            val currentComponent = iteratorAS.next()
            if (currentComponent == MathOperator.ADD || currentComponent == MathOperator.SUBTRACT) {
                if (!iteratorAS.hasNext() || componentsFinal.size < 1) return IMathValue.getInvalid(
                    "$currentComponent, doesn't have componenets either side: [$components]:[$componentsMD]"
                )
                val next = iteratorAS.next()
                val last = componentsFinal.last
                if (!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only: [$components]:[$componentsMD]")
                componentsFinal.removeLast()
                componentsFinal.add(
                    MathBinaryExpressionComponent(
                        last,
                        currentComponent as MathOperator,
                        next
                    )
                )
            } else {
                componentsFinal.add(currentComponent)
            }
        }
        components = componentsFinal
    }

    //here componentsFinal should only be a list containing 1 single IMathValue
    if(components.size != 1){
        return IMathValue.getInvalid("final didn't resolve to 1 component: $components")
    }

    // this is a IMathValue either a MathNumber or a binary tree of MathBinaryExpressionComponents
    // with each binary branch ending on a MathNumber

    return if (components.first is IMathValue ) {
        if(inBrackets) (components.first as IMathValue).setBrackets()
        components.first as IMathValue
    }else {
        IMathValue.getInvalid("final wasn't a math value, likely invalid: $components")
    }
}
