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
            player.atualizaVelocityX(-1);
        }
        if (keycode == Input.Keys.D) {  // Move para a direita
            player.atualizaVelocityX(1);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) {
            player.atualizaVelocityX(1);
        }
        if (keycode == Input.Keys.D) {
            player.atualizaVelocityX(-1);
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
}
