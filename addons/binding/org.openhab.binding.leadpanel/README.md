# LEAD Panel Binding

This binding is used to control LEAD energy LED Panels connected by WiFi.

## Supported Things

All SW, PDW and PDC devices.
Only PSW is fully tested.

## Discovery

No discovery available.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The thing can be configured through the Paper UI.
The only thing that needed is the hostname or ip.


## Channels

| Channel Type ID   | Item Type | Description                             | Device Type  | Access |
|-------------------|-----------|-----------------------------------------|------------- |--------|
| power             | Switch    | Power state (ON/OFF)                    | SW, PDW, PDC | (R)/W  |
| brightness        | Dimmer    | Brightness (min=0, max=100)             | SW, PDW, PDC | (R)/W  |
| color-temperature | Dimmer    | Color-Temperature (min=0, max=100)      | PDW          | (R)/W  |
| color             | Color     | Color                                   | PDC          | (R)/W  |
| white             | Switch    | White LEDs (ON/OFF)                     | PDC          | (R)/W  |

## Full Example

The syntax for a thing is:

    LEADPanel:devicetype:NAME

LEADPanel.things:

    Thing LEADPanel:SW:celling      [ host="192.168.178.130", port=8899, pollingPeriod=30 ]
    Thing LEADPanel:PDW:kitchen     [ host="192.168.178.131", port=8899, pollingPeriod=30 ]
    Thing LEADPanel:PDC:lifingroom  [ host="192.168.178.132", port=8899, pollingPeriod=30 ]

Parameters:

    host: hostname or ip of the device (required)
    port: tcp control port of the device (default: 8899, optional)
    pollingPeriod: time for polling the device status (default: 30, optional)


LEADPanel.items:

    Switch LEDPanelCelling_power            "Power"             (Light) {channel="LEADPanel:SW:celling:power"}
    Dimmer LEDPanelCelling_brightness       "Brightness"        (Light) {channel="LEADPanel:SW:celling:brightness"}
    
    Switch LEDPanelKitchen_power            "Power"             (Light) {channel="LEADPanel:PDW:kitchen:power"}
    Dimmer LEDPanelKitchen_brightness       "Brightness"        (Light) {channel="LEADPanel:PDW:kitchen:brightness"}
    Dimmer LEDPanelKitchen_colortemperature "Color Temperature" (Light) {channel="LEADPanel:PDW:kitchen:color-temperature"}
    
    Switch LEDPanelLifingroom_power         "Power"             (Light) {channel="LEADPanel:PDC:lifingroom:power"}
    Dimmer LEDPanelLifingroom_brightness    "Brightness"        (Light) {channel="LEADPanel:PDC:lifingroom:brightness"}
    Color  LEDPanelLifingroom_color         "Color"             (Light) {channel="LEADPanel:PDC:lifingroom:color"}
    Switch LEDPanelKitchen_white            "White"             (Light) {channel="LEADPanel:PDC:lifingroom:white"}


## Any custom content here!

There is no response of the device to any command, except of a tcp acknowledgment.
So if you control the device with the app, the change is not updated in openhab.
Discovery is possible but not implemented, it interference with the WifiLED Binding because of the same Wifi Controller.
Also the device must be setup with the LEAD App, to connect it with the wifi.
Every device has other commands for controlling and there is no way to discover the devicetype.
The only command they share is the powerstate command.
