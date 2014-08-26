import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.sun.j3d.utils.timer.J3DTimer;


public class GamePanel extends JPanel implements Runnable{

	private static final int PWIDTH = 500; //size of panel
	private static final int PHEIGHT = 400;
	private static final int NO_DELAY_PER_YIELD = 16;
	/*
	 * number of frames with a delay of 0 ms before the
	 * animation thread yields to other running threads
	 */
	private static final int MAX_FRAME_SKIPS = 5;
	/*
	 * no. of frames that can be skipped in any one animation loop
	 * i.e game state is updated but not rendered
	 */
	
	private Thread animator; //for the animation
	
	private volatile boolean running = false; //stops the animation
	private volatile boolean gameOver = false; //for game termination
	private volatile boolean isPaused = false;
	private Graphics dbg;
	private Image dbImage = null;
	//more variable...
	
	public GamePanel(){
		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH , PHEIGHT));
		setFocusable(true);
		requestFocus(); //JPanel now receive Key events
		readyForTermination();
		
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				testPress(e.getX() , e.getY());
			}
		});
		//more...
	}//end of GamePanel
	
	private void readyForTermination(){
		addKeyListener(new KeyAdapter(){
			/*
			 * listen to esc , q , end . ctrl=c
			 */
			public void keyPressed(KeyEvent e){
				int keyCode = e.getKeyCode();
				if((keyCode == KeyEvent.VK_ESCAPE) ||
					(keyCode == KeyEvent.VK_Q) ||
					(keyCode == KeyEvent.VK_END) ||
					((keyCode == KeyEvent.VK_C) && e.isControlDown())){
					running = false;
				}
			}
		});
	}//end of readyForTermination
	
	public void pauseGame(){
		isPaused = true;
	}
	
	public void resumeGame(){
		isPaused = false;
	}
	private void testPress(int x , int y){
		/*
		 * is (x,y) important to the game?
		 */
		if(!isPaused &&!gameOver){
			//do something...
		}
	}//end of testPress
	
	public void addNotify(){
		/*
		 * Wait for the JPanel to be added to JFrame/JApplet before starting
		 */
		super.addNotify();  //create the peers
		startGame();      //start the Thread
	}
	
	private void startGame(){
		/*
		 * initialize and start the thread
		 */
		if(animator == null || !running){
			animator = new Thread(this);
			animator.start();
		}
	}//end of startGame
	
	public void stopGame(){
		/*
		 * called by user to stop the execution
		 */
		running = false;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if(dbImage != null){
			g.drawImage(dbImage , 0, 0, null);
		}
	}
	
	public void run() {
		/*
		 * repeatedly (possibly pause) update, render, sleep so loop takes close
		 * to period nsecs.Sleep inaccuracies are handled.
		 * The timing calculation uses the JAVA 3D Timer
		 * 
		 * Overruns in update/render will cause extra updates
		 * to be carried out so UPS ~= requested FPS
		 */
		long beforeTime , afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;
		
		beforeTime = J3DTimer.getValue();
		
		running = true;
		while(running){
			
			gameUpdate(); //game state is updated
			gameRender(); //render to a buffer
			paintScreen();    //draw buffer to screen
			
			afterTime = J3DTimer.getValue();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime; //the left in this loop
			
			if(sleepTime > 0){ //some time left in this cycle
				try{
					Thread.sleep(sleepTime/1000000L);
				}catch(InterruptedException e){}
				overSleepTime = (J3DTimer.getValue() - afterTime) - sleepTime;
			}else{ //sleepTime <=0 , frame took longer than perild
				excess -= sleepTime; //store excess time value
				overSleepTime = 0L;
				
				if(++noDelays >= NO_DELAY_PER_YIELD){
					Thread.yield(); //give another thread the chance to run
					noDelays = 0;
				}
			}				
			beforeTime = J3DTimer.getValue();
			/*
			 * If frame animation is taking too long, update game state
			 * without rendering it, to get the update/sec nearer to
			 * the requested FPS
			 */
			int skips = 0;
			while((excess > period) && (skips < MAX_FRAME_SKIPS)){
				excess -= perios;
				gameUpdate(); //update but not render
				skips++;
			}
		}
		System.exit(0);
	}//end of run
	
	private void paintScreen(){
		/*
		 * actively render the buffer image to screen
		 */
		Graphics g;
		try{
			g = this.getGraphics(); //get the panel's graphics context
			if((g != null) && (dbImage != null)){
				g.drawImage(dbImage, 0, 0, null);
				Toolkit.getDefaultToolkit().sync(); //sync the display on some system
				g.dispose();
			}
		}catch(Exception e){
			System.out.println("Graphic context errer : " + e);
		}
	}//end of paintScreen
	
	private void gameUpdate(){
		if(!isPaused && !gameOver){
			//update game state
		}
		//more...
	}
	
	private void gameRender(){
		/*
		 * draw the current frame to an image buffer
		 */
		if(dbImage == null){ //create the buffer
			dbImage = createImage(PWIDTH , PHEIGHT);
			if(dbImage == null){
				System.out.println("Image is null");
				return;
			}else{
				dbg = dbImage.getGraphics();
			}
		}
		
		//clear the background
		dbg.setColor(Color.white);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
		
		//draw image element
		
		if(gameOver){
			gameOverMessage(dbg);
		}
	}//end of gameRender
	
	private void gameOverMessage(Graphics g){
		/*
		 * center the game over message
		 */
		//code to calculate x and y
		g.drawString(msg, x, y);
	}//end of gameOverMessage
	
	public static void main(String[] args) {


	}

}
