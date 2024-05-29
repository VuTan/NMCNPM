package main.game;

public class Piece {
	private Player player;
	private boolean king;

	public Piece(Player player, boolean king) {
		this.player = player;
		this.king = king;
	}

	public boolean isKing() {
		return king;
	}

	public Player getPlayer() {
		return player;
	}


	//trả về các nước đi hợp lệ theo trục Y
	public int[] getYMovements() {
		int[] result = new int[] {};

		//Vua có thể di chuyển lên hoặc xuống.
		if (king) {
			result = new int[] { -1, 1 };
		} else {
			switch (player) {
			
			case AI:
				//có thể di chuyển xuống.
				result = new int[] { 1 };
				break;
			
			case HUMAN:
				//chỉ có thể di chuyển lên
				result = new int[] { -1 };
				break;
			}
		}
		return result;
	}

	
//Quân cờ có thể di chuyển trái hoặc phải.
	public int[] getXMovements() {
		return new int[] { -1, 1 };
	}

}
