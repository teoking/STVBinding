/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package teoking.smarthome.binding.stv.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import teoking.smarthome.binding.stv.SuperTVBindingConstants;
import teoking.smarthome.binding.stv.handler.SuperTVHandler;

public class SuperTVHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SuperTVBindingConstants.THING_TYPE_SUPERTV);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        System.out.println("============" + thingTypeUID.getAsString());
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        System.out.println("+++++++++11" + thingTypeUID.getAsString());
        System.out.println("+++++++++22" + SuperTVBindingConstants.THING_TYPE_SUPERTV.getAsString());
        if (thingTypeUID.equals(SuperTVBindingConstants.THING_TYPE_SUPERTV)) {
            return new SuperTVHandler(thing);
        }

        return null;
    }
}
