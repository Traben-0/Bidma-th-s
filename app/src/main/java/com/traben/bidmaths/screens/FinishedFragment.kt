package com.traben.bidmaths.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.*
import com.traben.bidmaths.databinding.FragmentFinishedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** this fragment acts as the end game screen for the users
 *  Features:
 *  - View score at end of game
 *  - icon for thumbs up or down depending on score
 *  - save score to leaderboard
 *  - play another round of the same difficulty
 *  - link to landing screen
 *  - link to leaderboard screen
 *  - share button to share an image of your score with the stylised thumbs up and the game logo
 *
 *  for context this screen is only visited at the end of a game
 */
class FinishedFragment : Fragment() {

    private var _binding: FragmentFinishedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var finishedGame : MathGame? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFinishedBinding.inflate(inflater, container, false)

        // the layout is structured from two halves inside a linear layout
        // they are designed to display nicely in both landscape and portrait mode
        // but we must tell the LinearLayout to change its orientation
        if (requireActivity() is MainActivity
            && (requireActivity() as MainActivity).isLandscape(requireContext())
        ) {
            binding.root.orientation = LinearLayout.HORIZONTAL
        }

        //ensure this fragment is only tied to the just finished game and
        // thus unaffected by any new games
        finishedGame = MathGame.currentMathGame

        //retrieve the score as 0-100 from the completed game
        val score = finishedGame?.gameScore()

        // give a thumbs down icon for failing grades
        if ((score ?: 0) < 50) {
            binding.medalIcon.setImageResource(R.drawable.baseline_thumb_down_24)
            binding.medalIconShadow.setImageResource(R.drawable.baseline_thumb_down_24)
        }

        //display score text
        binding.result.text = getString(R.string.finished_score_display,finishedGame?.scoreGrade())
        binding.resultShadow.text = binding.result.text

        //setup the play again button to start a new game loop
        binding.playAgainButton.setOnClickListener {
            // do all the math building and processing outside of the main thread
            lifecycleScope.launch(Dispatchers.IO) {
                MathGame.loadNewGameInLastMode()
                //then return to the main thread to navigate to the new game
                withContext(Dispatchers.Main) {
                    val action = FinishedFragmentDirections.actionStartNewGame(gameIteration = 0)
                    findNavController().navigate(action)
                }

            }
        }
        //return to landing menu screen
        binding.returnToMenuButton.setOnClickListener {
            findNavController().navigate(FinishedFragmentDirections.actionReturnToLanding())
        }


        if (SettingsFragment.hideLeaderboard) binding.viewLeaderboardButton.isVisible = false
        binding.viewLeaderboardButton.setOnClickListener {
            findNavController().navigate(FinishedFragmentDirections.actionOpenLeaderboard())
        }

        //set the leaderboard submission button functionality
        binding.submitButton.setOnClickListener { button ->
            //if name isn't empty saves the finished game data to the leaderboard outside of the main thread
            if (binding.submitText.text.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    addToLeaderboard(binding.submitText.text.toString())
                }
                //disables further inputs
                button.isEnabled = false
                binding.submitText.isEnabled = false
            } else {
                Toast.makeText(requireContext(), getString(R.string.leader_board_needs_name), Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareButton.setOnClickListener {
            // initial simple text share i've left as an example of how pathetic this would've been :)
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.type = "text/plain"
//            val scoreText = MathGame.currentMathGame?.scoreGrade()?:"amazing"
//            shareIntent.putExtra(Intent.EXTRA_TEXT, "My BidMa(th)s score is $scoreText!")
//            startActivity(Intent.createChooser(shareIntent, "Share via"))
            shareScoreImage()

        }

        //creates an animation for the thumbs up/ down, to be started in onResume
        animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration =
            (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation?.duration = randomDuration.toLong()

        return binding.root

    }

    // this method takes the binding.topHolder view and parses it as a bitmap
    // then stores it and returns a Uri?
    // this method was heavily informed from stack overflow
    //
    // Note: that the logoHidden view is revealed for the timeframe the bitmap is generated thus
    // providing a neat little bit of branding for the shared image
    private fun getScoreImageUri(): Uri? {
        try {
            binding.logoHidden.visibility = View.VISIBLE
            val scoreLayout = binding.topHolder
            scoreLayout.measure(
                View.MeasureSpec.makeMeasureSpec(
                    requireContext().resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                    requireContext().resources.displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
                )
            )
            scoreLayout.layout(0, 0, scoreLayout.measuredWidth, scoreLayout.measuredHeight)
            val bitmap = Bitmap.createBitmap(
                scoreLayout.measuredWidth,
                scoreLayout.measuredHeight / 2,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            scoreLayout.layout(
                scoreLayout.left,
                scoreLayout.top,
                scoreLayout.right,
                scoreLayout.bottom / 2
            )
            scoreLayout.draw(canvas)
            binding.logoHidden.visibility = View.INVISIBLE

            bitmap?.let {
                @Suppress("DEPRECATION")
                val imagePath = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    bitmap,
                    "GameScoreImage",
                    null
                )
                return Uri.parse(imagePath)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            binding.logoHidden.visibility = View.INVISIBLE
        }
        return null
    }

    // generates and then shares a screenshot of the top half of the finished screen
    // capture the thumbs up/down  the score, and a hidden logo that appears in the bitmap
    private fun shareScoreImage() {
        val imageUri = getScoreImageUri()
        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.type = "image/*"
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        }
    }


    //saves the results of the last game to the provided name key in the leaderboard
    // the database is simply set to override previous keys at this time
    private fun addToLeaderboard(name: String) {
        val database = LeaderBoard.getDatabase(requireContext())
        val dao = database.getDao()
        val newData = LeaderboardEntry(
            name,
            finishedGame?.gameScore() ?: 0,
            finishedGame?.gameResultsDetailedInfo() ?: "null"
        )
        dao.insertData(newData)
        database.close()
    }

    private var animation: Animation? = null

    //thumbs up/down animation is started in onResume() as it seems to be automatically stopped
    // at onPause()
    override fun onResume() {
        super.onResume()
        binding.medalIcon.startAnimation(animation)
        binding.medalIconShadow.startAnimation(animation)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animation = null
    }
}