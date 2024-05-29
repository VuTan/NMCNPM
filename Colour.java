package main.gui;

//đại diện cho màu sắc của các quân cờ
public enum Colour {
    WHITE,
    BLACK;

    public Colour getOpposite(){
        Colour result = null;
      
        if (this == WHITE){
            result = BLACK;
        }
        else if (this == BLACK){
            result = WHITE;
        }
        if(result == null){
            throw new RuntimeException("Quân rỗng không có quân đối lập");
        }
        return result;
    }
}
