package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.types.RSArea;
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
	private AntiBan anti_ban = new AntiBan();
	private Transportation transport = new Transportation();
	private Banker banker = new Banker();
	private Clicking clicking = new Clicking();
    private final Image img = getImage("http://s15.postimg.org/izj9po1wr/Flawless_Flax_Picker.png");
    private final int FLAX_PLANT_ID = 7134;
    private static final long startTime = System.currentTimeMillis();
    private int flax_picked = 0;
    private RSObject target_flax;
    private boolean execute = true;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    private final RSArea flax_area = new RSArea(new RSTile(2739, 3439, 0), new RSTile(2749, 3449, 0));
    private final RSArea bank_area = new RSArea(new RSTile(2723, 3491, 0), new RSTile(2728, 3493, 0));
    public void run() {
    	General.useAntiBanCompliance(true);
    	
        while(execute) {
            State state = state();
            if (state != null) {
                switch (state) {
                    case WALK_TO_FLAX_FIELD:
                    	println("Walking to flax field");
                        WebWalking.walkTo(transport.getTile(flax_area, true));
                        break;
                    case WALK_TO_BANK:
                    	println("Walking to bank");
                        if (Banking.openBank()) {
                        	println("Banking");
                        } else {
                        	transport.checkRun();
                        	WebWalking.walkTo(transport.getTile(bank_area, true));
                        	println("Didnt work");
                        }
                        break;
                    case DEPOSIT_ITEMS:
                    	println("Depositing items in bank");
                    	banker.depositAll();
                        break;
                    case PICK_FLAX:
                    	println("Picking flax");
                        pickFlax(target_flax);
                        break;
                    case WALKING:
                    	println("Walking...");
                    	checkAntiban();
                    	break;
                    case SOMETHING_WENT_WRONG:
                    	println("Stopping script, something went wrong");
                    	execute = false;
                    	break;
                }
            }
            General.sleep(50,  250);
        }
    }

    private State state() {
        // whether or not we need to bank is the variable that drives the script
        if (Inventory.isFull() && !Player.isMoving()) {
             if (Banking.isInBank() && Banking.isBankScreenOpen()) {
                 return State.DEPOSIT_ITEMS;
             } else {
                 return State.WALK_TO_BANK;
             }
        } else if (!Player.isMoving() && Player.getAnimation() == -1) {
            RSObject[] nearest_flax = Objects.findNearest(3, FLAX_PLANT_ID);

            if (nearest_flax.length > 0) {
                // set nearest flax as target
                target_flax = nearest_flax[0];
                // anti ban compliance
                if (nearest_flax.length > 1 && this.anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
                    if (nearest_flax[1].getPosition().distanceToDouble(nearest_flax[0]) < 3.0)
                        target_flax = nearest_flax[1];
                }
                
                if (anti_ban.abc.BOOL_TRACKER.HOVER_NEXT.next()) {
                    target_flax.hover();
                }
                
                return State.PICK_FLAX;
            }  else {
                return State.WALK_TO_FLAX_FIELD;
            }
        } else if (Player.isMoving() || Player.getAnimation() > -1) {
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

    private void pickFlax(RSObject flax) {
    	if (clicking.collectAnimableObject(flax, "pick")) {
	    	flax_picked ++;
	        anti_ban.abc.BOOL_TRACKER.HOVER_NEXT.reset();
	        anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.reset();
    	}
    }
    
    private void checkAntiban() {
    	// dont call antiban every time possible
    	if (General.random(1, 50) >= 45) {
    		anti_ban.handleWait();
    	}
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
        g.drawString("Runtime: " + Timing.msToString(run_time), 320, 393);
        g.drawString("Flax Picked: " + flax_picked, 320, 412);
        g.drawString("Flax Per Hour: "+ flax_per_hour, 320, 433);
    }

    private Image getImage(String url) {
        // get paint image
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }
}