package main.gui;

import javax.swing.*;
import java.awt.*;


//--Huy
public class Square extends JPanel {

	  
    private Color color;

    public Square(int i, int j){
    	
    	
        this.setPreferredSize(new Dimension(Settings.squareSize,Settings.squareSize));
        
       
        if( ((i % 2) + (j % 2)) % 2 == 0){
        	//Mau sang
            color = hexToColor("#9E6B55");
        }
        else{
        	//Mau toi
            color = hexToColor("#F5C9B2");
        }
    }

    public void setHighlighted(){
        color = hexToColor("#E6B655");
    }

   
    private static Color hexToColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int rgb = Integer.parseInt(hex, 16);
        return new Color(rgb);
    }
    
    
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

}