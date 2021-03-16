
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

//import java.awt.*;


import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author David Cairns
 *
 */
@SuppressWarnings("serial")

public class Game extends GameCore implements MouseListener, MouseMotionListener
{
	// Useful game constants
	static int screenWidth = 1024; // or 1024, or 512
	static int screenHeight = 756; // or 778, or 384
	
	final int TILE_WIDTH = 32;
	final int TILE_HEIGTH = 32;
	float 	lift = 0.005f;
	float	gravity = 0.0001f;
	final int rows = 24; 
//	final int rows = 12;
	final int columns = 80;
	
//	final int tilex = 12;
	// Game state flags
	boolean jump = false;
	boolean moveRight = false;
	boolean moveLeft = false;
	boolean debugMode = false;
	int lastCheckpointSeen = 0;
	boolean hIsPressed;
	boolean holdingObject = false;
	boolean facingRight = true;
	boolean hasAJump = true;
	long lastTime = 0;
	//    boolean inAir = true;
	int mouseX;
	int mouseY;
	int layersNo = 7;
	// Game resources 
	Animation landingR;
	Animation landingL;
	Animation jumpingR;
	Animation jumpingL;
	Animation fallL;
	Animation fallR;
	Animation tramp_noJump;
	Animation tramp_jump;
	Animation r;
	Animation c;
	Animation bar_empty;
	Animation bar_full;

	Sprite moveable_object_bar;
	Sprite tramp;
	Sprite 	rock;
	Sprite	player = null;
	ArrayList<Sprite> clouds = new ArrayList<Sprite>();
	ArrayList<Sprite> coins = new ArrayList<Sprite>();
	ArrayList<Sprite> tramps = new ArrayList<Sprite>();
	ArrayList<Sprite> background = new ArrayList<Sprite>();
	ArrayList<Sprite> background_copy = new ArrayList<Sprite>();
	TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()

	long total;         			// The score will be the total time elapsed since a crash


	/**
	 * The obligatory main method that creates
	 * an instance of our class and starts it running
	 * 
	 * @param args	The list of parameters this program might use (ignored)
	 */
	public static void main(String[] args) {

		Game gct = new Game();
		gct.init();

		// Start in windowed mode with the given screen height and width
		gct.run(false,screenWidth,screenHeight);
	}

