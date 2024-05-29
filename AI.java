package main.game;

public class AI {

	private int depth;

	private Player player;

	public AI() {
		depth = Settings.AI_DEPTH;
		player = Player.AI;

	}

	public AI(int depth, Player player) {
		this.depth = depth;
		this.player = player;

	}

	public BoardState minimax(BoardState state, int depth) {
		if (depth == 0 || state.isGameOver()) {
			return state;
		}
		BoardState bestMove = null;

		if (state.getTurn() == player) {

			int maxEval = Integer.MIN_VALUE;

			for (BoardState child : state.getSuccessors()) {

				int eval = minimax(child, depth - 1).computeHeuristic(child);

				if (eval > maxEval) {
					maxEval = eval;
					bestMove = child;
				}
			}

			if (bestMove == null) {
				return state;
			}
			return bestMove;
		}

		if (state.getTurn() == player.getOpposite()) {

			int minEval = Integer.MAX_VALUE;
			for (BoardState child : state.getSuccessors()) {
				int eval = minimax(child, depth - 1).computeHeuristic(child);
				if (eval < minEval) {
					minEval = eval;
					bestMove = child;
				}
			}
			if (bestMove == null) {
				return state;
			}
			return bestMove;
		}
		return bestMove;
	}

	private BoardState alphabeta(BoardState state, int depth, int alpha, int beta) {
		if (depth == 0 || state.isGameOver()) {
			return state;
		}

		BoardState bestMove = null;

		if (state.getTurn() == player) {
			int maxEval = Integer.MIN_VALUE;

			for (BoardState child : state.getSuccessors()) {
				int eval = alphabeta(child, depth - 1, alpha, beta).computeHeuristic(child);

				if (eval > maxEval) {
					maxEval = eval;
					bestMove = child;
				}

				alpha = Math.max(alpha, eval);

				if (beta <= alpha) {
					break;
				}
			}

			return bestMove;
		} else {
			int minEval = Integer.MAX_VALUE;

			for (BoardState child : state.getSuccessors()) {
				int eval = alphabeta(child, depth - 1, alpha, beta).computeHeuristic(child);

				if (eval < minEval) {
					minEval = eval;
					bestMove = child;
				}

				beta = Math.min(beta, eval);

				if (beta <= alpha) {
					break;
				}
			}

			return bestMove;
		}
	}

	public BoardState move(BoardState state, Player currentPlayer, String algorithm) {
		  long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	        long startTime = System.nanoTime();

	        BoardState result = null;
	        if (currentPlayer == player) {
	            if ("minimax".equals(algorithm)) {
	                result = minimax(state, depth);
	            } else if ("alphabeta".equals(algorithm)) {
	                result = alphabeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
	            }
	        } else {
	            result = state; // Trả về trạng thái ban đầu nếu không phải lượt của AI
	        }

	        long endTime = System.nanoTime();
	        long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	        long actualMemUsed = Math.max(0, afterUsedMem - beforeUsedMem); // Đảm bảo không có giá trị âm
	        long duration = (endTime - startTime) / 1000000; // Đổi sang milligiây

	        System.out.println("Thuật toán " + algorithm + " mất " + duration + "ms");
	        System.out.println("Thuật toán " + algorithm + " sử dụng " + actualMemUsed + " bytes bộ nhớ");

	        return result;
	    }
}
