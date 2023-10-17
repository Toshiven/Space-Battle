package asteroid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet2 extends Entities{
	
	public static final Color COLOR = new Color(0, 168, 0);

	private final float SPEED = 1280.0f;
	private final int SIZE = 8;
	private final int RADIUS = 4;
	private final double LIFE_TIME = 1.0;

	private double timeRemaining;
	
	public Bullet2(Player2 player2) {
		setPosition(player2.getX() + player2.getWidth() / 2, player2.getY() + player2.getHeight() / 2);
		setSize(SIZE, SIZE);
		setXSpeed((float)(player2.getXSpeed() + SPEED * Math.cos(Math.toRadians(player2.getDirection()))));
		setYSpeed((float)(player2.getYSpeed() + SPEED * Math.sin(Math.toRadians(player2.getDirection()))));
		timeRemaining = LIFE_TIME;
	}

	public void decreaseTime(double period) {
		timeRemaining -= period;
	}

	public boolean hasTime() {
		return (timeRemaining > 0.0);
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(COLOR);
		g.fillOval((int)x - RADIUS, (int)y - RADIUS, SIZE, SIZE);
		
	}
	
	public Rectangle getBounds() {
		
		return new Rectangle((int)x - RADIUS, (int)y - RADIUS, width, height);
	}

}