	/**
	 * Initialise the class, e.g. set up variables, load images,
	 * create animations, register event handlers
	 */
	public void init()
	{         
		
		Sprite s;	// Temporary reference to a sprite 
		// Load the tile map and print it out so we can check it is valid
		tmap.loadMap("maps", "map2.txt");
//		setSize(tmap.getPixelWidth()/2, tmap.getPixelHeight() / 2);
		setSize(1024, 576);
		setLocation(350, 150);
//		setSize(1280, 800);
//		setSize(1920, 1080);
		setVisible(true);

		r = new Animation();
		r.addFrame(loadImage("images/rock.png"), 250);
		rock = new Sprite(r);

		for (int i = 1; i < 8; i ++) {
			Animation tempAnim = new Animation();
			String image = String.format("images/layer%d.png", i);
			tempAnim.addFrame(loadImage(image), 5000);
			Sprite tempS = new Sprite(tempAnim);
			tempS.show();
			tempS.setPosition(0, 0);
			double scaleFactor = 0.8;
			tempS.setY((int)((rows * 32) - tempS.getHeight() * scaleFactor ));
			tempS.setScale((float) scaleFactor);
			background.add(tempS);
		}
//		setLayersHeight();
		// Create a set of background sprites that we can 
		// rearrange to give the illusion of motion
		jumpingR = new Animation();
		jumpingL = new Animation();
		landingR = new Animation();
		landingL = new Animation();
		fallR = new Animation();
		fallL = new Animation();


		//        landing.loadAnimationFromSheet("images/landbird.png", 4, 1, 60);
		jumpingR.loadAnimationFromSheet("images/Jump (32x32).png", 1, 1, 200);
		jumpingL.loadAnimationFromSheet("images/jump left.png", 1, 1, 200);

		landingL.loadAnimationFromSheet("images/Run Left.png", 12, 1, 200);
		landingR.loadAnimationFromSheet("images/Run (32x32).png", 12, 1, 200);

		fallR.loadAnimationFromSheet("images/Fall (32x32).png", 1, 1, 200);
		fallL.loadAnimationFromSheet("images/fall left.png", 1, 1, 200);

		// Initialise the player with an animation
		player = new Sprite(landingR);

		// Load a single cloud animation
		Animation ca = new Animation();
//		ca.addFrame(loadImage("images/x32-layer-6-clouds.png"), 1000);
		ca.addFrame(loadImage("images/cloud.png"), 1000);

		// Create 3 clouds at random positions off the screen
		// to the right
		for (int c=0; c<3; c++)
		{
			s = new Sprite(ca);
			s.setX(screenWidth - 100 + (int)(Math.random()*200.0f));
			s.setY(30 + (int)(Math.random()*150.0f)); // 30 +  or -150 +
			s.setVelocityX(-0.02f);
			s.show();
			clouds.add(s);
		}
		
		Sprite coin;
		// Load a single coin animation
		
	
		for (int col = 0; col < columns; col++)
		{
			for (int row = 0; row < rows; row++)
			{
				c = new Animation();
				c.loadAnimationFromSheet("images/coins.png", 1, 4, 200);
				Tile tempT = tmap.getTile(col, row);
				char ch = tempT.getCharacter();
				String st = String.valueOf(ch);
				if (st.equals("C")) {
					coin = new Sprite(c);
					coin.setX(tempT.getXC());
					coin.setY(tempT.getYC());
					coin.setVelocity(0, 0);
					coin.show();
					coin.setScale(0.5f);
					coins.add(coin);

					ch = '.';
					tempT.setCharacter(ch);
//					System.out.println(st);
//					System.out.println("X: " + tempT.getXC() + " Y: " + tempT.getYC() + " char:" + tempT.getCharacter());
				}
			}	
		}
		
		tramp_jump = new Animation();
		tramp_jump.loadAnimationFromSheet("images/trampoline jump.png", 8, 1, 150);
		tramp_noJump = new Animation();
		tramp_noJump.loadAnimationFromSheet("images/trampoline no jump.png", 1, 1, 10000);
		tramp = new Sprite(tramp_noJump);
		for (int col = 0; col < columns; col++)
		{
			for (int row = 0; row < rows; row++)
			{
				Tile tempT = tmap.getTile(col, row);
				char ch = tempT.getCharacter();
				String st = String.valueOf(ch);
				if (st.equals("S")) {
					tramp = new Sprite(tramp_jump);;
					tramp.setX(tempT.getXC());
					tramp.setY(tempT.getYC());
					tramp.setVelocity(0, 0);
					tramp.show();
					tramps.add(tramp);

					ch = '.';
					tempT.setCharacter(ch);
//					System.out.println(st);
//					System.out.println("X: " + tempT.getXC() + " Y: " + tempT.getYC() + " char:" + tempT.getCharacter());
				}
			}	
		}
		bar_empty = new Animation();
		bar_full = new Animation();
		bar_empty.addFrame(loadImage("images/greenbar empty.png"), 250);
		bar_full.addFrame(loadImage("images/greenbar full.png"), 250);
		moveable_object_bar = new Sprite(bar_full);

		moveable_object_bar.setScale(0.35f);
		//        Animation fa = new Animation();
		//        fa.addFrame(loadImage("images/Cartoon_Forest_BG_04.png"), 10000);
		//        f = new Sprite(fa);
		//        f.setX(0);
		//    	f.setY(0);
		//    	f.setVelocityX(-0.002f);
		//    	f.show();
		//    	clouds.add(f);

		addMouseListener(this);
		addMouseMotionListener(this);
		initialiseGame();


		System.out.println(tmap);
	}


	/**
	 * You will probably want to put code to restart a game in
	 * a separate method so that you can call it to restart
	 * the game.
	 */
	public void initialiseGame()
	{
		total = 0;

		hIsPressed = false;
		lastCheckpointSeen = 0;
		player.setX(64);
		player.setY(200);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();

		rock.setX(300);
		rock.setY(150);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();
	}

	public void restartGame()
	{
		total = 0;

		player.setX(64);
		player.setY(200);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();
	}

