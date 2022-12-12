package com.example.alienbattle

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

class ExtraHealth internal constructor(res: Resources?, screenX: Int) : ItemGame() {
    var wasShot = true
    var width: Int
    var height: Int
    var extraHealth: Bitmap

    // for collision making shape of extraHealth
    val collisionShape: Rect
        get() = Rect(x, y, x + width, y + height)

    init {
        extraHealth = BitmapFactory.decodeResource(res, R.drawable.health)
        width = extraHealth.width
        height = extraHealth.height
        val imageRatio =
            extraHealth.height.toFloat() / extraHealth.width // for maintaining original size of image
        width = screenX / 11
        height = (imageRatio * width).toInt()
        extraHealth = Bitmap.createScaledBitmap(extraHealth, width, height, false)
        y = -height //for placing extraHealth off the screen initially
    }
}