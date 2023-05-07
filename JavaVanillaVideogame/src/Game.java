//Student Number: 2720255
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math;
//import org.graalvm.compiler.core.amd64.AMD64ArithmeticLIRGenerator.Maths;

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
	float	gravity = 0.0002f;
	final int rows = 33; ;
	final int columns = 120;
	final int MAX_LIVES = 3;
	final int MIN_LIVES = 1;
	final float MAX_VELOCITY_WATER = 0.04f;
	final float MIN_VELOCITY_TRAMP_PLAYER = 0.2f;
	final float ANT_SPEED_X = 0.1f;
	final int waterAnimSpeed = 150;
	int lives = 3;
	
	float xSpeed = 0.08f;
	
	int level = 1;
	int previousLevel = level;
	int coinsTotal = 0;
	int coinsTaken = 0;
	
	// Game state flags
	boolean jump = false;
	boolean moveRight = false;
	boolean moveLeft = false;
	long hurt_timer = 0;
	boolean hurt_invincibility = false;
	boolean debugMode = false;
	boolean hIsPressed;
	
	boolean editingMode = false;
	boolean usingObject = false;
	boolean holdingObject = false;
	String objectType = "tramp";
	boolean objectColliding = false;
	boolean instructionsDisplayed = true;
	
	boolean facingRight = true; //false if facing left
	boolean hasJump = true; //true when has a jump available
	boolean cheatingOn = false;
	int mouseX;
	int mouseY;
	final int LAYERS_LVL1 = 7;
	final int LAYERS_LVL2 = 4;
	boolean restarting = false;
	boolean soundsOn = true;
	
	
	// Game resources 
	//For all dead enemeis
	Animation deadEnemy;
	
	Animation antR;
	Animation antL;
	
	Animation landingR;
	Animation landingL;
	Animation jumpingR;
	Animation jumpingL;
	Animation fallL;
	Animation fallR;
	Animation hitPlayer;
	
	Animation tramp_noJump;
	Animation tramp_jump;
	Animation r;
	Animation c;
	Animation bar_empty;
	Animation bar_full;
	Animation heart;
	Animation snowing;
	Animation flagStill;
	Animation small_waterfall;
	boolean flagchanged = false;
	Sprite snow;
	Sprite snow_copy;
	Font font;
	Sprite objectHeld;
	Sprite copy_objectHeld;
	Sprite ant;
	Sprite moveable_object_bar;
	Sprite tramp;
	Sprite	player = null;
	//Background Array lists
	ArrayList<Sprite> background = new ArrayList<Sprite>();
	ArrayList<Sprite> background_copy = new ArrayList<Sprite>();
	//Coins array list
	ArrayList<Sprite> coins = new ArrayList<Sprite>();
	//Tramps array list
	ArrayList<Sprite> tramps = new ArrayList<Sprite>();
	//Waterfalls array list
	ArrayList<Sprite> waterfalls = new ArrayList<Sprite>();
	
	//Enemies: ants. Array list
	ArrayList<Sprite> enemies_ants = new ArrayList<Sprite>();
	//Enemies: gators. Array list
	ArrayList<Sprite> enemies_gators = new ArrayList<Sprite>();
	//Enemies: dead. Array list
	ArrayList<Sprite> enemies_dead = new ArrayList<Sprite>();
	
	//Power-ups array lists
	ArrayList<Sprite> powerups_waterfalls = new ArrayList<Sprite>();
	ArrayList<Sprite> powerups_trampolines = new ArrayList<Sprite>();
	
	//Commands array list
	ArrayList<String> commands = new ArrayList<String>();
	//Flags array lists, there were supposed to be more flags initially.
	ArrayList<Sprite> flags = new ArrayList<Sprite>();
	
	
	TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
	TileMap tmap2 = new TileMap();

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
		//Create heart animation
		heart = new Animation();
		heart.loadAnimationFromSheet("images/heart.png", 1, 1, 10000);
		
		font = new Font(Font.DIALOG, Font.PLAIN, 18);
//		tmap.loadMap("maps", "map2.txt");
		setSize(screenWidth, screenHeight);
		setLocation(350, 150);
		setVisible(true);
		//Create flag animation
		flagStill = new Animation();
		flagStill.loadAnimationFromSheet("images/Checkpoint (No Flag).png", 1, 1, 15000);
		small_waterfall = new Animation();
		small_waterfall.loadAnimationFromSheet("images/waterfall_small.png", 1, 1, 15000);
//		flagStill.loadAnimationFromSheet("images/trampoline no jump.png", 1, 1, 15000);
		
