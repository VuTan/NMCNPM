package main.game;

public enum Player {
    AI,
    HUMAN;

    public Player getOpposite() {
        Player result = null;
        if (this == AI) {
            result = HUMAN;
        }
        else if (this == HUMAN) {
            result = AI;
        }
        if (result == null){
            throw new RuntimeException("Không có đối thủ");
        }
        return result;
    }
}
