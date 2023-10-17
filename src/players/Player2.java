package players;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import entities.Entities;
/**
 * 
 * @author Abler, Andrew Kevin M.
 *
 */
public class Player2 extends Entities{
	
	public static final int A = 0;
	public static final int D = 1;
	public static final int W = 2;
	public static final int S = 3;
	public static final Color color = new Color(0, 25, 149);
	
	private final int WIDTH = 32;
	private final int HEIGHT = 32;
	private final float FRICTION = 256.0f;
	private final float ACCELERATION = 640.0f;
	private final float ANGULAR_SPEED = 360.0f;
	
	private float direction;
	private int pointsX[], pointsY[];
	
	//movements of player
	public Player2() {
		
		setSize(WIDTH, HEIGHT);
		direction = 0.0f;
		pointsX = new int [3];
		pointsY = new int [3];
	}
	
	@Override
	public void move(double period) {
		super.move(period);
		float moveDirection;
		if (xSpeed != 0.0f) {
			moveDirection = (float)Math.toDegrees(Math.atan(ySpeed / xSpeed));
			if (xSpeed < 0.0f) {
				moveDirection += 180.0f;
			} else if (ySpeed < 0.0f) {
				moveDirection += 360.0f;
			}
		} else {
			if (ySpeed < 0.0f) {
				moveDirection = 270.0f;
			} else {
				moveDirection = 90.0f;
			}
		}
		if (xSpeed != 0.0f) {
			float xFric = (float)(FRICTION * period * Math.cos(Math.toRadians(moveDirection)));
			if (Math.abs(xSpeed) - Math.abs(xFric) < 0.0f) {
				xSpeed = 0.0f;
			} else {
				xSpeed -= xFric;
			}
		}
		if (ySpeed != 0.0f) {
			float yFric = (float)(FRICTION * period * Math.sin(Math.toRadians(moveDirection)));
			if (Math.abs(ySpeed) - Math.abs(yFric) < 0.0f) {
				ySpeed = 0.0f;
			} else {
				ySpeed -= yFric;
			}
		}
	}
	
	//rotates player
	public void turn(double period, int dir) {
		switch (dir) {
			case A:
				direction -= ANGULAR_SPEED * period;
				break;
			case D:
				direction += ANGULAR_SPEED * period;
				break;
		}
		if (direction < 0.0f) {
			direction = 360.0f + direction;
		}
		direction %= 360.0f;
	}

	//speed of player and acceleration
	public void accelerate(double period, int dir) {
		switch (dir) {
			case W:
				xSpeed += (ACCELERATION * period * Math.cos(Math.toRadians(direction)));
				ySpeed += (ACCELERATION * period * Math.sin(Math.toRadians(direction)));
				break;
			case S:
				xSpeed -= (ACCELERATION * period * Math.cos(Math.toRadians(direction)));
				ySpeed -= (ACCELERATION * period * Math.sin(Math.toRadians(direction)));
				break;
		}
	}

	public void setDirection(float dir) {
		direction = dir % 360.0f;
	}

	public float getDirection() {
		return direction;
	}

	public int[] getXPoints() {
		return pointsX;
	}

	public int[] getYPoints() {
		return pointsY;
	}
	
	public Polygon getBounds() {
		
		return new Polygon(pointsX, pointsY, 3);
	}

	//draws player
	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		float radWidth = WIDTH / 2;
		float radHeight = HEIGHT / 2;
		float xMid = x + radWidth;
		float yMid = y + radHeight;
		pointsX[0] = (int)(xMid + (radWidth * (float)Math.cos(Math.toRadians(direction))));
		pointsY[0] = (int)(yMid + (radHeight * (float)Math.sin(Math.toRadians(direction))));
		pointsX[1] = (int)(xMid - (radWidth * (float)Math.cos(Math.toRadians(direction + 30))));
		pointsY[1] = (int)(yMid - (radHeight * (float)Math.sin(Math.toRadians(direction + 30))));
		pointsX[2] = (int)(xMid - (radWidth * (float)Math.cos(Math.toRadians(direction - 30))));
		pointsY[2] = (int)(yMid - (radHeight * (float)Math.sin(Math.toRadians(direction - 30))));
		g.fillPolygon(pointsX, pointsY, 3);
		
	}
	
	@Override
	public boolean contains2(float x1, float y1) {
		float xMid = x + WIDTH + HEIGHT / 2;
		float yMid = y + WIDTH + HEIGHT  / 2;
		float xDiff = x1 - xMid;
		float yDiff = y1 - yMid;
		float distance = (float)Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
		return (distance <= WIDTH + HEIGHT  / 2);
	}
	

}
