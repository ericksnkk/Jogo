package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Inimigo {
    protected Sprite sprite;
    protected float scale;
    protected Rectangle hitboxRect;
    protected boolean isAlive, wasHited;
    protected int life;
    protected float speed;

    public Inimigo(Texture texture, float startX, float startY, float scale, float speed, int life) {
        this.sprite = new Sprite(texture);
        this.scale = scale;
        this.speed = speed;
        this.life = life;
        this.isAlive = true;
        this.wasHited = false;

        sprite.setScale(scale);
        sprite.setOrigin(0, 0);
        sprite.setPosition(startX, startY);
        atualizaHitbox();
    }

    protected void atualizaHitbox() {
        hitboxRect = new Rectangle(sprite.getX() - 5, sprite.getY() - 5,
                sprite.getWidth() * scale + 10,
                sprite.getHeight() * scale + 10);
    }

    public abstract void update(float deltaTime, Player player, Rectangle[] obstacles, Rectangle cameraBounds);

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
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
        if (life <= 0) {
            isAlive = false;
        }
    }

    public void setWasHited(boolean wasHited) {
        this.wasHited = wasHited;
    }

    public Vector2 getCenterPosition() {
        return new Vector2(sprite.getX() + sprite.getWidth() * scale / 2,
                sprite.getY() + sprite.getHeight() * scale / 2);
    }
}
