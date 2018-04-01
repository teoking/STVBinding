package teoking.smarthome.binding.stv;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class SuperTVBindingConstants {

    public static final String BINDING_ID = "supertv";

    // List all Thing Type UIDs, related to the STV Binding
    public static final ThingTypeUID THING_TYPE_SUPERTV = new ThingTypeUID(BINDING_ID, "service");

    // List all channels
    public static final String CHANNEL_SERVICE_STATUS = "status";
    public static final String CHANNEL_CPU_TEMPERATURE = "cpuTemperature";
    public static final String CHANNEL_FREE_MEMORY = "freeMemory";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SUPERTV);

    // List all configs
    public static final String THING_CONFIG_REFRESH = "refresh";
}
