package com.traben.bidmaths

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.databinding.FragmentLandingBinding
import com.traben.bidmaths.maths.ParsedMathEquation

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LandingFragment : Fragment() {

    private var _binding: FragmentLandingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLandingBinding.inflate(inflater, container, false)


        val animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration = (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation.duration = randomDuration.toLong()
        binding.imageView.startAnimation(animation)
        binding.imageViewShadow.startAnimation(animation)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences: SharedPreferences? =context?.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val respectLeftToRight : Boolean = sharedPreferences?.getBoolean(LEFT_TO_RIGHT_KEY,false) ?: false
        //equation = ParsedMathEquation.createRandomExpression(3,respectLeftToRight)


        binding.easyButton.setOnClickListener {
            MathGame.loadEasyGame(respectLeftToRight)
            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
            findNavController().navigate(action)
        }
        binding.mediumButton.setOnClickListener {
            MathGame.loadMediumGame(respectLeftToRight)
            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
            findNavController().navigate(action)
        }
        binding.hardButton.setOnClickListener {
            MathGame.loadHardGame(respectLeftToRight)
            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}