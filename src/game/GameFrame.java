package game;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
/**
 * 
 * @author Abler, Andrew Kevin M.
 *
 */
public class GameFrame extends JFrame{
	
	private static final int NUM_BUFFERS = 2;
	
	private GameLogic game;
	private GraphicsDevice gd;
	private BufferStrategy bufferStrat;
	private int width, height;
	
	//Starts game
	public GameFrame() {
		
		frame();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game =  new GameLogic(this, bufferStrat, width, height);
		addKeyListener(game);
		addMouseListener(game);
		setVisible(true);
		game.startGame();
	}
	
	//Sets game frame to fullscreen
	private void frame() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		if(gd.isFullScreenSupported()) {
			setUndecorated(true);
			setIgnoreRepaint(true);
			setResizable(false);
			gd.setFullScreenWindow(this);
			width = getBounds().width;
			height = getBounds().height;
			setupBuffer();
		} else {
			System.out.println("Error: Display screen not supported");
			System.exit(1);
		}
	}
	
	//Sets up buffer
	private void setupBuffer() {
		
		createBufferStrategy(NUM_BUFFERS);
		bufferStrat = getBufferStrategy();
	}
}