	/**
	 * Draw the current state of the game
	 */
	public void draw(Graphics2D g)
	{    	
		// Be careful about the order in which you draw objects - you
		// should draw the background first, then work your way 'forward'

		// First work out how much we need to shift the view 
		// in order to see where the player is.
		//    	System.out.println( (int) (player.getX() / tmap.getTileWidth()));
		//    	System.out.println( (int) (player.getX()));
		//        int xo  =  (int) (screenWidth / 2 - player.getWidth() - player.getX());
		int xo = (int) (screenWidth/2 - player.getX());
		int yo = (int) (800/2 - player.getY());

		// If relative, adjust the offset so that
		// it is relative to the player
		//        tmap.draw(g, 15, 1);
		// ...?

		g.setColor(Color.pink);
		g.fillRect(0, 0, getWidth(), getHeight());
		background.get(0).draw(g);
		// Apply offsets to sprites then draw them
		for (int i = 1; i < layersNo; i++) {
			background.get(i ).setOffsets((int)(xo * ((i ) * 0.1)), yo);
			background.get(i ).drawTransformed(g);
		}
//		for (Sprite layer: background) {
//			layer.draw(g);
//		}
		for (Sprite s: clouds)
		{
			s.setOffsets(xo,yo);
			s.draw(g);
		}

		for (Sprite aCoin: coins)
		{
			aCoin.setOffsets(xo,yo);
			aCoin.drawTransformed(g);
		}
		
		for (Sprite aTramp: tramps)
		{
			aTramp.setOffsets(xo,yo);
			aTramp.drawTransformed(g);
		}
		// Apply offsets to tile map and draw  it
		tmap.draw(g,xo,yo);   

		// Apply offsets to player and draw 
		//        moveable_object_bar.setScale(0.5f);
		moveable_object_bar.setOffsets(xo, yo);
		moveable_object_bar.drawTransformed(g);
		player.setOffsets(xo, yo);
		player.draw(g);
		rock.setOffsets(xo, yo);
		rock.draw(g);
		//        if (boundingBoxCollision(rock, aCoin)) {
		Color myColor = new Color(255, 0, 0, 65);
		g.setColor(myColor);
		g.fillRect((int) rock.getX() + xo, (int) rock.getY() + yo, rock.getWidth(), rock.getHeight());
		//        }

		if (hIsPressed) {
			player.drawBoundingBox(g);
		} 


		// Show score and status information
		String msg = String.format("Score: %d", total/100);
		g.setColor(Color.darkGray);
		g.drawString(msg, getWidth() - 80, 50);
	}

	/**
	 * Update any sprites and check for collisions
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of elapsed
	 */    
	public void update(long elapsed)
	{
//		double fps = 1000000.0 / (lastTime - (lastTime = System.nanoTime())); //This way, lastTime is assigned and used at the same time.
//		System.out.println(fps);
		
		player.setVelocityX(player.getVelocityX() * 0.5f);
		// Make adjustments to the speed of the sprite due to gravity

		player.setVelocityY(player.getVelocityY()+(gravity * elapsed));

		//Make coins disappear when player touches them
		Iterator<Sprite> itr = coins.iterator();            
		while(itr.hasNext()){
			Sprite aCoin = itr.next();
			if(boundingBoxCollision(player, aCoin)) {
				itr.remove();
				Sound s = new Sound("sounds/coin1.wav");
				s.start();	
			}
		}
		
		Iterator<Sprite> itr2 = tramps.iterator();            
		while(itr2.hasNext()){
			Sprite tramp = itr2.next();	
			if(boundingBoxCollision(player, tramp)) {
				tramp_jump.setHasLooped(false);
				tramp.setAnimation(tramp_jump);
				player.setVelocityY(-0.12f);
			}
			if (tramp.getAnimation().hasLooped())
				tramp.setAnimation(tramp_noJump);
		}
//		for (Sprite layer: background) {
//			layer.update((long) (elapsed * 0.8));
//		}
		for (Sprite s: clouds)
			s.update(elapsed);
		for (Sprite coin: coins) {
//			coin.setAnimationSpeed(1.0f);
			coin.update(elapsed);
		}
		for (Sprite tramp: tramps)
			tramp.update(elapsed);

		player.setAnimationSpeed(1.0f);

		if (jump)
			player.setVelocityY( -0.1f);

		if (moveRight)
			player.setVelocityX(player.getVelocityX() + 0.04f);
		else if (moveLeft)
			player.setVelocityX(player.getVelocityX() - 0.04f);



		//Set Player animations
		if ((jump || player.getVelocityY() < 0) && (moveRight || facingRight)) {
			player.setAnimation(jumpingR);
			player.setAnimationSpeed(1.8f);
		}
		else if (Math.abs(player.getVelocityY()) < 0.01f && (moveRight || facingRight)) {
			player.setAnimation(landingR);
		}
		else if (Math.abs(player.getVelocityY()) < 0.01f && (moveLeft || !facingRight)) {
			player.setAnimation(landingL);
		}  
		else if ((jump || player.getVelocityY() < 0) && (moveLeft || !facingRight)) {
			player.setAnimation(jumpingL);
			player.setAnimationSpeed(1.8f);
		}
		else if (player.getVelocityY() > 0.05f && (moveRight || facingRight)) {
			player.setAnimation(fallR);
		}

		else if (player.getVelocityY() > 0.05f && (moveLeft || !facingRight)) {
			player.setAnimation(fallL);
		}


		if (boundingBoxCollision(player, rock))
			player.setVelocityY(player.getVelocityY() - 0.01f);

		moveable_object_bar.setX(player.getX() - 5);
		moveable_object_bar.setY(player.getY() - 15);


		// Then check for any collisions that may have occurred
		handleScreenEdge(player, tmap, elapsed);
		//        checkTileCollision(player, tmap);
		
//		testCollision2(player, tmap, elapsed);
//		checkTileCollisionTest(player, tmap);
		
//		testCollision2(rock, tmap, elapsed);
		// Now update the sprites animation and position
//		player.setX(player.getX() + player.getVelocityX() * elapsed);
//		player.setY(player.getY() + player.getVelocityY() * elapsed);
		player.update(elapsed);
		
//		testCollision(player, tmap);
		
	}


