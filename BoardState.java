package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.game.Settings;

public class BoardState {

	public static final int SIDE_LENGTH = 8;

	public static final int NO_SQUARES = SIDE_LENGTH * SIDE_LENGTH;

	Piece[] state;

	private int fromPos = -1;
	private int toPos = -1;

	private int doublejumpPos = -1;

	private Player turn;

	public HashMap<Player, Integer> pieceCount;
	private HashMap<Player, Integer> kingCount;

	public BoardState() {
		state = new Piece[BoardState.NO_SQUARES];
	}

	public static BoardState InitialState() {
		BoardState bs = new BoardState();

		bs.turn = Settings.FIRSTMOVE;

		for (int i = 0; i < bs.state.length; i++) {
			int y = i / SIDE_LENGTH;
			int x = i % SIDE_LENGTH;

			if ((x + y) % 2 == 1) {

				if (y < 3) {
					bs.state[i] = new Piece(Player.AI, false);
				}

				else if (y > 4) {
					bs.state[i] = new Piece(Player.HUMAN, false);
				}
			}
		}

		int aiCount = (int) Arrays.stream(bs.state).filter(x -> x != null).filter(x -> x.getPlayer() == Player.AI)
				.count();
		int humanCount = (int) Arrays.stream(bs.state).filter(x -> x != null).filter(x -> x.getPlayer() == Player.HUMAN)
				.count();

		bs.pieceCount = new HashMap<>();
		bs.pieceCount.put(Player.AI, aiCount);
		bs.pieceCount.put(Player.HUMAN, humanCount);

		bs.kingCount = new HashMap<>();
		bs.kingCount.put(Player.AI, 0);
		bs.kingCount.put(Player.HUMAN, 0);

		return bs;
	}

	private BoardState deepCopy() {
		BoardState bs = new BoardState();
		System.arraycopy(this.state, 0, bs.state, 0, bs.state.length);
		return bs;
	}

	public int computeHeuristic(BoardState state) {
	
		if (state.pieceCount.get(state.turn.getOpposite()) == 0) {
			return Integer.MAX_VALUE;
		}

		if (state.pieceCount.get(state.turn) == 0) {
			return Integer.MIN_VALUE;
		}

		return pieceScore(state.turn) - pieceScore(state.turn.getOpposite());
	}

	private int pieceScore(Player player) {
		return this.pieceCount.get(player) + this.kingCount.get(player);
	}

	
	public ArrayList<BoardState> getSuccessors() {
		ArrayList<BoardState> successors = getSuccessors(true);

		if (Settings.FORCETAKES) {

			if (successors.size() > 0) {
				return successors;
			} else {

				return getSuccessors(false);
			}
		}

		else {

			successors.addAll(getSuccessors(false));

			return successors;
		}
	}

	public ArrayList<BoardState> getSuccessors(boolean jump) {
		ArrayList<BoardState> result = new ArrayList<>();

		for (int i = 0; i < this.state.length; i++) {

			if (state[i] != null) {

				if (state[i].getPlayer() == turn) {
					result.addAll(getSuccessors(i, jump));
				}
			}
		}

		return result;
	}

	public ArrayList<BoardState> getSuccessors(int position) {

		if (Settings.FORCETAKES) {

			ArrayList<BoardState> jumps = getSuccessors(true);

			if (jumps.size() > 0) {

				return getSuccessors(position, true);
			} else {

				return getSuccessors(position, false);
			}

		}

		else {

			ArrayList<BoardState> result = new ArrayList<>();

			result.addAll(getSuccessors(position, true));

			result.addAll(getSuccessors(position, false));

			return result;
		}
	}

	public ArrayList<BoardState> getSuccessors(int position, boolean jump) {

		if (this.getPiece(position).getPlayer() != turn) {
			throw new IllegalArgumentException("Không có quân cờ nào tại vị trí đó");
		}

		Piece piece = this.state[position];

		if (jump) {
			return jumpSuccessors(piece, position);
		} else {
			return nonJumpSuccessors(piece, position);
		}
	}

