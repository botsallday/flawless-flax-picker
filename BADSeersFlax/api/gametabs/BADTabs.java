package scripts.BADSeersFlax.api.gametabs;

import org.tribot.api2007.GameTab;

public class BADTabs {
	
	// Ensure we don't try to open the tab if it is already open
	public boolean openTab(GameTab.TABS tab) {
		if (GameTab.getOpen() != tab) {
			if (GameTab.open(tab)) {
				return true;
			}
		}
		
		return false;
	}
	
}
