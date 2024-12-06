package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Texture texture;
    private Sprite sprite;
    private Vector2 velocity;
    private float speed;
    private float jumpSpeed;
    private boolean isJumping;
    private float gravity;
    private float verticalSpeed;

    public Player(String texturePath, float startX, float startY) {
        texture = new Texture(texturePath);
        sprite = new Sprite(texture);
        sprite.setPosition(startX, startY);

        sprite.setScale(1f / 3f);  // Tamanho do personagem

        velocity = new Vector2(0, 0);
        speed = 200; // Velocidade
        jumpSpeed = 300; // Velocidade do pulo
        isJumping = false;
        gravity = -500; // Gravidade
        verticalSpeed = 0;
    }

    public void update(float deltaTime) {
        // Atualiza a posição do jogador com base na velocidade horizontal
        sprite.translateX(velocity.x * deltaTime);

        //Gravidade
        if (isJumping) {
            verticalSpeed += gravity * deltaTime;
        }

        // Posicao vertical
        sprite.translateY(verticalSpeed * deltaTime);

        //Impedir que passe o chao
        if (sprite.getY() <= 0) {
            sprite.setY(0);
            verticalSpeed = 0;
            isJumping = false;
        }
    }

    public void moveLeft() {
        velocity.x = -speed;
    }

    public void moveRight() {
        velocity.x = speed;
    }

    public void stop() {
        velocity.x = 0;
    }

    public void jump() {
        if (!isJumping) {
            verticalSpeed = jumpSpeed;
            isJumping = true;
        }
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        texture.dispose();
    }
}
