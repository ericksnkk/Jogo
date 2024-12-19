package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private Sprite sprite;
    private Vector2 velocity;
    private float speed;
    private float jumpSpeed;
    private boolean isJumping, isAttacking;
    private float gravity;
    private int spriteDirection;
    private float scale;
    private float jumpTimer, attackTimer;
    private float attackDelay;

    public Player(Texture texture, float startX, float startY) {
        sprite = new Sprite(texture);

        this.scale = 1f / 4f;
        sprite.setScale(scale);  // Tamanho do personagem

        sprite.setOrigin(0, 0);

        sprite.setPosition(startX, startY);

        velocity = new Vector2(0, 0);
        speed = 350; // Velocidade
        jumpSpeed = 800; // Velocidade do pulo
        isJumping = true;
        isAttacking = false;
        gravity = -4500; // Gravidade
        jumpTimer = 1;
        attackTimer = 1;
        attackDelay = 0;
        this.spriteDirection = 1;
    }

    public void update(float deltaTime) {
        // Atualiza a posição do jogador com base na velocidade horizontal
        sprite.translateX(velocity.x * deltaTime);

        //Gravidade
        if (isJumping) {
            velocity.y += gravity * deltaTime;
        }
        if(jumpTimer < 0.4){
            jumpTimer += deltaTime;
            velocity.y += (1300 / (jumpTimer + 0.2)) * deltaTime;
        }

        if(attackTimer < 0.2){
            attackTimer += deltaTime;
        } else if(isAttacking) {
            isAttacking = false;
            attackDelay = 0.4f;
        }
        if (attackDelay > 0) {
            attackDelay -= deltaTime;

        } else if(attackDelay != 0){
            attackDelay = 0;
        }

        // Posicao vertical
        sprite.translateY(velocity.y * deltaTime);

        //Impedir que passe o chao
        if (sprite.getY() <= 30) {
            sprite.setY(30);
            velocity.y = 0;
            isJumping = false;
            jumpTimer = 1;
        }

        if (this.velocity.x < 0 && spriteDirection != -1){ //Ajusta direcao do sprite ao andar
            sprite.flip(true, false);
            spriteDirection = -1;
        }
        else if (this.velocity.x > 0 && spriteDirection != 1) {
            sprite.flip(true, false);
            spriteDirection = 1;
        }
    }

    public void startJump() {
        if (!isJumping) {
            velocity.y = jumpSpeed;
            isJumping = true;
            jumpTimer = 0;
        }
    }

    public void endJump() {
        jumpTimer = 1;
    }

    public void attack(){
        if(!isAttacking && attackDelay == 0){
            isAttacking = true;
            attackTimer = 0;
        }
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void atualizaVelocityX(float direction) {
        this.velocity.x += direction * speed;
    }

    public void setVelocityY(float verticalSpeed) {
        this.velocity.y = verticalSpeed;
    }

    public void setVelocityX(float velocityX) {
        this.velocity.x = velocityX;
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public Vector2 getCenterPosition() {
        return new Vector2(sprite.getX() + (sprite.getWidth() * scale)/2, sprite.getY() + (sprite.getHeight() * scale)/2);
    }

    public Rectangle getPlayerBounds() {
        return new Rectangle(sprite.getX(), sprite.getY(), sprite.getWidth() * scale, sprite.getHeight() * scale);
    }

    public Rectangle getAttackHitBox(){
        if(spriteDirection == 1){
            return new Rectangle(sprite.getX() + (sprite.getWidth() * scale), sprite.getY(), 30, 50);
        }
        else{
            return new Rectangle(sprite.getX() - 30, sprite.getY(), 30, 50);
        }
    }

    public float getVelocityY() {
        return velocity.y;
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
