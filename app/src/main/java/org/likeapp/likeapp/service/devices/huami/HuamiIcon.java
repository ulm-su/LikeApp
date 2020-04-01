/*  Copyright (C) 2017-2020 Andreas Shimokawa, Daniele Gobbetti, Lukas
    Veneziano

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


import org.likeapp.likeapp.model.NotificationType;

public class HuamiIcon {
    // icons which are unsure which app they are for are suffixed with _NN
    // Ресурс v84-153
    public static final byte WECHAT = 0;
    // Иконка 4PDA
    public static final byte FOUR_PDA = WECHAT;
    // Ресурс v84-140
    // Пингвин, Домик
    public static final byte PENGUIN_1 = 1;
    // Ресурс v84-136
    // Зелёная иконка mi
    public static final byte MI_CHAT_2 = 2;
    // Иконка "полная батарея"
    public static final byte BATTERY_FULL = MI_CHAT_2;
    // Ресурс v84-123
    public static final byte FACEBOOK = 3;
    // Ресурс v84-149
    public static final byte TWITTER = 4;
    // Ресурс v84-133
    // Красная круглая иконка mi
    public static final byte MI_APP_5 = 5;
    // Ресурс v84-144
    public static final byte SNAPCHAT = 6;
    // Иконка Disk Server Manager
    public static final byte DSM = SNAPCHAT;
    // Ресурс v84-154
    public static final byte WHATSAPP = 7;
    // Ресурс v84-142
    public static final byte RED_WHITE_FIRE_8 = 8;
    // Иконка ВТБ
    public static final byte VTB = RED_WHITE_FIRE_8;
    // Ресурс v84-145
    public static final byte CHINESE_9 = 9;
    // Иконка SofaScore
    public static final byte SOFA_SCORE = CHINESE_9;
    // Ресурс v84-292
    // Будильник + текущее время
    public static final byte ALARM_CLOCK = 10;
    // Ресурс v84-125
    // app
    public static final byte APP_11 = 11;
    // Ресурс v84-127
    // Фотоаппарат
    public static final byte INSTAGRAM = 12;
    // Ресурс v84-137
    public static final byte CHAT_BLUE_13 = 13;
    // Иконка "разряженная батарея"
    public static final byte BATTERY_LOW = CHAT_BLUE_13;
    // Ресурс v84-139
    // Корова, Белый человечек на красном фоне
    public static final byte COW_14 = 14;
    // Mi Home
    public static final byte MI_HOME = COW_14;
    // Ресурс v84-148
    public static final byte CHINESE_15 = 15;
    // Ресурс v84-156
    // Значок Alipay
    public static final byte CHINESE_16 = 16;
    // Ali Express
    public static final byte ALI_EXPRESS = CHINESE_16;
    // Ресурс v84-141
    public static final byte STAR_17 = 17;
    // Сбербанк
    public static final byte SBERBANK = STAR_17;
    // Ресурс v84-155
    public static final byte APP_18 = 18;
    // Ресурс v84-147
    public static final byte CHINESE_19 = 19;
    // Youtube
    public static final byte YOUTUBE = CHINESE_19;
    // Ресурс v84-121
    // Крылья
    public static final byte CHINESE_20 = 20;
    // Ресурс v84-228
    public static final byte CALENDAR = 21;
    // Ресурс v84-124
    public static final byte FACEBOOK_MESSENGER = 22;
    // Ресурс v84-150
    public static final byte VIBER = 23;
    // Ресурс v84-130
    public static final byte LINE = 24;
    // Ресурс v84-146
    public static final byte TELEGRAM = 25;
    // Ресурс v84-129
    public static final byte KAKAOTALK = 26;
    // Ресурс v84-143
    public static final byte SKYPE = 27;
    // Ресурс v84-151
    public static final byte VKONTAKTE = 28;
    // Ресурс v84-138
    public static final byte POKEMONGO = 29;
    // Ресурс v84-126
    public static final byte HANGOUTS = 30;
    // Ресурс v84-135
    // Красная квадратная иконка mi
    public static final byte MI_31 = 31;
    // Ресурс v84-? app
    public static final byte CHINESE_32 = 32;
    // Иконка Bluetooth
    public static final byte BLUETOOTH = CHINESE_32;
    // Ресурс v84-? app
    public static final byte CHINESE_33 = 33;
    // Иконка Chrome
    public static final byte CHROME = CHINESE_33;
    // Ресурс v84-122
    public static final byte EMAIL = 34;
    // Ресурс v84-152
    public static final byte WEATHER = 35;
    // Ресурс v84-212,213
    // Человек с бьющимся сердцем и сообщением "подтолкнул вас"
    public static final byte HR_WARNING_36 = 36;

    public static byte mapToIconId(NotificationType type) {
        switch (type) {
            case UNKNOWN:
                return APP_11;
            case CONVERSATIONS:
            case RIOT:
            case HIPCHAT:
            case KONTALK:
            case ANTOX:
                return WECHAT;
            case GENERIC_EMAIL:
            case GMAIL:
            case YAHOO_MAIL:
            case OUTLOOK:
                return EMAIL;
            case GENERIC_NAVIGATION:
                return APP_11;
            case GENERIC_SMS:
                return WECHAT;
            case GENERIC_CALENDAR:
            case BUSINESS_CALENDAR:
                return CALENDAR;
            case FACEBOOK:
                return FACEBOOK;
            case FACEBOOK_MESSENGER:
                return FACEBOOK_MESSENGER;
            case GOOGLE_HANGOUTS:
            case GOOGLE_MESSENGER:
                return HANGOUTS;
            case INSTAGRAM:
            case GOOGLE_PHOTOS:
                return INSTAGRAM;
            case KAKAO_TALK:
                return KAKAOTALK;
            case LINE:
                return LINE;
            case SIGNAL:
                return CHAT_BLUE_13;
            case TWITTER:
                return TWITTER;
            case SKYPE:
                return SKYPE;
            case SNAPCHAT:
                return SNAPCHAT;
            case TELEGRAM:
                return TELEGRAM;
            case THREEMA:
                return CHAT_BLUE_13;
            case VIBER:
                return VIBER;
            case WECHAT:
                return WECHAT;
            case WHATSAPP:
                return WHATSAPP;
            case GENERIC_ALARM_CLOCK:
                return ALARM_CLOCK;
            case VKONTAKTE:
                return VKONTAKTE;
            case SOFA_SCORE:
                return SOFA_SCORE;
            case BATTERY_LOW:
                return BATTERY_LOW;
            case BATTERY_FULL:
                return BATTERY_FULL;
            case DSM:
                return DSM;
            case CHROME:
                return CHROME;
            case BLUETOOTH:
                return BLUETOOTH;
            case FOUR_PDA:
                return FOUR_PDA;
            case VTB_ONLINE:
            case SMART_SMS:
                return VTB;
            case SBERBANK:
                return SBERBANK;
            case YOUTUBE:
                return YOUTUBE;
            case ALI_EXPRESS:
                return ALI_EXPRESS;
            case MI_HOME:
                return MI_HOME;
            case WEATHER:
                return WEATHER;
        }
        return APP_11;
    }
}
