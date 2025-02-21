package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Player {
    final float MAX_SPEED = 300;//350;
    final float DASH_FORCE = 3;

    private Texture texture;
    private Sprite sprite;
    private Vector2 velocity;
    private float velocityBoost;
    private float speed;
    private float jumpSpeed;
    private boolean onGround, isAttacking, onKnockback;
    private boolean doubleJump, invulnerable;
    private float gravity;
    private int spriteDirection, direction, lookDirection; // 1 para direita, -1 para esquerda
    private float scale;
    private float jumpTimer, attackTimer, dashTimer, knockbackTimer;
    private float attackDelay, dashDelay;

    private TextureAtlas idleAtlas;
    private Animation<AtlasRegion> idleAnimation;
    private TextureAtlas walkingAtlas;
    private Animation<AtlasRegion> walkingAnimation;

    private float stateTime;
    private boolean isIdle, isWalking;

    public Player(Texture texture, float startX, float startY) {
        sprite = new Sprite(texture);

        this.scale = 1f / 4f;
        sprite.setScale(scale);
        sprite.setOrigin(0, 0);
        sprite.setPosition(startX, startY);

        velocity = new Vector2(0, 0);
        velocity.x = MAX_SPEED;
        velocityBoost = 1;
        speed = 350; // Velocidade
        direction = 0;
        lookDirection = 0;
        jumpSpeed = 650; // Velocidade do pulo
        onGround = false;
        isAttacking = false;
        onKnockback = false;
        doubleJump = true;
        invulnerable = false;
        gravity = -4500; // Gravidade
        jumpTimer = 1;
        attackTimer = 1;
        dashTimer = 1;
        knockbackTimer = 1;
        attackDelay = 0;
        spriteDirection = 1; // Inicialmente o personagem olha para a direita

        // Carregar animações
        idleAtlas = new TextureAtlas("IdleAnimation/Idle.atlas");
        idleAnimation = new Animation<>(0.1f, idleAtlas.getRegions());
        walkingAtlas = new TextureAtlas("WalkingAnimation/Walking.atlas");
        walkingAnimation = new Animation<>(0.1f, walkingAtlas.getRegions());

        stateTime = 0;
        isIdle = true;
        isWalking = false;
    }

    public void update(float deltaTime) {
        System.out.println(velocityBoost);

        stateTime += deltaTime;
        isWalking = velocity.x != 0;
        isIdle = !isWalking;

        // Atualiza a posição do jogador com base na velocidade horizontal
        if(!onKnockback){
            sprite.translateX(direction * velocityBoost * velocity.x * deltaTime);
        }
        else {
            sprite.translateX(spriteDirection * -velocityBoost * velocity.x * deltaTime);
            knockbackTimer += deltaTime;
            if(knockbackTimer > 0.15){
                onKnockback = false;
            }
        }

        if(velocityBoost > 1){
            velocityBoost -= velocityBoost * deltaTime * 3;
            if(velocityBoost < 1){
                velocityBoost = 1;
            }
        }


        //Gravidade
        if (!onGround) {
            velocity.y += gravity * deltaTime;
        }
        else {
            velocity.y = -1000;
        }

        if(jumpTimer < 0.35){
            jumpTimer += deltaTime;
            velocity.y += (1400 / (jumpTimer + 0.2f)) * deltaTime;
        }

        if (attackTimer < 0.15) {
            attackTimer += deltaTime;
        } else if (isAttacking) {
            isAttacking = false;
            attackDelay = 0.4f;
        }
        if (attackDelay > 0) {
            attackDelay -= deltaTime;
        } else if (attackDelay != 0) {
            attackDelay = 0;
        }

        if(dashTimer < 0.15){
            dashTimer += deltaTime;
            velocity.y = 0;

        } else if(dashTimer != 1){
            dashTimer = 1;
            velocityBoost = 1;
        }

        if(dashDelay > 0){
            dashDelay -= deltaTime;
        }

        // Posicao vertical
        sprite.translateY(velocity.y * deltaTime);

        //Impedir que passe o chao
        if (sprite.getY() <= -20) {
            sprite.setY(-20);
            velocity.y = 0;
            onGround = true;
            doubleJump = true;
            jumpTimer = 1;
        }


        // Controle da direção do sprite (esquerda ou direita)
        if (direction < 0 && spriteDirection != -1) {
            spriteDirection = -1; // Personagem olha para a esquerda
        } else if (direction > 0 && spriteDirection != 1) {
            spriteDirection = 1; // Personagem olha para a direita
        }
    }

    public void render(SpriteBatch batch) {
        // Se estiver andando, mostra a animação de walking
        if (isWalking) {
            if (spriteDirection == -1) {
                sprite.setRegion(walkingAnimation.getKeyFrame(stateTime, true));
                sprite.flip(true, false);  // Flip horizontal para a esquerda
            } else {
                sprite.setRegion(walkingAnimation.getKeyFrame(stateTime, true));
                sprite.flip(false, false);  // Sem flip (para direita)
            }
        } else if (isIdle) {
            // Se estiver em idle, mostra a animação de idle na direção correta
            if (spriteDirection == -1) {
                sprite.setRegion(idleAnimation.getKeyFrame(stateTime, true));
                sprite.flip(true, false);  // Flip horizontal para a esquerda
            } else {
                sprite.setRegion(idleAnimation.getKeyFrame(stateTime, true));
                sprite.flip(false, false);  // Sem flip (para direita)
            }
        }
        sprite.draw(batch);
    }

    public void startJump() {
        if (onGround) {
            velocity.y = jumpSpeed;
            onGround = false;
            jumpTimer = 0;
        } else if (doubleJump){
            velocity.y = jumpSpeed;
            jumpTimer = 0.1f;
            doubleJump = false;
        }
    }

    public void endJump() {
        jumpTimer = 1;
    }

    public void attack() {
        if (!isAttacking && attackDelay == 0) {
            isAttacking = true;
            attackTimer = 0;
        }
    }

    public void dash(){
        dashTimer = 0;
        velocity.y = 0;
        jumpTimer = 1;
        velocityBoost = DASH_FORCE;
    }

    public void attackKnockback(){
        onKnockback = true;
        knockbackTimer = 0;
        velocityBoost = 1.15f;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void atualizaDirection(float direction) {
        this.direction += (int) direction;
    }

    public void atualizaLookDirection(float direction) {
        this.lookDirection += (int) direction;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        this.doubleJump = true;
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
        return new Vector2(sprite.getX() + (sprite.getWidth() * scale) / 2, sprite.getY() + (sprite.getHeight() * scale) / 2);
    }

    public Rectangle getPlayerBounds() {
        return new Rectangle(sprite.getX(), sprite.getY(), sprite.getWidth() * scale, sprite.getHeight() * scale);
    }

    public Rectangle getAttackHitBox() {
        if(!onGround && lookDirection == -1){
            return new Rectangle(sprite.getX() - 10,
                                    sprite.getY() - 35,
                                sprite.getWidth() * scale + 10,
                                45);
        }
        if (spriteDirection == 1) {
            return new Rectangle(sprite.getX() + (sprite.getWidth() * scale) - 10, sprite.getY(), 35, this.sprite.getHeight() * scale);
        } else {
            return new Rectangle(sprite.getX() - 25, sprite.getY(), 35, this.sprite.getHeight() * scale);
        }
    }

    public float getVelocityY() { return velocity.y;  }

    public float getVelocityX(){  return velocity.x;   }

    public float getDirection(){  return direction;  }

    public void dispose() {
        idleAtlas.dispose();
        walkingAtlas.dispose();
    }
}
