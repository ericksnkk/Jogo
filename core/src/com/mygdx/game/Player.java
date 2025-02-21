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
    final float DASH_FORCE = 3.5f;

    final float DASH_TIMER = 0.2f,
                ATTACK_TIMER = 0.15f,
                JUMP_TIMER = 0.35f,
                KNOCKBACK_TIMER = 0.15f;

    final float DASH_DELAY = 0.45f,
                ATTACK_DELAY = 0.4f;


    private Texture texture;
    private Sprite sprite, attack_sprite;
    private Vector2 velocity;
    private float velocityBoost;
    private float speed;
    private float jumpSpeed;
    private boolean onGround, isAttacking, onKnockback, isDashing;
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

    public Player(Texture texture, Texture attack_texture ,float startX, float startY) {
        sprite = new Sprite(texture);
        attack_sprite = new Sprite(attack_texture);

        this.scale = 1f / 4f;
        sprite.setScale(scale);
        sprite.setOrigin(0, 0);
        sprite.setPosition(startX, startY);
        attack_sprite.setScale(1f / 2f);
        attack_sprite.setOrigin(0, 0);
        attack_sprite.setPosition(startX, startY);

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
        isDashing = false;
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
            if(knockbackTimer > KNOCKBACK_TIMER){
                onKnockback = false;
            }
        }

        //Reduz constantemente qualquer aumento na velocidade do player
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

        //Timers e delays
        if(jumpTimer < JUMP_TIMER){
            jumpTimer += deltaTime;
            velocity.y += (1400 / (jumpTimer + 0.2f)) * deltaTime;
        }

        if (attackTimer < ATTACK_TIMER) {
            attackTimer += deltaTime;
        } else if (isAttacking) {
            isAttacking = false;
            attackDelay = ATTACK_DELAY;
        }
        if (attackDelay > 0) {
            attackDelay -= deltaTime;
        } else if (attackDelay != 0) {
            attackDelay = 0;
        }

        if(dashTimer < DASH_TIMER){
            dashTimer += deltaTime;
            velocity.y = 0;

        } else if(dashTimer != 1){
            dashTimer = 1;
            isDashing = false;
            //velocityBoost = 1;
            dashDelay = DASH_DELAY;
        }

        if(dashDelay > 0){
            dashDelay -= deltaTime;
        }
        else if(dashDelay != 0){
            dashDelay = 0;
        }

        // Atualiza posicao vertical
        sprite.translateY(velocity.y * deltaTime);


        //Impedir que passe o chao
        //Subtituir em algum momento
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
        if(isAttacking){
            if(!onGround && lookDirection == -1){
                attack_sprite.setRotation(-90);
                attack_sprite.setPosition(sprite.getX() , sprite.getY() + 10);
                attack_sprite.setFlip(false, false);
            } else if(spriteDirection == -1){
                attack_sprite.setRotation(0);
                attack_sprite.setPosition(sprite.getX() - attack_sprite.getWidth() * (1f/2f) + 15, sprite.getY());
                attack_sprite.setFlip(true, false);
            } else {
                attack_sprite.setRotation(0);
                attack_sprite.setPosition(sprite.getX() + sprite.getWidth() * scale - 15, sprite.getY());
                attack_sprite.setFlip(false, false);
            }
            attack_sprite.draw(batch);
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
        if (!isDashing && dashDelay == 0) {
            dashTimer = 0;
            velocity.y = 0;
            jumpTimer = 1;
            isDashing = true;
            velocityBoost = DASH_FORCE;
        }

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

    public Rectangle getPlayerHitBox(){
        return new Rectangle(sprite.getX() + 10, sprite.getY(), sprite.getWidth() * scale - 20, sprite.getHeight() * scale);
    }

    public Rectangle getAttackHitBox() {
        if(!onGround && lookDirection == -1){
            return new Rectangle(sprite.getX() - 10,
                                    sprite.getY() - 90,
                                sprite.getWidth() * scale + 10,
                                100);
        }
        if (spriteDirection == 1) {
            return new Rectangle(sprite.getX() + (sprite.getWidth() * scale) - 10,
                                    sprite.getY(),
                                    100,
                                    this.sprite.getHeight() * scale);
        } else {
            return new Rectangle(sprite.getX() - 90,
                                    sprite.getY(),
                                    100,
                                    this.sprite.getHeight() * scale);
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
