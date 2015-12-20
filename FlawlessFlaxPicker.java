package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.types.RSObject;
import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;
import org.tribot.api.Timing; 
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.Player;
import java.awt.RenderingHints;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Game;

// Paint Imports
import java.awt.Color; 
import java.awt.Font;
import java.awt.Graphics; 
import java.awt.Graphics2D; 
import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO; 

@ScriptManifest(authors = {"botsallday"}, category = "Money Making", name = "FlawlessFlaxPicker")

public class FlawlessFlaxPicker extends Script implements Painting {
    
    // set variables
	private ABCUtil abc = new ABCUtil();
    private final Image img = getImage("http://s15.postimg.org/izj9po1wr/Flawless_Flax_Picker.png");
    private final int FLAX_PLANT_ID = 7134;
    private static final long startTime = System.currentTimeMillis();
    private int flax_picked = 0;
    private RSObject target_flax;
    private boolean execute = true;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    private final int FLAX_FIELD_X_MAX = 2749;
    private final int FLAX_FIELD_X_MIN = 2739;
    private final int FLAX_FIELD_Y_MIN = 3439;
    private final int FLAX_FIELD_Y_MAX = 3449;
    private final int BANK_X_MAX = 2729;
    private final int BANK_X_MIN = 2722;
    private final int BANK_Y_MIN = 3490;
    private final int BANK_Y_MAX = 3493;
    
    public RSTile getTile(boolean use_bank) {
    	final int x;
    	final int y;
    	
    	if (use_bank) {
    		x = General.random(BANK_X_MIN, BANK_X_MAX);
        	y = General.random(BANK_Y_MIN, BANK_Y_MAX);
    	} else {
    		x = General.random(FLAX_FIELD_X_MIN, FLAX_FIELD_X_MAX);
        	y = General.random(FLAX_FIELD_Y_MIN, FLAX_FIELD_Y_MAX);
        	
    	}
    	
    	return new RSTile(x, y, 0);
    }

    public void run() {
    	General.useAntiBanCompliance(true);
    	
        while(execute) {
            State state = state();
            if (state != null) {
                switch (state) {
                    case WALK_TO_FLAX_FIELD:
                    	log("Walking to flax field");
                        walk(false);
                        break;
                    case WALK_TO_BANK:
                    	log("Walking to bank");
                        walk(true);
                        break;
                    case DEPOSIT_ITEMS:
                    	log("Depositing items in bank");
                        handleBanking();
                        break;
                    case PICK_FLAX:
                    	log("Picking flax");
                        pickFlax(target_flax);
                        break;
                    case WALKING:
                    	log("Walking...");
                    	handleWait();
                    	break;
                    case SOMETHING_WENT_WRONG:
                    	log("Stopping script, something went wrong");
                    	execute = false;
                    	break;
                }
            }
            General.sleep(892,  2134);
        }
    }

    private State state() {
        // whether or not we need to bank is the variable that drives the script
        boolean need_to_bank = Inventory.isFull();
        
        if (need_to_bank && !Banking.isBankScreenOpen() && !Player.isMoving()) {
             if (Banking.isInBank()) {
                 return State.DEPOSIT_ITEMS;
             } else {
                 return State.WALK_TO_BANK;
             }
        } else if (!Banking.isBankScreenOpen() && !Player.isMoving()) {
            RSObject[] nearest_flax = Objects.findNearest(3, FLAX_PLANT_ID);

            if (nearest_flax.length > 0) {
            	// set nearest flax as target
            	target_flax = nearest_flax[0];
            	// anti ban compliance
                if (nearest_flax.length > 1 && this.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
                    if (nearest_flax[1].getPosition().distanceToDouble(nearest_flax[0]) < 3.0)
                        target_flax = nearest_flax[1];
                }
                
                if (abc.BOOL_TRACKER.HOVER_NEXT.next()) {
                	target_flax.hover();
                }
                
                return State.PICK_FLAX;
            }  else {
                return State.WALK_TO_FLAX_FIELD;
            }
        } else if (Player.isMoving()) {
        	return State.WALKING;
        }
        // if we dont satisfy any of the above conditions, we may have a problem
        return State.SOMETHING_WENT_WRONG;

        
    }