//		snowing = new Animation();
//		snowing.loadAnimationFromSheet("images/snow.png", 1, 1, 15000);
//		snow = new Sprite(snowing);
//		snow.setPosition(0, 0);
//		snow_copy = new Sprite(snowing);
//		snow_copy.setPosition(snow.getX(), snow.getY() + snow.getHeight());
		tramp_jump = new Animation();
		tramp_jump.loadAnimationFromSheet("images/trampoline jump.png", 8, 1, 150);
		
		//Animation for trampoline when not touched by another sprite
		tramp_noJump = new Animation();
		tramp_noJump.loadAnimationFromSheet("images/trampoline no jump.png", 1, 1, 15000);
		
		//Load all player animations
		loadPlayerAnimations();
		//Load all instructions
		loadInstructions();
		
		if (level == 1) loadLevel1(); 		//If level is 1, load level 1
		else if (level == 2) loadLevel2();	//If level is 2, load level 2
			
		objectHeld = new Sprite(tramp_noJump);
		copy_objectHeld = new Sprite(tramp_noJump);
	
		bar_empty = new Animation();
		bar_full = new Animation();
		bar_empty.addFrame(loadImage("images/greenbar empty.png"), 250);
		bar_full.addFrame(loadImage("images/greenbar full.png"), 250);
		moveable_object_bar = new Sprite(bar_full);
		moveable_object_bar.setScale(0.35f);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		initialiseGame();

		System.out.println(tmap);
	}


	/**
	 * Initialise the player animations, and assign one to player sprite
	 */
	private void loadPlayerAnimations() {
		
		jumpingR = new Animation(); //Animation : when the player jumps right
		jumpingL = new Animation(); //Animation : when the player jumps left
		
		landingR = new Animation(); //Animation : when the player walks right, or faces right
		landingL = new Animation(); //Animation : when the player walks left, or faces left
		
		fallR = new Animation();	//Animation : when the player falls and faces right
		fallL = new Animation();	//Animation : when the player falls and faces left
		
		hitPlayer = new Animation(); //Animation : when the player is hit and invulnerable from new hits

		//Load Jumping animations
		jumpingR.loadAnimationFromSheet("images/Jump (32x32).png", 1, 1, 200);
		jumpingL.loadAnimationFromSheet("images/jump left.png", 1, 1, 200);
		
		//Load running animations
		landingL.loadAnimationFromSheet("images/Run Left.png", 12, 1, 200);
		landingR.loadAnimationFromSheet("images/Run (32x32).png", 12, 1, 200);
		
		//Load falling animations
		fallR.loadAnimationFromSheet("images/Fall (32x32).png", 1, 1, 200);
		fallL.loadAnimationFromSheet("images/fall left.png", 1, 1, 200);
		
		//Load hit/invulnerability animation
		hitPlayer.loadAnimationFromSheet("images/Hit (32x32).png", 7, 1, 200);

		// Initialise the player with an animation
		player = new Sprite(landingR);
		
	}

	private void substituteTiles(TileMap tmap) {
		for (int col = 0; col < columns; col++)
		{
			for (int row = 0; row < rows; row++)
			{
				Tile tempT = tmap.getTile(col, row);
				char ch = tempT.getCharacter();
				String st = String.valueOf(ch);
				ch = '.';
				if (st.equals("C")) { //create coin
					createCoins(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("a")) { //create ant enemy
					createAnts(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("t")) { //create trampoline
					createTrampolines(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("g")) { //create aligator
					createGators(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("f")) { //create flag
					createFlags(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("w")) { //create waterfall
					createWaterfalls(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("A")) { //create apple power up (trampoline)
					createTrampPUs(tempT);
					tempT.setCharacter(ch);
				}
				else if (st.equals("M")) { //create melon power up (waterfall)
					createWaterfallPUs(tempT);
					tempT.setCharacter(ch);
				}
			}
		}
		
	}
	/**
	 * Remove all coins, and add them to the coins taken
	 */
	public void takeAllCoins() {
		for (Sprite coin: coins) {
		coin.hide();
		}
		coinsTaken = coinsTotal;
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
		player.setX(11 * 32);
		player.setY(2 * 32);
		
//		player.setX(11 * 32);
//		player.setY(25 * 32);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();

	}

	public void restartGame()
	{
		total = 0;
		flagchanged = false;
		
		player.setX(11 * 32);
		player.setY(2 * 32);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();
		
		if (level == 1) loadLevel1();
		if (level == 2) loadLevel2();
		
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
		// If relative, adjust the offset so that
		// it is relative to the player
		int xo = (int) (screenWidth/2 - player.getX() - player.getWidth()/2);
		int yo = (int) (screenHeight/2 - player.getY() - player.getHeight()/2);
		xo = Math.min(xo, 0);
		xo = Math.max(screenWidth - tmap.getPixelWidth(), xo);
		yo = Math.min(yo, 0);
		yo = Math.max(screenHeight - tmap.getPixelHeight(), yo);

		//        tmap.draw(g, 15, 1);
		// ...?

		g.setColor(Color.blue);
		g.fillRect(0, 0, getWidth(), getHeight());
//		background.get(0).draw(g); //Sky 
		// Apply offsets to sprites then draw them
		
		for (int i = 0; i < background.size(); i++) {
			background.get(i).setOffsets(xo, yo);
			background.get(i).draw(g);		
			background_copy.get(i).setOffsets(xo, yo);
			background_copy.get(i).draw(g);
//			if (i == 1) snow.draw(g);
		}
		for (Sprite powerup: powerups_trampolines) {
			powerup.setOffsets(xo, yo);
			powerup.draw(g);
		}
		for (Sprite powerup: powerups_waterfalls) {
			powerup.setOffsets(xo, yo);
			powerup.draw(g);
		}
		
		setPlayerAnimation();
		
		for (Sprite ant: enemies_ants) {
			ant.setOffsets(xo,yo);
			ant.draw(g);
		}//end for loop enemies_ants
		for (Sprite gator: enemies_gators) {
			gator.setOffsets(xo, yo);
			gator.draw(g);
		}//end for loop enemies_gators
		for (Sprite dead: enemies_dead) {
			dead.setOffsets(xo, yo);
			dead.draw(g);
		}//end for loop enemies_dead
		
		for (Sprite aTramp: tramps)
		{
			aTramp.setOffsets(xo,yo);
			aTramp.drawTransformed(g);
		}//end for loop tramps
		for (Sprite flag: flags)
		{
			flag.setOffsets(xo,yo);
			flag.drawTransformed(g);
		}//end for loop flags
		for (Sprite w: waterfalls)
		{
			w.setOffsets(xo,yo);
			w.drawTransformed(g);
		}//end for loop waterfalls
		for (Sprite aCoin: coins)
		{
			aCoin.setOffsets(xo,yo);
			aCoin.drawTransformed(g);
		}//end for loop coins
		// Apply offsets to tile map and draw  it
		tmap.draw(g,xo,yo);   

		// Apply offsets to player and draw 
		moveable_object_bar.setOffsets(xo, yo);
		moveable_object_bar.drawTransformed(g);
		player.setOffsets(xo, yo);
		player.draw(g);

		if (hIsPressed) { 
			player.drawBoundingBox(g);
			for(Sprite gator: enemies_gators) {
				gator.drawBoundingBox(g);
			}//end for loop gators
			for (Sprite ant: enemies_ants) {
				ant.drawBoundingBox(g);
			}//end for loop ants
		}//end if statement


		// Show score and status information
		String msg = String.format("Score: %d", total/100);
		
		g.setFont(font);
		g.setColor(Color.darkGray);
		g.drawString(msg, getWidth() - 120, 50);
		
		String msgCoin = String.format("Take all coins: " + coinsTaken +"/" + coinsTotal );
		g.drawString(msgCoin, getWidth() - 300, 50);
		
		
		String msgTest = String.format("Collect all coins to go to the next level!");
		g.setColor(Color.red);
		g.drawString(msgTest,  50 + xo, 100 + yo);
		
		String msgInstructions = String.format("Press \"L\" to see all commands of the game!");
		g.drawString(msgInstructions,  600 + xo, 200 + yo);
		
		String firstObject = String.format("A trampoline would help you skip a part of the level, if you only had an apple that gave you one...");
		g.drawString(firstObject,  55 * 32 + xo, 18 * 32 + yo);
		
		String secondObject = String.format("It would be nice to place a waterfall on the let to reach those coins and the flag.");
		g.drawString(secondObject,  10 * 32 + xo,  23 * 32 + yo);
		
		String finalHelp = String.format("Press F to cheat and gather all coins!");
		g.drawString(finalHelp,  3 * 32 + xo,  18 * 32 + yo);
		
		for (int i = 0; i < lives; i++) {
			Sprite heartSprite = new Sprite(heart);
			heartSprite.setX(50 + i * 30);
			heartSprite.setY(40);
			heartSprite.draw(g);
		}
		if (editingMode && holdingObject) {
			objectHeld.setOffsets(xo, yo);
			objectHeld.draw(g);
			Color myColor = new Color(255, 0, 0, 65);
			if (!checkSimpleTileCollision(tmap, objectHeld)) {
			g.setColor(myColor);
			g.fillRect((int) objectHeld.getX() + xo, (int) objectHeld.getY() + yo, objectHeld.getWidth(), objectHeld.getHeight());
			}
		}
		if (holdingObject && !editingMode) {
		String msgObj = String.format("Press E to use this object: ");
		g.setFont(font);
		g.setColor(Color.darkGray);
		g.drawString(msgObj, screenWidth / 2 - 250, 50);
		
		copy_objectHeld.setX(screenWidth / 2 - 30);
		copy_objectHeld.setY(30);
		copy_objectHeld.draw(g);
		}
		else if (holdingObject && editingMode) {
			String msgObj = String.format("Drag your mouse to move the object: ");
			String msgObj2 = String.format("Spacebar to place it! ");
			g.setFont(font);
			g.setColor(Color.darkGray);
			g.drawString(msgObj, screenWidth / 2 - 350, 50);
			g.drawString(msgObj2, screenWidth / 2 - 300, 70);
			
			copy_objectHeld.setX(screenWidth / 2 - 30);
			copy_objectHeld.setY(30);
			copy_objectHeld.draw(g);
		}
		else {
			String msgObj = String.format("Collect new objects to place them in the map!");
			g.setFont(font);
			g.setColor(Color.red);
			g.drawString(msgObj, screenWidth / 2 - 300, 50);
		}
	
		if (instructionsDisplayed)
			showInstructions(g);
	}

	private void showInstructions(Graphics2D g) {
		g.setColor(Color.red);
		g.drawRect(screenWidth/2 - 300, screenHeight/2 - 300, 600, 600);
		g.fillRect(screenWidth/2 - 300, screenHeight/2 - 300, 600, 600);
		
		for (int i = 0; i < commands.size(); i++) {
		g.setFont(font);
		g.setColor(Color.yellow);
		g.drawString(commands.get(i), screenWidth / 2 - 290 , screenHeight/2 - 250 + i * 30);
		}
	}

	private void loadInstructions() {
		String c_L = "Press L to leave or enter the instructions area";
		String c_howtoplay = ("These instructions are for movements, and object placing around the map!");
		String c_UP = ("Use the arrow keys to move around the map");
		String c_E = "Press E when holding an object to be able to place it around the map";
		String c_MOUSE ="Drag the mouse to the position where you would like to place the object";
		String c_SPACE = "Finalise the object position by pressince space. This cannot be undone!";
		String c_M = "Press M to mute/unmute all sounds";
		String c_R = "Press R to restart the level";
		String c_empty ="";
		String c_cheats ="These are the cheating commands:";
		String c_C = "Press C to toggle cheating mode on and off, no damage, infinite jumps!";
		String c_H = "Press H to see all the drawing boxes";
		String c_1 = "Press 1 to switch to level 1";
		String c_2 = "Press 2 to switch to level 2";
		String c_T = "Press T to give yourself a brand new trampoline object";
		String c_W = "Press W to give yourself a brand new waterfall object";
		String c_F = "Forgot to take tall coins? Press F!";
		//Add commands to the list
		commands.add(c_L);
		commands.add(c_empty);
		commands.add(c_howtoplay);
		commands.add(c_UP);
		commands.add(c_E);
		commands.add(c_MOUSE);
		commands.add(c_SPACE);
		commands.add(c_M);
		commands.add(c_R);
		
		commands.add(c_empty);
		
		commands.add(c_cheats);
		commands.add(c_C);
		commands.add(c_H);
		commands.add(c_1);
		commands.add(c_2);
		commands.add(c_T);
		commands.add(c_W);
		commands.add(c_F);
		
	}

	/**
	 * Update any sprites and check for collisions
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of elapsed
	 */    
	public void update(long elapsed)
	{
		if (restarting) {
			restarting = false;
			restartGame();
		}
		copy_objectHeld.update(elapsed);
		if (coinsTaken == coinsTotal && flagchanged == false) {
			for (Sprite flag: flags) {
				Animation anim = new Animation();
				anim.loadAnimationFromSheet("images/Checkpoint (Flag Idle)(64x64).png", 10, 1, 100);
				flag.setAnimation(anim);
			}
			flagchanged = true;
		}
		
		if (previousLevel != level) {
			switch (level) {
			case 1: loadLevel1(); break;
			case 2: loadLevel2(); break;
			}
			previousLevel = level;
		}
//		if (debugMode) System.out.println("X:" + player.getX() + "Y" + player.getY());
		// Make adjustments to the speed of the sprite due to gravity

		if (hurt_invincibility) {
			hurt_timer += elapsed;
			if (hurt_timer > 2500)
				hurt_invincibility = false;
		}
		total += elapsed;
		player.setVelocityY(player.getVelocityY()+(gravity * elapsed));
		if (cheatingOn) {hasJump = true; lives = 3;}
		
		//Player movements:
		//If pressing jump button, and it has a jump
		if (jump && hasJump) {
			hasJump = false;
			player.setVelocityY( -0.2f);
			if (soundsOn) { //If unmuted
//				SoundFilter s = new SoundFilter("sounds/jump1.wav");
				Sound s = new Sound("sounds/jump1.wav");
				s.start();
			}
		}
		
		if (moveRight) //If pressing right button
			player.setVelocityX(xSpeed);
		else if (moveLeft) //If pressing left button
			player.setVelocityX(-xSpeed);
		else  //If not right nor left
			player.setVelocityX(0);

		//Sprite lists:
		//For each waterfall sprite
		for (Sprite w: waterfalls) {
			if (boundingBoxCollision(w, player)){ //If the two sprites are touching
				if (jump) //If the player is jumping in the water, make it go slightly upwards
					player.setVelocityY(player.getVelocityY() - 0.01f);
				else player.setVelocityY(player.getVelocityY()+(gravity * elapsed)); //Apply gravity again, the "force of the waterfall" makes you go down faster
				if (player.getVelocityY() < - MAX_VELOCITY_WATER) //Cap velocity
					player.setVelocityY(- MAX_VELOCITY_WATER);
			}//end bounding box if
		}//end for loop
		
		//If the animation is over, hide it
		for (Sprite deadEnemy: enemies_dead) {
			if (deadEnemy.getAnimation().hasLooped()) {
				deadEnemy.hide();
			}
		}
		
		//Make coins disappear when player touches them
		for (Sprite aCoin: coins) {
			if(boundingBoxCollision(player, aCoin) && aCoin.isVisible()) { //If the player touches a coin
				aCoin.hide(); //remove the coin
				coinsTaken++; //add a coin to the taken ones
				if (soundsOn) { //if not muted
					Sound s = new Sound("sounds/coin1.wav"); //load sound
					s.start();	//play sound
				}
			}
		}
		for (Sprite flag: flags) {
			if (boundingBoxCollision(flag, player)) {
				if (coinsTotal == coinsTaken) {
					if (level == 1) level = 2;
					else if (level == 2) level = 1;
					restartLevel();
					break;
				}
			}
			flag.update(elapsed);
		}
		//For each tramp Sprite
		Iterator<Sprite> itr2 = tramps.iterator();            
		while(itr2.hasNext()){
			Sprite tramp = itr2.next();
			if(boundingBoxCollision(player, tramp)) { //If the player touched the trampoline
				Animation a = new Animation();
				a.loadAnimationFromSheet("images/trampoline jump.png", 8, 1, 150);
				a.setHasLooped(false); //set the animation to not looped
				tramp.setAnimation(a); //set new animation
				if (-player.getVelocityY() < - MIN_VELOCITY_TRAMP_PLAYER) //If player velocityY is high
					player.setVelocityY(-player.getVelocityY() * 0.9f); //Take momentum and give it back on the Y axis with a friction variable
				else 
					player.setVelocityY(- MIN_VELOCITY_TRAMP_PLAYER); //If player velocity is low, give it a small jump upwards
				hasJump = false; //No jump after a trampoline jump
				if (soundsOn) { //if not muted
					Sound s = new Sound("sounds/tramp1.wav"); //load sound
					s.start(); //play sound
				}
			}
			//Check whether ants are on a trampoline
			for (Sprite ant: enemies_ants) {
				if (ant.isVisible()) {
					if(boundingBoxCollision(ant, tramp)) { //If an ant is on a trampoline
						tramp_jump.setHasLooped(false); //set the animation to not looped
						tramp.setAnimation(tramp_jump); //set new animation
						ant.setVelocityY(-0.18f); //Set ant velocityY
						if (soundsOn && isSpriteOnScreen(tramp)) { //If not muted and sprite on screen
							Sound s = new Sound("sounds/tramp1.wav"); //load sound
							s.start(); //play sound
							
						}
					}
				}
			}
			if (tramp.getAnimation().hasLooped())
				tramp.setAnimation(tramp_noJump);
		}
		
		updateAnts(elapsed);
		updateGators(elapsed);
		//for each powerup
		for (Sprite p: powerups_waterfalls) {
			if (boundingBoxCollision(player, p) && p.isVisible()) {
				setWaterfallObject(); //give player a waterfall object
				if (soundsOn) {//if not muted
					Sound s = new Sound("sounds/pu2.wav"); //load sound
					s.start();	//play sound
				}
				p.hide(); //Hide it
			}
			p.update(elapsed); //update
		}
		//for each powerup
		for (Sprite p: powerups_trampolines) {
			if (boundingBoxCollision(player, p) && p.isVisible()) {
				setTrampolineObject();//give player a trampoline object
				if (soundsOn) { //if not muted
					Sound powerup_sound = new Sound("sounds/pu2.wav");  //load sound
					powerup_sound.start();	//play sound
				}
				p.hide(); //hide it
			}
			p.update(elapsed); //update
		}
		for (Sprite w: waterfalls)
			w.update(elapsed);
		for (Sprite coin: coins) 
			coin.update(elapsed);
		for (Sprite dead: enemies_dead)
			dead.update(elapsed);
		for (Sprite tramp: tramps)
			tramp.update(elapsed);
		for (Sprite powerup: powerups_trampolines)
			powerup.update(elapsed);
		for (Sprite powerup: powerups_waterfalls)
			powerup.update(elapsed);
		
		setPlayerAnimation();
		//Set Player animations
		player.update(elapsed);
		// Then check for any collisions that may have occurred
		handleScreenEdge(player, tmap, elapsed);
		checkTileCollision(player, tmap);
		moveable_object_bar.setX(player.getX() - 5);
		moveable_object_bar.setY(player.getY() - 15);
		
		if (objectType == "waterfall") {
			copy_objectHeld.setAnimation(small_waterfall);
			moveable_object_bar.setAnimation(bar_full);
		}
		else if (objectType == "tramp") {
			copy_objectHeld.setAnimation(tramp_noJump);
			moveable_object_bar.setAnimation(bar_full);
		}
		else moveable_object_bar.setAnimation(bar_empty);
		
		//Move background, faster for closer layers, slower for further layers
		int extraOffSet = 0;
		for (int i = 0; i < background.size(); i++) {
				//if player is in background, (therefore not in background_copy) 
				if (player.getX() > background.get(i).getX() && player.getX() < background.get(i).getX() + background.get(i).getWidth()) {
//					if (debugMode) System.out.println("a " + background_copy.get(i).getWidth() +  " b " +  background.get(i).getWidth());
					if (player.getX() - screenWidth / 2 - extraOffSet < background.get(i).getX()) {
						background_copy.get(i).setX(background.get(i).getX() - (float) (background.get(i).getWidth()));
					}
					//Draw copy Right
					else if (player.getX() + screenWidth / 2 + extraOffSet > background.get(i).getX() + background.get(i).getWidth())
						background_copy.get(i).setX(background.get(i).getX() + (float) (background.get(i).getWidth()));
//					if (debugMode) System.out.println("getx " + player.getX() + "background.get(i).getX()" + background.get(i).getX() + " xo" + xo);
				}
				//If the player is in a copied background
				else if (player.getX() > background_copy.get(i).getX() && player.getX() < background_copy.get(i).getX() + background_copy.get(i).getWidth()) {
					//If the player is almost at the beginning of the sprite background, make a copy left
					if (player.getX() - screenWidth / 2 - extraOffSet < background_copy.get(i).getX()) {
						background.get(i).setX(background_copy.get(i).getX() -  (background_copy.get(i).getWidth()));
					}
					//If the player is almost at the end of the sprite background, make a copy right
					else if (player.getX() + screenWidth / 2 + extraOffSet > background_copy.get(i).getX() + background.get(i).getWidth())
						background.get(i).setX(background_copy.get(i).getX() +  (background_copy.get(i).getWidth()));
			}
				if (level ==1) {
			background.get(i).setVelocityX(-player.getVelocityX() * (i * i * i) * 0.005f);	
			background.get(i).update(elapsed);
			background_copy.get(i).setVelocityX(-player.getVelocityX() * (i * i * i ) * 0.005f); //try 0.05
			background_copy.get(i).update(elapsed);
				}
				else { 
					// For level 2 it's better to increase the i value to make the background go faster, otherwise with a cubic function,
					// and small values it would look too stationary
					int j = i+2;
					background.get(i).setVelocityX(-player.getVelocityX() * (j * j * j) * 0.005f);	
					background.get(i).update(elapsed);
					background_copy.get(i).setVelocityX(-player.getVelocityX() * (j * j * j) * 0.005f); //try 0.05
					background_copy.get(i).update(elapsed);
				}
		}
		
		//Tried to make snow work but it didn't really work that great
//		float f = Math.max(-player.getVelocityY() + 0.09f, 0.09f);
//		if (player.getY() > snow.getY() && player.getY() < snow_copy.getY() + snow_copy.getWidth()) {
//			
//		}
//		snow.setVelocityY(f);
//		snow.update(elapsed);
//		snow_copy.setVelocityY(f);
//		snow_copy.update(elapsed);
		
		
	}
	
	//Set player animation to the correct one
	private void setPlayerAnimation() {
		if (hurt_invincibility) {
			player.setAnimation(hitPlayer);
		}
		else if ((jump || player.getVelocityY() < 0) && (moveRight || facingRight)) {
			player.setAnimation(jumpingR);
		}
		else if (Math.abs(player.getVelocityY()) < 0.01f && (moveRight || facingRight)) {
			player.setAnimation(landingR);
		}
		else if (Math.abs(player.getVelocityY()) < 0.01f && (moveLeft || !facingRight)) {
			player.setAnimation(landingL);
		}  
		else if ((jump || player.getVelocityY() < 0) && (moveLeft || !facingRight)) {
			player.setAnimation(jumpingL);
		}
		else if (player.getVelocityY() > 0.03f && (moveRight || facingRight)) {
			player.setAnimation(fallR);
		}
		else if (player.getVelocityY() > 0.05f && (moveLeft || !facingRight)) {
			player.setAnimation(fallL);
		}
	}

	/**
	 * Update any flying alligator in the emies_gators list
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of elapsed
	 */   
	private void updateGators(long elapsed) {
		for (Sprite gator: enemies_gators) {
			if (gator.isVisible()) {
				if (gator.getVelocityY() == 0 ) //If hit ground
					gator.setVelocityY(-0.05f); // go up
//				if (gator.getX() > player.getX()) {
//					String fileNameL = "images/gator spritesheet left.png";
//					if (!gator.getAnimation().getFileName().equals(fileNameL)) {
//						Animation anim = new Animation();
//						anim.loadAnimationFromSheet(fileNameL, 4, 1, 200);
//						gator.setAnimation(anim);
//					}
//				}
//				else {
//					String fileNameR = "images/gator spritesheet right.png";
//					if (!gator.getAnimation().getFileName().equals(fileNameR)) {
//						Animation anim = new Animation();
//						anim.loadAnimationFromSheet(fileNameR, 4, 1, 200);
//						gator.setAnimation(anim);
//					}
//				}
				if(boundingBoxCollision(player, gator)) {
					if (isSpriteOnTop(player, gator)){ //If player jumped on the ant
						deadEnemy = new Animation(); //New animation
						deadEnemy.loadAnimationFromSheet("images/enemy death spritesheet.png", 4, 1, 300); //load animation
						deadEnemy.setLoop(false); //it should not loop
						Sprite dead_enemy = new Sprite(deadEnemy); //Temporary sprite
						dead_enemy.setPosition(gator.getX(), gator.getY()); //set x and y position to the killed ant x,y
						enemies_dead.add(dead_enemy); //add to the list of dead enemies
						player.setVelocityY(-0.2f); 
						gator.hide(); //hide gator
						if (soundsOn) {
							SoundFilter s = new SoundFilter("sounds/enemyhit1.wav");
							s.start();	//play sound
						}
					}
					else {
						if (!hurt_invincibility) { //If the player is not temporarily invincible
							removeLife(); //remove a life
							startInvincibility(); //start invincibility
						}
					}
				}
				if (gator.isVisible()) {
					gator.update(elapsed);
					checkTileCollision(gator, tmap);
				}
			}
		}

	}
	/**
	 * Update any ant sprite in the enemies_ants list
	 * 
	 * @param elapsed The elapsed time between this call and the previous call of elapsed
	 */  
	public void updateAnts(long elapsed) {
		for (Sprite anAnt: enemies_ants) {
			if (anAnt.isVisible()) { //If it is visible
				anAnt.setVelocityY(anAnt.getVelocityY() + (gravity * elapsed)); //apply gravity
				if (anAnt.getVelocityX() == 0 && anAnt.getX() < anAnt.getOldX()) { //If moving right, and hit a wall, turn left
					Animation anim = new Animation(); // New animation
					anim.loadAnimationFromSheet("images/ant spritesheet.png", 8, 1, 200); //load animation
					anAnt.setAnimation(anim); //set new animation
					anAnt.setVelocityX(- ANT_SPEED_X); // go left
				}
				else if(anAnt.getVelocityX() == 0) { //if moving left, and hit a wall, turn right
					Animation anim = new Animation();// New animation
					anim.loadAnimationFromSheet("images/ant right.png", 8, 1, 200);//load animation
					anAnt.setAnimation(anim);//set new animation
					anAnt.setVelocityX(ANT_SPEED_X); // go right
				}
				if(boundingBoxCollision(player, anAnt)) {
					if (isSpriteOnTop(player, anAnt)){ //If player jumped on the ant
						deadEnemy = new Animation(); //New animation
						deadEnemy.loadAnimationFromSheet("images/enemy death spritesheet.png", 4, 1, 300); //load animation
						deadEnemy.setLoop(false); //it should not loop
						Sprite dead_enemy = new Sprite(deadEnemy); //Temporary sprite
						dead_enemy.setPosition(anAnt.getX(), anAnt.getY()); //set x and y position to the killed ant x,y
						enemies_dead.add(dead_enemy); //add to the list of dead enemies
						player.setVelocityY(-0.2f); 
						anAnt.hide(); //remove ant from list
						if (soundsOn) {
//							Sound s = new Sound("sounds/enemyhit1.wav"); //load sound //load sound
							SoundFilter s = new SoundFilter("sounds/enemyhit1.wav");
							s.start();	//play sound
						}
					}
					else {
						if (!hurt_invincibility) { //If the player is not temporarily invincible
							removeLife(); //remove a life
							startInvincibility(); //start invincibility
						}
					}
				}
				if (anAnt.isVisible()) {
				anAnt.update(elapsed); //update position
				checkTileCollision(anAnt, tmap); //fix it if necessary
				}
			}
		}
	}
	/**
	 * Play sound with sound filter, start the hurt timer, making the player invincible
	 */   
	private void startInvincibility() {
		Sound s = new Sound("sounds/hit3.wav");
		s.start();
		hurt_invincibility = true;
		hurt_timer = 0;
		
	}

	/**
	 * Checks whether the straight line between the first pair of points crosses the line between the second pair of points,
	 * a graphical representation is in the report.
	 * 
	 * @param x1, x2	The x coordinates of the first pair of points
	 * @param x3, x4	The x coordinates of the second pair of points
	 * @param y1, y2	The y coordinates of the first pair of points
	 * @param y3, y4	The y coordinates of the second pair of points
	 */
	public boolean checkLines(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		float denominator = ((x2 - x1) * (y4 - y3)) - ((y2 - y1) * (x4 - x3));
		float numerator1 = ((y1 - y3) * (x4 - x3)) - ((x1 - x3) * (y4 - y3));
		float numerator2 = ((y1 - y3) * (x2 - x1)) - ((x1 - x3) * (y2 - y1));
		
		if (denominator == 0) 
			return (numerator1 == 0 && numerator2 == 0);
		
		float r = numerator1 / denominator;
		float s = numerator2 / denominator;
		return ((r >= 0 && r <= 1) && (s >= 0 && s <= 1));
			
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


	/**
	 * Checks and handles collisions between two sprites
	 * 
	 * @param s1, s2	The Sprites that are considered for the collision detection
	 */
	public boolean boundingBoxCollision(Sprite s1, Sprite s2)
	{
		return ((s1.getX() + s1.getImage().getWidth(null) > s2.getX()) &&
				(s1.getX() < (s2.getX() + s2.getImage().getWidth(null))) &&
				((s1.getY() + s1.getImage().getHeight(null) > s2.getY()) &&
						(s1.getY() < s2.getY() + s2.getImage().getHeight(null))));  	
	}

	/**
	 * Restart level
	 */
	public void restartLevel()
	{
		if (level == 1)
			loadLevel1();
		else if (level == 2)
			loadLevel2();
//		int mapWidth = tmap.getMapWidth(); //64 
		player.setX(64);
		player.setY(80);
		lives = 3;
		hurt_invincibility = false;
	}
	
	
	/**
	 * Check and handles collisions with a tile map for the
	 * given sprite 's'. Initial functionality is limited...
	 * 
	 * @param s			The Sprite to check collisions for
	 * @param tmap		The tile map to check 
	 */
	public void checkTileCollision(Sprite s, TileMap tmap)
	{
		// Take a note of a sprite's current position
		int sx = (int)s.getX();
		int sy = (int)s.getY();
		
		//set that all 4 corners do not collide
		boolean tl = false;
		boolean bl = false;
		boolean tr = false;
		boolean br = false;
		
		int sxright = (int) (s.getX() + s.getWidth());
		int sybottom =(int) (s.getY() + s.getHeight());
		// Find out how wide and how tall a tile is
		
		//Take the tile name for each corner
		char topleft = tmap.getTileChar((int) (sx / TILE_WIDTH), (int) (sy / TILE_HEIGTH));
		char bottomleft = tmap.getTileChar((int) (sx / TILE_WIDTH), (int) ((sy + s.getHeight()) / TILE_HEIGTH));
		char topright = tmap.getTileChar((int) ((sx + s.getWidth()) / TILE_WIDTH), (int) (sy / TILE_HEIGTH));
		char bottomright = tmap.getTileChar((int) ((sx + s.getWidth()) / TILE_WIDTH), (int) ((sy + s.getHeight()) / TILE_HEIGTH));
		
		//If all tiles are touching an "air block", return
		if (topleft == '.' && topright == '.' && bottomleft == '.'&& bottomright == '.') 
			return;
		
		//Check which corner is not in an "air block"
		if (bottomleft != '.')
			bl = true;
		if (bottomright != '.')
			br = true;
		if (topleft != '.')
			tl = true;
		if (topright != '.')
			tr = true;
		
		// In total we check 12 different scenarios, 4 with 3 corners having a collision, 4 with 2 corners having a collision
		// and 4 with 1 corner having a collision
		
		 // Check two "3 corners scenario" (BL-BR-TR; BL-BR-TL),
		//and one "two corners" scenario(BL-BR)
		if (bl && br ) {
			s.setVelocityY(0);
			s.setY(((sybottom / TILE_HEIGTH) * TILE_HEIGTH) - s.getHeight());
			if (isPlayer(s)) hasJump = true; //
			if (tl) { 
			s.setVelocityX(0);
			s.setX(s.getOldX());
//			s.setX(((sx / TILE_WIDTH) * TILE_WIDTH) +TILE_WIDTH);
			}
			else if (tr) {
			s.setVelocityX(0);
			s.setX(((sx / TILE_WIDTH) * TILE_WIDTH));
//			s.setX(s.getOldX());
			}
			return;
		}
		// Check two "3 corners scenario" (TL-TR-BL; TL-TR-BR),
		//and one "2 corners" scenario (TL-TR)
		if (tl && tr) { 
//			s.setY(s.getOldY());
			s.setY( (int) ((sy / TILE_HEIGTH) + 1) * TILE_HEIGTH );
			s.setVelocityY(0.05f);
			if (bl) {
				s.setVelocityX(0);
				//s.setX(((sx / TILE_WIDTH) * TILE_WIDTH) + s.getWidth());
				s.setX(s.getOldX());
			}
			else if (br) {
				s.setVelocityX(0);
				//s.setX(((sx / TILE_WIDTH) * TILE_WIDTH));
				s.setX(s.getOldX());
			}
			return;
		}
		//There are no more 3 corners scenarios

		//Check "2 corners scenario" (BL-TL)
		if (bl && tl) {
			s.setVelocityX(0);
			s.setX(((sx / TILE_WIDTH) * TILE_WIDTH ) + TILE_WIDTH);
			return;
		}
		//Check "2 corners scenario" (BR-TR)
		if (br && tr) { //Move right
			s.setVelocityX(0);
			s.setX(((sxright / TILE_WIDTH) * TILE_WIDTH) - s.getWidth());
			
//			s.setX(((sxright / TILE_WIDTH) * TILE_WIDTH) - s.getWidth());
			return;
		}
		//There are no more 2 corners scenarios
		
		//Take the coordinates of the tile the sprite is colliding with
		int tileXL = (sx / TILE_WIDTH) * TILE_WIDTH;
		int tileXR = tileXL + TILE_WIDTH;
		int tileYU = (sy / TILE_HEIGTH) * TILE_HEIGTH;
		int tileYB = tileYU + TILE_HEIGTH;
		//Check one corner scenario (BL)
		if (bl) {
			if (s.getVelocityY() >= 0 &&
					(checkLines(tileXL, tileYU, tileXR, tileYU, sx, sy, (int) s.getOldX(), (int) s.getOldY()))) {
				s.setVelocityY(0);
				s.setY(((sybottom / TILE_HEIGTH) * TILE_HEIGTH) - s.getHeight());
				if (isPlayer(s)) hasJump = true;
			}
			else{
				s.setVelocityX(0);
				s.setX(s.getOldX());

			}
		}
		
		//Check one corner scenario (BR)
		else if (br) {
//			If the sprite is going down, and the horizontal line between the two upper corners of the collision tile is crossed by
//			the line between the bottom right corners of the old position and the current position
			if (s.getVelocityY() >= 0 && 
//					(checkLines(tileXL, tileYU, tileXR, tileYU, sxright, sy, (int) s.getOldX() + s.getWidth(), (int) s.getOldY()))) {
					(checkLines(tileXL, tileYU, tileXR, tileYU, sx, sy, (int) s.getOldX(), (int) s.getOldY()))) {
				if(debugMode) System.out.println("Crossed up");
				s.setVelocityY(0);
				s.setY(((sybottom / TILE_HEIGTH) * TILE_HEIGTH) - s.getHeight());
				if (isPlayer(s)) hasJump = true;
			}
			else{
				if(debugMode) System.out.println("Crossed Left");
				s.setVelocityX(0);
//				s.setX(((sxright / TILE_WIDTH) * TILE_WIDTH) - s.getWidth());
				s.setX(s.getOldX());
			}
		}
		
		//Check one corner scenario (TR)
		else if (tr) {
			if (s.getVelocityY() <= 0 &&
					(checkLines(tileXL, tileYB, tileXR, tileYB, sx, sy, (int) s.getOldX(), (int) s.getOldY()))) {
				if(debugMode) System.out.println("Crossed Down");
				s.setVelocityY(0);
				s.setY(((sy / TILE_HEIGTH) * TILE_HEIGTH) + TILE_HEIGTH);
			}
			else{
				if(debugMode) System.out.println("Crossed Left");
				s.setVelocityX(0);
//				s.setX(((sxright / TILE_WIDTH) * TILE_WIDTH) - s.getWidth());
				s.setX(s.getOldX());
			}
		}
		
		//Check one corner scenario (TL)
		else if (tl) {
			if (s.getVelocityY() <= 0 &&
					(checkLines(tileXL, tileYB, tileXR, tileYB, s.getX(), s.getY(), (int) s.getOldX(), (int) s.getOldY()))) {
				if(debugMode) System.out.println("Crossed Down");
				s.setVelocityY(0);
				s.setY(((sy / TILE_HEIGTH) * TILE_HEIGTH) + TILE_HEIGTH);
			}
			else{
				if(debugMode) System.out.println("Crossed Right");
				s.setVelocityX(0);
				s.setX(((sx / TILE_WIDTH) * TILE_WIDTH) + TILE_WIDTH);
			}
		}
		
	} //End method checkTileCollision

	
	/**
	 * Create an ant animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createAnts(Tile tempT) {
		Sprite ant;

		antR = new Animation();
		antR.loadAnimationFromSheet("images/ant right.png", 8, 1, 600);

		ant = new Sprite(antR);
		ant.setX(tempT.getXC());
		ant.setY(tempT.getYC());
		ant.setVelocity(0, 0);
		ant.show();
		ant.setVelocityX(0.05f);
		enemies_ants.add(ant);

	}
	/**
	 * Create a trampoline power up animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createTrampPUs(Tile tempT) {
		Sprite s;
		
		Animation anim = new Animation();
		anim.loadAnimationFromSheet("images/Apple.png", 17, 1, 100);

		s = new Sprite(anim);
		s.setX(tempT.getXC());
		s.setY(tempT.getYC());
		s.setVelocity(0, 0);
		s.show();
		powerups_trampolines.add(s);
	}
	/**
	 * Create a waterfall power up animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createWaterfallPUs(Tile tempT) {
		Sprite s;
		
		Animation anim = new Animation(); //New animation
		anim.loadAnimationFromSheet("images/Melon.png", 17, 1, 100); //load animation

		s = new Sprite(anim);
		s.setX(tempT.getXC());
		s.setY(tempT.getYC());
		s.setVelocity(0, 0);
		s.show();
		powerups_waterfalls.add(s);
	}
	/**
	 * Create a flag animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createFlags(Tile tempT) {
		Sprite flag = new Sprite(flagStill);
		flag.setX(tempT.getXC());
		flag.setY(tempT.getYC());
		flag.setVelocity(0, 0);
		flag.show();
		flags.add(flag);
	}
	
	/**
	 * Create a coin animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createCoins(Tile tempT) {
		Sprite coin;
		// Load a single coin animation

		c = new Animation();
		c.loadAnimationFromSheet("images/coins.png", 1, 4, 200);
		coin = new Sprite(c);
		coin.setX(tempT.getXC());
		coin.setY(tempT.getYC());
		coin.setVelocity(0, 0);
		coin.show();
		coin.setScale(0.5f);
		coins.add(coin);
		coinsTotal ++;
	}
	
	
	/**
	 * Create a trampoline animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createTrampolines(Tile tempT) {
		tramp = new Sprite(tramp_noJump);	
		tramp.setX(tempT.getXC());
		tramp.setY(tempT.getYC());
		tramp.setVelocity(0, 0);
		tramp.show();
		tramps.add(tramp);
	}
	/**
	 * Create a trampoline animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createWaterfalls(Tile tempT) {
		Sprite w;
		Animation anim = new Animation();
		anim.loadAnimationFromSheet("images/waterfall spritesheet.png", 4, 1, waterAnimSpeed);
		w = new Sprite(anim);	
		w.setX(tempT.getXC());
		w.setY(tempT.getYC());
		w.setVelocity(0, 0);
		w.show();
		waterfalls.add(w);
	}
	/**
	 * Create a gator animation and sprite, add it to the list
	 * 
	 * @param tmap		The tile to get the position from
	 */
	private void createGators(Tile tempT) {
		Animation gatorRight = new Animation();
		gatorRight.loadAnimationFromSheet("images/gator spritesheet left.png", 4, 1, 300);
		Sprite gator = new Sprite(gatorRight);
		gator.setX(tempT.getXC());
		gator.setY(tempT.getYC());
		gator.setVelocity(0, 0);
		gator.show();
		enemies_gators.add(gator);
	}
	
	/**
	 * Create background for the second level
	 */
	private void createBackgroundLvl1() {
		// Create a set of background sprites that we can 
		// rearrange to give the illusion of motion
		for (int i = 0; i < LAYERS_LVL1; i ++) {
			Animation tempAnim = new Animation();
			String image = String.format("images/layer%d.png", i+1);
			tempAnim.addFrame(loadImage(image), 5000);
			Sprite tempS = new Sprite(tempAnim);
			tempS.show();
			tempS.setPosition(0, 0);
//			System.out.println(tempS.getWidth());
			tempS.setY((int)((rows * 32) - tempS.getHeight()));
//			System.out.println(tempS.getWidth());
			background.add(tempS);
//			
//			if ( i == 4 || i == 6) {
			Sprite nextS = new Sprite(tempAnim);
			nextS.show();
			nextS.setPosition((float)(tempS.getWidth()), tempS.getY());
//			System.out.println(nextS.getX());
			background_copy.add(nextS);
//			}	
		}
	}
	
	/**
	 * Create a trampoline animation and sprite, add it to the list
	 * 
	 * @param xo, yo		x and y position to set the new position of the new trampoline
	 */
	public void addTrampoline(float xo, float yo) {
		tramp = new Sprite(tramp_noJump);	
		tramp.setX(xo);
		tramp.setY(yo);
		tramp.setVelocity(0, 0);
		tramp.show();
		tramps.add(tramp);
	}
	
	/**
	 * Create a waterfall animation and sprite, add it to the list
	 * 
	 * @param xo, yo		x and y position to set the new position of the new waterfall
	 */
	public void addWaterfall(float xo, float yo) {
		Sprite w;
		Animation anim = new Animation();
		anim.loadAnimationFromSheet("images/waterfall spritesheet.png", 4, 1, waterAnimSpeed);
		objectHeld.setAnimation(anim);
		Animation anim2 = new Animation();
		anim2.loadAnimationFromSheet("images/waterfall spritesheet.png", 4, 1, waterAnimSpeed);
		copy_objectHeld.setAnimation(anim2);
		w = new Sprite(anim);	
		w.setX(xo);
		w.setY(yo);
		w.setVelocity(0, 0);
		w.show();
		waterfalls.add(w);
	}
	
	
	private void createBackgroundLvl2() {
		// Create a set of background sprites that we can 
		// rearrange to give the illusion of motion
		for (int i = 0; i < LAYERS_LVL2; i ++) {
			Animation tempAnim = new Animation();
			String image = String.format("images/layerlvl2_%d.png", i+1);
			tempAnim.addFrame(loadImage(image), 5000);
			Sprite tempS = new Sprite(tempAnim);
			tempS.show();
			tempS.setPosition(0, 0);
			tempS.setY((int)((rows * 32) - tempS.getHeight() ));
			background.add(tempS);
//			
			Sprite nextS = new Sprite(tempAnim);
			nextS.show();
			nextS.setPosition((float)(tempS.getWidth()), tempS.getY());
			background_copy.add(nextS);
//			}	
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


		if (key == KeyEvent.VK_ESCAPE) System.exit(1);

		if (key == KeyEvent.VK_UP) jump = true;

		if (key == KeyEvent.VK_RIGHT) {moveRight = true; moveLeft = false; facingRight = true;}

		if (key == KeyEvent.VK_LEFT) {moveLeft = true; moveRight = false; facingRight = false;}
		
		if (key == KeyEvent.VK_C) { cheatingOn = ! cheatingOn;}

		if (key == KeyEvent.VK_H) hIsPressed = !hIsPressed;
		
		if (key == KeyEvent.VK_D) debugMode = !debugMode;
		
		if (key == KeyEvent.VK_W) setWaterfallObject();
		
		if (key == KeyEvent.VK_T) setTrampolineObject();

		if (key == KeyEvent.VK_SPACE) { placeObject(); }
		
		if (key == KeyEvent.VK_E) { editingMode = !editingMode; }
		
		if (key == KeyEvent.VK_L) { instructionsDisplayed = !instructionsDisplayed; }
		
		if (key == KeyEvent.VK_M) { soundsOn = !soundsOn; }
		
		if (key == KeyEvent.VK_F) { takeAllCoins(); }
		
		if (key == KeyEvent.VK_1) { level = 1;}
		
		if (key == KeyEvent.VK_2) { level = 2;}
			
		if (key == KeyEvent.VK_R) restarting = true;
	}
	
	/**
	 * Load the map from the first level, clear all the objects, create new background,
	 * and substitute specific tiles with sprites
	 */
	private void loadLevel1() {
		coinsTaken = 0;
		coinsTotal = 0;
		flagchanged = false;
		holdingObject = false;
		objectType = "null";
		player.setX(11*32);
		player.setY(2 * 32);
		tmap.loadMap("maps", "map2.txt");
		resetLists();
		createBackgroundLvl1();
		substituteTiles(tmap);
	}
	/**
	 * Load the map from the second level, clear all the objects, create new background,
	 * and substitute specific tiles with sprites
	 */
	private void loadLevel2() {
		coinsTaken = 0;
		coinsTotal = 0;
		holdingObject = false;
		objectType = "null";
		flagchanged = false;
		player.setX(11*32);
		player.setY(2 * 32);
		tmap.loadMap("maps", "map.txt");
		resetLists();
		createBackgroundLvl2();
		substituteTiles(tmap);
	}
	/**
	 * Clear all the objects from all array lists
	 */
	private void resetLists() {
		//Clear all layers of the background
		background.clear();
		background_copy.clear();
		//Remove all coins
		coins.clear();
		//Remove all trampolines
		tramps.clear();
		
		//Remove enemies:
		//ants
		enemies_ants.clear();
		//Flying aligators
		enemies_gators.clear();
		//Dead enemeis
		enemies_dead.clear();
		//Powerups
		powerups_trampolines.clear();
		powerups_waterfalls.clear();
		flags.clear();
		waterfalls.clear();
	}
	
	/**
	 * It finalises the position of the object dragged from the user based on objectType
	 */
	private void placeObject() {
		//does the player have an object?
		if (holdingObject) {
			//Is the object in the air?
			if (checkSimpleTileCollision(tmap, objectHeld)) {
				switch (objectType) {
				case "null": {return;} 
				case "tramp": { addTrampoline(objectHeld.getX(), objectHeld.getY()); holdingObject = false; objectType = "null"; editingMode = false; return;} //Add trampoline
				case "waterfall": {addWaterfall(objectHeld.getX(), objectHeld.getY()); holdingObject = false; objectType = "null"; editingMode = false; return;} //Add waterfall
				}
			}
		}
	}
	
	/**
	 * The object held by the player is now a waterfall, change the objectHeld sprite animation
	 */
	private void setWaterfallObject() {
		
		holdingObject = true; //The player has an object
		objectType = "waterfall"; //The type of object is a waterfall
		Animation anim = new Animation(); //Temporary animation
		anim.loadAnimationFromSheet("images/waterfall spritesheet.png", 4, 1, waterAnimSpeed); //Load animation from spritesheet
		objectHeld.setAnimation(anim); //Change object held animation to the new one created
	}
	
	/**
	 * The object held by the player is now a waterfall, change the objectHeld sprite animation
	 */
	private void setTrampolineObject() {
		
		holdingObject = true; //The player has an object
		objectType = "tramp"; //The type of object is a waterfall
		objectHeld.setAnimation(tramp_noJump); //Change object held animation to the new one created
	}
	/**
	 * Return true if the sprite is not touching any tile, false if it is touching at least one tile
	 */
	private boolean checkSimpleTileCollision(TileMap tmap, Sprite s) {
		int sx = (int)s.getX();
		int sy = (int)s.getY();
	
		// Find out how wide and how tall a tile is
		
		char topleft = tmap.getTileChar((int) (sx / TILE_WIDTH), (int) (sy / TILE_HEIGTH));
		char bottomleft = tmap.getTileChar((int) (sx / TILE_WIDTH), (int) ((sy + s.getHeight()) / TILE_HEIGTH));
		char topright = tmap.getTileChar((int) ((sx + s.getWidth()) / TILE_WIDTH), (int) (sy / TILE_HEIGTH));
		char bottomright = tmap.getTileChar((int) ((sx + s.getWidth()) / TILE_WIDTH), (int) ((sy + s.getHeight()) / TILE_HEIGTH));
		
		return (topleft == '.' && topright == '.' && bottomleft == '.'&& bottomright == '.');
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
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int offsetX = (int) (screenWidth/2 - player.getX() - player.getWidth() / 2);
		int offsetY = (int) (screenHeight / 2 - player.getY() - player.getHeight() / 2);
		offsetX = Math.min(offsetX, 0);
		offsetX = Math.max(screenWidth - tmap.getPixelWidth(), offsetX);
		offsetY = Math.min(offsetY, 0);
		offsetY = Math.max(screenHeight - tmap.getPixelHeight(), offsetY);
		mouseX = (int) (e.getX());
		mouseY = (int) (e.getY());
		
//		rock.setX(mouseX - offsetX - rock.getWidth() / 2);
//		rock.setY(mouseY - offsetY - rock.getHeight() / 2);
		objectHeld.setX(mouseX - offsetX - objectHeld.getWidth() / 2);
		objectHeld.setY(mouseY - offsetY - objectHeld.getHeight() / 2);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
	/**
	 * check whether the sprite is visible by the player
	 * 
	 * @param s The sprite it checks whether it is in the visible range of the player
	 */ 
	public boolean isSpriteOnScreen(Sprite s){
		int yo = (int) (screenHeight/2 - player.getY() - player.getHeight()/2);
		yo = Math.min(yo, 0);
		yo = Math.max(screenHeight - tmap.getPixelHeight(), yo);
		boolean onScreenX = false;
		boolean onScreenY = false;
		int soundLength = 400; //There would be too many sounds if we heard all sounds on screen
		if (player.getX() - s.getX() < soundLength && player.getX() - s.getX() > - soundLength) onScreenX = true; //Don't really want to hear sounds too far away
		if (yo == 0 && s.getY() < screenHeight) onScreenY = true;
		else if (yo == screenHeight - TILE_HEIGTH && s.getY() > screenHeight - TILE_HEIGTH) onScreenY = true;

		else if(s.getY() < player.getY()  + screenHeight/ 2  &&
				(s.getY() + s.getHeight()) > player.getY()  - screenHeight/ 2 ) 
		{onScreenY = true;}
		if (onScreenX && onScreenY) return true;
		return false;
//		return (s.getX() < player.getX()  +  player.getWidth() / 2 + screenWidth / 2 &&
//				(s.getX() + s.getWidth()) > player.getX()  + player.getWidth() / 2 - screenWidth / 2 );
	}
	
	/**
	 * it returns true if s1 is on top of s2, false otherwise
	 * 
	 * @param s1, s2 It checks whether s1 is on top of S2
	 */ 
	public boolean isSpriteOnTop(Sprite s1, Sprite s2) {
		return (s1.getOldY() + s1.getHeight() <= s2.getOldY() && s1.getVelocityY() > 0);
	}
	
	/**
	 * Check whether sprite s is the player
	 * 
	 * @param s the sprite we analyze
	 */ 
	public boolean isPlayer(Sprite s) {
		if (s.equals(player))
			return true;
		return false;
	}
	
	/**
	 * Add a life, maximum is three
	 */ 
	public void addLife() {
		lives++;
		
		if (lives > MAX_LIVES)
			lives = 3;
	}
	
	/**
	 * Remove a life, end game if no more lives available
	 */ 
	public void removeLife() {
		lives--; //Remove a life
		//If no more lives, restart level
		if (lives < MIN_LIVES) restartLevel();		
	}
}
