package tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class TetrisModel implements GameEventsListener {
	public static final int DEFAULT_HEIGHT = 20;
	public static final int DEFAULT_WIDTH = 10;
	public static final int DEFAULT_COLORS_NUMBER = 7;
	public static final int DEFAULT_NEXT_LEVEL = 100;
	public static final int DEFAULT_MAX_LEVEL = 1000;
	private boolean canOperation = false;

	public long level = 1000;
	public long maxLevel = 1000;
	public boolean FirstTry = true;
	public int score = 0;
	public TetrisState state = new TetrisState();

	int maxColors;

	List<ModelListener> listeners = new ArrayList<>();
	public boolean finished = false;
	public boolean paused = false;
	public List<Player> players = new ArrayList<>();

	public void initFigure() {
		canOperation = true;
		deleteFullRows();
		state.field = shiftRowsDown(state.field);
		state.figure = new FigureFactory().createNextFigure();
		state.position = new Pair(this.state.width / 2 - 2, 0);
	}

	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ModelListener listener) {
		listeners.remove(listener);
	}

	public TetrisModel() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_COLORS_NUMBER);
	}

	public TetrisModel(int width, int height, int maxColor) {
		this.state.width = width;
		this.state.height = height;
		this.state.field = new int[height][width];
		this.maxColors = maxColor;
		initFigure();
	}

	public Pair size() {
		return new Pair(state.width, state.height);
	}

	public void notifyListeners() {
		listeners.forEach(listener -> listener.onChange(this));
	}

	public void updateScore(int total) {
		score += total;
		notifyListeners();
	}

	public void infinite() {
		if (!paused) {
			if (FirstTry) FirstTry = false;
			else {
				if (!finished) slideDown();
				else itsOver();
			}
		}else{
			notifyListeners();
		}
	}

	private void itsOver() {
		listeners.forEach(listener -> listener.over(this));
	}

	@Override
	public void slideDown() {
		var newPosition = new Pair(state.position.x(), state.position.y() + 1);
		if (isNewFigurePositionValid(newPosition)) {
			state.position = newPosition;
			notifyListeners();
		} else {
			canOperation = false;
			stay();
		}
	}

	public void stay() {
		pasteFigure();
		initFigure();
		notifyListeners();
		if (!isNewFigurePositionValid(state.position)) {
			gameOver();
		}

		updateScore(10);
		nextLevel();
	}

	public void nextLevel() {
		if (this.score % DEFAULT_NEXT_LEVEL == 0) {
			int current = this.score / DEFAULT_NEXT_LEVEL;
			if (current < 1000 / DEFAULT_NEXT_LEVEL) {
				this.level = Math.min(this.level, 1000 - current * DEFAULT_NEXT_LEVEL);
				this.maxLevel = 1000 - current * DEFAULT_NEXT_LEVEL;
			}
		}
	}

	public void gameOver() {
		finished = true;
		players.add(new Player("Player " + players.size(), score));
		players.sort(Comparator.comparingInt(Player::getScore).reversed());
		itsOver();
	}

	public int[][] shiftRowsDown(int[][] field) {
		for (int row = field.length - 2; row >= 0; row--) {
			int current = row;
			while (current < field.length - 1 && isRowFullOfZeros(current + 1)) {
				System.arraycopy(field[current], 0, field[current + 1], 0, field[current].length);
				Arrays.fill(field[current], 0);
				current++;
			}
		}
		return field;
	}

	public void deleteFullRows() {
		for (int row = 0; row < state.field.length; row++) {
			if (isRowFullOfOnes(row)) {
				Arrays.fill(state.field[row], 0);
			}
		}
	}

	public boolean isNewFigurePositionValid(Pair pair) {
		boolean[] result = new boolean[1];
		result[0] = true;

		walkThroughAllFigureCells(pair, (absPos, relPos) -> {
			if (result[0]) {
				result[0] = checkAbsPos(absPos);
			}
		});

		return result[0];
	}

	@Override
	public void moveLeft() {
		if(canOperation) {
			var newPosition = new Pair(state.position.x() - 1, state.position.y());
			if (isNewFigurePositionValid(newPosition)) {
				state.position = newPosition;
				notifyListeners();
			}
		}
	}

	@Override
	public void moveRight() {
		if(canOperation) {
			var newPosition = new Pair(state.position.x() + 1, state.position.y());
			if (isNewFigurePositionValid(newPosition)) {
				state.position = newPosition;
				notifyListeners();
			}
		}
	}

	@Override
	public void drop() {
		Pair newPosition = new Pair(state.position.x(), state.position.y() + 1);
		while (isNewFigurePositionValid(newPosition)) {
			state.position = newPosition;
			newPosition = new Pair(state.position.x(), state.position.y() + 1);
		}
		canOperation = false;
	}

	@Override
	public void rotate() {
		if(canOperation) {
			tryRotation();
			notifyListeners();
		}
	}

	public void tryRotation() {
		int[][] rotated = new int[state.figure.length][state.figure[0].length];
		int[][] prev = state.figure;
		for (int row = 0; row < state.figure.length; row++) {
			for (int col = 0; col < state.figure[row].length; col++) {
				rotated[col][3 - row] = state.figure[row][col];
			}
		}
		state.figure = rotated;
		if (!isNewFigurePositionValid(state.position)) {
			state.figure = prev;
		}
	}

	public boolean checkAbsPos(Pair absPos) {
		int absX = absPos.x();
		int absY = absPos.y();
		if (0 > absX || absX >= this.state.width) {
			return false;
		}
		if (0 > absY || absY >= this.state.height) {
			return false;
		}

		return state.field[absY][absX] == 0;
	}

	public void walkThroughAllFigureCells(Pair position, BiConsumer<Pair, Pair> payload) {
		for (int row = 0; row < state.figure.length; row++) {
			for (int col = 0; col < state.figure[row].length; col++) {
				if (state.figure[row][col] == 0) continue;

				int absRow = position.y() + row;
				int absCol = position.x() + col;
				payload.accept(new Pair(absCol, absRow), new Pair(col, row));
			}
		}
	}

	public void pasteFigure() {
		walkThroughAllFigureCells(state.position, (absPos, relPos) -> state.field[absPos.y()][absPos.x()] = state.figure[relPos.y()][relPos.x()]);
	}


	public boolean isRowFullOfOnes(int i) {
		for (int j = 0; j < state.field[i].length; j++) {
			if (state.field[i][j] == 0) return false;
		}
		return true;
	}

	public boolean isRowFullOfZeros(int i) {
		for (int j = 0; j < state.field[i].length; j++) {
			if (state.field[i][j] != 0) return false;
		}
		return true;
	}

	public void levelUp() {
		this.level = Math.max(this.level - 100, 100);
		notifyListeners();
	}

	public void levelDown() {
		this.level = Math.min(this.level + 100, maxLevel);
		notifyListeners();
	}

	public void reset() {
		state.field = new int[state.height][state.width];
		score = 0;
		finished = false;
		level = 1000;
		initFigure();
		notifyListeners();
	}

	public void pause() {
		this.paused = !this.paused;
	}

	public void refresh() {
		this.state.width = DEFAULT_WIDTH;
		this.state.height = DEFAULT_HEIGHT;
		this.state.field = new int[DEFAULT_HEIGHT][DEFAULT_WIDTH];
		this.maxColors = DEFAULT_COLORS_NUMBER;
		this.finished = false;
		this.level = DEFAULT_MAX_LEVEL;
		this.score = 0;
		initFigure();
	}
}
