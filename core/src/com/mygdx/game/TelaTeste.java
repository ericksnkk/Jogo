package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class TelaTeste implements Screen {
    final int MAP_SCALE = 3;
    final MyGdxGame game;
    private Player player;
    private PlayerController playerController;
    private Array<Inimigo> inimigos;

    private ShapeRenderer shape;

    private OrthogonalTiledMapRenderer tmr;
    private TiledMap map;

    private Rectangle[] plataformas = new Rectangle[15], paredes = new Rectangle[10];
    private int nPlataformas, nParedes;
    private boolean debugMode = true;

    public TelaTeste(final MyGdxGame game) {
        this.game = game;

        map = new TmxMapLoader().load("maps/lvl2.tmx");
        tmr = new OrthogonalTiledMapRenderer(map, MAP_SCALE);


        shape = new ShapeRenderer();

        nPlataformas = 15;
        nParedes = 3;

        paredes[0] = new Rectangle(1840*MAP_SCALE, 0, 1380 * MAP_SCALE, 16*MAP_SCALE);
        paredes[1] = new Rectangle(1856*MAP_SCALE, 0, 16 * MAP_SCALE, 400*MAP_SCALE);
        paredes[2] = new Rectangle(3200*MAP_SCALE, 0, 16 * MAP_SCALE, 400*MAP_SCALE);

        plataformas[0] = new Rectangle(1984*MAP_SCALE, 64 * MAP_SCALE, 160 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[1] = new Rectangle(2223*MAP_SCALE, 64 * MAP_SCALE, 67 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[2] = new Rectangle(2300*MAP_SCALE, 112 * MAP_SCALE, 80 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[3] = new Rectangle(2608*MAP_SCALE, 0 * MAP_SCALE, 16 * MAP_SCALE, 64*MAP_SCALE);
        plataformas[4] = new Rectangle(2672*MAP_SCALE, 0 * MAP_SCALE, 16 * MAP_SCALE, 96*MAP_SCALE);
        plataformas[5] = new Rectangle(2768*MAP_SCALE, 112 * MAP_SCALE, 144 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[6] = new Rectangle(3006*MAP_SCALE, 48 * MAP_SCALE, 144 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[7] = new Rectangle(2416*MAP_SCALE, 208 * MAP_SCALE, 128 * MAP_SCALE, 16*MAP_SCALE);
        plataformas[8] = new Rectangle(2016*MAP_SCALE, 256 * MAP_SCALE, 352 * MAP_SCALE, 16*MAP_SCALE);

        player = new Player(game.assetManager.get("Wraith_idle.png", Texture.class), game.assetManager.get("attack_sprite.png", Texture.class) ,2500 * MAP_SCALE, 300);

        playerController = new PlayerController(player);
        Gdx.input.setInputProcessor(playerController);

        inimigos = new Array<Inimigo>();
        inimigos.add(new InimigoTerrestre(
                game.assetManager.get("Skeleton enemy.png", Texture.class),
                3000 * MAP_SCALE, 16 * MAP_SCALE));
        // Adiciona um inimigo voador
        inimigos.add(new InimigoVoador(
                game.assetManager.get("Skeleton enemy.png", Texture.class),
                2800 * MAP_SCALE, 500));
    }

    @Override
    public void show() {    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.21f, .37f, .23f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(game.camera.combined);

        tmr.setView(game.camera);
        tmr.render();

        Rectangle[] colisoes = {paredes[0], paredes[1], paredes[2],
                plataformas[0], plataformas[1], plataformas[2], plataformas[3],
                plataformas[4], plataformas[5], plataformas[6], plataformas[7],
                plataformas[8]};

        Rectangle previousPlayerBounds = player.getPlayerBounds(); //Posicao antiga do player para usa na colisao

        float camX = game.camera.position.x - game.camera.viewportWidth / 2;
        float camY = game.camera.position.y - game.camera.viewportHeight / 2;
        Rectangle cameraBounds = new Rectangle(camX, camY, game.camera.viewportWidth, game.camera.viewportHeight);

        player.update(delta);
        for (Inimigo inimigo : inimigos) {
            inimigo.update(delta, player, colisoes, cameraBounds);
        }

        //Get hitbox para colisao
        Rectangle playerBounds = player.getPlayerBounds();
        Rectangle playerHitbox = player.getPlayerHitBox();
        detectarColisao(playerBounds, previousPlayerBounds, colisoes);

        //Updates do jogo


        if(!player.isAttacking()){ //Reseta o Foi Atacado de todos os inimigos
            for (Inimigo inimigo : inimigos) {
                inimigo.setWasHited(false);
            }
        }
        else {
            Rectangle attackHitBox = player.getAttackHitBox();
            for (Inimigo inimigo : inimigos) {
                colisaoAtaque(attackHitBox, inimigo);
            }
        }


        game.batch.begin();                                 //Renderiza sprites na tela
        for (Inimigo inimigo : inimigos) {
            if (inimigo.isAlive()) {
                inimigo.draw(game.batch);
            }
        }
        player.render(game.batch);

        game.batch.end();

        Rectangle teste = plataformas[8];

        shape.setProjectionMatrix(game.camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);        //Renderiza shapes na tela



        shape.end();

        if(debugMode) {
            shape.begin(ShapeRenderer.ShapeType.Line);

            shape.setColor(Color.FIREBRICK);
            shape.rect(teste.x, teste.y, teste.width, teste.height);

//            if (player.isAttacking()) {
//                shape.setColor(Color.YELLOW);
//                Rectangle attackHitBox = player.getAttackHitBox();
//                shape.rect(attackHitBox.x, attackHitBox.y, attackHitBox.width, attackHitBox.height);
//            }

            for (Inimigo inimigo : inimigos) {
                if (inimigo.isAlive()) {
                    shape.setColor(Color.ORANGE);
                    Rectangle hitboxInimigo = inimigo.getHitboxRect();
                    shape.rect(hitboxInimigo.x, hitboxInimigo.y, hitboxInimigo.width, hitboxInimigo.height);
                    // Se o inimigo for voador, desenha os círculos de detecção
                    if (inimigo instanceof InimigoVoador) {
                        ((InimigoVoador) inimigo).desenharAreas(shape);
                    }
                    if (inimigo instanceof InimigoTerrestre) {
                        ((InimigoTerrestre) inimigo).desenharAreas(shape);
                    }
                }
            }

            playerBounds = player.getPlayerBounds();
            shape.setColor(Color.WHITE);
            shape.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);

            shape.setColor(Color.RED);
            shape.rect(playerHitbox.x, playerHitbox.y, playerHitbox.width, playerHitbox.height);

            shape.end();
        }

        atualizaCamera();
    }

    @Override
    public void pause() {    }

    @Override
    public void resume() {    }

    @Override
    public void hide() {    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, false);
    }

    @Override
    public void dispose() {    }

    private void atualizaCamera(){
        Vector2 position = player.getCenterPosition();

        if(position.x - game.camera.position.x > 100){
            game.camera.position.set(position.x + -100, game.camera.position.y, 0);
            game.camera.update();
        } else if (position.x - game.camera.position.x < -100) {
            game.camera.position.set(position.x + 100, game.camera.position.y, 0);
            game.camera.update();
        }
        if(position.y - game.camera.position.y > 200){
            game.camera.position.set(game.camera.position.x, position.y - 200, 0);
            game.camera.update();
        }
        else if(position.y - game.camera.position.y < -200){
            game.camera.position.set(game.camera.position.x, position.y + 200, 0);
            game.camera.update();
        }

    }

    private void detectarColisao(Rectangle playerBounds, Rectangle previousPlayerBounds, Rectangle[] platformBounds) {
        Rectangle auxBounds = previousPlayerBounds;

        auxBounds.x = playerBounds.x;
        for(int i = 0 ; i < platformBounds.length ; i++){
            Rectangle rect = platformBounds[i];

            if(auxBounds.overlaps(rect)){
                if(player.getDirection() < 0){
                    auxBounds.x = rect.x + rect.width + 0.01f;
                } else {
                    auxBounds.x = rect.x - auxBounds.width - 0.01f;
                }
            }
        }

        auxBounds.y = playerBounds.y;
        for(int i = 0 ; i < platformBounds.length ; i++) {
            Rectangle rect = platformBounds[i];

            if(auxBounds.overlaps(rect)){
                if(player.getVelocityY() < 0){
                    auxBounds.y = rect.y + rect.height + 0.01f;
                    player.setOnGround(true);
                    //player.setVelocityY(0);
                } else {
                    auxBounds.y = rect.y - auxBounds.height - 0.01f;
                    player.endJump();
                    player.setVelocityY(0);
                }
            }
            else if(player.getVelocityY() < 0){
                player.setOnGround(false);
            }
        }

        player.setPosition(auxBounds.x, auxBounds.y);
    }

    private void colisaoAtaque(Rectangle playerAtack, Inimigo inimigo){
        Rectangle hitBoxInimigo = inimigo.getHitboxRect();
        if (playerAtack.overlaps(hitBoxInimigo)) {
            if (inimigo.isAlive() && !inimigo.wasHited()) {
                inimigo.decreaseLife(1);
                inimigo.setWasHited(true);

                player.attackKnockback();
            }
        }
    }

    private void colisaoInimigoPlayer(Rectangle playerBounds, Inimigo inimigo){
        Rectangle hitBoxInimigo = inimigo.getHitboxRect();

        if(playerBounds.overlaps(hitBoxInimigo)){
            player.attackKnockback();
        }
    }
}
