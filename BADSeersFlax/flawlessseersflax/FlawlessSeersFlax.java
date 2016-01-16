package scripts.BADSeersFlax.flawlessseersflax;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.api.General;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.Starting;

import scripts.BADSeersFlax.framework.flawlessseersflax.FlawlessSeersFlaxCore;
import scripts.BADSeersFlax.framework.paint.BADPaint;
import java.awt.Graphics; 

@ScriptManifest(authors = {"botsallday"}, category = "Money Making", name = "FlawlessSeersFlax")

public class FlawlessSeersFlax extends Script implements Painting, Starting {
    
	private BADPaint painter = new BADPaint();
	private FlawlessSeersFlaxCore core = new FlawlessSeersFlaxCore();
    
    public void run() {
    	
    	while (core.executing()) {
    		// execute the script
    		core.run();
    	}
    }
   
    public void onPaint(Graphics g) {
    	painter.paint(g, core.getFlaxSpun(), core.getFlaxPicked());
    }

	@Override
	public void onStart() {
    	General.useAntiBanCompliance(true);
	}
}