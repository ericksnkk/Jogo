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
    final float MAX_SPEED = 325;//300;
    final float DASH_FORCE = 3.5f;
    final float FINAL_FALL_VELOCITY = -1300;

    final float DASH_TIMER = 0.2f,
            JUMP_TIMER = 0.35f,
            ATTACK_TIMER = 0.1f,
            KNOCKBACK_TIMER = 0.15f;

    final float DASH_DELAY = 0.45f,
                ATTACK_DELAY = 0.35f;


    private Sprite sprite, attack_sprite;
    private Vector2 velocity;
    private float velocityBoost;
    private float speed;
    private float jumpSpeed;
    private boolean onGround, isAttacking, onKnockback, isDashing, onAction, isjumping, isfalling;
    private boolean doubleJump, invulnerable;
    private float gravity;
    private int spriteDirection, direction, lookDirection, fixDirection; // 1 para direita, -1 para esquerda
    private float scale;
    private float jumpTimer, attackTimer, dashTimer, knockbackTimer;
    private float attackDelay, dashDelay;
    private int attackDirection; //1-cima; 2-direita; 3-baixo; 4-esquerda

    private TextureAtlas idleAtlas;
    private Animation<AtlasRegion> idleAnimation;
    private TextureAtlas walkingAtlas;
    private Animation<AtlasRegion> walkingAnimation;
    private TextureAtlas jumpingAtlas;
    private Animation<AtlasRegion> jumpingAnimation;
    private TextureAtlas fallingAtlas;
    private Animation<AtlasRegion> fallingAnimation;
    private TextureAtlas atk1Atlas;
    private Animation<AtlasRegion> atk1Animation;
    private TextureAtlas atk2Atlas;
    private Animation<AtlasRegion> atk2Animation;
    private  TextureAtlas atk3Atlas;
    private Animation<AtlasRegion> atk3Animation;
    private TextureAtlas deathAtlas;
    private Animation<AtlasRegion> deathAnimation;
    private TextureAtlas hitedAtlas;
    private Animation<AtlasRegion> hitedAnimation;

    private float stateTime;
    private boolean isIdle, isWalking;
    AtlasRegion frame;

    public Player(Texture texture, Texture attack_texture ,float startX, float startY) {
        sprite = new Sprite(texture);
        attack_sprite = new Sprite(attack_texture);

        this.scale = 1.5f / 4f;
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
        fixDirection = 0;
        jumpSpeed = 650; // Velocidade do pulo
        onGround = false;
        onKnockback = false;
        isDashing = false;
        doubleJump = true;
        invulnerable = false;
        onAction = false;  //true se estiver dashando ou atacando
        gravity = -4500; // Gravidade
        jumpTimer = 1;
        attackTimer = 1;
        dashTimer = 1;
        knockbackTimer = 1;
        attackDelay = 0;
        spriteDirection = 1; // Inicialmente o personagem olha para a direita

        // Carregar animações
        idleAtlas = new TextureAtlas("IdleAnimation/idlev2.atlas");
        idleAnimation = new Animation<>(0.1f, idleAtlas.getRegions());
        walkingAtlas = new TextureAtlas("WalkingAnimation/Walking.atlas");
        walkingAnimation = new Animation<>(0.1f, walkingAtlas.getRegions());
        jumpingAtlas = new TextureAtlas("JumpAnimation/jump.atlas");
        jumpingAnimation = new Animation<>(0.1f, jumpingAtlas.getRegions());
        fallingAtlas = new TextureAtlas("FallingAnimation/falling.atlas");
        fallingAnimation = new Animation<>(0.1f, fallingAtlas.getRegions());
        atk1Atlas = new TextureAtlas("atk1/atk1.atlas");
        atk1Animation = new Animation<>(0.1f, atk1Atlas.getRegions());
        atk2Atlas = new TextureAtlas("atk2/atk2.atlas");
        atk2Animation = new Animation<>(0.1f, atk2Atlas.getRegions());
        atk3Atlas = new TextureAtlas("atk3/atk3.atlas");
        atk3Animation = new Animation<>(0.1f, atk3Atlas.getRegions());
        deathAtlas = new TextureAtlas("death/death.atlas");
        deathAnimation = new Animation<>(0.1f, deathAtlas.getRegions());
        hitedAtlas = new TextureAtlas("hited/hited.atlas");
        hitedAnimation = new Animation<>(0.1f, hitedAtlas.getRegions());


        stateTime = 0;
        isIdle = true;
        isWalking = false;
        isjumping = false;
        isfalling = false;
        isAttacking = false;
    }

    public void update(float deltaTime) {
        System.out.println(this.sprite.getX() + "   " + this.sprite.getY() + "  " + lookDirection);
        stateTime += deltaTime;
        isWalking = direction != 0;
        isIdle = !isWalking;

        // Atualiza a posição do jogador com base na velocidade horizontal
        if(!onKnockback){
            if(!isDashing){
                sprite.translateX(direction * velocityBoost * velocity.x * deltaTime);
            }
            else {
                sprite.translateX(fixDirection * velocityBoost * velocity.x * deltaTime);
            }

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
        velocity.y += gravity * deltaTime;


        //Timers e delays
        if(jumpTimer < JUMP_TIMER){
            jumpTimer += deltaTime;
            velocity.y += (1400 / (jumpTimer + 0.2f)) * deltaTime;
            isjumping = true;
            isfalling = false;
        }else if(velocity.y < 0 && !onGround){
            isjumping = false;
            isfalling = true;
        }else if(onGround){
            isjumping = false;
            isfalling = false;
        }

        if (attackTimer < ATTACK_TIMER) {
            attackTimer += deltaTime;
        } else if (isAttacking) {
            fixDirection = 0;
            isAttacking = false;
            attackDelay = ATTACK_DELAY;
            onAction = false;
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
            fixDirection = 0;
            dashTimer = 1;
            isDashing = false;
            dashDelay = DASH_DELAY;
            onAction = false;
        }

        if(dashDelay > 0){
            dashDelay -= deltaTime;
        }
        else if(dashDelay != 0){
            dashDelay = 0;
        }

        // Atualiza posicao vertical
        if(velocity.y < FINAL_FALL_VELOCITY){
            velocity.y = FINAL_FALL_VELOCITY;
        }
        sprite.translateY(velocity.y * deltaTime);



        // Controle da direção do sprite (esquerda ou direita)
        if (direction < 0 && spriteDirection != -1) {
            spriteDirection = -1; // Personagem olha para a esquerda
        } else if (direction > 0 && spriteDirection != 1) {
            spriteDirection = 1; // Personagem olha para a direita
        }
        if(fixDirection != 0){
            spriteDirection = fixDirection;
        }
    }

    public void render(SpriteBatch batch) {
        // Se estiver andando, mostra a animação de walking
        if(isjumping) {
            frame = jumpingAnimation.getKeyFrame(stateTime, false);
        }
        else if(isfalling) {
            frame = fallingAnimation.getKeyFrame(stateTime, false);
        }else if(isWalking) {
            frame = walkingAnimation.getKeyFrame(stateTime,true);
        }else{
            frame = idleAnimation.getKeyFrame(stateTime,true);
        }

        if(isAttacking){
            switch (attackDirection){
                case 1:
                    attack_sprite.setPosition(sprite.getX() + (sprite.getWidth() * scale), sprite.getY()  + (sprite.getHeight() * scale) - 15);
                    break;
                case 2:
                    attack_sprite.setPosition(sprite.getX() + (sprite.getWidth() * scale)- 15, sprite.getY());
                    break;
                case 3:
                    attack_sprite.setPosition(sprite.getX() - 10 , sprite.getY() + 15);
                    break;
                case 4:
                    attack_sprite.setPosition(sprite.getX() - (attack_sprite.getWidth() * (1f/2f)) + 15, sprite.getY());
                    break;
            }
            attack_sprite.draw(batch);
        }
        sprite.setRegion(frame);
        sprite.setSize(frame.getRegionWidth() * scale * 15, frame.getRegionHeight() * scale * 15);

        if(spriteDirection == -1 && !sprite.isFlipX()){
            sprite.flip(true, false);
        } else if (spriteDirection == 1 && sprite.isFlipX()) {
            sprite.flip(true, false);
        }


        sprite.draw(batch);

    }

    public void startJump() {
        if(!onAction){
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

    }

    public void endJump() {
        jumpTimer = 1;
    }

    public void attack() {
        if(!onAction){
            if (!isAttacking && attackDelay == 0) {
                fixDirection = spriteDirection;
                isAttacking = true;
                attackTimer = 0;
                onAction = true;

                if(!onGround && lookDirection == -1) {
                    attackDirection = 3;    //Baixo
                    attack_sprite.setRotation(-90);
                    attack_sprite.setFlip(false, false);

                } else if(lookDirection == 1) {
                    attackDirection = 1;    //Cima
                    attack_sprite.setRotation(90);
                    attack_sprite.setFlip(false, false);
                } else {
                    if(spriteDirection == 1){
                        attackDirection = 2;    //Direita
                        attack_sprite.setRotation(0);
                        attack_sprite.setFlip(false, false);
                    } else {
                        attackDirection = 4;    //Esquerda
                        attack_sprite.setRotation(0);
                        attack_sprite.setFlip(true, false);
                    }
                }
            }
        }

    }

    public void dash(){
        if(!onAction){
            if (!isDashing && dashDelay == 0) {
                fixDirection = spriteDirection; //Salva ultima direcao q o player olhou
                dashTimer = 0;
                velocity.y = 0;
                jumpTimer = 1;
                isDashing = true;
                velocityBoost = DASH_FORCE;
                onAction = true;
            }
        }


    }

    public void attackKnockback(){
        onKnockback = true;
        knockbackTimer = 0;

        if(lookDirection == -1){
            velocity.y = 2000;
        }
        else {
            velocityBoost = 1.15f;
        }
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
        if(onGround){
            velocity.y=0;
            this.doubleJump = true;
        }

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
        switch (attackDirection){
            case 1:
                return new Rectangle(sprite.getX() - 10,
                                    sprite.getY() + (sprite.getHeight() * scale) - 10,
                                    sprite.getWidth() * scale + 10,
                                    100);
            case 2:
                return new Rectangle(sprite.getX() + (sprite.getWidth() * scale) - 10,
                                    sprite.getY(),
                                    100,
                                    this.sprite.getHeight() * scale);
            case 3:
                return new Rectangle(sprite.getX() - 10,
                                    sprite.getY() - 90,
                                    sprite.getWidth() * scale + 10,
                                    100);
            case 4:
                return new Rectangle(sprite.getX() - 90,
                                    sprite.getY(),
                                    100,
                                    this.sprite.getHeight() * scale);
        }
        return null;
    }

    public float getVelocityY() { return velocity.y;  }

    public float getVelocityX(){  return velocity.x;   }

    public float getDirection(){  return spriteDirection;  }

    public void dispose() {
        idleAtlas.dispose();
        walkingAtlas.dispose();
        jumpingAtlas.dispose();
        fallingAtlas.dispose();
    }
}
