package com.traben.bidmaths.math.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.traben.bidmaths.databinding.MathNumberBinding
import com.traben.bidmaths.math.MathNumber

/**
 * simple view for expression number to be constructed quickly with just a MathNumber
 * */

@SuppressLint("ViewConstructor")//not a concern
class MathNumberView(
    number: MathNumber,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: MathNumberBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = MathNumberBinding.inflate(inflater, this, true)

        binding.number.text = number.toString()
        binding.numberShadow.text = binding.number.text
    }


}
