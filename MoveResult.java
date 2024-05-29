package com.game.app;

import com.game.app.enums.MoveType;

public class MoveResult {
	private MoveType type;

	public MoveType getType() {
		return type;
	}

	private Piece piece;

	public Piece getPiece() {
		return piece;
	}

	public MoveResult(MoveType type) {
		this(type, null);
	}

	public MoveResult(MoveType type, Piece piece) {
		this.type = type;
		this.piece = piece;
	}

	public boolean isValidMoveType() {
		return type.equals(MoveType.NORMAL) || type.equals(MoveType.KILL);
	}

}
