/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leadpanel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LEADPanelBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sascha Hacker - Initial contribution
 */
@NonNullByDefault
public class LEADPanelBindingConstants {

    private static final String BINDING_ID = "leadpanel";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SW = new ThingTypeUID(BINDING_ID, "SW");
    public static final ThingTypeUID THING_TYPE_PDW = new ThingTypeUID(BINDING_ID, "PDW");
    public static final ThingTypeUID THING_TYPE_PDC = new ThingTypeUID(BINDING_ID, "PDC");

    // List of thing Parameters names
    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_POLLINGPERIOD = "pollingPeriod";

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color-temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_WHITE = "white";

    public static final Integer DEFAULTPORT = 8899;
    public static final Integer DEFAULTPOLLINGPERIOD = 30;
}
