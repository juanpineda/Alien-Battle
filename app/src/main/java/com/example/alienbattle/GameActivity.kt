package com.example.alienbattle

import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import com.example.alienbattle.Constants.Companion.PLAYERS

class GameActivity : AppCompatActivity() {
    private var gameView: GameView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        val bundle = intent.extras
        val players = bundle!!.getInt(PLAYERS)
        val point = Point(0, 0)
        windowManager.defaultDisplay.getSize(point)
        gameView = GameView(this, players, point.x, point.y)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        gameView!!.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView!!.resume()
    }
}