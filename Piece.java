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


	
	public int[] getYMovements() {
		int[] result = new int[] {};

		
		if (king) {
			result = new int[] { -1, 1 };
		} else {
			switch (player) {
			
			case AI:
				result = new int[] { 1 };
				break;
			
			case HUMAN:
				result = new int[] { -1 };
				break;
			}
		}
		return result;
	}

	

	public int[] getXMovements() {
		return new int[] { -1, 1 };
	}

}
