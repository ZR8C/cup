package com.dave.cupgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.box2d.body

class Ball(world: World, texture: Texture, posX: Float = Gdx.graphics.width / 2f, posY: Float = Gdx.graphics.height / 2f, val radius : Float = 1f,
           val girth : Float = 0.1f) : Image(texture) {

    val body = world.body {
        type = BodyDef.BodyType.DynamicBody
        circle(radius) {
            restitution = 0.9f
            friction = 1f
            density = girth
            userData = this@Ball
        }
    }

    init {
        width = radius * 2
        height = radius * 2
        body.setTransform(posX, posY, 0f)
        setOrigin(radius, radius)
    }

    override fun act(delta: Float) {
        super.act(delta)

        setPosition(body.position.x - (width / 2), body.position.y  - (height / 2))
        rotation = MathUtils.radiansToDegrees * body.angle
    }
}