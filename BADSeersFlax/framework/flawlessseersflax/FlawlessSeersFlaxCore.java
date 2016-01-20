package scripts.BADSeersFlax.framework.flawlessseersflax;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.Sorting;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;

import scripts.BADSeersFlax.api.antiban.BADAntiBan;
import scripts.BADSeersFlax.api.areas.BADAreas;
import scripts.BADSeersFlax.api.banking.BADBanking;
import scripts.BADSeersFlax.api.conditions.BADConditions;
import scripts.BADSeersFlax.api.gametabs.BADTabs;
import scripts.BADSeersFlax.api.interfaces.BADInterfaces;
import scripts.BADSeersFlax.api.transportation.BADTransportation;

public class FlawlessSeersFlaxCore {
    private long flax_picked;
    private long flax_spun;
    private boolean has_flax;
    private boolean checked_for_flax;
    private String FLAX = "Flax";
	private BADBanking BANKER;
	private BADAntiBan ANTIBAN;
	private BADTransportation TRANSPORT;
	private BADTabs TABS;
	private boolean execute;

    
    public FlawlessSeersFlaxCore() {
    	BANKER = new BADBanking();
    	ANTIBAN = new BADAntiBan();
    	ANTIBAN.setHoverSkill(Skills.SKILLS.CRAFTING);
    	TRANSPORT = new BADTransportation();
    	TABS = new BADTabs();
    	execute = true;
    }
    