	private ArrayList<BoardState> nonJumpSuccessors(Piece piece, int position) {

		ArrayList<BoardState> result = new ArrayList<>();

		int x = position % SIDE_LENGTH;
		int y = position / SIDE_LENGTH;

		for (int dx : piece.getXMovements()) {
			for (int dy : piece.getYMovements()) {

				int newX = x + dx;
				int newY = y + dy;

				if (isValid(newY, newX)) {

					if (getPiece(newY, newX) == null) {

						int newpos = SIDE_LENGTH * newY + newX;

						result.add(createNewState(position, newpos, piece, false, dy, dx));
					}
				}
			}
		}
		return result;
	}

	private ArrayList<BoardState> jumpSuccessors(Piece piece, int position) {

		ArrayList<BoardState> result = new ArrayList<>();

		if (doublejumpPos > 0 && position != doublejumpPos) {
			return result;
		}

		int x = position % SIDE_LENGTH;
		int y = position / SIDE_LENGTH;

		for (int dx : piece.getXMovements()) {
			for (int dy : piece.getYMovements()) {

				int newX = x + dx;
				int newY = y + dy;

				if (isValid(newY, newX)) {

					if (getPiece(newY, newX) != null
							&& getPiece(newY, newX).getPlayer() == piece.getPlayer().getOpposite()) {
						newX = newX + dx;
						newY = newY + dy;

						if (isValid(newY, newX)) {

							if (getPiece(newY, newX) == null) {

								int newpos = SIDE_LENGTH * newY + newX;

								result.add(createNewState(position, newpos, piece, true, dy, dx));
							}
						}
					}
				}
			}
		}
		return result;
	}

	private BoardState createNewState(int oldPos, int newPos, Piece piece, boolean jumped, int dy, int dx) {

		BoardState result = this.deepCopy();

		result.pieceCount = new HashMap<>(pieceCount);
		result.kingCount = new HashMap<>(kingCount);

		boolean kingConversion = false;

		if (isKingPosition(newPos, piece.getPlayer())) {
			piece = new Piece(piece.getPlayer(), true);
			kingConversion = true;

			result.kingCount.putIfAbsent(piece.getPlayer(), 0);
			result.kingCount.replace(piece.getPlayer(), result.kingCount.get(piece.getPlayer()) + 1);
		}


		result.state[oldPos] = null;
		result.state[newPos] = piece;

		
		result.fromPos = oldPos;
		result.toPos = newPos;

		Player oppPlayer = piece.getPlayer().getOpposite();

		result.turn = oppPlayer;

		if (jumped) {
			result.state[newPos - SIDE_LENGTH * dy - dx] = null;

			result.pieceCount.replace(oppPlayer, result.pieceCount.get(oppPlayer) - 1);

			if (result.jumpSuccessors(piece, newPos).size() > 0 && !kingConversion) {
				result.turn = piece.getPlayer();
				result.doublejumpPos = newPos;
			}
		}

		return result;
	}

	private boolean isKingPosition(int pos, Player player) {

		int y = pos / SIDE_LENGTH;

		if (y == 0 && player == Player.HUMAN) {
			return true;
		} else

			return y == SIDE_LENGTH - 1 && player == Player.AI;
	}

	public int getToPos() {
		return this.toPos;
	}

	public int getFromPos() {
		return this.fromPos;
	}

	public Player getTurn() {
		return turn;
	}

	public boolean isGameOver() {
		return (pieceCount.get(Player.AI) == 0 || pieceCount.get(Player.HUMAN) == 0);
	}

	public Piece getPiece(int i) {

		return state[i];
	}

	private Piece getPiece(int y, int x) {

		return getPiece(SIDE_LENGTH * y + x);
	}

	private boolean isValid(int y, int x) {
		return (0 <= y) && (y < SIDE_LENGTH) && (0 <= x) && (x < SIDE_LENGTH);
	}

}
