package com.example.alienbattle

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

class Bullet internal constructor(
    res: Resources?,
    var orientation: Orientation,
    var playerId: Int,
    screenX: Int
) {
    var x = 0
    var y = 0
    var width: Int
    var height: Int
    var bullet: Bitmap

    enum class Orientation {
        UP, DOWN, DO_NOT_SHOOT
    }

    val collisionShape: Rect
        get() = Rect(x, y, x + width, y + height)

    init {
        bullet = BitmapFactory.decodeResource(res, R.drawable.bullet_round)
        width = bullet.width
        height = bullet.height
        val imageRatio = height.toFloat() / width
        width = screenX / 50
        height = (imageRatio * width).toInt()
        bullet = Bitmap.createScaledBitmap(bullet, width, height, false)
    }
}