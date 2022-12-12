package com.example.alienbattle

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.alienbattle.Bullet.Orientation.DO_NOT_SHOOT

class Player internal constructor(
    private val gameView: GameView,
    screenX: Int,
    var id: Int,
    res: Resources?,
    xCoordinate: Int,
    yCoordinate: Int
) : ItemGame() {

    var health = (10..20).random()
    var hitPoints = (10..300).random()
    var toShoot = false
    var width: Int
    var height: Int
    var playerBitmap: Bitmap

    // for returning dead state
    var shootState = DO_NOT_SHOOT

    fun getPlayer(): Bitmap {
        if (toShoot && hitPoints > 0) {
            hitPoints--
            toShoot = false
            gameView.newBullet(shootState, this)
        }
        return playerBitmap
    }

    val collisionShape: Rect
        get() = Rect(x, y, x + width, y + height)

    init {
        playerBitmap = BitmapFactory.decodeResource(res, R.drawable.space_craft)
        val imageRatio = playerBitmap.height.toFloat() / playerBitmap.width // for maintaining original size of image
        width = screenX / 8
        height = (imageRatio * width).toInt()
        playerBitmap = Bitmap.createScaledBitmap(playerBitmap, width, height, false)

        // for co-ordinates of player
        x = xCoordinate
        y = yCoordinate
    }
}