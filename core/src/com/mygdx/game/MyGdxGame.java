package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MyGdxGame extends Game {
	public SpriteBatch batch;

	public AssetManager assetManager;

	public FitViewport viewport;
	public OrthographicCamera camera;

	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		int tam = 30;
		camera = new OrthographicCamera(tam, tam * (h / w));

		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		camera.update();

		viewport = new FitViewport(w, h, camera);

		batch = new SpriteBatch();

		assetManager = new AssetManager();

		assetManager.load("Wraith_idle.png", Texture.class);
		assetManager.load("bg_menu.png", Texture.class);
		assetManager.load("Skeleton enemy.png", Texture.class);

		assetManager.finishLoading();

		setScreen(new Menu(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
		assetManager.clear();
	}
}
