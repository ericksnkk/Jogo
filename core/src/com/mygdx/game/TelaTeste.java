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
    final int TILE_SCALE = 16;
    final int SCALE = TILE_SCALE * MAP_SCALE;
    final MyGdxGame game;
    private Player player;
    private PlayerController playerController;
    private Array<Inimigo> inimigos;

    private ShapeRenderer shape;

    private OrthogonalTiledMapRenderer tmr;
    private TiledMap map;

    private Rectangle[] plataformas = new Rectangle[15];
    private Rectangle nextMapHitbox;
    private int nPlataformas;
    private boolean debugMode = true;

    public TelaTeste(final MyGdxGame game, Player player) {
        this.game = game;

        map = new TmxMapLoader().load("maps/lvl1.tmx");
        tmr = new OrthogonalTiledMapRenderer(map, MAP_SCALE);


        shape = new ShapeRenderer();

        nPlataformas = 15;

        plataformas[0] = new Rectangle(0*SCALE, 7 * SCALE, 50 * SCALE, 2 * SCALE);
        plataformas[1] = new Rectangle(10*SCALE, 9 * SCALE, 2 * SCALE, 2 * SCALE);
        plataformas[2] = new Rectangle(9*SCALE, 8 * SCALE, 1 * SCALE, 10 * SCALE);
        plataformas[3] = new Rectangle(9*SCALE, 21 * SCALE, 1 * SCALE, 7 * SCALE);
        plataformas[4] = new Rectangle(8*SCALE, 17 * SCALE, 1 * SCALE, 5 * SCALE);
        plataformas[5] = new Rectangle(9*SCALE, 24 * SCALE, 3 * SCALE, 2 * SCALE);
        plataformas[6] = new Rectangle(15*SCALE, 14 * SCALE, 8 * SCALE, 1 * SCALE);
        plataformas[7] = new Rectangle(28*SCALE, 16 * SCALE, 8 * SCALE, 1 * SCALE);
        plataformas[8] = new Rectangle(40*SCALE, 19 * SCALE, 10 * SCALE, 1 * SCALE);
        plataformas[9] = new Rectangle(24*SCALE, 9 * SCALE, 6 * SCALE, 2 * SCALE);
        plataformas[10] = new Rectangle(39*SCALE, 9 * SCALE, 6 * SCALE, 3 * SCALE);
        plataformas[11] = new Rectangle(46*SCALE, 9 * SCALE, 1 * SCALE, 10 * SCALE);
        plataformas[12] = new Rectangle(9*SCALE, 25 * SCALE, 36 * SCALE, 2 * SCALE);
        plataformas[13] = new Rectangle(45*SCALE, 23 * SCALE, 5 * SCALE, 3 * SCALE);
        plataformas[14] = new Rectangle(49*SCALE, 20 * SCALE, 1 * SCALE, 4 * SCALE);
        //plataformas[15] = new Rectangle(0*SCALE, 0 * SCALE, 100 * SCALE, 9 * SCALE);

        nextMapHitbox = new Rectangle(48*SCALE, 20 * SCALE, 1 * SCALE, 4 * SCALE);

        this.player = player;
        this.player.setPosition(15 * SCALE, 11 * SCALE);

        playerController = new PlayerController(player);
        Gdx.input.setInputProcessor(playerController);

        inimigos = new Array<Inimigo>();
        inimigos.add(new InimigoTerrestre(
                game.assetManager.get("Skeleton enemy.png", Texture.class),
                34 * SCALE, 9 * SCALE));
        // Adiciona um inimigo voador
        inimigos.add(new InimigoVoador(
                game.assetManager.get("Skeleton enemy.png", Texture.class),
                15 * SCALE, 20 * SCALE));
        inimigos.add(new InimigoVoador(
                game.assetManager.get("Skeleton enemy.png", Texture.class),
                34 * SCALE, 20 * SCALE));
    }

    @Override
    public void show() {    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(game.camera.combined);

        tmr.setView(game.camera);
        tmr.render();

        Rectangle previousPlayerBounds = player.getPlayerBounds(); //Posicao antiga do player para usa na colisao

        float camX = game.camera.position.x - game.camera.viewportWidth / 2;
        float camY = game.camera.position.y - game.camera.viewportHeight / 2;
        Rectangle cameraBounds = new Rectangle(camX, camY, game.camera.viewportWidth, game.camera.viewportHeight);

        player.update(delta);
        for (Inimigo inimigo : inimigos) {
            inimigo.update(delta, player, plataformas, cameraBounds);
        }

        //Get hitbox para colisao
        Rectangle playerBounds = player.getPlayerBounds();
        Rectangle playerHitbox = player.getPlayerHitBox();
        detectarColisao(playerBounds, previousPlayerBounds, plataformas);

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

        if(player.getPlayerBounds().overlaps(nextMapHitbox)){
            game.setScreen(new TelaBoss(game, player));
        }


        game.batch.begin();                                 //Renderiza sprites na tela
        for (Inimigo inimigo : inimigos) {
            if (inimigo.isAlive()) {
                inimigo.draw(game.batch);
            }
        }
        player.render(game.batch);

        game.batch.end();


        shape.setProjectionMatrix(game.camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);        //Renderiza shapes na tela



        shape.end();

        if(debugMode) {
            shape.begin(ShapeRenderer.ShapeType.Line);

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

            for(int i = 0; i < nPlataformas; i++){
                shape.setColor(Color.RED);
                shape.rect(plataformas[i].x, plataformas[i].y, plataformas[i].width, plataformas[i].height);
            }

            shape.setColor(Color.ORANGE);
            shape.rect(nextMapHitbox.x, nextMapHitbox.y, nextMapHitbox.width, nextMapHitbox.height);

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
