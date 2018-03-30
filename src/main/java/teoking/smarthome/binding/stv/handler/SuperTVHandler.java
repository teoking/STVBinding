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

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.StringType;
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
    private static final int CACHE_EXPIRY = 10 * 1000; // 10s
    private static final String CACHE_KEY_CONFIG = "CONFIG_STATUS";
    private static final String CACHE_KEY_SERVICES_STATUS = "SERVICES_STATUS";

    private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

    private final SuperTVConnection connection = new SuperTVConnection();

    private long lastUpdateTime;

    private BigDecimal refresh;

    ScheduledFuture<?> refreshJob;

    private String serviceStatusData = null;

    public SuperTVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        refresh = new BigDecimal(10);

        cache.put(CACHE_KEY_SERVICES_STATUS, () -> connection.getResponseFromQuery(""));

        startAutomaticRefresh();

        logger.debug("initialize...");
        System.out.println("hahahahahaah");
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);

        logger.debug("dispose...");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand...");

        if (command instanceof RefreshType) {
            boolean success = updateServiceStatusData();
            if (success) {
                switch (channelUID.getId()) {
                    case SuperTVBindingConstants.CHANNEL_SERVICES_STATUS:
                        updateState(channelUID, getServiceStatus());
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
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                boolean success = updateServiceStatusData();
                if (success) {
                    updateState(new ChannelUID(getThing().getUID(), SuperTVBindingConstants.CHANNEL_SERVICES_STATUS),
                            getServiceStatus());
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private synchronized boolean updateServiceStatusData() {
        final String data = cache.get(CACHE_KEY_SERVICES_STATUS);
        if (data != null) {
            updateStatus(ThingStatus.ONLINE);
            return true;
        }
        serviceStatusData = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Device Offline");
        return false;
    }

    private boolean isCurrentDataExpired() {
        return lastUpdateTime + MAX_DATA_AGE < System.currentTimeMillis();
    }

    private State getServiceStatus() {
        if (serviceStatusData != null) {
            return new StringType(serviceStatusData);
        }
        return StringType.EMPTY;
    }

}