package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;

public class InputHandlerMenu extends InputAdapter {

    public Menu menu;

    public InputHandlerMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public boolean keyDown(int keycode) {

        if (keycode == Input.Keys.SPACE) {
//            //menu.game.setScreen(new TelaTeste(menu.game), new Player(assetManager.get("Wraith_idle.png", Texture.class),
//                                                                    assetManager.get("attack_sprite.png", Texture.class) ,
//                                                                    0, 0)));
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        return false;
    }

    public boolean touchUp (int x, int y, int pointer, int button) {

        if(button == Input.Buttons.LEFT) {// 150-500 x 220-295
            if(x >= 580 && x <= 700){
                int yMundo = (int)menu.game.viewport.getWorldHeight() - y;
                if(yMundo >= 495 && yMundo <= 540){
                    menu.iniciaGame();
                }
            }
        }

        return false;
    }

}
