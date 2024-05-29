package main.game;

import java.util.ArrayList;
import java.util.Stack;

public class Game {

	private Stack<BoardState> state;

	private int memory;

	private boolean humanWon;

	public Game() {
		memory = Settings.UNDO_MEMORY;
		state = new Stack<>();
		state.push(BoardState.InitialState());
	}

	// thực hiện nước đi của người chơi
	public void playerMove(BoardState newState) {

		if (!isGameOver() && state.peek().getTurn() == Player.HUMAN
				|| (!isGameOver() && state.peek().getTurn() == Player.AI)) {
// cập nhật bằng trạng thái mới
			updateState(newState);
		}
	}

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

	public MoveFeedback moveFeedbackClick(int pos) {

		ArrayList<BoardState> jumpSuccessors = this.state.peek().getSuccessors(true);

		if (jumpSuccessors.size() > 0) {
			return MoveFeedback.FORCED_JUMP;
		} else {
			return MoveFeedback.PIECE_BLOCKED;
		}

	}
// ds trạng thái bàn cờ sau các bước đi hợp lệ.
	public ArrayList<BoardState> getValidMoves(int pos) {
//lấy ra trạng thái hiện tại của bàn cờ sau khi thực hiện các bước đi hợp lệ 
		return state.peek().getSuccessors(pos);
	}

	// Cập nhật trạng thái của trò chơi
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

	// Trả về trạng thái hiện tại của trò chơi.
	public BoardState getState() {
		return state.peek();
	}

	// trả về lượt người chơi
	public Player getTurn() {
		return state.peek().getTurn();
	}

	// Kiểm tra xem trò chơi đã kết thúc chưa và trả về kết quả.
	public boolean isGameOver() {

		boolean isOver = state.peek().isGameOver();

		if (isOver) {

			humanWon = state.peek().pieceCount.get(Player.AI) == 0;
		}
		return isOver;
	}

	// Trả về kết quả của trò chơi.
	public String getGameOverMessage() {
		String result = "Trò chơi kết thúc.";
		if (humanWon == true) {
			result += "BẠN THẮNG!";
		} else {
			result += "BẠN ĐÃ THUA!";
		}
		return result;
	}

	public void undo() {

		if (state.size() > 2) {
			state.pop();

			while (state.peek().getTurn() == Player.AI) {
				state.pop();
			}
		}
	}

}
