package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BoardState {
	// thể hiện các trạng thái của bàn cờ 

	public static final int SIDE_LENGTH = 8;

	public static final int NO_SQUARES = SIDE_LENGTH * SIDE_LENGTH;

	Piece[] state;

	private int fromPos = -1;
	private int toPos = -1;

	private int doublejumpPos = -1;

	private Player turn;

	public HashMap<Player, Integer> pieceCount; // so luong quan co moi nguoi choi
	private HashMap<Player, Integer> kingCount; // so luong quan co vua moi ben

	public BoardState() {
		state = new Piece[BoardState.NO_SQUARES];
	}
	
	
	// Khoi tao trang thai ban dau cua ban co voi quan co duoc sap xep theo quy tac cua tro choi
	public static BoardState InitialState() {
		BoardState bs = new BoardState();

		bs.turn = Settings.FIRSTMOVE; // Dat luot di dau tien cho van co

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
				.count(); // dem lai so quan co cua may
		int humanCount = (int) Arrays.stream(bs.state).filter(x -> x != null).filter(x -> x.getPlayer() == Player.HUMAN)
				.count(); // dem lai so quan co cua nguoi choi

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
	
	// ham tinh toan gia tri uoc luong cua trang thai ban co
	
	public int computeHeuristic(BoardState state) {
	
		if (state.pieceCount.get(state.turn.getOpposite()) == 0) {
			return Integer.MAX_VALUE;
		}

		if (state.pieceCount.get(state.turn) == 0) {
			return Integer.MIN_VALUE;
		}

		return pieceScore(state.turn) - pieceScore(state.turn.getOpposite());
	}

	// tinh tong so quan co va quan co vua cua mot nguoi choi cu the
	private int pieceScore(Player player) {
		return this.pieceCount.get(player) + this.kingCount.get(player);
	}

	// tra ve danh sach cac trang thai ke tiep dua tren vi tri quan co va cai ep buoc nhay
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
	
	//Trả về danh sách các trạng thái kế tiếp dựa trên vị trí quân cờ và liệu nước đi có nhảy hay không.
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
	
	//Trả về danh sách các trạng thái kế tiếp dựa trên vị trí quân cờ và liệu nước đi có nhảy hay không.
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
	
	//Trả về danh sách các trạng thái kế tiếp cho các nước đi không nhảy.
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
	
	//Trả về danh sách các trạng thái kế tiếp cho các nước đi nhảy.
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

	//Tạo một trạng thái mới của bàn cờ sau khi thực hiện một nước đi.
	//Cập nhật thông tin về số lượng quân cờ, quân cờ vua, và lượt đi tiếp theo.
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
	
	//Kiểm tra xem vị trí hiện tại có phải là vị trí để phong quân thành vua không.
	private boolean isKingPosition(int pos, Player player) {

		int y = pos / SIDE_LENGTH;

		if (y == 0 && player == Player.HUMAN) {
			return true;
		} else

			return y == SIDE_LENGTH - 1 && player == Player.AI;
	}
	
	// 3 phuong thuc ben duoi tra ve cac thong tin ve nuoc di va luot choi hien tai
	public int getToPos() {
		return this.toPos;
	}

	public int getFromPos() {
		return this.fromPos;
	}

	public Player getTurn() {
		return turn;
	}
	
	// Kiểm tra xem trò chơi đã kết thúc chưa.
	public boolean isGameOver() {
		return (pieceCount.get(Player.AI) == 0 || pieceCount.get(Player.HUMAN) == 0);
	}
	
	//Trả về quân cờ tại một vị trí cụ thể.
	public Piece getPiece(int i) {

		return state[i];
	}

	private Piece getPiece(int y, int x) {

		return getPiece(SIDE_LENGTH * y + x);
	}
	
	//Kiểm tra xem vị trí có hợp lệ trong bàn cờ không.
	private boolean isValid(int y, int x) {
		return (0 <= y) && (y < SIDE_LENGTH) && (0 <= x) && (x < SIDE_LENGTH);
	}

}
