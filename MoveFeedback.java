package main.game;

public enum MoveFeedback {
    NOT_DIAGONAL ("Bạn chỉ có thể di chuyển theo đường chéo."),
    FORCED_JUMP ("Bạn buộc phải thực hiện bước đi."),
    NO_FREE_SPACE ("Bạn không thể chuyển sang quân khác."),
    ONLY_SINGLE_DIAGONALS ("Bạn chỉ có thể thực hiện một bước đi."),
    NO_BACKWARD_MOVES_FOR_SINGLES ("Chỉ có vua mới có thể lùi lại!"),
    NOT_ON_BOARD(""),
    PIECE_BLOCKED ("Quân này không có đường chéo.."),
    UNKNOWN_INVALID("Nước đi không hợp lệ."),
    SUCCESS ("Thành công");

    private final String name;

    MoveFeedback(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
