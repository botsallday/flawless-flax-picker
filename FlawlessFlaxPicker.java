package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.ext.Filters;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.Sorting;
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;

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
    
	private AntiBan anti_ban = new AntiBan();
	private Transportation transport = new Transportation();
	
    private final Image img = getImage("http://i.imgur.com/1a4Aimp.png");
    private static final long startTime = System.currentTimeMillis();
    private long flax_picked = 0;
    private long flax_spun = 0;
    private boolean has_flax;
    private boolean execute = true;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    
    private final RSArea flax_area = new RSArea(new RSTile(2739, 3439, 0), new RSTile(2749, 3449, 0));
    private final RSTile ladder_tile = new RSTile(2714, 3470, 0);
    private final RSArea ladder_area = new RSArea(new RSTile(2713, 3470), new RSTile(2715, 3473, 0));
    private final RSTile door_tile = new RSTile(2719, 3471, 0);
    private final RSArea door_area = new RSArea(new RSTile(2716, 3469, 0), new RSTile(2721, 3476, 0));
    private final RSArea spin_area = new RSArea(new RSTile(2710, 3470, 1), new RSTile(2715, 3473, 1));
    private final RSArea bank_area = new RSArea(new RSTile(2723, 3491, 0), new RSTile(2728, 3493, 0));
    
	public Condition spun_flax = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// see if we have flax left
        	if (Inventory.getCount("Flax") > 0) {
        		// see if we are still spinning (handles reaction to leveling up which makes you stop spinning)
        		if (Player.getAnimation() == -1) {
        			// since we will be idle for about 1/2 second we must ensure that we are actually done spinning and not in between spins
        			return waitIdle(2000);
        		}
        		return false;
        	}
			return true;
        }
	};
	
	public Condition interface_open = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return getInterface() != null;
        }
	};
	
	public Condition interface_closed = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return getInterface() == null;
        }
	};
	
	public Condition is_upstairs = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return Player.getPosition().getPlane() > 0;
        }
	};
	
	public Condition is_downstairs = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return Player.getPosition().getPlane() == 0;
        }
	};
	
	public Condition pick_flax = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	if (anti_ban.abc.BOOL_TRACKER.HOVER_NEXT.next()) {
        		hoverNext();
        		anti_ban.abc.BOOL_TRACKER.HOVER_NEXT.reset();
        	}
        	return waitIdle(General.random(500, 750));
        }
	};
	
	public Condition inventory_open = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return GameTab.getOpen() == GameTab.TABS.INVENTORY;
        }
	};
	
	public Condition at_door_area = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return door_area.contains(Player.getPosition());
        }
	};
	
	public Condition at_ladder_area = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	return ladder_area.contains(Player.getPosition());
        }
	};
	
	public Condition withdrew_flax = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// we always withdraw the maximum amount, so if we have any we have it all
        	return Inventory.getCount("Flax") > 0;
        }
	};
	
	public Condition deposited_all_items = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// ensure we have no items
        	return Inventory.getAll().length == 0;
        }
	};
	
	public Condition near_ladder_tile = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// ensure we have no items
        	return Player.getPosition().distanceTo(ladder_tile) < 1;
        }
	};
	
	public Condition near_door_tile = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// ensure we have no items
        	return Player.getPosition().distanceTo(door_tile) < 1;
        }
	};
	
	public Condition inside_flax_field = new Condition() {
        @Override
        public boolean active() {
        	sleep(1000);
        	// ensure we have no items
        	return flax_area.contains(Player.getPosition());
        }
	};
    
    public void run() {
    	General.useAntiBanCompliance(true);
    	anti_ban.setHoverSkill(Skills.SKILLS.CRAFTING);
        while(execute) {
            State state = state();
            println(state);
            if (state != null) {
                switch (state) {
                    case WALK_TO_FLAX_FIELD:
                    	println("Walking to flax field");
                    	transport.checkRun();
                        WebWalking.walkTo(flax_area.getRandomTile(), inside_flax_field, General.random(1200, 2500));
                        break;
                    case WALK_TO_BANK:
                    	println("Walking to bank");
                        if (Banking.openBank()) {
                        	println("Banking");
                        } else {
                        	transport.checkRun();
                        	WebWalking.walkTo(transport.getTile(bank_area, true));
                        }
                        break;
                    case WALK_TO_DOOR:
                    	println("Walking to flax spinning building");
                    	if (WebWalking.walkTo(door_area.getRandomTile(), near_door_tile, General.random(8000, 12000))) {
                    		Timing.waitCondition(at_door_area, General.random(3000, 6000));
                    	};
                    	break;
                    case DEPOSIT_ITEMS:
                    	println("Depositing items in bank");
                    	if (!Banking.isBankScreenOpen()) {
                    		Banking.openBank();
                    	}
                    	if (Banking.isBankScreenOpen()) {
                    		if (Banking.find(Filters.Items.nameEquals("Flax")).length > 0) {
                    			has_flax = true;
                    		}
                        	if (depositItems()) {
                        		println("Deposited items successfully");
                        	}
                    	}
                        break;
                    case WITHDRAW_FLAX:
                    	println("Withdrawing flax");
                    	if (!Banking.isBankScreenOpen()) {
                    		Banking.openBank();
                    	}
                    	if (withdrawFlax()) {
                    		println("Withdrew flax");
                    	}
                    	break;
                    case PICK_FLAX:
                    	// sometimes open inventory if it isn't already open
                    	openInventory();
                    	println("Picking flax");
                        pickFlax();
                        break;
                    case SPIN:
                    	println("Spinning flax");
                    	// use the wheel to spin flax
                    	spin();
                    	break;
                    case CLIMB_DOWN_LADDER:
                    	println("Climbing down ladder");
                    	// search for ladder in the room
                    	climbLadder(6);
                    	break;
                    case CLIMB_UP_LADDER:
                    	println("Climbing up ladder");
                    	// search for a ladder at the distance we are from the one we want
                    	climbLadder(Player.getPosition().distanceTo(ladder_tile) + 1);
                    	break;
                    case WALK_OUTSIDE:
                    	println("Walking outside");
                    	// we need to handle the door
                    	walkOutside();
                    	break;
                    case WALK_INSIDE:
                    	println("Walking inside");
                    	// we need to handle the door
                    	walkInside();
                    	break;
                    case BUSY:
                    	println("Checking antiban while busy");
                    	// handle antiban
                    	anti_ban.handleWait();
                    	break;
                    case SOMETHING_WENT_WRONG:
                    	println("Ending script, something has gone wrong");
                    	execute = false;
                    	break;
                }
            }
            General.sleep(50,  250);
        }
    }

    private State state() {
    	// ensure we aren't performing actions while busy
    	if (Player.getAnimation() > -1 || Player.isMoving()) {
    		return State.BUSY;
    	}
    	
    	// handle spin area
    	if (spin_area.contains(Player.getPosition())) {
    		// if we have flax, we need to spin it
    		if (Inventory.getCount("Flax") > 0) {
    			return State.SPIN;
    		}
    		// we have no business up stairs other than spinning flax
    		return State.CLIMB_DOWN_LADDER;
    	}
    	// handle ladder area
    	if (ladder_area.contains(Player.getPosition())) {
    		// see if we need to bank
    		if (needsToBank()) {
    			// walk outside to handle door before banking
    			return State.WALK_OUTSIDE;
    		}
    		// climb ladder to spin flax
    		return State.CLIMB_UP_LADDER;
    	}
    	// handle door area
    	if (nearArea(door_area) && Player.getPosition().getPlane() == 0) {
    		// see if we need to bank
    		if (needsToBank()) {
    			return State.WALK_TO_BANK;
    		}
    		// see if we need to pick flax
    		if (!Inventory.isFull() && Inventory.getCount("Flax") == 0) {
    			return State.WALK_TO_FLAX_FIELD;
    		}
    		// go to spin flax
    		return State.WALK_INSIDE;
    	}
    	// handle banking area
    	if (nearArea(bank_area) || Banking.isInBank()) {
    		if (Inventory.isFull() || Inventory.getCount("Flax") > 0) {
    			println("Full");
	    		// see if we need to deposit
	    		if (Inventory.getCount("Bow String") > 0) {
	    			return State.DEPOSIT_ITEMS;
	    		}
	    		// see if we need to spin flax into bow string
	    		if (Inventory.getCount("Flax") > 0) {
	    			return State.WALK_TO_DOOR;
	    		}
    		}
    		// see if we have flax to spin into bow string
    		if (Banking.isBankScreenOpen() && !Inventory.isFull()) {
    			if (has_flax) {
    				return State.WITHDRAW_FLAX;
    			}
    		}
    		// pick flax
    		return State.WALK_TO_FLAX_FIELD;
    	}
    	// handle flax field
    	if (nearArea(flax_area)) {
    		// see if we need to pick flax
    		if (!Inventory.isFull()) {
    			return State.PICK_FLAX;
    		}
    		// spin flax
    		return State.WALK_TO_DOOR;
    	}
    	// end the script
    	return State.SOMETHING_WENT_WRONG;
    }

   enum State {
        WALK_TO_FLAX_FIELD,
        WALK_TO_BANK,
        PICK_FLAX,
        DEPOSIT_ITEMS,
        SOMETHING_WENT_WRONG,
        WALK_TO_SPIN_WHEEL,
        SPIN,
        WALKING,
        WALK_TO_LADDER,
        CLIMB_DOWN_LADDER,
        CLIMB_UP_LADDER,
        WALK_OUTSIDE,
        WALK_INSIDE,
        WALK_TO_DOOR,
        WITHDRAW_FLAX,
        BUSY
    }
   
   private boolean nearArea(RSArea area) {
	   // get closest tile
	   RSTile[] tiles = getClosestTileFromArea(area);
	   // we know the areas all have tiles, so no null check needed
	   return area.contains(Player.getPosition()) || tiles[0].getPosition().distanceTo(Player.getPosition()) < 5;
   }
   
   private RSTile[] getClosestTileFromArea(RSArea area) {
	   RSTile[] tiles = area.getAllTiles();
	   Sorting.sortByDistance(tiles, Player.getPosition(), true);
	   return tiles;
   }
   
   private boolean needsToBank() {
	   // we only ever put bow string into the bank
	   return Inventory.isFull() && Inventory.getCount("Flax") < 1;
   }
   
   private void hoverNext() {
	   RSObject[] flax = Objects.findNearest(10, Filters.Objects.nameContains("Flax").combine(Filters.Objects.inArea(flax_area), true));
	   // if we have a next flax then hover it
	   if (flax.length > 0) {
		   flax[0].hover();
	   }
   }

    private void pickFlax() {
    	// find flax objects
    	RSObject[] flax = Objects.findNearest(10, Filters.Objects.nameContains("Flax").combine(Filters.Objects.inArea(flax_area), true));
    	println("Flax in area");
    	println(flax.length);
    	// since we are finding a new item, we will wait the delay timer
    	anti_ban.handleItemInteractionDelay();
    	// null check
    	if (flax.length > 0) {
    		// check if we should use the closest
    		if (flax.length > 1) {
    			println("Pick flax 1");
    			if (anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
    				// see if next is within 3 tiles of closest
    				if (flax[0].getPosition().distanceTo(flax[1]) < 4) {
    					// we should use next
    					pick(flax[1]);
    					// reset the tracker
    					anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.reset();
    				}
    			}
    		}
    		// pick the closest flax
			println("Pick flax 0");
			pick(flax[0]);
    	}
    }
    
    private void pick(RSObject flax) {
    	// ensure flax is on screen
    	if (!flax.isOnScreen()) {
    		Camera.turnToTile(flax);
    	}
    	// click the flax 
		if (flax.isClickable()) {
			if (flax.click("Pick")) {
				// wait for picking, and check hover next anti ban
				Timing.waitCondition(pick_flax, General.random(3000, 5000));
				flax_picked++;
			}
		} 
    }
    
    private void walkOutside() {
    	transport.nav().setStoppingCondition(near_door_tile);
    	transport.nav().setStoppingConditionCheckDelay(General.random(1000, 2000));
    	transport.nav().traverse(door_area.getRandomTile());
    	Timing.waitCondition(at_door_area, General.random(16000, 20000));
    }
    
    private void walkInside() {
    	transport.nav().setStoppingCondition(near_ladder_tile);
    	transport.nav().setStoppingConditionCheckDelay(General.random(1000, 2000));
    	transport.nav().traverse(ladder_area.getRandomTile());
    	Timing.waitCondition(at_ladder_area, General.random(5000, 8000));
    }
    
    public boolean waitIdle(long amount) {
    	// capture current time
    	long time = System.currentTimeMillis();
    	// we will break if the player animation moves from idle or we wait the requested amount of time
    	while (Player.getAnimation() == -1) {
    		if (System.currentTimeMillis() > time + amount) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private RSInterfaceChild getInterface() {
		return Interfaces.get(459, 90);
    }
    
    private void typeAmount() {
		Keyboard.typeSend(String.valueOf(General.random(28, 888)));
    }
    
    private void spin() {
	   
		// check if menu is open
		if (getInterface() == null) {
			// find a spinning wheel
    		RSObject[] wheel = Objects.find(10, "Spinning Wheel");
    		// null check
    		if (wheel.length > 0 && Inventory.getCount("Flax") > 0) {
    			println("Found wheel");
    			if (!wheel[0].isOnScreen()) {
    				Camera.turnToTile(wheel[0]);
    			}
    			// try to click the spin option
    			if (wheel[0].click("Spin")) {
    				// wait for the interface to open up
    				Timing.waitCondition(interface_open, General.random(3000, 5000));
    				// be like a human and make sure inventory is open
    				openInventory();
    			}
    		}
		} else {
			// cache interface since it can return null
			RSInterfaceChild sp = getInterface();
			if (sp != null) {
				println("Clicking make x");
				// click make x option
				sp.click("Make X");
				// wait for interface to close
				Timing.waitCondition(interface_closed, General.random(3000, 5000));
				// type amount
				typeAmount();
				// wait while it spins
	    		Timing.waitCondition(spun_flax, General.random(55000, 70000));
			}
		}
    }
    
    private void openInventory() {
    	// dont do it every time
    	if (General.random(1, 10) > 4) {
	    	if (GameTab.getOpen() != GameTab.TABS.INVENTORY) {
	    		GameTab.open(GameTab.TABS.INVENTORY);
	    		Timing.waitCondition(inventory_open, 2000);
	    	}
    	}
    }
    
    private boolean withdrawFlax() {
    	if (Banking.withdraw(28, "Flax")) {
    		Timing.waitCondition(withdrew_flax, General.random(2000, 4000));
    		return true;
    	}
    	
    	return false;
    }
    
    public String getDirection() {
    	if (Player.getPosition().getPlane() == 1) {
    		return "Climb-down";
    	}
    	return "Climb-up";
    }
    
    public boolean climbLadder(int distance) {
    	// detect if we should go up or down the ladder
    	String direction = getDirection();
    	// find a ladder object
		RSObject[] ladder = Objects.find(distance, Filters.Objects.nameEquals("Ladder"));
		// null check
		if (ladder.length > 0) {
			Camera.turnToTile(ladder[0]);
			// try to click the desired ladder option
			if (ladder[0].click(direction)) {
				if (direction == "Climb-down") {
					// wait until we are downstairs
					Timing.waitCondition(is_downstairs, General.random(4000, 6000));
					return true;
				} else {
					// wait until we are upstairs
					Timing.waitCondition(is_upstairs, General.random(4000, 6000));
				    return true;
				}
			}
		}
    	return false;
    }
    
    public boolean depositItems() {
    	int deposited = Banking.depositAll();
    	
    	if (deposited > 0) {
    		Timing.waitCondition(deposited_all_items, General.random(2000, 3000));
    		flax_spun += deposited;
    		return true;
    	}
    	
    	return false;
    }
    
    public void onPaint(Graphics g) {
        // setup image
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHints(aa);
        gg.drawImage(img, 0, 338, null);
        // set variables for display
        long run_time = System.currentTimeMillis() - startTime;
        int flax_per_hour = (int)(flax_spun * 3600000 / run_time);
    
        g.setFont(font);
        g.setColor(new Color(0, 0, 0));
        g.drawString(""+Timing.msToString(run_time), 80, 450);
        g.drawString(""+flax_picked, 100, 400);
        g.drawString(""+flax_per_hour, 120, 425);
        g.drawString(""+(flax_spun * 170), 300, 450);
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