package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class TelaTeste implements Screen {
    final MyGdxGame game;
    private Player player;
    private PlayerController playerController;
    private ShapeRenderer shape;

    private Rectangle rectangle, enemie;
    private boolean enemieAlive;


    public TelaTeste(final MyGdxGame game) {
        this.game = game;

        shape = new ShapeRenderer();

        rectangle = new Rectangle(200, 200, 300, 200);

        enemie = new Rectangle(200, 30, 55, 100);
        enemieAlive = true;


        player = new Player(game.assetManager.get("Wraith_idle.png", Texture.class), 100, 50);
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

        Gdx.gl.glClearColor(.21f, .37f, .23f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Rectangle previousPlayerBound = player.getPlayerBounds();

        // Atualiza o player
        player.update(delta);

        Rectangle playerBounds = player.getPlayerBounds();

        detectarColisao(playerBounds, rectangle, previousPlayerBound);

        game.batch.begin();
        player.render(game.batch);
        game.batch.end();

        shape.setProjectionMatrix(game.camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(Color.SKY);
        shape.rect(0, 0, 750, 30);

        shape.setColor(Color.TAN);
        shape.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        if (player.isAttacking()){
            shape.setColor(Color.YELLOW);
            Rectangle attackHitBox = player.getAttackHitBox();
            shape.rect(attackHitBox.x, attackHitBox.y, attackHitBox.width, attackHitBox.height);

            colisaoAtaque(attackHitBox, enemie);
        }
        if(enemieAlive){
            shape.setColor(Color.RED);
            shape.rect(enemie.x, enemie.y, enemie.width, enemie.height);
        }

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

        //System.out.println(position.x - game.camera.position.x);
    }

    private void detectarColisao(Rectangle playerBounds, Rectangle platformBounds, Rectangle previousPlayerBound) {
        if(playerBounds.overlaps(platformBounds)){
            Vector2 newPosition = new Vector2(playerBounds.x, playerBounds.y);
            if(playerBounds.x + playerBounds.width > platformBounds.x && playerBounds.x < platformBounds.x + platformBounds.width){
                newPosition.x = previousPlayerBound.x;
            }
            if((player.getVelocityY() > 0) && (playerBounds.y + playerBounds.height > platformBounds.y && playerBounds.y < platformBounds.y + platformBounds.height)){
                newPosition.y = previousPlayerBound.y;
                player.endJump();
                player.setVelocityY(0);
                System.out.println(player.getVelocityY());
            }
//            if (playerBounds.y < platformBounds.y + platformBounds.height && playerBounds.y > platformBounds.y) {
//                playerBounds.y = platformBounds.y + platformBounds.height;
//            }
//            // Colisão pela parte inferior
//            else if (playerBounds.y + playerBounds.height > platformBounds.y) {
//                playerBounds.y = platformBounds.y - playerBounds.height;
//                player.endJump();
//                player.setVelocityY(0);
//            }
//            // Colisão pela direita
//            else if (playerBounds.x > platformBounds.x + platformBounds.width) {
//                playerBounds.x = platformBounds.x + platformBounds.width;
//            }
//            // Colisão pela esquerda
//            else if (playerBounds.x + playerBounds.width < platformBounds.x) {
//                playerBounds.x = platformBounds.x - playerBounds.width;
//            }

            player.setPosition(newPosition.x, newPosition.y
            );
        }
    }

    private void colisaoAtaque(Rectangle playerAtack, Rectangle enemie){
        if(playerAtack.overlaps(enemie)){
            enemieAlive = false;
        }
    }
}
