package com.traben.bidmaths

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.databinding.FragmentLandingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


        binding.easyButton.setOnClickListener {
            launchGameMode(MathGame.GameMode.EASY)
        }
        binding.mediumButton.setOnClickListener {
            launchGameMode(MathGame.GameMode.MEDIUM)
        }
        binding.hardButton.setOnClickListener {
            launchGameMode(MathGame.GameMode.HARD)
        }
        animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration = (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation?.duration = randomDuration.toLong()


        return binding.root

    }



    private fun launchGameMode(mode : MathGame.GameMode){
        lifecycleScope.launch(Dispatchers.IO) {
            MathGame.loadGameMode(mode)
            withContext(Dispatchers.Main) {
                val action = LandingFragmentDirections.actionStartGame(gameIteration = 0)
                findNavController().navigate(action)
            }

        }
    }



    private var animation : Animation? = null
    override fun onResume() {
        super.onResume()
        binding.imageView.startAnimation(animation)
        binding.imageViewShadow.startAnimation(animation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }




    override fun onPause() {
        super.onPause()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animation = null
    }
}