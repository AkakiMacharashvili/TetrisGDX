package tetris;

public class Controller implements ModelListener, GameEventsListener {
	
	private TetrisModel model;
	private View view;

	public Controller(TetrisModel model, View view) {
		this.model = model;
		model.addListener(this);
		this.view = view;
	}

	@Override
	public void onChange(TetrisModel model) {
		view.draw(model);
	}

	@Override
	public void over(TetrisModel tetrisModel) {

	}

	@Override
	public void slideDown() {
		model.slideDown();
	}

	@Override
	public void moveLeft() {
		model.moveLeft();
	}

	@Override
	public void moveRight() {
		model.moveRight();
	}

	@Override
	public void rotate() {
		model.rotate();
	}

	@Override
	public void drop() {
		model.drop();
	}

	public void levelUp() {
		model.levelUp();
	}

	public void levelDown() {
		model.levelDown();
	}

	public void infinite(){
		model.infinite();
	}
}