   enum State {
        WALK_TO_FLAX_FIELD,
        WALK_TO_BANK,
        PICK_FLAX,
        DEPOSIT_ITEMS,
        SOMETHING_WENT_WRONG,
        WALKING
    }
   
   private void walk(boolean to_bank) {
	   	checkRun();
	    WebWalking.walkTo(getTile(to_bank));
	    handleWait();
   }

    private boolean depositAll() {
    	if (Inventory.isFull()) {
	        int items_deposited = Banking.depositAll();
	        
	        // if we deposited any items, print the number
	        if (items_deposited > 0) {
	        	General.sleep(845, 3558);
	            log("Deposited "+ items_deposited +" items.");
	            // close bank
	            closeBankScreen();
	        }
        
	        return true;
    	} else {
    		return false;
    	}
    }
    
    private boolean handleBanking() {
        // we know we are in the bank, so try to open bank screen
        boolean bank_screen_is_open = Banking.openBank();
        
        if (bank_screen_is_open) {
        	General.sleep(250, 1340);
            return depositAll();
        }
        
        return false;
    }

    private void closeBankScreen() {
        if (Banking.isBankScreenOpen()) {
            Banking.close();
        }
    }

    private void pickFlax(RSObject flax) {
    	if (!Inventory.isFull()) {
	        if (flax.isOnScreen() && flax.isClickable()) {
	            flax.click("pick");
	            flax_picked ++;
	            
	            abc.BOOL_TRACKER.HOVER_NEXT.reset();
	            abc.BOOL_TRACKER.USE_CLOSEST.reset();
	            // item interaction delay
	            General.sleep(abc.DELAY_TRACKER.ITEM_INTERACTION.next());
	        }
    	}
    }

    private void log(String message) {
        println(message);
    }

    public void onPaint(Graphics g) {
        // setup image
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHints(aa);
        gg.drawImage(img, 0, 338, null);
        // set variables for display
        long run_time = System.currentTimeMillis() - startTime;
        int flax_per_hour = (int)(flax_picked * 3600000 / run_time);
    
        g.setFont(font);
        g.setColor(new Color(200, 200, 200));
        g.drawString("Runtime: " + Timing.msToString(run_time), 330, 395);
        g.drawString("Flax Picked: " + flax_picked, 330, 415);
        g.drawString("Flax Per Hour: "+ flax_per_hour, 330, 435);
    }

    private Image getImage(String url) {
        // get paint image
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }
    
    private void checkRun() {
    	final int run_energy = Game.getRunEnergy();
    	if (run_energy >= abc.INT_TRACKER.NEXT_RUN_AT.next() && !Game.isRunOn()) {
    		log("Turning run on");
    		WebWalking.setUseRun(true);
    		abc.INT_TRACKER.NEXT_RUN_AT.reset();
    	}
    }
    
    private void handleWait() {
    	log("Checking timer based anti-ban");
    	while (Player.isMoving()) {
    		// control cpu usage
    		General.sleep(50, 250);
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.EXAMINE_OBJECT.next()) {
    			abc.performExamineObject();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.ROTATE_CAMERA.next()) {
    			abc.performRotateCamera();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.PICKUP_MOUSE.next()) {
    			abc.performPickupMouse();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.LEAVE_GAME.next()) {
    			abc.performLeaveGame();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.RANDOM_MOUSE_MOVEMENT.next()) {
    			abc.performRandomMouseMovement();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.RANDOM_MOUSE_MOVEMENT.next()) {
    			abc.performRandomRightClick();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.CHECK_EQUIPMENT.next()) {
    			abc.performEquipmentCheck();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.CHECK_FRIENDS.next()) {
    			abc.performFriendsCheck();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.CHECK_COMBAT.next()) {
    			abc.performCombatCheck();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.CHECK_MUSIC.next()) {
    			abc.performMusicCheck();
    		}
    		
    		if (System.currentTimeMillis() >= abc.TIME_TRACKER.CHECK_QUESTS.next()) {
    			abc.performQuestsCheck();
    		}
    	}
    }


}