	/**
	 * Checks and handles collisions with the edge of the screen
	 * 
	 * @param s			The Sprite to check collisions for
	 * @param tmap		The tile map to check 
	 * @param elapsed	How much time has gone by since the last call
	 */
	public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
	{
		// This method just checks if the sprite has gone off the bottom screen.
		// Ideally you should use tile collision instead of this approach

		if (s.getY() + s.getHeight() > tmap.getPixelHeight())
		{
			// Put the player back on the map 1 pixel above the bottom
			s.setY(tmap.getPixelHeight() - s.getHeight() - 1); 

			// and make them bounce
			s.setVelocityY(-s.getVelocityY());
		}
	}





	public boolean boundingBoxCollision(Sprite s1, Sprite s2)
	{
		return ((s1.getX() + s1.getImage().getWidth(null) > s2.getX()) &&
				(s1.getX() < (s2.getX() + s2.getImage().getWidth(null))) &&
				((s1.getY() + s1.getImage().getHeight(null) > s2.getY()) &&
						(s1.getY() < s2.getY() + s2.getImage().getHeight(null))));  	
	}

	public void restartFromCheckpoint()
	{
		int mapWidth = tmap.getMapWidth(); //64 
	}
	/**
	 * Check and handles collisions with a tile map for the
	 * given sprite 's'. Initial functionality is limited...
	 * 
	 * @param s			The Sprite to check collisions for
	 * @param tmap		The tile map to check 
	 */

