package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Inimigo {
    private Sprite sprite;
    private Texture texture;
    private float scale;
    private Rectangle hitboxRect;
    private int height, width;
    private boolean isAlive, wasHited;
    private int life;
    private float speed = 100f;
    private int direction =1;

    public Inimigo(Texture texture, float startX, float startY) {
        sprite = new Sprite(texture);

        this.scale = 2f;
        sprite.setScale(scale);
        sprite.setOrigin(0,0);
        sprite.setPosition(startX, startY);

        isAlive = true;
        wasHited = false;
        life = 100;

        hitboxRect = new Rectangle();
    }

    public void update(float deltaTime) {
        if(life <= 0) {
            isAlive = false;
        }

//        if(sprite.getX()<= 2900 * 3){
//            direction = 1;
//        }else if(sprite.getX()>=3100 * 3){
//            direction = -1;
//        }
        //sprite.setPosition(sprite.getX() + (direction * speed * deltaTime), sprite.getY());
        hitboxRect = new Rectangle(sprite.getX() - 10, sprite.getY() - 10, (sprite.getWidth() * scale) + 20, (sprite.getHeight() * scale) + 20);
    }

    public Rectangle getHitboxRect() {
        return hitboxRect;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean wasHited() {
        return wasHited;
    }

    public int getLife() {
        return life;
    }

    public void decreaseLife(int dano) {
        life -= dano;
    }

    public void setWasHited(boolean wasHited) {
        this.wasHited = wasHited;
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
