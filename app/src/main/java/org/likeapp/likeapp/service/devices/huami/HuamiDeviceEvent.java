/*  Copyright (C) 2015-2020 Andreas Shimokawa, Carsten Pfeiffer

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.likeapp.likeapp.service.devices.huami;


public class HuamiDeviceEvent {
    public static final byte FELL_ASLEEP = 0x01;
    public static final byte WOKE_UP = 0x02;
    public static final byte STEPSGOAL_REACHED = 0x03;
    public static final byte BUTTON_PRESSED = 0x04;
    public static final byte START_NONWEAR = 0x06;
    public static final byte CALL_REJECT = 0x07;
    public static final byte FIND_PHONE_START = 0x08;
    public static final byte CALL_IGNORE = 0x09;
    public static final byte ALARM_TOGGLED = 0x0a;
    public static final byte BUTTON_PRESSED_LONG = 0x0b;
    public static final byte TICK_30MIN = 0x0e; // unsure
    public static final byte FIND_PHONE_STOP = 0x0f;
    public static final byte MTU_REQUEST = 0x16;
    public static final byte DEBUG_LOG = (byte) 0x80;
    public static final byte DEBUG_VERSION = (byte) 0x81;
    public static final byte DEBUG_DISPLAY_SIZE = (byte) 0x82;
    public static final byte DEBUG_DISPLAY_DATA = (byte) 0x83;
    public static final byte APP_NAME = (byte) 0x84;
    public static final byte APP_DATA = (byte) 0x85;
    public static final byte APP_DATA_END = (byte) 0x86;
    public static final byte MUSIC_CONTROL = (byte) 0xfe;
}
