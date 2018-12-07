/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leadpanel.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LEADPanelHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sascha Hacker - Initial contribution
 */
public class LEADPanelHandler extends BaseThingHandler {

    private String host;
    private int port;

    private byte[] magic = { (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    private byte[] unknown = { (byte) 0x02, (byte) 0xff };
    private byte[] end = { (byte) 0xaa, (byte) 0xaa };
    private byte[] powerCommand = { (byte) 0x02, (byte) 0x12 };

    private final Logger logger = LoggerFactory.getLogger(LEADPanelHandler.class);
    private ScheduledFuture<?> pollingJob;

    private LEADPanelConfiguration config;

    public LEADPanelHandler(Thing thing) {
        super(thing);
    }

    private byte calcChecksum(List<byte[]> payload) {
        byte checksum = 0x00;
        for (byte[] a : payload) {
            for (byte b : a) {
                checksum += b;
            }
        }
        checksum &= 0xff;
        return checksum;
    }

    protected void sendData(List<byte[]> payload) throws UnknownHostException, IOException {
        List<byte[]> message = new ArrayList<byte[]>();
        message.add(magic);
        message.addAll(payload);
        message.add(new byte[] { calcChecksum(payload) });
        message.add(end);

        Socket server = new Socket(host, port);
        OutputStream out = server.getOutputStream();

        for (byte[] a : message) {
            out.write(a);
        }

        server.close();
    }

    protected void setPowerstate(boolean state) throws UnknownHostException, IOException {
        List<byte[]> payload = new ArrayList<byte[]>();
        payload.add(unknown);
        payload.add(powerCommand);
        payload.add(new byte[] { (state) ? (byte) 0xab : (byte) 0xa9 });
        sendData(payload);
    }

    private void handlePowerCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            setPowerstate(command == OnOffType.ON);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for {}", command, channelUID);

        try {
            if (command == RefreshType.REFRESH) {
                update();
            } else if (channelUID.getId().equals(LEADPanelBindingConstants.CHANNEL_POWER)) {
                handlePowerCommand(command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing LEADPanel handler '{}'", getThing().getUID());

        config = getConfigAs(LEADPanelConfiguration.class);

        this.host = config.getHost();
        this.port = (config.getPort() == null) ? LEADPanelBindingConstants.DEFAULTPORT : config.getPort();

        update();

        int pollingPeriod = (config.getPollingPeriod() == null) ? LEADPanelBindingConstants.DEFAULTPOLLINGPERIOD
                : config.getPollingPeriod();
        if (pollingPeriod > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> update(), 0, pollingPeriod, TimeUnit.SECONDS);
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing LEADPanel handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    protected void update() {
        try {
            Socket server = new Socket(this.host, this.port);
            server.close();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }
}
