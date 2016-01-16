package scripts.BADSeersFlax.api.areas;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

public class BADAreas {
    public static final RSArea CAMELOT_BANK_AREA = new RSArea(new RSTile(2723, 3491, 0), new RSTile(2728, 3493, 0));
    public static final RSArea SEERS_SPIN_AREA = new RSArea(new RSTile(2710, 3470, 1), new RSTile(2715, 3473, 1));
    public static final RSArea SEERS_SPIN_DOOR_AREA = new RSArea(new RSTile(2716, 3469, 0), new RSTile(2721, 3476, 0));
    public static final RSArea SEERS_SPIN_LADDER_AREA = new RSArea(new RSTile(2713, 3470), new RSTile(2715, 3473, 0));
    public static final RSArea SEERS_FLAX_AREA = new RSArea(new RSTile(2739, 3439, 0), new RSTile(2749, 3449, 0));
    public static final RSTile SEERS_SPIN_LADDER_TILE = new RSTile(2714, 3470, 0);
    public static final RSTile SEERS_SPIN_DOOR_TILE = new RSTile(2719, 3471, 0);
}
