package com.dave.cupgame

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.actors.centerPosition
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import java.util.*


class CupGame : KtxGame<Screen>() {

    override fun create() {
        val skin = Skin(Gdx.files.internal("uiskin.json"))
        addScreen(CupScreen(this, skin))
        addScreen(StartScreen(this, skin))
        setScreen<StartScreen>()
    }
}

class StartScreen(val game: KtxGame<Screen>, skin: Skin) : KtxScreen {

    val cam = OrthographicCamera()
    val viewport = ExtendViewport(800f, 600f, cam)
    val stage = Stage(viewport)

    val welcome = Label("Cup", Label.LabelStyle(BitmapFont(), Color.GREEN))


    val timeAttack = TextButton("Time attack", skin).apply {
        onClick {
            game.setScreen<CupScreen>()
        }

    }
    val sandbox = TextButton("Sandbox", skin).apply {
        onClick {
            game.setScreen<CupScreen>() //todo
        }
        width = 500f
    }
    val options = TextButton("Options", skin).apply {
        onClick {
            game.setScreen<CupScreen>() //todo
        }
        width = 500f
    }

    init {
        val verticalGroup = VerticalGroup().apply {
            addActor(welcome)
            addActor(timeAttack)
            addActor(sandbox)
            addActor(options)
            setFillParent(true)
            center()
            space(30f)
            width = 100f
        }

        stage.addActor(verticalGroup)
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        super.show()
    }

    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}
    const val THING_BEING_CUPPED = "Balls"

class CupScreen(val game: KtxGame<Screen>, skin: Skin) : KtxScreen, KtxInputAdapter {

    val font = BitmapFont()
    val cam = OrthographicCamera()
    val viewport = ExtendViewport(100f, 100f, cam)

    val logo = Label("${THING_BEING_CUPPED.toUpperCase()}", Label.LabelStyle(BitmapFont(), Color.GREEN))
    val cuppedCount = Label("$THING_BEING_CUPPED Cupped: 0", Label.LabelStyle(BitmapFont(), Color.GREEN))

    val touchPad = Touchpad(0f, skin).apply {
        setSize(10f, 10f)
        setPosition(10f, 10f)
        onChangeEvent { _, actor ->
            cup.setLinearVelocity(-actor.knobPercentX * 50f, -actor.knobPercentY * 50f)
        }
    }

    val world = createWorld(earthGravity)
    val cup = Cup(world, 50f, 50f)

    val stage = Stage(viewport).apply {
        addActor(logo)
        addActor(cuppedCount)
        addActor(cup)
        addActor(touchPad)

        logo.centerPosition()
    }

    val multiplexer = InputMultiplexer()

    var ballsCupped = 0
    val balls = mutableListOf<Ball>()
//    private val soundTrack = Gdx.audio.newSound(Gdx.files.internal("cupTrack_2.mp3"))!!
    private val soundTrack = Gdx.audio.newMusic(Gdx.files.internal("cupTrack_2.mp3"))!!
    var soundOn = true

    val destroyedBalls = mutableSetOf<Ball>()

    init {
        multiplexer.addProcessor(this)
        multiplexer.addProcessor(stage)

        world.setContactListener(object: ContactListener {
            override fun endContact(contact: Contact?) {
            }

            override fun beginContact(contact: Contact?) {
                val a = contact!!.fixtureA.userData
                val b = contact.fixtureB.userData

                if ((a is Cup.CuppedZone && b is Ball) || (b is Cup.CuppedZone && a is Ball)) {
                    if (a is Ball) destroyedBalls.add(a)
                    else destroyedBalls.add(b as Ball)
                }
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
            }

        })

        viewport.update(Gdx.graphics.width, Gdx.graphics.height, true);
    }

    override fun show() {
        Gdx.input.inputProcessor = multiplexer
        super.show()

        if (soundOn) {
            soundTrack.play()
            soundTrack.isLooping = true
        }
    }

    override fun hide() {
        balls.forEach {
            it.remove()
        }
        balls.clear()
        ballsCupped = 0
        cuppedCount.setText("$THING_BEING_CUPPED Cupped: $ballsCupped")
    }

    private var ballCreationTimer = 0f

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(delta, 6, 2) //todo vel/pos iterations
        clearDestroyedBalls()

        stage.act(delta)
        stage.draw()

        ballCreationTimer += delta

        if (ballCreationTimer > 0.05f) {
            createBall()
            ballCreationTimer = 0f
        }

    }

    override fun dispose() {
        font.dispose()
        stage.dispose()
        world.dispose()
        soundTrack.dispose()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        logo.centerPosition()
    }

    private fun clearDestroyedBalls() {
        if (destroyedBalls.isEmpty()) return

        destroyedBalls.forEach {
            balls.remove(it)
            it.remove()
            world.destroyBody(it.body)
            ballsCupped++
        }
        destroyedBalls.clear()

        cuppedCount.setText("$THING_BEING_CUPPED Cupped: $ballsCupped")
    }


//    private val ballTexture = Texture(Gdx.files.internal("not_synch.gif"))

    private fun createBall() {
        val pixmap = Pixmap(8 * 2, 8 * 2, Pixmap.Format.RGBA8888)

        val r = Random()
        pixmap.setColor(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1f)
        pixmap.fillCircle(8, 8, 8)

        val newBall = Ball(world, Texture(pixmap), r.nextFloat() * stage.width, stage.height, r.nextFloat() * 1f, r.nextFloat() * 100f)
//        val newBall = Ball(world, ballTexture, r.nextFloat() * stage.width, stage.height, r.nextFloat() * 3f, r.nextFloat() * 100f)

        pixmap.dispose()

        balls.add(newBall)
        stage.addActor(newBall)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (Input.Keys.M == keycode) {
            if (!soundOn) {
                soundTrack.play()
                soundOn = true
                return true
            } else {
                soundTrack.stop()
                soundOn = false
                return true
            }
        }

        if (Input.Keys.ESCAPE == keycode) {
            game.setScreen<StartScreen>()
            return true
        }

        return super.keyDown(keycode)
    }

}



