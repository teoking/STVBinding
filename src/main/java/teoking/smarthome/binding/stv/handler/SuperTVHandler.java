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
package teoking.smarthome.binding.stv.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import teoking.smarthome.binding.stv.SuperTVBindingConstants;
import teoking.smarthome.binding.stv.internal.connection.SuperTVConnection;

public class SuperTVHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SuperTVHandler.class);

    private static final int MAX_DATA_AGE = 3 * 60 * 60 * 1000; // 3h
    private static final int CACHE_EXPIRY = 60 * 1000; // 60s

    private static final String CACHE_KEY_STV_DATA = "STV_DATA";
    private static final String CACHE_KEY_SERVICE_STATUS = "SERVICE_STATUS";
    private static final String CACHE_KEY_CPU_TEMP = "CPU_TEMPERATURE";
    private static final String CACHE_KEY_FREE_MEM = "FREE_MEMORY";

    private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

    private final SuperTVConnection connection = new SuperTVConnection();

    private long lastUpdateTime;

    private BigDecimal refresh;

    ScheduledFuture<?> refreshJob;

    private String stvData = null;

    public SuperTVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        try {
            refresh = (BigDecimal) config.get(SuperTVBindingConstants.THING_CONFIG_REFRESH);
        } catch (Exception e) {
            logger.debug("Cannot set refresh parameter.", e);
        }

        if (refresh == null) {
            // let's go for the default
            refresh = new BigDecimal(60);
        }

        cache.put(CACHE_KEY_STV_DATA, () -> connection.getStvData(""));

        startAutomaticRefresh();

        logger.debug("initialize...");
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);

        logger.debug("dispose...");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand... id=" + channelUID.getId());

        if (command instanceof RefreshType) {
            boolean success = updateServiceData();
            if (success) {
                switch (channelUID.getId()) {
                    case SuperTVBindingConstants.CHANNEL_SERVICE_STATUS:
                        updateState(channelUID, getServiceStatus());
                        break;
                    case SuperTVBindingConstants.CHANNEL_CPU_TEMPERATURE:
                        updateState(channelUID, getCpuTemp());
                        break;
                    case SuperTVBindingConstants.CHANNEL_FREE_MEMORY:
                        updateState(channelUID, getFreeMem());
                        break;
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        logger.debug("getConfigStatus...");
        return new ArrayList<>();
    }

    private void startAutomaticRefresh() {
        logger.debug("startAutomaticRefresh...");
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                boolean success = updateServiceData();
                if (success && stvData != null) {
                    logger.debug("update data...");
                    // TODO handle the types and data.
                    updateState(new ChannelUID(getThing().getUID(), SuperTVBindingConstants.CHANNEL_SERVICE_STATUS),
                            getServiceStatus());
                    updateState(new ChannelUID(getThing().getUID(), SuperTVBindingConstants.CHANNEL_CPU_TEMPERATURE),
                            getCpuTemp());
                    updateState(new ChannelUID(getThing().getUID(), SuperTVBindingConstants.CHANNEL_FREE_MEMORY),
                            getFreeMem());
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private synchronized boolean updateServiceData() {
        // If current data not expired, the device is online.
        if (!isCurrentDataExpired()) {
            updateStatus(ThingStatus.ONLINE);
        }

        final String data = cache.get(CACHE_KEY_STV_DATA);
        if (data != null) {
            lastUpdateTime = System.currentTimeMillis();
            stvData = data;
            return true;
        }
        stvData = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Device Offline");
        return false;
    }

    private boolean isCurrentDataExpired() {
        return lastUpdateTime + MAX_DATA_AGE < System.currentTimeMillis();
    }

    private State getServiceStatus() {
        if (stvData != null) {
            return new StringType(StringUtils.split(stvData, '|')[0]);
        }
        return StringType.EMPTY;
    }

    private State getCpuTemp() {
        if (stvData != null) {
            return new QuantityType<Temperature>(Double.parseDouble(StringUtils.split(stvData, '|')[1]),
                    SIUnits.CELSIUS);
        }
        return StringType.EMPTY;
    }

    private State getFreeMem() {
        if (stvData != null) {
            return new StringType(StringUtils.split(stvData, '|')[2]);
        }
        return StringType.EMPTY;
    }

}