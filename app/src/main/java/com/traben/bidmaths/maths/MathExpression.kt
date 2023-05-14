package com.traben.bidmaths.maths

import java.util.LinkedList

class MathExpression(var components : List<IMathComponent>?) : IMathComponent {

    fun isValid(): Boolean{
        return !components.isNullOrEmpty()
    }


    companion object{
        //parses a string expression, returning null if invalid
        fun parseExpression(expression : String) : IMathValue{
            //clear spaces
            val formattedExpression = expression.replace(" ","")

            if(formattedExpression.matches(Regex.fromLiteral("[^0-9\\.\\+\\-\\*/)(]"))){
                // expression has illegal characters
                return IMathValue.getInvalid("has illegal characters")
            }

            val components = LinkedList<IMathComponent>()

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
                            val nestedExpression  = parseExpression(rollingRead.toString())
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
            if(components.isEmpty()) return IMathValue.getInvalid("no components after initial object parse")

            // here we should have a components object containing parsed operators numbers and nested expressions
            // now we do some tricky shit
            // lets check the formatting and see if we can settle this into a binary tree to make things easier in runtime
            // if we can resolve to binary expressions it will effectively have the bomdas ordering innately


            //parse out negative numbers, into inverted ImathValues
            val componentsNegative = LinkedList<IMathComponent>()
            val iteratorNegative = components.iterator()

            var nextOverride : IMathComponent? = null

            while (iteratorNegative.hasNext()){
                val currentComponent = nextOverride ?: iteratorNegative.next()
                nextOverride = null

                if(currentComponent == MathOperator.SUBTRACT){
                    if (!iteratorNegative.hasNext()) return IMathValue.getInvalid("negative operator has no next value" +
                            "")
                    if(componentsNegative.size == 0){
                        val next = iteratorNegative.next()
                        if (next is IMathValue) {
                            next.invert()
                            componentsNegative.add(next)
                        }else return IMathValue.getInvalid("started with negative then followed by operator")
                    }else {
                        val next = iteratorNegative.next()
                        val last = componentsNegative.last
                        if (next is IMathValue) {
                            if (last is MathOperator) {
                                next.invert()
                                componentsNegative.add(next)
                            }else{
                                componentsNegative.add(currentComponent)
                                componentsNegative.add(next)//skips a loop
                            }
                        }else{
                            componentsNegative.add(currentComponent)
                            nextOverride = next //overrides for next loop, catches "--" cases
                        }
                    }
                }else{
                    componentsNegative.add(currentComponent)
                }
            }
            //all negative numbers should be parsed

            // if the expression is valid the next stages should all succeed and respect negative numbers in the order of operations
            // unlike some other math libraries.. looking at you MXParser.........................................................................................................................................................................................................hi

            //resolve powers into binary expression component
            val componentsPower = LinkedList<IMathComponent>()
            val iteratorPower = componentsNegative.iterator()
            while (iteratorPower.hasNext()){
                val currentComponent = iteratorPower.next()
                if(currentComponent == MathOperator.POWER){
                    if(!iteratorPower.hasNext() || componentsPower.size<1) return IMathValue.getInvalid("power doesnt have component both sides")
                    val next = iteratorPower.next()
                    val last = componentsPower.last
                    if(!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("power isnt surrounded by values")
                    componentsPower.removeLast()
                    componentsPower.add(MathBinaryExpressionComponent(last, currentComponent as MathOperator,next))
                }else{
                    componentsPower.add(currentComponent)
                }
            }

            //resolve * / into binary expression component
            val componentsMD = LinkedList<IMathComponent>()
            val iteratorMD = componentsPower.iterator()
            while (iteratorMD.hasNext()){
                val currentComponent = iteratorMD.next()
                if(currentComponent == MathOperator.MULTIPLY || currentComponent == MathOperator.DIVIDE ){
                    if(!iteratorMD.hasNext() || componentsMD.size<1) return IMathValue.getInvalid("$currentComponent, doesn't have components either side")
                    val next = iteratorMD.next()
                    val last = componentsMD.last
                    if(!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only")
                    componentsMD.removeLast()
                    componentsMD.add(MathBinaryExpressionComponent(last, currentComponent as MathOperator,next))
                }else{
                    componentsMD.add(currentComponent)
                }
            }

            //resolve + - into binary expression components
            val componentsFinal = LinkedList<IMathComponent>()
            val iteratorAS = componentsMD.iterator()
            while (iteratorAS.hasNext()){
                val currentComponent = iteratorAS.next()
                if(currentComponent == MathOperator.ADD || currentComponent == MathOperator.SUBTRACT ){
                    if(!iteratorAS.hasNext() || componentsFinal.size<1) return IMathValue.getInvalid("$currentComponent, doesn't have componenets either side")
                    val next = iteratorAS.next()
                    val last = componentsFinal.last
                    if(!(next is IMathValue && last is IMathValue)) return IMathValue.getInvalid("$currentComponent, isn't affecting values only")
                    componentsFinal.removeLast()
                    componentsFinal.add(MathBinaryExpressionComponent(last, currentComponent as MathOperator,next))
                }else{
                    componentsFinal.add(currentComponent)
                }
            }


            //here componentsFinal should only be a list containing 1 single IMathValue
            if(componentsFinal.size != 1){
                return IMathValue.getInvalid("final didn't resolve to 1 component")
            }
            return if (componentsFinal.first is IMathValue ) componentsFinal.first as IMathValue else IMathValue.INVALID_VALUE
        }
    }
}