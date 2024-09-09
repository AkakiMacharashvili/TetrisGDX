package kiu.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import tetris.*;

public class TetrisStage extends Stage implements Graphics {

	static Color[] COLORS = {
			Color.DARK_GRAY, Color.RED, Color.GREEN,
			Color.BLUE, Color.CYAN, Color.YELLOW,
			Color.MAGENTA, Color.MAROON
	};

	private ShapeRenderer shape;
	private SpriteBatch batch;
	private BitmapFont font;
	private View view;
	private TetrisModel model;
	private Controller controller;
	private Timer timer;
	private Timer.Task task;
	private TextButton refreshButton;
	private TextButton quitButton;

	public TetrisStage() {
		// Initialize camera and viewport
		OrthographicCamera camera = new OrthographicCamera();
		camera.setToOrtho(false, 400, 700); // Set the camera's viewport width and height
		setViewport(new ScreenViewport(camera));

		shape = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont(); // Load the default font

		createRefreshButton();
		addRefreshButton();
		refreshButton.setVisible(false);

		createQuitButton();
		addQuitButton();
		quitButton.setVisible(false);

		Gdx.graphics.setWindowedMode(400, 700);
		Gdx.input.setInputProcessor(this);
	}

	public void init() {
		model = new TetrisModel();
		view = new View(this);
		controller = new Controller(model, view);

		model.addListener(controller);
		timer = new Timer();
		scheduleTask();

		addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
					case Input.Keys.LEFT:
						controller.moveLeft();
						break;
					case Input.Keys.RIGHT:
						controller.moveRight();
						break;
					case Input.Keys.DOWN:
						controller.drop();
						break;
					case Input.Keys.UP:
						controller.rotate();
						break;
					case Input.Keys.EQUALS:
						controller.levelUp();
						model.FirstTry = true;
						scheduleTask();
						break;
					case Input.Keys.MINUS:
						controller.levelDown();
						model.FirstTry = true;
						scheduleTask();
						break;
					case Input.Keys.SPACE:
						model.slideDown();
						break;


					case Input.Keys.P:
						model.pause();
				}
				return true;
			}
		});
	}

	private void addQuitButton() {
		quitButton.setPosition(190, Gdx.graphics.getHeight() - 50);
		System.out.println(Gdx.graphics.getHeight());
		quitButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
				quitButton.setVisible(false);
			}
		});
		addActor(quitButton);
	}

	private void createQuitButton() {
		BitmapFont font = new BitmapFont();
		TextButtonStyle textButtonStyle = new TextButtonStyle();

		textButtonStyle.font = font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.downFontColor = Color.GRAY;
		textButtonStyle.overFontColor = Color.YELLOW;

		quitButton = new TextButton("Quit", textButtonStyle);
	}

	private void createRefreshButton() {
		BitmapFont font = new BitmapFont();
		TextButtonStyle textButtonStyle = new TextButtonStyle();

		textButtonStyle.font = font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.downFontColor = Color.GRAY;
		textButtonStyle.overFontColor = Color.YELLOW;

		refreshButton = new TextButton("Refresh", textButtonStyle);
	}

	private void addRefreshButton() {
		refreshButton.setPosition(180, (float) Gdx.graphics.getHeight());
		refreshButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				model.refresh();
				refreshButton.setVisible(false);
				quitButton.setVisible(false);
			}
		});
		addActor(refreshButton);
	}

	private void scheduleTask() {
		if (task != null) {
			task.cancel();
		}
		task = new Timer.Task() {
			@Override
			public void run() {
				controller.infinite();
			}
		};
		System.out.println(model.level);
		timer.scheduleTask(task, 1.0f, Math.max(model.level, 1) / 1000.0f);
	}

	@Override
	public void draw() {
		// Clear the screen with a color if needed
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

		// Draw the game board
		view.draw(model);

		// Draw the stage (including UI components)
		super.draw();
	}

	@Override
	public void drawScore() {
		font.getData().setScale(1f);
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		batch.begin();
		font.setColor(Color.WHITE); // Set the color for text
		String scoreMessage = "Score: " + model.score; // Retrieve the score from the model
		float xPosition = 50;
		float yPosition = Gdx.graphics.getHeight() - 20; // Y position of the text
		font.draw(batch, scoreMessage, xPosition, yPosition); // Draw the text
		batch.end();
	}

	@Override
	public void drawLevel() {
		font.getData().setScale(1f);
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		batch.begin();
		font.setColor(Color.WHITE); // Set the color for text
		int currentLevel = (int) ((1100 - model.level) / 100);
		String scoreMessage = "Level: " + currentLevel; // Retrieve the score from the model
		float xPosition = 300;
		float yPosition = Gdx.graphics.getHeight() - 20; // Y position of the text
		font.draw(batch, scoreMessage, xPosition, yPosition); // Draw the text
		batch.end();
	}

	@Override
	public void gameOver() {
		int xPosition = 150;
		int yPosition = Gdx.graphics.getHeight() - 100; // Y position of the text
		drawString("Game Over", xPosition, yPosition, Color.RED);
	}

	@Override
	public void activateGameOver() {
		refreshButton.setVisible(true);
		quitButton.setVisible(true);
	}

	@Override
	public void drawPlayer(int posY, Player player, int i) {
		font.getData().setScale(1);
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		batch.begin();
		font.setColor(Color.GREEN); // Set the color for text
		String scoreMessage = (i + 1) + ". Player name: " + player.getName() + ", " + "Score: " + player.getScore();
		float xPosition = 100;
		int yPosition = posY; // Y position of the text
		font.draw(batch, scoreMessage, xPosition, yPosition); // Draw the text
		batch.end();
	}

	@Override
	public void drawScoreTable() {
		int xPosition = 150;
		int yPosition = Gdx.graphics.getHeight() - 450; // Y position of the text
		drawString("LeaderBoard", xPosition, yPosition, Color.GREEN);
	}

	@Override
	public void drawBoxAt(int x, int y, int size, int colorIndex) {
		shape.setProjectionMatrix(getViewport().getCamera().combined);
		shape.begin(ShapeRenderer.ShapeType.Filled);
		shape.setColor(COLORS[colorIndex]);

		// Adjust y to flip the coordinate system
		float adjustedY = Gdx.graphics.getHeight() - y - size;

		shape.rect(x, adjustedY, size, size);
		shape.end();
	}

	@Override
	public void setColor(Color color) {
		shape.setColor(color);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		shape.setProjectionMatrix(getViewport().getCamera().combined);
		shape.begin(ShapeRenderer.ShapeType.Line);
		shape.setColor(Color.BLACK); // Set default color, adjust if needed
		shape.line(x1, y1, x2, y2);
		shape.end();
	}

	@Override
	public void drawString(String message, int x, int y, Color color) {
		font.getData().setScale(1.5f);
		batch.setProjectionMatrix(getViewport().getCamera().combined);
		batch.begin();
		font.setColor(color);
		font.draw(batch, message, x, y);
		batch.end();
	}

	@Override
	public void dispose() {
		shape.dispose();
		batch.dispose();
		font.dispose();
		super.dispose();
	}



}
