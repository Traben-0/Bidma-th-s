package com.traben.bidmaths.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.LandingFragmentDirections
import com.traben.bidmaths.MainActivity
import com.traben.bidmaths.MathGame
import com.traben.bidmaths.R
import com.traben.bidmaths.databinding.FragmentLandingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The landing/ main menu fragment for the app
 * primarily functions as a navigation space
 */
class LandingFragment : Fragment() {

    private var _binding: FragmentLandingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLandingBinding.inflate(inflater, container, false)

        // the layout is structured from two halves inside a linear layout
        // they are designed to display nicely in both landscape and portrait mode
        // but we must tell the LinearLayout to change its orientation
        if (requireActivity() is MainActivity
            && (requireActivity() as MainActivity).isLandscape(requireContext())
        ) {
            binding.root.orientation = LinearLayout.HORIZONTAL
        }


        //set the buttons to start a game loop when clicked
        binding.easyButton.setOnClickListener {
            launchGameMode(MathGame.GameDifficultyMode.EASY)
        }
        binding.mediumButton.setOnClickListener {
            launchGameMode(MathGame.GameDifficultyMode.MEDIUM)
        }
        binding.hardButton.setOnClickListener {
            launchGameMode(MathGame.GameDifficultyMode.HARD)
        }

        // open the leaderboard
        if (SettingsFragment.hideLeaderboard) binding.leaderBoardButton.isVisible = false
        binding.leaderBoardButton.setOnClickListener {
            findNavController().navigate(LandingFragmentDirections.actionOpenLeaderboards())
        }

        //initialise the animation variable started in onResume()
        animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration =
            (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation?.duration = randomDuration.toLong()


        return binding.root

    }


    private fun launchGameMode(mode: MathGame.GameDifficultyMode) {
        lifecycleScope.launch(Dispatchers.IO) {
            MathGame.loadGameMode(mode)
            withContext(Dispatchers.Main) {
                val action = LandingFragmentDirections.actionStartGame(gameIteration = 0)
                findNavController().navigate(action)
            }

        }
    }


    //animation started in onResume() as it appears to be stopped automatically in onPause()
    private var animation: Animation? = null
    override fun onResume() {
        super.onResume()
        binding.imageView.startAnimation(animation)
        binding.imageViewShadow.startAnimation(animation)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animation = null
    }
}