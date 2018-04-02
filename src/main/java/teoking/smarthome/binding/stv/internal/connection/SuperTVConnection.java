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
package teoking.smarthome.binding.stv.internal.connection;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for Yahoo Weather url connection.
 *
 * @author Christoph Weitkamp - Changed use of caching utils to ESH ExpiringCacheMap
 *
 */
public class SuperTVConnection {

    private final Logger logger = LoggerFactory.getLogger(SuperTVConnection.class);

    private static final Random random = new Random();

    public String getStvData(String svc) {
        // Dummy now
        return "Ready|" + getCpuTemperature() + "|" + getFreeMemory();
    }

    private String getCpuTemperature() {
        // Dummy now
        return 100 * random.nextDouble() + "";
    }

    private String getFreeMemory() {
        // Dummy now
        return random.nextInt(24680000) + "";
    }
}
