package com.example.takoyakimaster

import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.media.MediaPlayer
import android.util.Log
import android.widget.EditText
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    val db = Firebase.firestore

    //cooking tools
    private var greaserTool = false
    private var batterTool = false
    private var octoTool = false
    private var pokeTool = false

    //topping tools
    private var crumbles = false
    private var brush = false
    private var shaker = false
    private var piper = false

    //game progression assets
    private var score = 36
    private var name = "Starfy"
    private var gamestarted = false
    private lateinit var countDownTimer: CountDownTimer
    internal val initialCountDown: Long = 30000
    internal val countDownInterval: Long = 1000

    private lateinit var timerTV: TextView

    //music
    private var mMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score_screen)
        displayHighscores()
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.menu_song)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    fun startGame(view: View) {
        if (view is Button) {
            setContentView(R.layout.activity_main)
            timerTV = findViewById(R.id.timer)
            resetGame()
            gamestarted = true
            countDownTimer.start()
            //start the music
            if (mMediaPlayer != null) {
                mMediaPlayer!!.stop()
                mMediaPlayer = MediaPlayer.create(this, R.raw.cooking_music)
                mMediaPlayer!!.isLooping = true
                mMediaPlayer!!.start()
            } else mMediaPlayer!!.start()
        }
    }

    private fun speedUp() {
        //start speed up music
        mMediaPlayer!!.stop()
        mMediaPlayer = MediaPlayer.create(this, R.raw.speed_up)
        mMediaPlayer!!.isLooping = true
        mMediaPlayer!!.start()
    }

    fun endGame() {
        //switch to the score screen
        setContentView(R.layout.score_screen)
        displayHighscores()
        //switch back to menu music
        if (mMediaPlayer != MediaPlayer.create(this, R.raw.menu_song)) {
            mMediaPlayer!!.stop()
            mMediaPlayer = MediaPlayer.create(this, R.raw.menu_song)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        }
    }

    private fun resetGame() {
        score = 0
        resetTools()
        val initialTimeLeft = initialCountDown / 1000
        timerTV.text = initialTimeLeft.toString()

        countDownTimer = object: CountDownTimer(initialCountDown, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = millisUntilFinished / 1000
                timerTV.text = timeLeft.toString()
                if (timeLeft == 60L) {
                    speedUp()
                }
            }

            override fun onFinish() {
                checkScore()
            }
        }
        gamestarted = false
    }

    /******************************************************************
     * resets tools to default. Setting them to false makes them go
     * back to their previous positions and tells the code the user
     * isn't using them.
     ******************************************************************/
    private fun resetTools() {
        //reset all tools to default
        when {
            greaserTool -> {
                greaserTool = false
                findViewById<ImageView>(R.id.greaser).setImageResource(R.drawable.greaser) }
            batterTool -> {
                batterTool = false
                findViewById<ImageView>(R.id.batter).setImageResource(R.drawable.batter) }
            octoTool -> {
                octoTool = false
                findViewById<ImageView>(R.id.octopus).setImageResource(R.drawable.octopus) }
            pokeTool -> {
                pokeTool = false
                findViewById<ImageView>(R.id.poke).setImageResource(R.drawable.poke) }
            crumbles -> {
                crumbles = false
                findViewById<ImageView>(R.id.crumbs).setImageResource(R.drawable.crumbs) }
            brush -> {
                brush = false
                findViewById<ImageView>(R.id.brush).setImageResource(R.drawable.brush) }
            shaker -> {
                shaker = false
                findViewById<ImageView>(R.id.shaker).setImageResource(R.drawable.shaker) }
            piper -> {
                piper = false
                findViewById<ImageView>(R.id.piper).setImageResource(R.drawable.piper) }
        }

    }

    /******************************************************************
     * When the user taps on a tool it is picked up and can now
     * be used.
     ******************************************************************/
    fun grabTool(view: View) {
        if (view is ImageView) {
            //reset the tools so the user can't grab more than one.
            resetTools()
            //change the one that is being used to true
            when (view.id) {
                R.id.greaser -> { greaserTool = true; view.setImageResource(R.drawable.hgreaser) }
                R.id.batter -> { batterTool = true; view.setImageResource(R.drawable.hbatter) }
                R.id.octopus -> { octoTool = true; view.setImageResource(R.drawable.hocto) }
                R.id.poke -> { pokeTool = true; view.setImageResource(R.drawable.hpoke) }
                R.id.crumbs -> { crumbles = true; view.setImageResource(R.drawable.hcrumbs) }
                R.id.brush -> { brush = true; view.setImageResource(R.drawable.hbrush) }
                R.id.shaker -> { shaker = true; view.setImageResource(R.drawable.hshaker) }
                R.id.piper -> { piper = true; view.setImageResource(R.drawable.hpiper) }
            }
        }
    }

    /****************************************************************
     * Allows the user to interact with the grill and cook takoyaki
     ****************************************************************/
    fun cookTakoyaki(view: View) {
        if (view is ImageView) {
            if (greaserTool && view.drawable.level <= 0) { view.setImageLevel(1) }
            else if (batterTool && view.drawable.level == 1) { view.setImageLevel(2) }
            else if (octoTool && view.drawable.level == 2) { view.setImageLevel(3) }
            else if (pokeTool && view.drawable.level == 3) { view.setImageLevel(4) }
            else if (pokeTool && view.drawable.level == 4) { view.setImageLevel(5) }
            else if (pokeTool && view.drawable.level == 5) { view.setImageLevel(0); score += 1 }
        }
    }

    fun setName(view: View) {
        if (view is Button) {
            val username = findViewById<EditText>(R.id.name_edit).text.toString()
            if (username != "") {
                name = username
                addHighscore()
            }
        }
    }

    fun checkScore(){
        var highscore = false
        db.collection("highscores").orderBy("score", Query.Direction.DESCENDING)
            .limit(5).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    println("Your score was ${document.get("score").toString().toInt()}")
                    if (score > document.get("score").toString().toInt()) {
                        setContentView(R.layout.name_entry)
                        mMediaPlayer!!.stop()
                        mMediaPlayer = MediaPlayer.create(this, R.raw.menu_song)
                        mMediaPlayer!!.isLooping = true
                        mMediaPlayer!!.start()
                        highscore = true
                        break
                    }
                    Log.d("Scores", "${document.id} => ${document.data}")
                }
                if (!highscore) {
                    endGame()
                }
            }
            .addOnFailureListener {
                Log.e("Scores", "Error getting document in checkScore")
            }
    }

    fun addHighscore() {
        // Create a new user with a name and score
        val user = hashMapOf(
            "user" to name,
            "score" to score
        )

        // Add a new document with a generated ID in the users name
        db.collection("highscores").document(name)
            .set(user)
            .addOnSuccessListener { Log.d("Scores", "DocumentSnapshot added with ID") }
            .addOnFailureListener { e -> Log.w("Scores", "Error adding document", e) }

        endGame()
    }

    fun displayHighscores() {
        var score_names = arrayOf<TextView>(
            findViewById(R.id.first_name),
            findViewById(R.id.second_name),
            findViewById(R.id.third_name),
            findViewById(R.id.fourth_name),
            findViewById(R.id.fifth_name)
        )

        var scores = arrayOf<TextView>(
            findViewById(R.id.first_score),
            findViewById(R.id.second_score),
            findViewById(R.id.third_score),
            findViewById(R.id.fourth_score),
            findViewById(R.id.fifth_score)
        )

        db.collection("highscores").orderBy("score", Query.Direction.DESCENDING)
            .limit(5).get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result) {
                    score_names[i].text = document.id
                    scores[i].text = document.get("score").toString()
                    i++
                    Log.d("Scores", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener {
                Log.e("Scores", "Error getting document in displayHighscores")
            }
    }
}