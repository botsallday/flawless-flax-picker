package scripts.BADSeersFlax.framework.paint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.tribot.api.Timing;

public class BADPaint {
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    private final Image img = getImage("http://i.imgur.com/1a4Aimp.png");
    private static final long startTime = System.currentTimeMillis();
    
    private Image getImage(String url) {
        // get paint image
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }
    
	public void paint(Graphics g, long flax_spun, long flax_picked) {
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
}
