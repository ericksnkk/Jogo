package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class Menu implements Screen{
    final MyGdxGame game;
    private Texture background;
    private ShapeRenderer shape;
    private BitmapFont font;
    private float worldHeight, worldWidth;
    private InputHandlerMenu inputHandler;

    public Menu(final MyGdxGame game) {
        this.game = game;
        this.background = game.assetManager.get("bg_menu.png", Texture.class);
        this.shape = new ShapeRenderer();
        this.font = new BitmapFont(Gdx.files.internal("bitmap/arial.fnt"), Gdx.files.internal("bitmap/arial.png"), false);
        this.font.getData().setScale(0.5f);


        float worldHeight = game.viewport.getWorldHeight();
        float worldWidth = game.viewport.getWorldWidth();

        this.inputHandler = new InputHandlerMenu(this);
        Gdx.input.setInputProcessor(this.inputHandler);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.5f, 0, 1);

        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin(); //Batch principal
        game.batch.draw(background, 0, 0);
        game.batch.end(); //Batch acaba

        game.batch.begin();

        font.getData().setScale(1f);
        font.setColor(255, 255, 255, 1f);
        font.draw(game.batch, "JOGAR", (game.viewport.getWorldWidth()/2)-50 , (game.viewport.getWorldHeight()/2)+170);
        game.batch.end();
    }

    public void iniciaGame(){
        game.setScreen(new TelaBoss(game,
                        new Player(game.assetManager.get("Wraith_idle.png", Texture.class),
                        game.assetManager.get("attack_sprite.png", Texture.class) ,
                        0, 0)));
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void dispose() {

    }
}
