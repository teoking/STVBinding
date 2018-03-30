package teoking.smarthome.binding.stv;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class SuperTVBindingConstants {

    public static final String BINDING_ID = "supertv";

    // List all Thing Type UIDs, related to the YahooWeather Binding
    public static final ThingTypeUID THING_TYPE_SUPERTV = new ThingTypeUID(BINDING_ID, "service");

    // List all channels
    public static final String CHANNEL_SERVICES_STATUS = "service_status";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SUPERTV);
}
