package scripts.BADSeersFlax.api.banking;

import org.tribot.api.Timing;
import org.tribot.api2007.Banking;

import scripts.BADSeersFlax.api.conditions.BADConditions;

public class BADBanking {
	// deposit all items
	public int depositAll() {
		if (canBank()) {
			return Banking.depositAll();
		}
		return 0;
	}
	
	public boolean withdraw(String item_name, int count) {
		if (canBank()) {
			return Banking.withdraw(count, item_name);
		}
		
		return false;
	}
	
	public boolean hasItem(String item_name) {
		if (canBank()) {
			return Banking.find(item_name).length > 0;
		}
		
		return false;
	}
	
	private boolean canBank() {
		// ensure we can bank, and wait for the bank to open if needed
		if (ensureAbleToBank()) {
			Timing.waitCondition(BADConditions.BANK_OPEN, 3000);
			return true;
		}
		
		return false;
	}
	
	// Ensures we are in the bank, and opens bank if needed. Will not move into the bank if you arent already inside.
	private boolean ensureAbleToBank() {
		if (!Banking.isInBank()) {
			return false;
		}
		
		if (!Banking.isBankScreenOpen()) {
			if (Banking.openBank()) {
				if (Banking.isBankScreenOpen()) {
					Timing.waitCondition(BADConditions.BANK_OPEN, 3000);
					return true;
				}
			};
		}
		
		return true;
	}
}
