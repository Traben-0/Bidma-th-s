package com.traben.bidmaths.maths.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.traben.bidmaths.databinding.MathBinaryExpressionBinding
import com.traben.bidmaths.maths.MathBinaryExpressionComponent
import com.traben.bidmaths.maths.MathOperator

class MathBinaryExpressionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

        private val binding: MathBinaryExpressionBinding

        init {
                val inflater = LayoutInflater.from(context)
                binding = MathBinaryExpressionBinding.inflate(inflater, this, true)

        }

        lateinit var expression: MathBinaryExpressionComponent

        fun set(expressionComponent: MathBinaryExpressionComponent){
                expression = expressionComponent
                setLeft(expression.valueOne.getAsView(context))
                setOperator(expression.operator)
                setRight(expression.valueTwo.getAsView(context))
        }



        private fun setLeft(view: View) {
                binding.left.removeAllViews()
                if(expression.hasBrackets)
                        binding.left.addView(MathBracketView(context).setLeft(true))
                binding.left.addView(view)
        }


        private fun setOperator(operator: MathOperator) {
                binding.operator.text = operator.toStringPretty()
        }
        private fun setRight(view: View) {
                binding.right.removeAllViews()
                binding.right.addView(view)
                if(expression.hasBrackets)
                        binding.left.addView(MathBracketView(context).setLeft(false))
        }

}
