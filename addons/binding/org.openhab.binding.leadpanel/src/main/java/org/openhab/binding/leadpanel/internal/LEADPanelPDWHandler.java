/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leadpanel.internal;

import static java.lang.Math.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LEADPanelPDWHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sascha Hacker - Initial contribution
 */
public class LEADPanelPDWHandler extends LEADPanelHandler {

    private byte[] unknown = { (byte) 0x02, (byte) 0x00 };
    private byte[] brightnessCommand = { (byte) 0x08, (byte) 0x33 };
    private byte[] temperatureCommand = { (byte) 0x08, (byte) 0x36 };

    private PercentType brightness;
    private PercentType colorTemperature;

    private final Logger logger = LoggerFactory.getLogger(LEADPanelPDWHandler.class);

    public LEADPanelPDWHandler(@NonNull Thing thing) {
        super(thing);
    }

    private void setBrightness(byte brightness) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(brightnessCommand);
        payload.add(new byte[] { brightness });
        super.sendData(payload);
    }

    private void setColorTemperature(byte temperature) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(temperatureCommand);
        payload.add(new byte[] { temperature });
        super.sendData(payload);
    }

    private void handlePowerCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            setPowerstate((OnOffType) command == OnOffType.ON);
        }
    }

    private void handleBrightnessCommand(Command command) throws IOException {
        if (command instanceof PercentType) {
            this.brightness = (PercentType) command;
            setBrightness((byte) (this.brightness.intValue() * 0.64));
        } else if (command instanceof OnOffType) {
            super.setPowerstate((OnOffType) command == OnOffType.ON);
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            int calcBrightness = ((PercentType) command).intValue();
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                calcBrightness = max(min(calcBrightness + 1, 0), 100);
            } else {
                calcBrightness = max(min(calcBrightness - 1, 0), 100);
            }
            this.brightness = new PercentType(calcBrightness);
            setBrightness((byte) (this.brightness.intValue() * 0.64));
        }
    }

    private void handleColorTemperatureCommand(Command command) throws IOException {
        if (command instanceof PercentType) {
            this.colorTemperature = (PercentType) command;
            setColorTemperature((byte) (this.colorTemperature.intValue() * 0.32));
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            int calcColorTemperature = ((PercentType) command).intValue();
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                calcColorTemperature = max(min(calcColorTemperature + 1, 0), 100);
            } else {
                calcColorTemperature = max(min(calcColorTemperature - 1, 0), 100);
            }
            this.colorTemperature = new PercentType(calcColorTemperature);
            setColorTemperature((byte) (this.colorTemperature.intValue() * 0.32));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for {}", command, channelUID);

        try {
            if (command == RefreshType.REFRESH) {
                super.update();
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_POWER)) {
                handlePowerCommand(command);
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_BRIGHTNESS)) {
                handleBrightnessCommand(command);
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_COLOR_TEMPERATURE)) {
                handleColorTemperatureCommand(command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
