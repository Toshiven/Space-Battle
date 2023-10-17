package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
/**
 * 
 * @author Abler, Andrew Kevin M.
 *
 */
public class MainMenu {

	public Rectangle playButton = new Rectangle(650, 350, 200, 100);
	public Rectangle quitButton = new Rectangle(650, 500, 200, 100);
	
	public void render(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		//Title of the game.
		Font fnt0 = new Font("arial", Font.BOLD, 100);
		g.setFont(fnt0);
		g.setColor(Color.white);
		g.drawString("Space Battle", 450, 300);
		
		//Start button
		Font fnt1 = new Font("arial", Font.BOLD, 50);
		g.setFont(fnt1);
		g.drawString("Play", playButton.x + 50, playButton.y + 65);
		g2d.draw(playButton);
		
		//Quit button
		Font fnt2 = new Font("arial", Font.BOLD, 50);
		g.setFont(fnt2);
		g.drawString("Quit", quitButton.x + 50, quitButton.y + 65);
		g2d.draw(quitButton);
	}
}
