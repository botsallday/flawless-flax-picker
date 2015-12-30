package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.Banking;

public class Banker {
	
	final Transportation transport;
	
	Banker() {
		transport = new Transportation();
	}
	
	public boolean depositAll() {
		if (!Banking.isBankScreenOpen() && Banking.isInBank()) {
			Banking.openBank();
		}
	    if (Inventory.isFull()) {
	        if (Banking.depositAll() > 0) {
	            // condition for waiting until items are deposited
	            Timing.waitCondition(new Condition() {
	                @Override
	                public boolean active() {
	                    // control cpu usage
	                    General.sleep(300, 600);
	                    // ensure we have deposited items
	                    return !Inventory.isFull();
	                }
	            }, General.random(1000, 2500));
	        }
	    }
	    
	    return !Inventory.isFull();

	}
	
    public boolean closeBankScreen() {
        if (Banking.isBankScreenOpen()) {
            return Banking.close();
        }
        
        return false;
    }
    
    public boolean withdrawItem(String item_name, int amount) {
    	if (Banking.isInBank()) {
    		if (!Banking.isBankScreenOpen()) {
    			Banking.openBank();
    		}
    		
    		RSItem[] item = Banking.find(item_name);
    		
    		if (item.length > 0) {
    			if (Banking.withdraw(amount, item_name)) {
    	            Timing.waitCondition(new Condition() {
    	                @Override
    	                public boolean active() {
    	                    // control cpu usage
    	                    General.sleep(300, 600);
    	                    // ensure we have deposited items
    	                    return Inventory.find(item_name).length > 0;
    	                }
    	            }, General.random(1000, 2500));
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    public boolean handleBanking(RSArea area, boolean check_run) {
         if (Banking.isInBank() && !Banking.isBankScreenOpen()) {
        	 Banking.openBank();
        	 System.out.println("banking");
         } else {
         	return WebWalking.walkTo(transport.getTile(area, check_run));
         }
         
         return false;
     }
}