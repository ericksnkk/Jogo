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
    private int spriteDirection;
    private float scale;

    public Player(Texture texture, float startX, float startY) {
        sprite = new Sprite(texture);

        this.scale = 1f / 4f;
        sprite.setScale(scale);  // Tamanho do personagem

        sprite.setOrigin(0, 0);

        sprite.setPosition(startX, startY);

        velocity = new Vector2(0, 0);
        speed = 200; // Velocidade
        jumpSpeed = 1400; // Velocidade do pulo
        isJumping = false;
        gravity = -5000; // Gravidade
        verticalSpeed = 0;
        this.spriteDirection = 1;
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
        if (sprite.getY() <= 30) {
            sprite.setY(30);
            verticalSpeed = 0;
            isJumping = false;
        }
        if (this.velocity.x < 0 && spriteDirection != -1){
            sprite.flip(true, false);
            spriteDirection = -1;
        }
        else if (this.velocity.x > 0 && spriteDirection != 1) {
            sprite.flip(true, false);
            spriteDirection = 1;
        }
    }

    public void atualizaVelocityX(float direction) {
        this.velocity.x += direction * speed;
    }

    public void jump() {
        if (!isJumping) {
            verticalSpeed = jumpSpeed;
            isJumping = true;
        }
    }

    public Sprite getSprite() {
        return sprite;
    }

    public Vector2 getCenterPosition() {
        return new Vector2(sprite.getX() + (sprite.getWidth() * scale)/2, sprite.getY() + (sprite.getHeight() * scale)/2);
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        texture.dispose();
    }
}
