package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class PlayerController extends InputAdapter {
    private Player player;

    public PlayerController(Player player) {
        this.player = player;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A) {  // Move para a esquerda
            player.moveLeft();
        }
        if (keycode == Input.Keys.D) {  // Move para a direita
            player.moveRight();
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) {
            player.stop();
        }
        if (keycode == Input.Keys.D) {
            player.stop();
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        // Pulo
        if (character == ' ') {
            player.jump();
        }
        return true;
    }

    public void update() {
        // Movimentação para a esquerda
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.moveLeft();
        }
        // Movimentação para a direita
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.moveRight();
        }
        else {
            player.stop(); //
        }
    }
}
