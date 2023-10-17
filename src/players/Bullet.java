package players;

import java.awt.Color;
import java.awt.Graphics2D;
import entities.Entities;
/**
 * 
 * @author Abler, Andrew Kevin M.
 *
 */
public class Bullet extends Entities{
	
	public static final Color COLOR = new Color(41, 168, 255);

	private final float SPEED = 1280.0f;
	private final int SIZE = 8;
	private final int RADIUS = 4;
	private final double LIFE_TIME = 1.0;

	private double timeRemaining;
	
	public Bullet(Player player) {
		setPosition(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2);
		setSize(SIZE, SIZE);
		setXSpeed((float)(player.getXSpeed() + SPEED * Math.cos(Math.toRadians(player.getDirection()))));
		setYSpeed((float)(player.getYSpeed() + SPEED * Math.sin(Math.toRadians(player.getDirection()))));
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

}
