package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class TelaPrincipal implements Screen {
    final MyGdxGame game;
    private Player player;
    private PlayerController playerController;
    private ShapeRenderer shape;


    public TelaPrincipal(final MyGdxGame game) {
        this.game = game;

        shape = new ShapeRenderer();

        player = new Player(game.assetManager.get("Wraith_idle.png", Texture.class), game.assetManager.get("attack_sprite.png", Texture.class) ,100, 50);
        playerController = new PlayerController(player);
        Gdx.input.setInputProcessor(playerController);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.5f, 0, 1);

        game.batch.setProjectionMatrix(game.camera.combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atualiza o player
        player.update(delta);

        game.batch.begin();
        player.render(game.batch);
        game.batch.end();

        shape.setProjectionMatrix(game.camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(Color.SKY);
        shape.rect(0, 0, 750, 30);

        shape.setColor(Color.VIOLET);
        shape.rect(200, 250, 300, 75);
        shape.end();

        atualizaCamera();

        //System.out.println(player.getSprite().getX());
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

    private void atualizaCamera(){
        Vector2 position = player.getCenterPosition();

        if(position.x - game.camera.position.x > 100){
            game.camera.position.set(position.x + -100, game.camera.position.y, 0);
            game.camera.update();
        } else if (position.x - game.camera.position.x < -100) {
            game.camera.position.set(position.x + 100, game.camera.position.y, 0);
            game.camera.update();
        }

        System.out.println(position.x - game.camera.position.x);
    }
}