    public void run() {
        if (execute) {
            switch (state()) {
                case WALK_TO_FLAX_FIELD:
                	General.println("Walking to flax field");
                	TRANSPORT.checkRun();
                    WebWalking.walkTo(BADAreas.SEERS_FLAX_AREA.getRandomTile(), BADConditions.inArea(BADAreas.SEERS_FLAX_AREA), General.random(1200, 2500));
                    break;
                case WALK_TO_BANK:
                	General.println("Walking to bank");
                    if (Banking.openBank()) {
                    	General.println("Banking");
                    } else {
                    	TRANSPORT.checkRun();
                    	WebWalking.walkTo(BADAreas.CAMELOT_BANK_AREA.getRandomTile());
                    }
                    break;
                case WALK_TO_DOOR:
                	General.println("Walking to flax spinning building");
                	if (WebWalking.walkTo(BADAreas.SEERS_SPIN_DOOR_AREA.getRandomTile(), BADConditions.nearTile(BADAreas.SEERS_SPIN_DOOR_TILE, 2), General.random(8000, 12000))) {
                		Timing.waitCondition(BADConditions.inArea(BADAreas.SEERS_SPIN_DOOR_AREA), General.random(3000, 6000));
                	};
                	break;
                case DEPOSIT_ITEMS:
                    	if (depositItems()) {
                    		General.println("Deposited items successfully");
                    	}
                    break;
                case CHECK_FOR_FLAX:
                	General.println("Checking for flax");
            		if (BANKER.hasItem(FLAX)) {
            			General.println("Found it");
            			has_flax = true;
            		} else {
            			General.println("Has no flax");
            			has_flax = false;
            		}
            		checked_for_flax = true;
                	break;
                case WITHDRAW_FLAX:
                	if (withdrawFlax()) {
                		General.println("Withdrew flax");
                	}
                	break;
                case PICK_FLAX:
                	// sometimes open inventory if it isn't already open
                	TABS.openTab(GameTab.TABS.INVENTORY);
                	General.println("Picking flax");
                    pickFlax();
                    break;
                case SPIN:
                	General.println("Spinning flax");
                	// use the wheel to spin flax
                	spin();
                	break;
                case CLIMB_DOWN_LADDER:
                	General.println("Climbing down ladder");
                	// search for ladder in the room
                	if (climbLadder(6)) {
                		General.println("Climbed ladder");
                	};
                	break;
                case CLIMB_UP_LADDER:
                	General.println("Climbing up ladder");
                	// search for a ladder at the distance we are from the one we want
                	if (climbLadder(Player.getPosition().distanceTo(BADAreas.SEERS_SPIN_LADDER_TILE) + 1)) {
                		General.println("Climbed ladder");
                	};
                	break;
                case WALK_OUTSIDE:
                	General.println("Walking outside");
                	// we need to handle the door
                	walkOutside();
                	break;
                case WALK_INSIDE:
                	General.println("Walking inside");
                	// we need to handle the door
                	walkInside();
                	break;
                case BUSY:
                	General.println("Checking antiban while busy");
                	// handle antiban
                	ANTIBAN.handleWait();
                	break;
                case SOMETHING_WENT_WRONG:
                	General.println("Ending script, something has gone wrong");
                	execute = false;
                	break;
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
		if (BADAreas.SEERS_SPIN_AREA.contains(Player.getPosition())) {
			// if we have flax, we need to spin it
			if (Inventory.getCount("Flax") > 0) {
				return State.SPIN;
			}
			// we have no business up stairs other than spinning flax
			return State.CLIMB_DOWN_LADDER;
		}
		// handle ladder area
		if (BADAreas.SEERS_SPIN_LADDER_AREA.contains(Player.getPosition())) {
			// see if we need to bank
			if (needsToBank()) {
				// walk outside to handle door before banking
				return State.WALK_OUTSIDE;
			}
			// climb ladder to spin flax
			return State.CLIMB_UP_LADDER;
		}
		// handle door area
		if (nearArea(BADAreas.SEERS_SPIN_DOOR_AREA) && Player.getPosition().getPlane() == 0) {
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
		if (nearArea(BADAreas.CAMELOT_BANK_AREA) || Banking.isInBank()) {
			if (!checked_for_flax) {
				return State.CHECK_FOR_FLAX;
			}
			if (Inventory.isFull() || Inventory.getCount("Flax") > 0) {
				General.println("Full");
	    		// see if we need to deposit
	    		if (Inventory.getCount("Bow string") > 0) {
	    			return State.DEPOSIT_ITEMS;
	    		}
	    		// see if we need to spin flax into bow string
	    		if (Inventory.getCount("Flax") > 0) {
	    			return State.WALK_TO_DOOR;
	    		}
			}
			General.println("Can pick flax");
			General.println(has_flax);
			// see if we have flax to spin into bow string
			if (!Inventory.isFull()) {
				if (has_flax) {
					General.println("With draw flax state");
					return State.WITHDRAW_FLAX;
				}
			}
			// pick flax
			return State.WALK_TO_FLAX_FIELD;
		}
		// handle flax field
		if (nearArea(BADAreas.SEERS_FLAX_AREA)) {
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
	    SPIN,
	    CLIMB_DOWN_LADDER,
	    CLIMB_UP_LADDER,
	    WALK_OUTSIDE,
	    WALK_INSIDE,
	    WALK_TO_DOOR,
	    WITHDRAW_FLAX,
	    CHECK_FOR_FLAX,
	    BUSY
	}
	
	public boolean executing() {
		return execute;
	}
    
    public long getFlaxPicked() {
    	return flax_picked;
    }
    
    public long getFlaxSpun() {
    	return flax_spun;
    }
    
   
    
    private boolean depositItems() {
    	General.println("Depositing");
    	// check for flax
    	if (BANKER.hasItem(FLAX)) {
    		has_flax = true;
    	} else {
    		has_flax = false;
    	}
    	// deposit items
    	int deposited = BANKER.depositAll();
    	Timing.waitCondition(BADConditions.INVENTORY_EMPTY, General.random(2000, 3000));
    	
    	if (deposited > 0) {
    		Timing.waitCondition(BADConditions.INVENTORY_EMPTY, General.random(2000, 3000));
    		flax_spun += deposited;
    		return true;
    	}
    	
    	return false;
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
					return Timing.waitCondition(BADConditions.ON_GROUND_FLOOR, General.random(4000, 6000));
				} else {
					// wait until we are upstairs
					return Timing.waitCondition(BADConditions.ON_FIRST_FLOOR, General.random(4000, 6000));
				}
			}
		}
    	return false;
    }
    
    public String getDirection() {
    	if (Player.getPosition().getPlane() == 1) {
    		return "Climb-down";
    	}
    	return "Climb-up";
    }
    
    private boolean withdrawFlax() {
    	if (BANKER.withdraw(FLAX, 28) && Timing.waitCondition(BADConditions.hasItem("Flax"), General.random(2000, 4000))) {
    		General.println("Withdrew flax");
    		return true;
    	}
    	
    	return false;
    }
    
    private void useSpinningWheel() {
		// find a spinning wheel
		RSObject[] wheel = Objects.find(10, "Spinning Wheel");
		// null check
		if (wheel.length > 0 && Inventory.getCount("Flax") > 0) {
			General.println("Found wheel");
			if (!wheel[0].isOnScreen()) {
				Camera.turnToTile(wheel[0]);
			}
			// try to click the spin option
			if (wheel[0].click("Spin")) {
				// wait for the interface to open up
				Timing.waitCondition(getInterfaceCondition(true), General.random(3000, 5000));
				// be like a human and make sure inventory is open
				TABS.openTab(GameTab.TABS.INVENTORY);
			}
		}
    }
    
    private void useInterface() {
		// cache interface since it can return null
		RSInterfaceChild sp = getInterface();
		if (sp != null) {
			General.println("Clicking make x");
			// click make x option
			if (sp.click("Make X")) {
				Timing.waitCondition(getInterfaceCondition(false), General.random(3000, 5000));
			};
			// type amount
			typeAmount();
			// wait while it spins
    		Timing.waitCondition(BADConditions.SPUN_FLAX, General.random(55000, 70000));
		}
    }
    
    private void spin() {
 	   
		// check if menu is open
		if (getInterface() == null) {
			useSpinningWheel();
		} else {
			useInterface();
		}
    }
    
    private void typeAmount() {
		Keyboard.typeSend(String.valueOf(General.random(28, 888)));
    }
    
    private RSInterfaceChild getInterface() {
    	return BADInterfaces.getChildInterface(BADInterfaces.SPINNING_WHEEL_PARENT_ID, BADInterfaces.SPINNING_WHEEL_CHILD_ID);
    }
    
    private Condition getInterfaceCondition(boolean open) {
    	if (open) {
    		return BADConditions.interfaceOpen(BADInterfaces.SPINNING_WHEEL_PARENT_ID, BADInterfaces.SPINNING_WHEEL_CHILD_ID);
    	} else {
    		return BADConditions.interfaceClosed(BADInterfaces.SPINNING_WHEEL_PARENT_ID, BADInterfaces.SPINNING_WHEEL_CHILD_ID);
    	}
    }
    
    private void walkOutside() {
    	TRANSPORT.nav().setStoppingCondition(BADConditions.nearTile(BADAreas.SEERS_SPIN_DOOR_TILE, 2));
    	TRANSPORT.nav().setStoppingConditionCheckDelay(General.random(1000, 2000));
    	if (TRANSPORT.nav().traverse(BADAreas.SEERS_SPIN_DOOR_AREA.getRandomTile())) {
        	Timing.waitCondition(BADConditions.inArea(BADAreas.SEERS_SPIN_DOOR_AREA), General.random(16000, 20000));

    	};
    }
    
    private void walkInside() {
    	TRANSPORT.nav().setStoppingCondition(BADConditions.nearTile(BADAreas.SEERS_SPIN_LADDER_TILE, 2));
    	TRANSPORT.nav().setStoppingConditionCheckDelay(General.random(1000, 2000));
    	if (TRANSPORT.nav().traverse(BADAreas.SEERS_SPIN_LADDER_AREA.getRandomTile())) {
        	Timing.waitCondition(BADConditions.inArea(BADAreas.SEERS_SPIN_LADDER_AREA), General.random(5000, 8000));
    	};
    }
    
    @SuppressWarnings({ "deprecation"})
	private void pickFlax() {
    	// find flax objects
    	RSObject[] flax = Objects.findNearest(10, Filters.Objects.nameContains("Flax").combine(Filters.Objects.inArea(BADAreas.SEERS_FLAX_AREA), true));
    	General.println("Flax in area");
    	General.println(flax.length);
    	// since we are finding a new item, we will wait the delay timer
    	ANTIBAN.handleSwitchObjectDelay();
    	// null check
    	if (flax.length > 0) {
    		// check if we should use the closest
    		if (flax.length > 1) {
    			General.println("Pick flax 1");
    			if (ANTIBAN.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
    				// see if next is within 3 tiles of closest
    				if (flax[0].getPosition().distanceTo(flax[1]) < 3) {
    					// we should use next
    					pick(flax[1]);
    					// reset the tracker
    					ANTIBAN.abc.BOOL_TRACKER.USE_CLOSEST.reset();
    				}
    			}
    		}
    		// pick the closest flax
			General.println("Pick flax 0");
			pick(flax[0]);
    	}
    }
    
    private void pick(RSObject flax) {
    	// ensure flax is on screen
    	if (!flax.isOnScreen()) {
    		Camera.turnToTile(flax);
    	}
    	// click the flax 
		if (flax.isClickable() && flax.click("Pick") && Timing.waitCondition(BADConditions.WAIT_IDLE, General.random(2500, 5000))) {
			flax_picked++;
			// handle hovering next
			handleHoverNext();
		} 
    }
    
    private void handleHoverNext() {
    	RSObject[] flax = Objects.findNearest(3, FLAX);
    	
    	if (flax.length > 0 && ANTIBAN.abc.BOOL_TRACKER.HOVER_NEXT.next()) {
    		if (flax[0].hover()) {
    			Timing.waitCondition(BADConditions.crosshairChange(), General.random(2500, 6000));
    			ANTIBAN.abc.BOOL_TRACKER.HOVER_NEXT.reset();
    		}
    	}
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
}
