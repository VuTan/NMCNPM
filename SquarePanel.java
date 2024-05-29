package main.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SquarePanel extends JPanel {

	  
    private Color color;

    public SquarePanel(int i, int j){
    	
    	
        this.setPreferredSize(new Dimension(Settings.squareSize,Settings.squareSize));
        
        // xác định màu sắc của ô vuông dựa trên vị trí của nó
       
        if( ((i % 2) + (j % 2)) % 2 == 0){
            color = hexToColor("#9E6B55");
        }
        else{
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
