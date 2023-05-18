package com.traben.bidmaths

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.databinding.FragmentFinishedBinding
import com.traben.bidmaths.databinding.FragmentLandingBinding
import com.traben.bidmaths.maths.ParsedMathEquation


class FinishedFragment : Fragment() {

    private var _binding: FragmentFinishedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFinishedBinding.inflate(inflater, container, false)



        binding.playAgainButton.setOnClickListener {
            MathGame.loadNewGameInLastMode()
//            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
//            findNavController().navigate(action)
        }
        binding.returnToMenuButton.setOnClickListener {
//            MathGame.loadMediumGame(SettingsFragment.respectLeftRight)
//            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
//            findNavController().navigate(action)
        }
        binding.viewLeaderboardButton.setOnClickListener {
//            MathGame.loadHardGame(SettingsFragment.respectLeftRight)
//            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
//            findNavController().navigate(action)
        }
        animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration = (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation?.duration = randomDuration.toLong()

        return binding.root

    }

    private var animation : Animation? = null

    override fun onResume() {
        super.onResume()
        binding.medalIcon.startAnimation(animation)
        binding.medalIconShadow.startAnimation(animation)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animation=null
    }
}