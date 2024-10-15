package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	float  xinput = 0, yinput = 0, ximg = 0, yimg = 0;
	float  tolerance = 5.0f, SPEED = 5.0f;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render() {
		ScreenUtils.clear(1, 0, 0, 1);

		if (Gdx.input.isTouched()) {
			xinput = Gdx.input.getX() - img.getWidth()/2;
			yinput = Gdx.graphics.getHeight() - Gdx.input.getY() - img.getHeight()/2;
		}

		batch.begin();

		float dx = xinput - ximg;
		float dy = yinput - yimg;

		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		if (distance > tolerance) {
			float moveX = (dx / distance) * SPEED;
			float moveY = (dy / distance) * SPEED;

			ximg += moveX;
			yimg += moveY;

			if (distance < SPEED) {
				ximg = xinput;
				yimg = yinput;
			}
		}

		batch.draw(img, ximg, yimg);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	//teste
}
