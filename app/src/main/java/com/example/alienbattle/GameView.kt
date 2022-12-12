package com.example.alienbattle

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.CountDownTimer
import android.view.SurfaceView
import com.example.alienbattle.Bullet.Orientation.DOWN
import com.example.alienbattle.Bullet.Orientation.UP
import com.example.alienbattle.Constants.Companion.GAME
import com.example.alienbattle.Constants.Companion.IS_MUTE
import java.util.*
import kotlin.math.abs

@SuppressLint("ViewConstructor")
class GameView(private val activity: GameActivity, playersNumber: Int, screenX: Int, screenY: Int) :
    SurfaceView(activity), Runnable {
    private var thread: Thread? = null
    private var isPlaying = false
    private val paint: Paint
    private val extraHealth: ExtraHealth
    private val prefs: SharedPreferences = activity.getSharedPreferences(GAME, MODE_PRIVATE)
    private val random: Random
    private var soundPool: SoundPool? = null
    private val bullets: MutableList<Bullet>
    private val sound: Int
    private val background1: Background
    private val background2: Background
    private val playerList: MutableList<Player>
    private var startTime = ""
    private var screenX: Int = 0
    private var screenY: Int = 0
    private var screenRatioX: Float = 0f
    private var screenRatioY: Float = 0f

    init {

        /**
         * for sound effects
         */
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()
            SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build()
        } else SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        sound = soundPool!!.load(activity, R.raw.shoot, 1)

        /**
         * for taking screen size
         */
        this.screenX = screenX
        this.screenY = screenY
        screenRatioX = 1080f / screenX
        screenRatioY = 1920f / screenY
        background1 = Background(screenX, screenY, resources)
        background2 = Background(screenX, screenY, resources)
        background2.y = -background2.background.height
        random = Random()
        playerList = ArrayList()

        /**
         * add items without intersecting
         */
        var addNumber = true
        var i = 0
        while (i < playersNumber) {
            val randomY = random.nextInt(screenY)
            for (player in playerList) {
                if (player.y + player.height > randomY && player.y - player.height < randomY) {
                    addNumber = false
                    break
                } else addNumber = true
            }
            if (addNumber) {
                playerList.add(Player(this, screenX, i, resources, random.nextInt(screenX), randomY))
                addNumber = false
            } else i--
            i++
        }
        bullets = ArrayList()
        paint = Paint()

        /**
         * Set timer game
         */
        paint.textSize = 128f
        paint.color = Color.WHITE
        object : CountDownTimer(30150, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                startTime = String.format("%02d", millisUntilFinished / 1000)
            }

            override fun onFinish() {
                val winner = playerList.maxByOrNull { it.health }
                playerList.removeAll { it.id != winner?.id }
            }
        }.start()
        extraHealth = ExtraHealth(resources, screenX) // for creating 1 extraHealth at a time on screen
    }

    override fun run() {
        while (isPlaying) {
            movePlayer()
            update()
            draw()
            sleep()
        }
    }

    private fun movePlayer() {
        // Move each player to found the near in X axis and shoot the near one in Y axis
        for (i in playerList.indices) {
            val nearItemIndex = findMinDiffX(i)
            // The nearest item is a player
            if (nearItemIndex != -1) {
                foundTheLowestHealthPlayer()?.let { lowestHealthPlayer ->
                    // current player is the lowest so move or shoot to the nearest player in the game
                    if (lowestHealthPlayer.id == playerList[i].id) movePlayerTo(i, playerList[nearItemIndex])
                    // Move or shoot to the lowest health player in the game
                    else movePlayerTo(i, lowestHealthPlayer)
                } ?: movePlayerTo(i, playerList[nearItemIndex])
            } else {
                // the nearest item is extraHealth so move or shoot to extra health box
                movePlayerTo(i, extraHealth)
            }
        }
    }

    private fun movePlayerTo(i: Int, itemGame: ItemGame) {
        if (playerList[i].x < itemGame.x) playerList[i].x += 1 // item is at right
        else if (playerList[i].x > itemGame.x) playerList[i].x -= 1 // item is at left
        // item is alignment so shoot it
        else {
            // item on bottom of screen so shoot down
            if (playerList[i].y < itemGame.y) {
                playerList[i].toShoot = true
                playerList[i].shootState = DOWN
            }
            // item on top of screen so shoot up
            else if (playerList[i].y > itemGame.y) {
                playerList[i].toShoot = true
                playerList[i].shootState = UP
            }
        }
    }

    private fun findMinDiffX(index: Int): Int {
        // Initialize difference as infinite
        var diff = Int.MAX_VALUE
        var nearItemIndex = -1 // the nearest item is a extraHealth box
        // Find the min diff by comparing difference
        // of all possible pairs in given array
        for (i in playerList.indices)
            if (i != index && abs(playerList[i].x - playerList[index].x) < diff) {
                diff = abs(playerList[i].x - playerList[index].x)
                nearItemIndex = i
            }
        if (abs(extraHealth.x - playerList[index].x) < diff) {
            nearItemIndex = -1
        }
        return nearItemIndex
    }

    private fun foundTheLowestHealthPlayer(): Player? = playerList.minByOrNull { it.health }

    private fun update() {

        /**
         * for moving background up to down
         *
         */
        background1.y += (100 * screenRatioY).toInt()
        background2.y += (100 * screenRatioY).toInt()

        /**
         * for creating loop of background
         */
        if (background1.y > screenY) background1.y = -background1.background.height
        if (background2.y > screenY) background2.y = -background2.background.height

        /**
         * for updating player position (moving effect)
         */
        for (player in playerList) {
            //for edges so that player will not move outside the screen
            if (player.x < 0) player.x = 0
            if (player.x >= screenX - player.width) player.x = screenX - player.width
            if (player.y < 0) player.y = 0
            if (player.y >= screenY - player.height) player.y = screenY - player.height

            /**
             * for updating bullets
             */
            val trash: MutableList<Bullet> = ArrayList()

            /**
             * when bullet hit the extraHealth
             */
            for (bullet in bullets) {
                //when bullet is out of the screen remove bullets
                if (bullet.y < 0) trash.add(bullet)
                else if (bullet.y > screenY) trash.add(bullet)

                when (bullet.orientation) {
                    UP -> bullet.y -= (20 * screenRatioY).toInt() // to move bullet up
                    DOWN -> bullet.y += (20 * screenRatioY).toInt() // to move bullet down
                }

                //collision between extraHealth and bullet
                if (Rect.intersects(extraHealth.collisionShape, bullet.collisionShape)) {
                    if (bullet.playerId == player.id) {
                        player.health = player.health + 10
                        //for removing the extraHealth and bullets from the screen
                        extraHealth.y = screenY
                        bullet.y = -500
                        extraHealth.wasShot = true
                    }
                }

                /**
                 * when bullet hit the player
                 */
                if (Rect.intersects(player.collisionShape, bullet.collisionShape)) {
                    player.health--
                    //for removing bullets from the screen
                    bullet.y = -500
                }
            }
            for (bullet in trash) bullets.remove(bullet)

            /**
             * for making extraHealth to move
             */
            if (extraHealth.wasShot) {
                // for placing new extraHealth
                extraHealth.y = random.nextInt(screenY - extraHealth.height)
                extraHealth.x = random.nextInt(screenX - 2 * extraHealth.width)
                extraHealth.wasShot = false
            }

            /**
             * when collision take place b/w extraHealth and player
             */
            if (Rect.intersects(extraHealth.collisionShape, player.collisionShape)) {
                player.health = player.health + 10
                //for removing the extraHealth and bullets from the screen
                extraHealth.y = screenY
                extraHealth.wasShot = true
                return
            }
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            /**
             * for showing background on screen
             */
            canvas.drawBitmap(
                background1.background,
                background1.x.toFloat(),
                background1.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                background2.background,
                background2.x.toFloat(),
                background2.y.toFloat(),
                paint
            )

            /**
             * for drawing extraHealths on screen
             */
            canvas.drawBitmap(
                extraHealth.extraHealth,
                extraHealth.x.toFloat(),
                extraHealth.y.toFloat(),
                paint
            )

            /**
             * set time
             */
            canvas.drawText(activity.getString(R.string.game_time, startTime), 50f, screenY - 50f, paint)

            /**
             * for winner condition
             */
            if (playerList.size == 0) {
                paint.textSize = 100f
                canvas.drawText(activity.getString(R.string.game_no_winner), 50f, 100f, paint)
                isPlaying = false
            } else if (playerList.size == 1) {
                paint.textSize = 100f
                canvas.drawText(activity.getString(R.string.game_winner, playerList[0].id.toString()), 50f, 100f, paint)
                isPlaying = false
            }

            for (player in playerList) {
                /**
                 * for drawing scores P: player, h: health, s: available shoots
                 */
                paint.textSize = 50f
                val scoresString = activity.getString(
                    R.string.game_player_health_available_shoots,
                    player.id.toString(),
                    player.health.toString(),
                    player.hitPoints.toString()
                )
                canvas.drawText(scoresString, (player.x - 40).toFloat(), player.y.toFloat(), paint)
                /**
                 * for making player on screen with co-ordinates
                 */
                canvas.drawBitmap(
                    player.getPlayer(),
                    player.x.toFloat(),
                    player.y.toFloat(),
                    paint
                )
                /**
                 * for remove kill player on screen
                 */
                if (player.health <= 0) {
                    playerList.remove(player)
                    break
                }
            }

            /**
             * for making bullets with co-ordinates
             */
            for (bullet in bullets) canvas.drawBitmap(
                bullet.bullet,
                bullet.x.toFloat(),
                bullet.y.toFloat(),
                paint
            )
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun sleep() {
        try {
            Thread.sleep(17)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isPlaying = true
        thread = Thread(this)
        thread!!.start()
    }

    fun pause() {
        try {
            isPlaying = false
            thread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun newBullet(orientation: Bullet.Orientation, player: Player) {

        /**
         * for bullet sound
         */
        if (!prefs.getBoolean(IS_MUTE, false)) soundPool!!.play(sound, 1f, 1f, 0, 0, 1f)
        /**
         * shoot bullet
         */
        val bullet = Bullet(resources, orientation, player.id, screenX)
        when (bullet.orientation) {
            UP -> {
                bullet.x = player.x + player.width / 2 - bullet.width / 2
                bullet.y = player.y - bullet.height
            }
            DOWN -> {
                bullet.x = player.x + player.width / 2 - bullet.width / 2
                bullet.y = player.y + player.height
            }
        }
        bullets.add(bullet)
    }

}