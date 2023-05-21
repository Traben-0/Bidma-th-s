package com.traben.bidmaths.math.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.traben.bidmaths.MathGame
import com.traben.bidmaths.R
import com.traben.bidmaths.databinding.MathBinaryExpressionBinding
import com.traben.bidmaths.math.BinaryExpressionComponent
import com.traben.bidmaths.math.MathOperator
import com.traben.bidmaths.math.ParsedEquation

class MathBinaryExpressionView(
    private val fullExpression: ParsedEquation,
    private val expression: BinaryExpressionComponent,
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


    fun update() {
        setLeft(expression.valueOne.getAsView(fullExpression, context))
        setRight(expression.valueTwo.getAsView(fullExpression, context))
    }


    private fun setLeft(view: View) {
        binding.left.removeAllViews()
        if (expression.hasBrackets)
            binding.left.addView(MathBracketView(true, context))
        binding.left.addView(view)
    }


    private fun setOperator(operator: MathOperator) {
        binding.operator.text = operator.toStringPretty()
        binding.operatorShadow.text = binding.operator.text

        val animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration =
            (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation.duration = randomDuration.toLong()
        binding.operator.startAnimation(animation)
        binding.operatorShadow.startAnimation(animation)

        binding.operator.setOnClickListener {
            if (expression.canResolve() && fullExpression.isNextOperationThisConsideringLeftToRight(
                    expression
                )
            ) {
                //correct operator
                val animation2 =
                    AnimationUtils.loadAnimation(context, R.anim.resolve_math)

                val thisPointer = this
                animation2.setAnimationListener(object :
                    Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        expression.resolve(thisPointer)
                        if (fullExpression.isCompleted()) {
                            fullExpression.completeAction.invoke()
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                binding.container.startAnimation(animation2)
            } else {
                //wrong operator
                fullExpression.timesAnsweredWrong++
                val animation2 =
                    AnimationUtils.loadAnimation(context, R.anim.shake)
                binding.container.startAnimation(animation2)
                MathGame.currentMathGame?.updateHint(operator)
            }
            //expression.resolve(parent?.parent?.parent)
        }
    }

    private fun setRight(view: View) {
        binding.right.removeAllViews()
        if (expression.operator == MathOperator.POWER) {
            view.y -= 25
            view.scaleY = 0.75f
            view.scaleX = 0.75f
            view.minimumWidth = 0
            //view.setOnClickListener { expression.resolve(parent?.parent?.parent) }
        }
        binding.right.addView(view)
        if (expression.hasBrackets)
            binding.right.addView(MathBracketView(false, context))
    }
}