package main.gui;

import main.game.Player;

public class Settings {

	// cấu hình và cài đặt chung cho trò chơi
	
	public static Colour AIcolour = Colour.BLACK; 
    
	  
    public static boolean helpMode = true;
    public static boolean hintMode = false;
    public static boolean dragDrop = false;
    
    public static int squareSize = 80;
 
    public static int checkerWidth = 5*squareSize/6;
    public static int checkerHeight = 5*squareSize/6;
 
    public static int ghostButtonWidth = 30*squareSize/29;
    public static int ghostButtonHeight = 5*squareSize/6;

 //trả về màu sắc của người chơi dựa trên đối tượng Player.
    public static Colour getColour(Player player){
        Colour result = null;
        if (player == Player.AI){
            result = Settings.AIcolour;
        }
        else if (player == Player.HUMAN){
            result = Settings.AIcolour.getOpposite();
        }
        if(result == null){
            throw new RuntimeException("Người chơi không có quân cờ.");
        }
        return result;
    }
}
