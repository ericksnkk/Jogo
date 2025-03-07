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
            player.atualizaDirection(-1);
        }
        if (keycode == Input.Keys.D) {  // Move para a direita
            player.atualizaDirection(1);
        }
        if (keycode == Input.Keys.S) {
            player.atualizaLookDirection(-1);
        }
        if (keycode == Input.Keys.W) {
            player.atualizaLookDirection(1);
        }
        if (keycode == Input.Keys.SPACE) {
            player.startJump();
        }
        if (keycode == Input.Keys.F) {
            player.attack();
        }
        if (keycode == Input.Keys.E) {
            player.dash();
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) {
            if(player.getDirection() != 0){
                player.atualizaDirection(1);
            }
        }
        if (keycode == Input.Keys.D) {
            if(player.getDirection() != 0){
                player.atualizaDirection(-1);
            }
        }
        if (keycode == Input.Keys.S) {
            player.atualizaLookDirection(1);
        }
        if (keycode == Input.Keys.W) {
            player.atualizaLookDirection(-1);
        }
        if (keycode == Input.Keys.SPACE) {
            player.endJump();
        }
        if(keycode == Input.Keys.G){
            player.stateTime += 0.05f;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            player.attack();
        }
        if (button == Input.Buttons.RIGHT) {
            player.dash();
        }


        return false;
    }
}
