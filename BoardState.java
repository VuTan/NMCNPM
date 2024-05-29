package main.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.game.Settings;

public class BoardState {

	public static final int SIDE_LENGTH = 8;

	// số ô trên bàn cờ
	public static final int NO_SQUARES = SIDE_LENGTH * SIDE_LENGTH;

	Piece[] state;

	// Vị trí xuất phát và kết thúc của một nước đi.

	private int fromPos = -1;
	private int toPos = -1;

	// Vị trí của nước đi nhảy đôi
	private int doublejumpPos = -1;

	private Player turn;

	// Số lượng quân cờ của mỗi người chơi.
	public HashMap<Player, Integer> pieceCount;
	// Số lượng quân vua của mỗi người chơi.
	private HashMap<Player, Integer> kingCount;

	public BoardState() {
		state = new Piece[BoardState.NO_SQUARES];
	}

	// trạng thái ban đầu của bàn cờ
	public static BoardState InitialState() {
		BoardState bs = new BoardState();

		bs.turn = Settings.FIRSTMOVE;

		// khởi tạo quân cờ trên bàn cờ
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

	// bản sao của đối tượng BoardState
	private BoardState deepCopy() {
		BoardState bs = new BoardState();
		System.arraycopy(this.state, 0, bs.state, 0, bs.state.length);
		return bs;
	}

	// tính điểm cho người chơi dựa trên số lượng quân cờ và quân vua.
	private int pieceScore(Player player) {
		return this.pieceCount.get(player) + this.kingCount.get(player);
	}

	// trả về danh sách các trạng thái bàn cờ kế tiếp.
	public ArrayList<BoardState> getSuccessors() {
		// tìm tất cả các trạng thái kế tiếp có thực hiện bước nhảy
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

	// tìm tất cả các trạng thái bàn cờ kế tiếp dựa trên các bước đi hợp lệ của tất
	// cả các quân cờ thuộc về người chơi hiện tại.
	public ArrayList<BoardState> getSuccessors(boolean jump) {
		ArrayList<BoardState> result = new ArrayList<>();

		// Lặp qua tất cả các ô trên bàn cờ
		for (int i = 0; i < this.state.length; i++) {

			if (state[i] != null) {

				if (state[i].getPlayer() == turn) {

					// Kết quả được thêm vào danh sách result.
					result.addAll(getSuccessors(i, jump));
				}
			}
		}

		return result;
	}

	// trả về một danh sách các trạng thái bàn cờ (ArrayList<BoardState>) tương ứng
	// với các bước đi kế tiếp của quân cờ tại vị trí position.
	// xử lý trường hợp FORCETAKES
	public ArrayList<BoardState> getSuccessors(int position) {

		if (Settings.FORCETAKES) {

			// lấy danh sách các bước nhảy có thể của quân cờ.
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

	// trả về một danh sách các trạng thái bàn cờ tương ứng
	// với các bước đi kế tiếp của quân cờ tại vị trí position.
	public ArrayList<BoardState> getSuccessors(int position, boolean jump) {

		if (this.getPiece(position).getPlayer() != turn) {
			throw new IllegalArgumentException("Chưa tới lượt chơi của bạn");
		}

		// Lấy quân cờ tại vị trí position và gán nó cho biến piece.
		Piece piece = this.state[position];

		if (jump) {
			return jumpSuccessors(piece, position);
		} else {
			return nonJumpSuccessors(piece, position);
		}
	}

	// xử lý các bước đi thông thường của quân cờ,
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

	// tạo ra các trạng thái bàn cờ mới sau khi thực hiện các bước nhảy
	private ArrayList<BoardState> jumpSuccessors(Piece piece, int position) {

		ArrayList<BoardState> result = new ArrayList<>();

		// Kiểm tra nếu có vị trí nhảy kép (doublejumpPos) và vị trí hiện tại của quân
		// cờ không khớp với vị trí đó, thì trả về danh sách kết quả rỗng. Điều này đảm
		// bảo rằng quân cờ chỉ có thể nhảy kép từ vị trí cuối cùng đã nhảy.
		if (doublejumpPos > 0 && position != doublejumpPos) {
			return result;
		}

		// toán tọa độ x và y từ vị trí hiện tại của quân cờ
		int x = position % SIDE_LENGTH;
		int y = position / SIDE_LENGTH;

		// Duyệt qua tất cả các chuyển động có thể của quân cờ
		for (int dx : piece.getXMovements()) {
			for (int dy : piece.getYMovements()) {

				int newX = x + dx;
				int newY = y + dy;

				// Kiểm tra xem tọa độ mới có hợp lệ không
				if (isValid(newY, newX)) {

					// độ mới hợp lệ và vị trí này có một quân cờ của đối thủ, tiếp tục tính toán vị
					// trí sau khi nhảy
					if (getPiece(newY, newX) != null
							&& getPiece(newY, newX).getPlayer() == piece.getPlayer().getOpposite()) {
						newX = newX + dx;
						newY = newY + dy;

						if (isValid(newY, newX)) {

							if (getPiece(newY, newX) == null) {

								int newpos = SIDE_LENGTH * newY + newX;
								// Thêm trạng thái mới của bàn cờ sau khi thực hiện nhảy
								result.add(createNewState(position, newpos, piece, true, dy, dx));
							}
						}
					}
				}
			}
		}
		return result;
	}

	// tạo ra một trạng thái mới của bàn cờ sau khi một quân cờ di chuyển
	private BoardState createNewState(int oldPos, int newPos, Piece piece, boolean jumped, int dy, int dx) {

		// Tạo một bản sao của trạng thái hiện tại của bàn cờ để tạo ra trạng thái mới
		// sau khi di chuyển.
		BoardState result = this.deepCopy();

		// Sao chép số lượng quân cờ và số lượng quân "king" cho trạng thái mới.
		result.pieceCount = new HashMap<>(pieceCount);
		result.kingCount = new HashMap<>(kingCount);

		boolean kingConversion = false;

		// Kiểm tra xem vị trí mới có phải là vị trí "king" cho quân cờ của người chơi
		// hay không.

		if (isKingPosition(newPos, piece.getPlayer())) {
			// Nếu đúng, tạo một quân cờ mới với thuộc tính "king" (true) và tăng số lượng
			// quân "king" của người chơi lên.
			piece = new Piece(piece.getPlayer(), true);
			kingConversion = true;

			result.kingCount.putIfAbsent(piece.getPlayer(), 0);
			result.kingCount.replace(piece.getPlayer(), result.kingCount.get(piece.getPlayer()) + 1);
		}

		// Di chuyển quân cờ từ vị trí cũ đến vị trí mới
		result.state[oldPos] = null;
		result.state[newPos] = piece;

		// Cập nhật vị trí bắt đầu và kết thúc của bước di chuyển.
		result.fromPos = oldPos;
		result.toPos = newPos;

		Player oppPlayer = piece.getPlayer().getOpposite();

		result.turn = oppPlayer;
		// Nếu là một bước nhảy
		if (jumped) {
			// Xóa quân cờ bị nhảy qua khỏi bàn cờ.
			result.state[newPos - SIDE_LENGTH * dy - dx] = null;
			// Giảm số lượng quân cờ của đối thủ.
			result.pieceCount.replace(oppPlayer, result.pieceCount.get(oppPlayer) - 1);

			// Kiểm tra bước nhảy kép
			// giữ lượt chơi cho người chơi hiện tại và cập nhật vị trí doublejumpPos.
			if (result.jumpSuccessors(piece, newPos).size() > 0 && !kingConversion) {
				result.turn = piece.getPlayer();
				result.doublejumpPos = newPos;
			}
		}

		return result;
	}

	// kiểm tra xem một vị trí trên bàn cờ có phải là vị trí mà quân cờ của người
	// chơi đạt được cấp bậc "king" hay không
	private boolean isKingPosition(int pos, Player player) {

		int y = pos / SIDE_LENGTH;

		if (y == 0 && player == Player.HUMAN) {
			return true;
		} else
// SIDE_LENGTH - 1 hàng cuối cùng
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

// kiểm tra xem trò chơi đã kết thúc hay chưa dựa trên số lượng quân cờ còn lại của mỗi người chơ
	public boolean isGameOver() {
		return (pieceCount.get(Player.AI) == 0 || pieceCount.get(Player.HUMAN) == 0);
	}

	// trả về quân cờ từ mảng state tại chỉ số i để lấy quân cờ tại vị trí đó
	public Piece getPiece(int i) {

		return state[i];
	}

	private Piece getPiece(int y, int x) {
//SIDE_LENGTH * y + x chuyển đổi tọa độ hàng sang chỉ số của mảng một chiều
		return getPiece(SIDE_LENGTH * y + x);
	}

	// kiểm tra tính hợp lệ của một vị trí trong giới hạn của bàn cờ 8x8
	private boolean isValid(int y, int x) {
		return (0 <= y) && (y < SIDE_LENGTH) && (0 <= x) && (x < SIDE_LENGTH);
	}

}
