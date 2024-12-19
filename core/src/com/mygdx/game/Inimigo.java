package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Inimigo {
    private Sprite sprite;
    private float scale;
    private Rectangle hitboxRect;
    private int height, width;
    private boolean isAlive, wasHited;
    private int life;

    public Inimigo(float startX, float startY) {
        width = 55;
        height = 100;
        scale = 1;

        isAlive = true;
        wasHited = false;
        life = 4;

        hitboxRect = new Rectangle(startX, startY, width * scale, height * scale);
    }

    public void update(float delta) {
        if(life <= 0) {
            isAlive = false;
        }
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
    }

}
