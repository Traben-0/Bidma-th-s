package com.traben.bidmaths.math.views

import android.annotation.SuppressLint
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
import com.traben.bidmaths.math.ParsedExpression

/**
 * more complex view for binary expression components to be constructed easily
 *
 * */
@SuppressLint("ViewConstructor")//not a concern
class MathBinaryExpressionView(
    private val fullExpressionObject: ParsedExpression,
    private val thisBinaryExpressionObject: BinaryExpressionComponent,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: MathBinaryExpressionBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = MathBinaryExpressionBinding.inflate(inflater, this, true)

        //setup the operator's view
        setOperatorView(thisBinaryExpressionObject.operator)

        //get and set the left and right MathValue views
        updateLeftAndRightMathValueViews()

    }


    fun updateLeftAndRightMathValueViews() {
        setLeftValueView(thisBinaryExpressionObject.valueOne.getAsView(fullExpressionObject, context))
        setRightValueView(thisBinaryExpressionObject.valueTwo.getAsView(fullExpressionObject, context))
    }


    private fun setLeftValueView(view: View) {
        binding.left.removeAllViews()
        if (thisBinaryExpressionObject.hasBrackets)
            binding.left.addView(MathBracketView(true, context))
        binding.left.addView(view)
    }


    private fun setOperatorView(operator: MathOperator) {
        binding.operator.text = operator.toStringPretty()
        binding.operatorShadow.text = binding.operator.text

        //animates the operator
        val animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration =
            (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation.duration = randomDuration.toLong()
        binding.operator.startAnimation(animation)
        binding.operatorShadow.startAnimation(animation)


        binding.operator.setOnClickListener {
            //if this operator is a correct valid choice for the user to pick
            if (thisBinaryExpressionObject.canResolve()
                && fullExpressionObject.isNextOperationThisConsideringLeftToRight(thisBinaryExpressionObject)
            ) {
                //correct operator

                //create and start the animation of the two values "smooshing" together then execute
                // the code to resolve the expression component
                val animation2 =
                    AnimationUtils.loadAnimation(context, R.anim.resolve_math)
                val thisPointer = this
                animation2.setAnimationListener(object :
                    Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        thisBinaryExpressionObject.resolve(thisPointer)
                        if (fullExpressionObject.isCompleted()) {
                            // invokes the action sent to the game by the game fragment
                            // this can only be executed if that fragment is still valid
                            fullExpressionObject.completeAction.invoke()
                            //clear this to alleviate memory leak concerns
                            fullExpressionObject.completeAction = {}
                        }
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                binding.container.startAnimation(animation2)
            } else {
                //wrong operator
                fullExpressionObject.timesAnsweredWrong++
                val animation2 = AnimationUtils.loadAnimation(context, R.anim.shake)
                binding.container.startAnimation(animation2)
                MathGame.currentMathGame?.updateHint(operator)
            }
        }
    }

    // this is a bit more involved than the left view
    private fun setRightValueView(view: View) {
        binding.right.removeAllViews()
        // if the operator is an index translate and scale the resulting right hand view
        if (thisBinaryExpressionObject.operator == MathOperator.POWER) {
            view.y -= 25
            view.scaleY = 0.75f
            view.scaleX = 0.75f
            view.minimumWidth = 0
        }
        binding.right.addView(view)
        if (thisBinaryExpressionObject.hasBrackets)
            binding.right.addView(MathBracketView(false, context))
    }
}
