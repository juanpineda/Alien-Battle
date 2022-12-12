package com.example.alienbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.alienbattle.Constants.Companion.GAME
import com.example.alienbattle.Constants.Companion.IS_MUTE
import com.example.alienbattle.Constants.Companion.PLAYERS

class MainActivity : AppCompatActivity() {
    private var isMute = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        val spinner = findViewById<View>(R.id.spinner) as Spinner
        val prefs = getSharedPreferences(GAME, MODE_PRIVATE)
        isMute = prefs.getBoolean(IS_MUTE, false)
        val volumeCtrl = findViewById<ImageView>(R.id.volumeCtrl)
        if (isMute) volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp)
        else volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp)
        findViewById<View>(R.id.play).setOnClickListener {
            val intent = Intent(this@MainActivity, GameActivity::class.java)
            intent.putExtra(PLAYERS, spinner.selectedItemPosition + 3)
            startActivity(intent)
        }
        volumeCtrl.setOnClickListener {
            isMute = !isMute
            if (isMute) volumeCtrl.setImageResource(R.drawable.ic_volume_off_black_24dp)
            else volumeCtrl.setImageResource(R.drawable.ic_volume_up_black_24dp)
            val editor = prefs.edit()
            editor.putBoolean(IS_MUTE, isMute)
            editor.apply()
        }

        // Spinner Drop down elements
        val categories: MutableList<String> = ArrayList()
        categories.add(getString(R.string.game_players, "3"))
        categories.add(getString(R.string.game_players, "4"))
        categories.add(getString(R.string.game_players, "5"))
        categories.add(getString(R.string.game_players, "6"))
        categories.add(getString(R.string.game_players, "7"))
        categories.add(getString(R.string.game_players, "8"))

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        spinner.adapter = dataAdapter
    }
}