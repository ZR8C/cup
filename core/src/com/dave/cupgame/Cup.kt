package com.dave.cupgame

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.body
import ktx.math.vec2

//todo fix texture
class Cup(world: World, startX: Float, startY: Float) : Actor() {
    val scale = 1f

    val horizontalPaddle = Pixmap((2 * scale).toInt(), (10 * scale).toInt(), Pixmap.Format.RGBA8888)
    val verticalPaddle = Pixmap((10 * scale).toInt(), (2 * scale).toInt(), Pixmap.Format.RGBA8888)

    init {
        horizontalPaddle.setColor(Color.RED)
        horizontalPaddle.fillRectangle(0, 0, horizontalPaddle.width, horizontalPaddle.height)

        verticalPaddle.setColor(Color.RED)
        verticalPaddle.fillRectangle(0, 0, verticalPaddle.width, verticalPaddle.height)
    }

    val leftPaddle = Sprite(Texture(horizontalPaddle))
    val bottomPaddle = Sprite(Texture(verticalPaddle))
    val rightPaddle = Sprite(Texture(horizontalPaddle))

    init {
        leftPaddle.setSize(2f * scale, 10f * scale)
        bottomPaddle.setSize(10f * scale, 2f * scale)
        rightPaddle.setSize(2f * scale, 10f * scale)
    }

    val leftBody = world.body {
        type = BodyDef.BodyType.KinematicBody
        box (2f * scale, 10f * scale) {
            userData = this@Cup
        }
    }

    val bottomBody = world.body {
        type = BodyDef.BodyType.KinematicBody
        box (10f * scale, 2f * scale) {
            userData = this@Cup
        }
    }

    val rightBody = world.body {
        type = BodyDef.BodyType.KinematicBody
        box (2f * scale, 10f * scale) {
            userData = this@Cup
        }
    }

    init {
        leftBody.setTransform(vec2(startX + -5f * scale, startY + -1f * scale), 0f)
        bottomBody.setTransform(vec2(startX + -1f * scale, startY + -7f * scale), 0f)
        rightBody.setTransform(vec2(startX + 3 * scale, startY + -1f * scale), 0f)
    }

    val cuppedZone = CuppedZone(world, leftBody.position.x + (4f * scale), leftBody.position.y - (10f * scale / 2), (10f * scale) - (2f * scale * 2f), 1f)

    class CuppedZone(world: World, x: Float, y: Float, width: Float, height: Float) {
        val body = world.body {
            type = BodyDef.BodyType.KinematicBody
            box (width, height) {
                userData = this@CuppedZone
            }
        }

        init {
            body.setTransform(x, y, 0f)
        }
    }


    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        leftPaddle.draw(batch)
        bottomPaddle.draw(batch)
        rightPaddle.draw(batch)

    }

    override fun act(delta: Float) {
        super.act(delta)

        // update sprites as per box2d objs
        leftPaddle.setPosition(leftBody.position.x - (leftPaddle.width / 2), leftBody.position.y - (leftPaddle.height / 2))
        bottomPaddle.setPosition(bottomBody.position.x - (bottomPaddle.width / 2), bottomBody.position.y - (bottomPaddle.height / 2))
        rightPaddle.setPosition(rightBody.position.x - (rightPaddle.width / 2), rightBody.position.y - (rightPaddle.height / 2))
    }

    fun setLinearVelocity(velX: Float, velY: Float) {
        leftBody.setLinearVelocity(velX, velY)
        bottomBody.setLinearVelocity(velX, velY)
        rightBody.setLinearVelocity(velX, velY)
        cuppedZone.body.setLinearVelocity(velX, velY)
    }
}

