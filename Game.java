package main.game;

import java.util.ArrayList;
import java.util.Stack;

public class Game {

	private Stack<BoardState> state;

	private int memory;

	private AI ai;

	private boolean humanWon;

	public Game() {
		memory = Settings.UNDO_MEMORY;
		state = new Stack<>();
		state.push(BoardState.InitialState());
		ai = new AI();
	}

	//Cập nhật trạng thái của bàn cờ nếu trò chơi chưa kết thúc và đến lượt người chơi.
	public void playerMove(BoardState newState) {

		if (!isGameOver() && state.peek().getTurn() == Player.HUMAN) {

			updateState(newState);
		}
	}
	
	//Xử lý nước đi của người chơi từ vị trí fromPos đến vị trí toPos với dịch chuyển dx và dy.
	//Kiểm tra và trả về các phản hồi khác nhau dựa trên tính hợp lệ của nước đi (ví dụ: không đi ra khỏi bàn cờ, không di chuyển chéo không hợp lệ, ô đến không trống, v.v.).
	public MoveFeedback playerMove(int fromPos, int dx, int dy) {

		int toPos = fromPos + dx + BoardState.SIDE_LENGTH * dy;

		if (toPos > getState().state.length) {
			return MoveFeedback.NOT_ON_BOARD;
		}

		ArrayList<BoardState> jumpSuccessors = this.state.peek().getSuccessors(true);
		boolean jumps = jumpSuccessors.size() > 0;

		if (jumps) {
			for (BoardState succ : jumpSuccessors) {
				if (succ.getFromPos() == fromPos && succ.getToPos() == toPos) {
					updateState(succ);
					return MoveFeedback.SUCCESS;
				}
			}
			return MoveFeedback.FORCED_JUMP;
		}

		if (Math.abs(dx) != Math.abs(dy)) {
			return MoveFeedback.NOT_DIAGONAL;
		}

		if (this.getState().state[toPos] != null) {
			return MoveFeedback.NO_FREE_SPACE;
		}

		ArrayList<BoardState> nonJumpSuccessors = this.state.peek().getSuccessors(fromPos, false);
		for (BoardState succ : nonJumpSuccessors) {
			if (succ.getFromPos() == fromPos && succ.getToPos() == toPos) {
				updateState(succ);
				return MoveFeedback.SUCCESS;
			}
		}

		if (dy > 1) {
			return MoveFeedback.NO_BACKWARD_MOVES_FOR_SINGLES;
		}

		if (Math.abs(dx) == 2) {
			return MoveFeedback.ONLY_SINGLE_DIAGONALS;
		}

		return MoveFeedback.UNKNOWN_INVALID;
	}
	
	//Trả về phản hồi dựa trên trạng thái hiện tại và các nước đi nhảy có thể.
	public MoveFeedback moveFeedbackClick(int pos) {

		ArrayList<BoardState> jumpSuccessors = this.state.peek().getSuccessors(true);

		if (jumpSuccessors.size() > 0) {
			return MoveFeedback.FORCED_JUMP;
		} else {
			return MoveFeedback.PIECE_BLOCKED;
		}

	}
	
	//Trả về danh sách các trạng thái kế tiếp hợp lệ cho một vị trí cụ thể.
	public ArrayList<BoardState> getValidMoves(int pos) {

		return state.peek().getSuccessors(pos);
	}

	public void aiMove() {

		if (!isGameOver() && state.peek().getTurn() == Player.AI) {
			String selectedAlgorithm = Settings.SELECTED_ALGORITHM;

			if ("minimax".equals(selectedAlgorithm)) {
				BoardState newState = ai.move(this.state.peek(), Player.AI, selectedAlgorithm);
				updateState(newState);
			}else if ("alphabeta".equals(Settings.SELECTED_ALGORITHM1)) {
					BoardState newState = ai.move(this.state.peek(), Player.AI, selectedAlgorithm);
					updateState(newState);
			}
								
			} else {
				System.out.println("Thuật toán không hợp lệ");
			}
		}
	
	
	//Cập nhật trạng thái của bàn cờ bằng cách đẩy trạng thái mới vào ngăn xếp state.
	//Nếu ngăn xếp vượt quá dung lượng nhớ (memory), loại bỏ trạng thái cũ nhất.
	// Bước 5 trong use case
	private void updateState(BoardState newState) {

		state.push(newState);

		if (state.size() > memory) {

			state.remove(0);
		}
		state.push(newState);
		if (state.size() > memory) {
			state.remove(0);
		}
	}
	
	//Trả về trạng thái hiện tại của bàn cờ (đỉnh của ngăn xếp state).
	public BoardState getState() {
		return state.peek();
	}
	
	//Trả về người chơi hiện tại (lượt đi hiện tại).
	public Player getTurn() {
		return state.peek().getTurn();
	}
	
	//Kiểm tra xem trò chơi đã kết thúc chưa bằng cách kiểm tra trạng thái đỉnh của ngăn xếp state.
	//Cập nhật biến cờ humanWon dựa trên kết quả trò chơi.
	//Bước 6: trong use case
	public boolean isGameOver() {

		boolean isOver = state.peek().isGameOver();

		if (isOver) {

			humanWon = state.peek().pieceCount.get(Player.AI) == 0;
		}
		return isOver;
	}

	//Trả về thông báo kết thúc trò chơi, bao gồm kết quả thắng hoặc thua của người chơi.
	// Bước 7: trong use case
	public String getGameOverMessage() {
		String result = "Trò chơi kết thúc.";
		if (humanWon == true) {
			result += "BẠN THẮNG!";
		} else {
			result += "BẠN ĐÃ THUA!";
		}
		return result;
	}
	
	//Quay lại một hoặc nhiều trạng thái trước đó để thực hiện undo nước đi.
	//Loại bỏ trạng thái khỏi ngăn xếp cho đến khi đến lượt người chơi.
	public void undo() {

		if (state.size() > 2) {
			state.pop();

			while (state.peek().getTurn() == Player.AI) {
				state.pop();
			}
		}
	}

}
