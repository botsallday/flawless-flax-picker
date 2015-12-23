package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Banking;

public class Banker {
	Banker() {
		
	}
	
	public boolean depositAll() {
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
}