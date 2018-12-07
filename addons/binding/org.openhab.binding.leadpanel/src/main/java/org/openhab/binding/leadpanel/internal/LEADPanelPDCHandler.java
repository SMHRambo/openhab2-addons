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
import org.eclipse.smarthome.core.library.types.HSBType;
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
 * The {@link LEADPanelPDCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sascha Hacker - Initial contribution
 */
public class LEADPanelPDCHandler extends LEADPanelHandler {

    private byte[] unknown = { (byte) 0x02, (byte) 0x00 };
    private byte[] brightnessCommand = { (byte) 0x08, (byte) 0x4c };
    private byte[] colorCommand = { (byte) 0x01, (byte) 0x01 };
    private byte[] whiteCommand = { (byte) 0x02, (byte) 0x05 };

    private HSBType hsb;

    private final Logger logger = LoggerFactory.getLogger(LEADPanelPDCHandler.class);

    public LEADPanelPDCHandler(@NonNull Thing thing) {
        super(thing);
    }

    private void setBrightness(byte brightness) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(brightnessCommand);
        payload.add(new byte[] { brightness });
        super.sendData(payload);
    }

    private void setColor(byte hsv) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(colorCommand);
        payload.add(new byte[] { hsv });
        super.sendData(payload);
    }

    private void setWhite(boolean state) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(whiteCommand);
        payload.add(new byte[] { (state) ? (byte) 0x8a : (byte) 0x8b });
        super.sendData(payload);
    }

    private void handlePowerCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            setPowerstate((OnOffType) command == OnOffType.ON);
        }
    }

    private void handleBrightnessCommand(Command command) throws IOException {
        if (command instanceof PercentType) {
            this.hsb = new HSBType(this.hsb.getHue(), this.hsb.getSaturation(), (PercentType) command);
            setBrightness((byte) (this.hsb.getBrightness().intValue() * 0.63));
        } else if (command instanceof OnOffType) {
            super.setPowerstate((OnOffType) command == OnOffType.ON);
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            int calcBrightness = this.hsb.getBrightness().intValue();
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                calcBrightness = max(min(calcBrightness + 1, 0), 100);
            } else {
                calcBrightness = max(min(calcBrightness - 1, 0), 100);
            }
            this.hsb = new HSBType(this.hsb.getHue(), this.hsb.getSaturation(), new PercentType(calcBrightness));
            setBrightness((byte) (this.hsb.getBrightness().intValue() * 0.63));
        }
    }

    private void handleColorCommand(Command command) throws IOException {
        if (command instanceof HSBType) {
            this.hsb = (HSBType) command;
            setColor((byte) (this.hsb.getHue().intValue() * 0.95));
            setBrightness((byte) (this.hsb.getBrightness().intValue() * 0.32));
        }
    }

    private void handleWhiteCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            setWhite((OnOffType) command == OnOffType.ON);
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
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_COLOR)) {
                handleColorCommand(command);
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_WHITE)) {
                handleWhiteCommand(command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
