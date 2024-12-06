package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Player player;
	private PlayerController playerController;

	@Override
	public void create() {
		batch = new SpriteBatch();
		player = new Player("Wraith_idle.png", 100, 10);
		playerController = new PlayerController(player);
		Gdx.input.setInputProcessor(playerController);  // Controlador de Inputs, acredito que só o player vai ter inputs no jogo
	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();

		// Fazer Fundo
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Atualiza o player
		playerController.update();
		player.update(deltaTime);

		batch.begin();
		player.render(batch);
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		player.dispose();
	}
}
