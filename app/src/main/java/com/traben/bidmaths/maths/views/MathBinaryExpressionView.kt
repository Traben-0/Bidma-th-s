package com.traben.bidmaths.maths.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.LandingFragmentDirections
import com.traben.bidmaths.MathGame
import com.traben.bidmaths.R
import com.traben.bidmaths.databinding.MathBinaryExpressionBinding
import com.traben.bidmaths.maths.MathBinaryExpressionComponent
import com.traben.bidmaths.maths.MathOperator
import com.traben.bidmaths.maths.ParsedMathEquation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MathBinaryExpressionView (
        private val fullExpression : ParsedMathEquation,
        private val expression: MathBinaryExpressionComponent,
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

        val binding: MathBinaryExpressionBinding

        init {
                val inflater = LayoutInflater.from(context)
                binding = MathBinaryExpressionBinding.inflate(inflater, this, true)

                setOperator(expression.operator)
                update()


        }



        fun update(){
                setLeft(expression.valueOne.getAsView(fullExpression,context))
                setRight(expression.valueTwo.getAsView(fullExpression,context))
        }



        private fun setLeft(view: View) {
                binding.left.removeAllViews()
                if(expression.hasBrackets)
                        binding.left.addView(MathBracketView(true,context))
                binding.left.addView(view)
        }


        private fun setOperator(operator: MathOperator) {
                binding.operator.text = operator.toStringPretty()

                val animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
                val randomDuration = (500..1500).random() // Random duration between 500 and 1500 milliseconds
                animation.duration = randomDuration.toLong()
                binding.operator.startAnimation(animation)

                binding.operator.setOnClickListener {
                        if(expression.canResolve() && fullExpression.isNextOperationThisConsideringLeftToRight(expression)) {

                                val animation2 =
                                        AnimationUtils.loadAnimation(context, R.anim.resolve_math)

                                val thisPointer = this
                                animation2.setAnimationListener(object :
                                        Animation.AnimationListener {
                                        override fun onAnimationStart(animation: Animation?) {}
                                        override fun onAnimationEnd(animation: Animation?) {
                                                expression.resolve(thisPointer)
                                                if(fullExpression.isCompleted()){
                                                        fullExpression.completeAction.invoke()
                                                }
                                        }
                                        override fun onAnimationRepeat(animation: Animation?) {}
                                })
                                binding.container.startAnimation(animation2)
                        }else{
                                fullExpression.timesAnsweredWrong++
                                val animation2 =
                                        AnimationUtils.loadAnimation(context, R.anim.shake)
                                binding.container.startAnimation(animation2)
                        }
                        //expression.resolve(parent?.parent?.parent)
                }
        }
        private fun setRight(view: View) {
                binding.right.removeAllViews()
                if(expression.operator == MathOperator.POWER){
                        view.y -=25
                        view.scaleY = 0.75f
                        view.scaleX = 0.75f
                        view.minimumWidth=0
                        //view.setOnClickListener { expression.resolve(parent?.parent?.parent) }
                }
                binding.right.addView(view)
                if(expression.hasBrackets)
                        binding.right.addView(MathBracketView(false,context))
        }
}