	public void testCollision2(Sprite s, TileMap tmap, long elapsed) {
		float newx = s.getX() + s.getVelocityX() * elapsed;
		float newy = s.getY() + s.getVelocityY() * elapsed;
		float sx = s.getX();
		float sy = s.getY();
		if (debugMode) System.out.println("newx " + newx + " sx" + sx);
		boolean topcolx = false;
		boolean botcolx = false;
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();
//		float spriteWidth = s.getWidth() / tileWidth;
//		float spriteHeight = s.getHeight() / tileHeight;

		int	xtile = (int)(sx / tileWidth);
		
		int newxtile = (int) (newx / tileWidth);
		// The same applies to the y coordinate
		int ytile = (int)(sy / tileHeight);
		
		int newytile = (int) (newy / tileWidth);

		char ch = tmap.getTileChar(newxtile, ytile);
		char ch2 = tmap.getTileChar(newxtile,(int) (sy / tileHeight + 0.99f));
		if (s.getVelocityX() < 0) {
			if (ch != '.' && ch2 != '.') // If it's not a dot (empty space), handle it
			{
				
					s.setVelocityX(0);  	
				
					if (debugMode) System.out.println("Left Collission occurring");
					// You should move the sprite to a position that is not colliding
//					newx = (int) newx + 1;
//					newx = ((int) (sx + 0.99f)) + 1.01f;
//					if (sx < 32)
//						System.out.println("NOW" + sx);
//					newx = (int) (s.getOldX()) ;
					newx = (int) sx;
					if (newx < 32)
					System.out.println("newx after modified" + newx);	
				// Here we just stop the sprite. 	
			}
		}
		else if (ch != '.' || ch2 != '.') // If it's not a dot (empty space), handle it
		{	
			s.setVelocityX(0);  	
			if (debugMode) System.out.println("Left Collission occurring B");
			// You should move the sprite to a position that is not colliding
			newx = (int) (sx);
//			newx = ((int) (sx + 0.99f)) + 1.01f;
//			if (sx < 32)
//				System.out.println("NOW" + sx);
//			newx = (int) (s.getOldX()) ;
			if (newx < 32)
			System.out.println("newx after modified" + newx);	
		// Here we just stop the sprite. 	
	}
	
		if (s.getVelocityX() >= 0){
			ch = tmap.getTileChar((int) (newx / tileWidth + 1.0f), ytile);
			ch2 = tmap.getTileChar((int) (newx / tileWidth + 1.0f), (int) (sy / tileHeight + 0.9f));
			if (ch != '.' || ch2 != '.') // If it's not a dot (empty space), handle it
			{
				// Here we just stop the sprite. 
				s.setVelocityX(0);

				if (debugMode) System.out.println("Right Collission occurring");
//				newx = (int) newx;
				newx = sx;
				// You should move the sprite to a position that is not colliding
				//    		s.setX((positionx) * tileWidth );
			}
		}
//		s.setX(newx);
		//Y axis
		boolean onGround = false;
		if (s.getVelocityY() < 0) {
			newxtile = (int) (newx / tileWidth);
//			newxtileright = (int) ((newxtile + 0.9f) );
			ch = tmap.getTileChar(newxtile, newytile);
			ch2 = tmap.getTileChar((int) (newx / tileWidth + 0.9f), newytile); 
			if (ch != '.' || ch2 != '.') // If it's not a dot (empty space), handle it
			{
				// Here we just stop the sprite. 
				s.setVelocityY(0);
				if (debugMode) System.out.println("Up Collission occurring");
//				newy = (int) newy + 2;
				newy = sy - 1;
				// You should move the sprite to a position that is not colliding
			}
		}
		else {
			newxtile = (int) (newx / tileWidth);
			ch = tmap.getTileChar(newxtile, (int)(newy / tileHeight + 1.0f));
			ch2 = tmap.getTileChar((int)(newx /tileWidth + 0.9f), (int)(newy / tileHeight + 1.0f)); 
			if (ch != '.' || ch2 != '.') // If it's not a dot (empty space), handle it
			{
				// Here we just stop the sprite. 
				s.setVelocityY(0);
				if (debugMode) System.out.println("Down Collission occurring");
				newy = (int) sy;
//				newy = sy;
				onGround = true;
				// You should move the sprite to a position that is not colliding
			}
		}
		if ((!onGround) && (botcolx || topcolx))
			s.setVelocityY(gravity);
		if (newx < 32)
			System.out.println("newx is" + newx + " sx was" + sx);
		s.setX(newx);
		s.setY(newy);
	}
	public void testCollision(Sprite s, TileMap tmpap) {
		//    	 float newX = player.getX() + player.getVelocityX() * elapsed;
		//        float newY = player.getY() + player.getVelocityY() * elapsed;
		// Take a note of a sprite's current position
		float sx = s.getX();
		float sy = s.getY();
		
		
		boolean topcol = false;
		boolean botcol = false;
		boolean botcolY = false;
		
		float sxright = s.getX() + s.getWidth() ;
		float sybottom = s.getY() + s.getHeight();
		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();
		
		char topleft = tmap.getTileChar((int) (sx / tileWidth), (int) (sy / tileHeight));
		char bottomleft = tmap.getTileChar((int) (sx / tileWidth), (int) ((sy + s.getHeight()) / tileHeight));
		char topright = tmap.getTileChar((int) ((sx + s.getWidth()) / tileWidth), (int) (sy / tileHeight));
		char bottomright = tmap.getTileChar((int) ((sx + s.getWidth()) / tileWidth), (int) ((sy + s.getHeight()) / tileHeight));
		
		char topleftsb = tmap.getTileChar((int) (sx / tileWidth), (int) ((sy + 2) / tileHeight));
		char bottomleftsb = tmap.getTileChar((int) (sx / tileWidth), (int) ((sy + s.getHeight() - 2) / tileHeight));
		char toprightsb = tmap.getTileChar((int) ((sx + s.getWidth()) / tileWidth), (int) (((sy + 2)) / tileHeight));
		char bottomrightsb = tmap.getTileChar((int) ((sx + s.getWidth()) / tileWidth), (int) ((sy + s.getHeight() - 2) / tileHeight));
		
		// Divide the sprite's x coordinate by the width of a tile, to get
		// the number of tiles across the x axis that the sprite is positioned at
		// The same applies to the y coordinate
		// What tile character is at the top left of the sprite s?
		
		if (topleftsb != '.' || toprightsb != '.') {
			s.setX(s.getOldX());
			s.setVelocityX(0);
			topcol = true;
		}
		if (bottomleftsb != '.' || bottomrightsb != '.') {
			s.setX(s.getOldX());
			s.setVelocityX(0);
			botcol = true;
		}
		//Moving up
		if (topright != '.' || topleft != '.') {
			// Here we just stop the sprite. 
			s.setVelocityY(0.05f); //Super Mario bounce
			// You should move the sprite to a position that is not colliding
			//						s.setY((ytile + 0.999f) * tileHeight );
			if (debugMode) System.out.println("Up Collission occurring");
			s.setY(s.getOldY());
		}
		//Moving down
		if (bottomright != '.' || bottomleft != '.'){ 
			// Here we just stop the sprite. 
			s.setVelocityY(0);
			if (debugMode) System.out.println("Down Collission occurring");
			botcolY = true;
			// You should move the sprite to a position that is not colliding
			s.setY(s.getOldY());
		}
		
				if ((!botcolY) && topcol || botcol)
					s.setVelocityY(gravity);
		}


	
	public void checkTileCollision(Sprite s, TileMap tmap)
	{

		boolean collisionLeft = false;
		boolean collisionRight = false;

		// Take a note of a sprite's current position
		float sx = s.getX();
		float sy = s.getY();

		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();
		System.out.print("Player height:" + player.getHeight() );
		System.out.print("Player Width:" + player.getWidth() );
		//TOP LEFT
		// Divide the sprite's x coordinate by the width of a tile, to get
		// the number of tiles across the x axis that the sprite is positioned at
		int	xtile = (int)(sx / tileWidth);
		// The same applies to the y coordinate
		int ytile = (int)(sy / tileHeight);
		// What tile character is at the top left of the sprite s?
		char chtr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), ytile);
		char chtl = tmap.getTileChar(xtile, ytile);
		char ch = tmap.getTileChar(xtile, ytile);
		char chbl = tmap.getTileChar(xtile, ytile);
		char br = tmap.getTileChar(xtile, ytile);
		if (ch != '.') // If it's not a dot (empty space), handle it
		{
			// Here we just stop the sprite. 
			s.stop();
			// You should move the sprite to a position that is not colliding
			//    		s.setY(tmap.getPixelHeight() - s.getHeight() - 1); 
			//    		s.setY(s.getY() - s.getHeight());	
		}


		//BOTTOM LEFT
		// We need to consider the other corners of the sprite
		// The above looked at the top left position, let's look at the bottom left.
		xtile = (int)(sx / tileWidth);
		ytile = (int)((sy + s.getHeight())/ tileHeight);
		ch = tmap.getTileChar(xtile, ytile);

		// If it's not empty space
		if (ch != '.') 
		{
			// Let's make the sprite bounce
			//    		s.setVelocityY(-s.getVelocityY()); // Reverse velocity 
			if (isPlayer(s)) {
				if (s.getVelocityY() > 0) {
					s.setVelocityY(0); //stop the sprite from falling
				}
				s.setY((float) (ytile * tmap.getTileHeight()) - s.getHeight());
				//     			hasAJump = true;
				//     			jump = false;
				//     			collisionLeft = true;
			}
			//     		s.stop();
			//     		s.shiftY(0.01f);
			//     		s.shiftX(0.02f);
			//     		s.setVelocityX(-s.getVelocityX());
			//     		gravity = 0.000f;
		}
		else {
			if (isPlayer(s)) {
				collisionLeft = false;
			}
		}

		//TOP RIGHT
		xtile = (int)((sx + s.getWidth()) / tileWidth);
		ytile = (int)(sy / tileHeight);
		ch = tmap.getTileChar(xtile, ytile);

		// If it's not empty space
		if (ch != '.') 
		{
			// Let's make the sprite bounce
			//    		s.setVelocityY(-s.getVelocityY()); // Reverse velocity 
			s.stop();
		}

		//BOTTOM RIGHT
		xtile = (int)((sx + s.getWidth()) / tileWidth);
		ytile = (int)((sy + s.getHeight())/ tileHeight);
		ch = tmap.getTileChar(xtile, ytile);

		// If it's not empty space
		if (ch != '.') 
		{
			// Let's make the sprite bounce
			//    		s.setVelocityY(-s.getVelocityY()); // Reverse velocity 
			if (isPlayer(s)) {
				hasAJump = true;
				jump = false;
				collisionRight = true;
				if (s.getVelocityY() > 0) {
					s.setVelocityY(0); //stop the sprite from falling
				}
				s.setY((float) (ytile * tmap.getTileHeight()) - s.getHeight());
				//     			s.shiftY(-0.01f);
			}
			//     		s.stop();
		}
		else {
			if (isPlayer(s)) {
				hasAJump = true;
				jump = false;
				collisionRight = false;
			}
		}

	}

	public void checkTileCollisionTest(Sprite s, TileMap tmap)
	{

		boolean collisionLeft = false;
		boolean collisionRight = false;

		// Take a note of a sprite's current position
		float sx = s.getX();
		float sy = s.getY();

		// Find out how wide and how tall a tile is
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();
		// Divide the sprite's x coordinate by the width of a tile, to get
		// the number of tiles across the x axis that the sprite is positioned at
		int	xtile = (int)(sx / tileWidth);
		// The same applies to the y coordinate
		int ytile = (int)(sy / tileHeight);
		// What tile character is at the top left of the sprite s?
		char chtr = tmap.getTileChar((int)((sx + s.getWidth()/ 2) / tileWidth), ytile);
		char chtl = tmap.getTileChar(xtile, ytile);
		char chbl = tmap.getTileChar(xtile, (int)((sy + s.getHeight()/ 2) / tileHeight));
		char chbr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), (int)((sy + s.getHeight()) / tileHeight));
		//Bottom
		if (chbl != '.' || chbr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityY(0);
			s.setY(s.getOldY());
		}
		//Top 
		else if (chtl != '.' || chtr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityY(0);
			s.setY(s.getOldY());
		}
		sx = s.getX();
		xtile = (int)(sx / tileWidth);
		// The same applies to the y coordinate
		// What tile character is at the top left of the sprite s?
		chtr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), ytile);
		chtl = tmap.getTileChar(xtile, ytile);
		chbl = tmap.getTileChar(xtile, (int)((sy + s.getHeight()) / tileHeight));
		chbr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), (int)((sy + s.getHeight()) / tileHeight));
		//Left
		if (chtl != '.' || chbl != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityX(0);
			s.setX(s.getOldX());
		}
		//Right
		else if (chtr != '.' || chbr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityX(0);
			s.setX(s.getOldX());
		}
	}
	public void checkTileCollisionTest2(Sprite s, TileMap tmap)
	{
		boolean collisionLeft = false;
		boolean collisionRight = false;
		float tileWidth = tmap.getTileWidth();
		float tileHeight = tmap.getTileHeight();
		
		int leftTile = (int) ((s.getX() - s.getWidth() / 2) / tileWidth);
		int rightTile = (int) ((s.getX() + s.getWidth() / 2 - 1) / tileWidth);
		int topTile = (int) ((s.getX() - s.getWidth() / 2) / tileHeight);
//		int leftTile = (int) ((s.getX() - s.getWidth() / 2) / tileHeight);
		// Take a note of a sprite's current position
		float sx = s.getX();
		float sy = s.getY();

		// Find out how wide and how tall a tile is
		
		// Divide the sprite's x coordinate by the width of a tile, to get
		// the number of tiles across the x axis that the sprite is positioned at
		int	xtile = (int)(sx / tileWidth);
		// The same applies to the y coordinate
		int ytile = (int)(sy / tileHeight);
		// What tile character is at the top left of the sprite s?
		char chtr = tmap.getTileChar((int)((sx + s.getWidth()/ 2) / tileWidth), ytile);
		char chtl = tmap.getTileChar(xtile, ytile);
		char chbl = tmap.getTileChar(xtile, (int)((sy + s.getHeight()/ 2) / tileHeight));
		char chbr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), (int)((sy + s.getHeight()) / tileHeight));
		//Bottom
		if (chbl != '.' || chbr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityY(0);
			s.setY(s.getOldY());
		}
		//Top 
		else if (chtl != '.' || chtr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityY(0);
			s.setY(s.getOldY());
		}
		sx = s.getX();
		xtile = (int)(sx / tileWidth);
		// The same applies to the y coordinate
		// What tile character is at the top left of the sprite s?
		chtr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), ytile);
		chtl = tmap.getTileChar(xtile, ytile);
		chbl = tmap.getTileChar(xtile, (int)((sy + s.getHeight()) / tileHeight));
		chbr = tmap.getTileChar((int)((sx + s.getWidth()) / tileWidth), (int)((sy + s.getHeight()) / tileHeight));
		//Left
		if (chtl != '.' || chbl != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityX(0);
			s.setX(s.getOldX());
		}
		//Right
		else if (chtr != '.' || chbr != '.') // If it's not a dot (empty space), handle it
		{
			s.setVelocityX(0);
			s.setX(s.getOldX());
		}
	}
	/**
	 * Override of the keyPressed event defined in GameCore to catch our
	 * own events
	 * 
	 *  @param e The event that has been generated
	 */
	public void keyPressed(KeyEvent e) 
	{ 
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_ESCAPE) stop();

		if (key == KeyEvent.VK_UP) jump = true;

		if (key == KeyEvent.VK_RIGHT) {moveRight = true; moveLeft = false; facingRight = true;}

		if (key == KeyEvent.VK_LEFT) {moveLeft = true; moveRight = false; facingRight = false;}

		if (key == KeyEvent.VK_H) hIsPressed = !hIsPressed;
		
		if (key == KeyEvent.VK_D) debugMode = !debugMode;

		if (key == KeyEvent.VK_S)
		{
			// Example of playing a sound as a thread
			Sound s = new Sound("sounds/caw.wav");
			s.start();
		}

		if (key == KeyEvent.VK_R) restartGame();
	}
	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
		case KeyEvent.VK_ESCAPE : stop(); break;
		case KeyEvent.VK_UP     : jump = false; break;
		case KeyEvent.VK_RIGHT     : moveRight = false; break;
		case KeyEvent.VK_LEFT     : moveLeft = false; break;
		default :  break;
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = (int) (e.getX() + player.getX());
		System.out.println( "X: " +  mouseX + "player position:" + player.getX());
		rock.setX(mouseX - 256);
		rock.setY(e.getY());
		//"Mouse pressed; # of clicks: "   + e.getClickCount() +

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("DRAGGING");
		//		Point p = e.getPoint();
		//		int xo  =  (int) (screenWidth / 2 - player.getX());
		int offset =  (int) (screenWidth / 2);
		//- player.getWidth() -  player.getX()
		mouseX = (int) (e.getX());
		mouseY = (int) (e.getY());
		//		mouseY = p.y;
		rock.setX(mouseX - offset + player.getX() - rock.getWidth()/2);
		rock.setY(mouseY - rock.getHeight()/2);
		System.out.println(mouseX + " and " + player.getX() + " Rock:" + rock.getX());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//		mouseX = e.getX();
		//		mouseY = e.getY();
		//		int minX = 0;
		//		int maxX = 0;
		//		int minY = 0;
		//		int maxY = 0;
		//		if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) 
		//			holdingObject = true;
		//		
		//		player.setX(mouseX);
		//		System.out.println(mouseX);
		//		System.out.println(player.getX());
		//		player.setY(mouseY);
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

	@Override
	public void mouseMoved(MouseEvent e) {
		//		rock.setX(e.getX() - 256 + player.getX());
		//		rock.setY(e.getY());
		// TODO Auto-generated method stub
	}

	public boolean isPlayer(Sprite s) {
		if (s.equals(player))
			return true;
		return false;
	}
	
//	public void substituteCh(Sprite s, String chString, float scale, ArrayList<Sprite> array) {
//		for (int col = 0; col < 80; col++)
//		{
//			for (int row = 0; row < 12; row++)
//			{
//				Tile tempT = tmap.getTile(col, row);
//				char ch = tempT.getCharacter();
//				String st = String.valueOf(ch);
//				if (st.equals(chString)) {
//					s = new Sprite(c);
//					s.setX(tempT.getXC());
//					s.setY(tempT.getYC());
//					s.setVelocity(0, 0);
//					s.show();
//					s.setScale(0.5f);
//					array.add(s);
//
//					ch = '.';
//					tempT.setCharacter(ch);
////					System.out.println(st);
////					System.out.println("X: " + tempT.getXC() + " Y: " + tempT.getYC() + " char:" + tempT.getCharacter());
//				}
//			}	
//		}
//	}
}
