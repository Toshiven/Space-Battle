package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import entities.Entities;
import main.MainMenu;
import players.Bullet;
import players.Bullet2;
import players.Player;
import players.Player2;
/**
 * 
 * @author Abler, Andrew Kevin M.
 *
 */
public class GameLogic extends KeyAdapter implements Runnable, MouseListener{
	
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
	private BufferedImage background;
	private Sound sound = new Sound();
	
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
	private MainMenu menu;
	
	public static enum STATE{
		MENU,
		GAME
	};
	public static STATE state = STATE.MENU;
	
	public GameLogic(JFrame par, BufferStrategy buff, int w, int h) {
		
		try {
			background = ImageIO.read(new File("C:/Users/kevin/Documents/Java/SpaceBattle/res/GameBackgroundDesign.png")); //Background image
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
		
		menu = new MainMenu();
		running = false;
		
		playMusic(0);
		
	}
	
	//Starts the game
	public void startGame() {
		
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
		bulletTime = BULLET_DELAY;
		bulletTime2 = BULLET_DELAY2;
		lives1 = START_LIVES;
		lives2 = START_LIVES;
		
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
		
		stopMusic();
		running = false;
	}
	
	/**
	 * Plays the music in loop
	 * @param i
	 */
	public void playMusic(int i) {
		
		sound.setFile(i);
		sound.play();
		sound.loop();
	}
	
	/**
	 * Plays the sound effects
	 * @param i
	 */
	public void playSE(int i) {
	
		sound.setFile(i);
		sound.play();
	}
	
	/**
	 * Stops the music
	 */
	public void stopMusic() {
		
		sound.stop();
	}


	/**
	 * Runs the game.
	 */
	@Override
	public void run() {
			
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
		//CONDITION: It will show How to play before game starts and if anyPressed is true it will start the game.
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
			playSE(1);
			bullets.add(new Bullet(player));
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
			playSE(1);
			bullets2.add(new Bullet2(player2));
			bulletTime2 = BULLET_DELAY2;
		} else if (!xPressed) {
			bulletTime2 = 0.0;
		}
		
		//Collision for bullet to eliminate player 1
		boolean contains = false;
		boolean contains2 = false;
		
		for(int i = 0; i < bullets2.size(); i++) {
			Bullet2 bullet2 = bullets2.get(i);
			
			//if player has same position of bullet2 it collides
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
			
			//if player2 has same position of bullet it collides
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

		//CONDITION: if state is GAME then it will play the game
		if(state == STATE.GAME) {
			
		/**CONDITION:
		 * before game starts it will show how to play and if anyPressed is true it will play game
		 */
		if(!anyPressed) {

		//draws how to play
		g.setFont(largeFont);
		g.setColor(new Color(178, 163, 255));
		g.drawString("Press any key to start!", (int)(800 / 2), 1600 / 2);
		g.setFont(medFont);
		g.drawString("How to play", 1400 / 2, 1150 / 2);
		g.drawString("Player 1: long press Arrow keys for movement and space to shoot", 580 / 2, 1250 / 2);
		g.drawString("Player 2: long press WASD for movement and X to shoot", 720 / 2, 1350 / 2);
		g.drawString("Press P to pause the game and C to continue playing", 780 / 2, 1450 / 2);
		}
		
		/**CONDITION:
		 * if gamePaused is true it will pause the game and draw pause menu 
		 */
		if (gamePaused && !gameOver && running) {
			g.setFont(largeFont);
			g.setColor(new Color(178, 163, 255));
			g.drawString("Paused", (int)(1250 / 2), height / 2);
			g.setFont(medFont);
			g.drawString("Press C to continue game", 1120 / 2, 1050 / 2);
			g.drawString("Press Esc to go back to main menu", 980 / 2, 950 / 2);
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
			//draws Game Over menu
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
			g.drawString("Press Enter to reset", 1235 / 2, 1300 / 2);
			g.drawString("Press Esc to go back to main menu", 980 / 2, 1400 / 2);
			g.setFont(medFont);
		}
			
		} else if(state == STATE.MENU) { //If state is MENU it will draw main menu
			menu.render(g);
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
		if(state == STATE.GAME) {
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
			state = STATE.MENU;
		} 
			anyPressed = true;
		}
		
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

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void mousePressed(MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		
		//Play Button
		if(mx >= 650 && mx <= 750) {
			if(my >= 350 && my <= 400) {
				//Pressed Play Button
				GameLogic.state = GameLogic.STATE.GAME;
			}
		}
		
		//Quit Button
		if(mx >= 650 && mx <= 750) {
			if(my >= 500 && my <= 550) {
				//Pressed Quit Button
				System.exit(1);
			}
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
