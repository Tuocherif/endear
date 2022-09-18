package com.imot.endear.ui.endear

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.imot.endear.R
import com.imot.endear.databinding.FragmentEndearBinding
import kotlin.properties.Delegates

/*Bonus game*/

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class EndearFragment : Fragment(), View.OnClickListener {
    private val hideHandler = Handler(Looper.myLooper()!!)

    private var activePlayer by Delegates.notNull<Boolean>()
    private var playerOneScoreCount by Delegates.notNull<Int>()
    private var playerTwoScoreCount by Delegates.notNull<Int>()
    private var rountCount by Delegates.notNull<Int>()

    /*
    Tracking buttons
    * player 1 => 0
    * player 2 => 1
    * empty => 2
    * */

    var gameState = intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 2)

    var winningPositions = arrayOf(
        intArrayOf(0, 1, 2), //row
        intArrayOf(3, 4, 5), //row
        intArrayOf(6, 7, 8), //row
        intArrayOf(0, 3, 6), //column
        intArrayOf(1, 4, 7), //column
        intArrayOf(2, 5, 8), //column
        intArrayOf(0, 4, 8), //cross
        intArrayOf(2, 4, 6)  //cross
    )



//    private var playerOneScore : TextView = view.findViewById(R.id.tv_playerOneScore) as TextView

    private lateinit var playerOneScore : TextView
    private lateinit var playerTwoScore : TextView
    private lateinit var playerStatus : TextView
    private lateinit var btn_ResetGame : Button
    private val buttons = arrayOfNulls<Button>(9)

    private lateinit var endearLayout : ScrollView


    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentEndearBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val EndearViewModel =
            ViewModelProvider(this)[EndearViewModel::class.java]

        _binding = FragmentEndearBinding.inflate(inflater, container, false)
        val root: View = binding.root

        EndearViewModel.text.observe(viewLifecycleOwner){

//            for (i in 0 until buttons.lastIndex) {
//                val buttonID = "btn_$i"
//            }
            endearLayout = view?.findViewById(R.id.endearLayout) as ScrollView
            playerOneScore = view?.findViewById(R.id.tv_playerOneScore) as TextView
            playerTwoScore = view?.findViewById(R.id.tv_playerTwoScore) as TextView
            playerStatus = view?.findViewById(R.id.tv_playerStatus) as TextView
            btn_ResetGame = view?.findViewById(R.id.btn_ResetGame) as Button

            for (i in buttons.indices) {
                val buttonID = "btn_$i"
                val resourceID = resources.getIdentifier(buttonID, "id", context?.packageName)
                buttons[i] = requireView().findViewById(resourceID)
                buttons[i]!!.setOnClickListener(this)
            }

            rountCount = 0
            playerOneScoreCount = 0
            playerTwoScoreCount = 0
            activePlayer = true
        }

        return root

    }//end onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

//        dummyButton = binding.dummyButton
//        fullscreenContent = binding.fullscreenContent
//        fullscreenContentControls = binding.fullscreenContentControls
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummyButton?.setOnTouchListener(delayHideTouchListener)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        //Log.i("test","Le bouton est cliqué!")
        if(((Button(v!!.context))).text.toString() != ""){
            return
        }

        val buttonID = v.resources.getResourceEntryName(v.id)//btn_x
        val gameStatePointer : Int = buttonID.substring(buttonID.length -1,buttonID.length).let {
            Integer.parseInt(it)}//2

            if(activePlayer){

                if(gameState[gameStatePointer] == 0 || gameState[gameStatePointer] == 1){
                    Toast.makeText(v.context,"Interdit !!!! Cette position est déjà occupée.",Toast.LENGTH_SHORT).show()
                    return
                }else{
                    (v as Button).text = "X"
                    v.setTextColor(Color.parseColor("#70FFEA"))
                    gameState[gameStatePointer] = 0
                }

            }else{
                if(gameState[gameStatePointer] == 0 || gameState[gameStatePointer] == 1){
                    Toast.makeText(v.context,"Interdit !!!! Cette position est déjà occupée.",Toast.LENGTH_SHORT).show()
                    return
                }else{
                    (v as Button).text = "O"
                    v.setTextColor(Color.parseColor("#FFC34A"))
                    gameState[gameStatePointer] = 1
                }
            }
                    rountCount++

        if(checkWinner()){
            if(activePlayer){
                if(gameState[gameStatePointer] == 0){
                    playerOneScoreCount++
                    updatePlayerScore()
                    Toast.makeText(v.context,"Joueur 1 a gagné cette partie !",Toast.LENGTH_SHORT).show()
                    playAgain()
                }
            }else{
                    playerTwoScoreCount++
                    updatePlayerScore()
                    Toast.makeText(context,"Joueur 2 a gagné cette partie !",Toast.LENGTH_SHORT).show()
                    playAgain()
            }
        }else if(rountCount == 9){
            playAgain()
            Toast.makeText(context,"Aucun gagnant!",Toast.LENGTH_SHORT).show()
        }else{
            activePlayer = !activePlayer
        }

        if(playerOneScoreCount > playerTwoScoreCount ){
            playerStatus.setText(R.string.Player1)

        }else if(playerTwoScoreCount > playerOneScoreCount ){
            playerStatus.setText(R.string.Player2)

        }else{
            playerStatus.text = ""
        }

        btn_ResetGame.setOnClickListener{
            playAgain()
            playerOneScoreCount = 0
            playerTwoScoreCount = 0
            playerStatus.text = ""
            updatePlayerScore()
        }
        }

    private fun checkWinner(): Boolean {
        var winnerResult = false
        for (winningPosition in winningPositions) {
            if (gameState[winningPosition[0]] == gameState[winningPosition[1]] &&
                gameState[winningPosition[1]] == gameState[winningPosition[2]] &&
                gameState[winningPosition[0]] != 2) {
                winnerResult = true
            }
        }
        return winnerResult
    }

    private fun updatePlayerScore() {
        playerOneScore.text = playerOneScoreCount.toString()
        playerTwoScore.text = playerTwoScoreCount.toString()
    }

    private fun playAgain() {
        rountCount = 0
        activePlayer = true
        for (i in buttons.indices) {
            gameState[i] = 2
            buttons[i]!!.text = ""
        }
    }

    }
