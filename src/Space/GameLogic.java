package asteroid;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class GameLogic extends KeyAdapter implements Runnable{
	
	private final int FPS = 144;
	private final int PERIOD_NS = 1000000000 / FPS;
	private final double PERIOD_S = 1.0 / FPS;
	private final int MAX_DRAWS_WITHOUT_SLEEP = 16;
	private final int MAX_FRAME_SKIPS = 12;
	
	private volatile boolean running;
	
	public int width;
	public int height;
	private JFrame parent;
	private BufferStrategy bufferStrat;
	private Graphics2D g;
	private Thread gameThread;
	private Font medFont;
	private Font largeFont;
	private FontMetrics medMetrics;
	private FontMetrics largeMetrics;
	private GameSounds sounds;
	private BufferedImage background;
	
	private final double BULLET_DELAY = 0.5;
	private double bulletTime;
	private final double BULLET_DELAY2 = 0.5;
	private double bulletTime2;
	private volatile boolean leftPressed, rightPressed, upPressed, downPressed, spacePressed;
	private volatile boolean aPressed, dPressed, wPressed, sPressed, xPressed;
	private volatile boolean enterPressed;
	
	private final int START_LIVES = 3;
	private boolean gamePaused, gameOver, anyPressed;
	private int lives1, lives2;
	private Player player;
	private Player2 player2;
	private LinkedList<Bullet> bullets;
	private LinkedList<Bullet2> bullets2;
	
	public GameLogic(JFrame par, BufferStrategy buff, int w, int h) {
		
		try {
			background = ImageIO.read(new File("C:/Users/kevin/Documents/Java/Asteroid/res/GameBackgroundDesign.png")); //Background image
		} catch (IOException e) {
			e.printStackTrace();
		}
		parent = par;
		bufferStrat = buff;
		width = w;
		height = h;
		medFont = new Font("Arial", Font.BOLD, 32);
		medMetrics = parent.getFontMetrics(medFont);
		largeFont = new Font("Arial", Font.BOLD, 72);
		largeMetrics = parent.getFontMetrics(largeFont);
		running = false;
	}
	
	//Starts the game
	public void startGame() {
		System.out.println("working");
		if (!running || gameThread == null) {
			gameThread = new Thread(this);
		}
		Entities.setWrapDimension(width, height);
		leftPressed = rightPressed = upPressed = downPressed = false;
		gamePaused = false;
		gameOver = false;
		anyPressed = false;
		player = new Player();
		player2 = new Player2();
		resetPlayer();
		bullets = new LinkedList<>();
		bullets2 = new LinkedList<>();
		sounds = new GameSounds();
		bulletTime = BULLET_DELAY;
		bulletTime2 = BULLET_DELAY2;
		lives1 = START_LIVES;
		lives2 = START_LIVES;
		sounds.playSound(GameSounds.SOUND_MUSIC, true);
		gameThread.start();
	}
	
	/**
	 * Resets player postion.
	 */
	private void resetPlayer() {
		
		if (player != null) {
			//sets player on the left side of screen
			player.setPosition(2900 / 2, height / 2);
			player.setDirection(180.0f);
			player.setSpeed(0.0f, 0.0f);
			//2900 / 2, height / 2
		}
		
		if (player2 != null) {
			//sets player2 on the right side of screen
			player2.setPosition(100 / 2, height / 2);
			player2.setDirection(0.0f);
			player2.setSpeed(0.0f, 0.0f);
		}
		
	}
	
	/**
	 * Resets the game
	 */
	private void resetGame() {
		gamePaused = false;
		gameOver = false;
		resetPlayer();
		bullets.clear();
		bulletTime = BULLET_DELAY;
		lives1 = START_LIVES;
		lives2 = START_LIVES;
	}
	
	/**
	 * Sets running to false to stop the game.
	 */
	public void stopGame() {
		
		sounds.stop();
		running = false;
	}

	/**
	 * Runs the game.
	 */
	@Override
	public void run() {
//		if(enterPressed)
			
		running = true;
		long startTime = System.nanoTime();
		long afterTime, sleepTime;
		long oversleep = 0L;
		long timeMissed = 0L;
		int drawsWithoutSleep = 0;
		int framesSkipped;
		while(running) {
			update();
			draw();
			afterTime = System.nanoTime();
			sleepTime = PERIOD_NS - (afterTime - startTime) - oversleep;
			if (sleepTime > 0L) {
				try {
					Thread.sleep(sleepTime / 1000000);
				} catch (InterruptedException ie) {}
				oversleep = (System.nanoTime() - afterTime) - sleepTime;
			} else {
				timeMissed -= sleepTime;
				drawsWithoutSleep++;
				if (drawsWithoutSleep >= MAX_DRAWS_WITHOUT_SLEEP) {
					drawsWithoutSleep = 0;
					Thread.yield();
				}
			}
			framesSkipped = 0;
			while (timeMissed >= PERIOD_NS && framesSkipped < MAX_FRAME_SKIPS) {
				update();
				timeMissed -= PERIOD_NS;
				framesSkipped++;
			}
			startTime = System.nanoTime();
		}
		System.exit(0);
		
	}
	/**
	 * Updates game and does the player movement, 
	 */
	private void update() {
		if(anyPressed && !gamePaused) {
		//CONDITION: if game is not over player 1 and player 2 can move and shoot
		if (!gameOver) {
			player.move(PERIOD_S);
			for (int i = 0; i < bullets.size(); i++) {
				Bullet bullet = bullets.get(i);
				bullet.decreaseTime(PERIOD_S);
				if (bullet.hasTime()) {
					bullet.move(PERIOD_S);
				} else {
					bullets.remove(i);
					i--;
				}
			}
		
		//Player 1 movements and gun
		if (leftPressed) {
			player.turn(PERIOD_S, Player.LEFT);
		}
		if (rightPressed) {
			player.turn(PERIOD_S, Player.RIGHT);
		}
		if (upPressed) {
			player.accelerate(PERIOD_S, Player.FORWARD);
		}
		if (downPressed) {
			player.accelerate(PERIOD_S, Player.BACKWARD);
		}
		if (bulletTime > 0.0) {
			bulletTime -= PERIOD_S;
		}
		if (spacePressed && bulletTime <= 0.0) {
			bullets.add(new Bullet(player));
			sounds.playSound(GameSounds.SOUND_LASER, false);
			bulletTime = BULLET_DELAY;
		} else if (!spacePressed) {
			bulletTime = 0.0;
		}
		
		//Player 2 movement and gun
		player2.move(PERIOD_S);
		for (int i = 0; i < bullets2.size(); i++) {
			Bullet2 bullet2 = bullets2.get(i);
			bullet2.decreaseTime(PERIOD_S);
			if (bullet2.hasTime()) {
				bullet2.move(PERIOD_S);
			} else {
				bullets2.remove(i);
				i--;
			}
		}
		
		player2.move(PERIOD_S);
		if (aPressed) {
			player2.turn(PERIOD_S, Player2.A);
		}
		if (dPressed) {
			player2.turn(PERIOD_S, Player2.D);
		}
		if (wPressed) {
			player2.accelerate(PERIOD_S, Player2.W);
		}
		if (sPressed) {
			player2.accelerate(PERIOD_S, Player2.S);
		} 
		if (bulletTime2 > 0.0) {
			bulletTime2 -= PERIOD_S;
		}
		if (xPressed && bulletTime2 <= 0.0) {
			bullets2.add(new Bullet2(player2));
			sounds.playSound(GameSounds.SOUND_LASER, false);
			bulletTime2 = BULLET_DELAY2;
		} else if (!xPressed) {
			bulletTime2 = 0.0;
		}
		
		//Collision for bullet to eliminate player 1
		boolean contains = false;
		boolean contains2 = false;
		
		for(int i = 0; i < bullets2.size(); i++) {
			Bullet2 bullet2 = bullets2.get(i);
			
			if(player.contains(bullet2.getX(), bullet2.getY())) {
				bullets2.remove(i);
				contains = true;
				
				if(contains) {
					lives1--;
					resetPlayer();
					break;
				}
				i--;
				
			}
		}
		
		//Collision for bullet to eliminate player 2
		for(int j = 0; j < bullets.size(); j++) {
			Bullet bullet = bullets.get(j);
			
			
			if(player2.contains2(bullet.getX(), bullet.getY())) {
				bullets.remove(j);
				contains2 = true;
				
				if(contains2) {
					lives2--;
					resetPlayer();
					break;
				}
				j--;
				
			}
		}
		
		// check for game over and if player 1 or player 2 is dead
		if (lives1 <= 0) {
			gameOver = true;
			bullets.clear();
			bullets2.clear();
		} else if(lives2 <= 0) {
			gameOver = true;
			bullets.clear();
			bullets2.clear();
		}
		}
		else{
			//Resets game after enter is pressed
			if (enterPressed) {
				resetGame();
			}
		}
		}
	}
	
	
	private void buffer(Graphics2D g) {
		  
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//draws background
		g.drawImage(background, 0, 0, null);
//		g.setColor(new Color(23, 12, 26));
//		g.fillRect(0, 0, width, height);
		if(!anyPressed) {

		g.setFont(largeFont);
		g.setColor(new Color(178, 163, 255));
		g.drawString("Press any key to start!", (int)(800 / 2), 1600 / 2);
		g.setFont(medFont);
		g.drawString("How to play", 1400 / 2, 1150 / 2);
		g.drawString("Player 1: long press Arrow keys for movement and space to shoot", 580 / 2, 1250 / 2);
		g.drawString("Player 2: long press WASD for movement and X to shoot", 720 / 2, 1350 / 2);
		g.drawString("Press P to pause the game and C to continue playing", 780 / 2, 1450 / 2);
		}
		
		if (gamePaused && !gameOver && running) {
			g.setFont(largeFont);
			g.setColor(new Color(178, 163, 255));
			g.drawString("Paused", (int)(1250 / 2), height / 2);
			g.setFont(medFont);
			g.drawString("Press C to continue game.", 1120 / 2, 1050 / 2);
		}

		//draws entities
		for (Bullet bullet : bullets) {
			bullet.draw(g);
		}
		for (Bullet2 bullet2 : bullets2) {
			bullet2.draw(g);
		}
		if (!gameOver) {
			player.draw(g);
		}
		if (!gameOver) {
			player2.draw(g);
		}
		if (!gameOver) {
			g.setFont(medFont);
			g.setColor(new Color(178, 163, 255));
			g.drawString("Player 2 Lives: " + lives2, 4, medMetrics.getHeight());
			g.drawString("Player 1 Lives: " + lives1, 1284, medMetrics.getHeight());
		} else {
			Rectangle2D textBound1 = largeMetrics.getStringBounds("Game Over", g);
			
			g.setFont(largeFont);
			g.setColor(new Color(178, 163, 255));
			g.drawString("Game Over", (int)(width / 2 - textBound1.getWidth() / 2), height / 2);
			if(lives1 > lives2) {
				g.setFont(medFont);
				g.drawString("Player 1 wins!", 1325 / 2, 1000 / 2);
			} else {
				g.setFont(medFont);
				g.drawString("Player 2 wins!", 1325 / 2, 1000 / 2);
			}
			g.setFont(medFont);
			g.drawString("Press enter to reset", 1235 / 2, 1300 / 2);
			g.drawString("Esc to exit game", 1290 / 2, 1400 / 2);
			g.setFont(medFont);
			
		}
	}
	
	
	
	private void draw() {
		
		//Draws players and bullets
		try {
			g = (Graphics2D)bufferStrat.getDrawGraphics();
			buffer(g);
			g.dispose();
			if (!bufferStrat.contentsLost()) {
				bufferStrat.show();
			} else {
				System.out.println("Warning: graphics buffer contents lost.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
		}
		
	}
	
	@Override
	public void keyPressed(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
			rightPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_UP) {
			upPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
			leftPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
			downPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
			spacePressed = true;
		} else if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
			enterPressed = true;
		} else if(evt.getKeyCode() == KeyEvent.VK_D){
			dPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_W) {
			wPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_A) {
			aPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_S) {
			sPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_X) {
			xPressed = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_P) {
			gamePaused = true;
		} else if (evt.getKeyCode() == KeyEvent.VK_C) {
			gamePaused = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
			stopGame();
		} 
			anyPressed = true;
		
		
	}

	@Override
	public void keyReleased(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
			rightPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_UP) {
			upPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
			leftPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
			spacePressed = false;
		} else if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
			enterPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
			downPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_D) {
			dPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_W) {
			wPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_A) {
			aPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_S) {
			sPressed = false;
		} else if (evt.getKeyCode() == KeyEvent.VK_X) {
			xPressed = false;
		} 
	}

}
