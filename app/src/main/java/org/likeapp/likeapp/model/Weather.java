/*  Copyright (C) 2016-2020 Andreas Shimokawa, Daniele Gobbetti, Sebastian
    Kranz

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
package org.likeapp.likeapp.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.likeapp.likeapp.GBApplication;
import org.likeapp.likeapp.util.Prefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Weather {
    private static final Logger LOG = LoggerFactory.getLogger(Weather.class);

    private WeatherSpec weatherSpec = null;

    private JSONObject reconstructedOWMForecast = null;

    public WeatherSpec getWeatherSpec() {
        return weatherSpec;
    }

    public void setWeatherSpec(WeatherSpec weatherSpec) {
        this.weatherSpec = weatherSpec;
    }

    public JSONObject createReconstructedOWMWeatherReply() {
        if (weatherSpec == null) {
            return null;
        }
        JSONObject reconstructedOWMWeather = new JSONObject();
        JSONArray weather = new JSONArray();
        JSONObject condition = new JSONObject();
        JSONObject main = new JSONObject();
        JSONObject wind = new JSONObject();

        try {
            condition.put("id", weatherSpec.currentConditionCode);
            condition.put("main", weatherSpec.currentCondition);
            condition.put("description", weatherSpec.currentCondition);
            condition.put("icon", Weather.mapToOpenWeatherMapIcon(weatherSpec.currentConditionCode));
            weather.put(condition);


            main.put("temp", weatherSpec.currentTemp);
            main.put("humidity", weatherSpec.currentHumidity);
            main.put("temp_min", weatherSpec.todayMinTemp);
            main.put("temp_max", weatherSpec.todayMaxTemp);

            wind.put("speed", (weatherSpec.windSpeed / 3.6f)); //meter per second
            wind.put("deg", weatherSpec.windDirection);

            reconstructedOWMWeather.put("weather", weather);
            reconstructedOWMWeather.put("main", main);
            reconstructedOWMWeather.put("name", weatherSpec.location);
            reconstructedOWMWeather.put("wind", wind);

        } catch (JSONException e) {
            LOG.error("Error while reconstructing OWM weather reply");
            return null;
        }
        LOG.debug("Weather JSON for WEBVIEW: " + reconstructedOWMWeather.toString());
        return reconstructedOWMWeather;
    }

    public JSONObject getReconstructedOWMForecast() {
        return reconstructedOWMForecast;
    }

    public void setReconstructedOWMForecast(JSONObject reconstructedOWMForecast) {
        this.reconstructedOWMForecast = reconstructedOWMForecast;
    }

    private static final Weather weather = new Weather();
    public static Weather getInstance() {return weather;}

    public static byte mapToPebbleCondition(int openWeatherMapCondition) {
/* deducted values:
    0 = sun + cloud
    1 = clouds
    2 = some snow
    3 = some rain
    4 = heavy rain
    5 = heavy snow
    6 = sun + cloud + rain (default icon?)
    7 = sun
    8 = rain + snow
    9 = 6
    10, 11, ... = empty icon
 */
        switch (openWeatherMapCondition) {
//Group 2xx: Thunderstorm
            case 200:  //thunderstorm with light rain:  //11d
            case 201:  //thunderstorm with rain:  //11d
            case 202:  //thunderstorm with heavy rain:  //11d
            case 210:  //light thunderstorm::  //11d
            case 211:  //thunderstorm:  //11d
            case 230:  //thunderstorm with light drizzle:  //11d
            case 231:  //thunderstorm with drizzle:  //11d
            case 232:  //thunderstorm with heavy drizzle:  //11d
            case 212:  //heavy thunderstorm:  //11d
            case 221:  //ragged thunderstorm:  //11d
                return 4;
//Group 3xx: Drizzle
            case 300:  //light intensity drizzle:  //09d
            case 301:  //drizzle:  //09d
            case 302:  //heavy intensity drizzle:  //09d
            case 310:  //light intensity drizzle rain:  //09d
            case 311:  //drizzle rain:  //09d
            case 312:  //heavy intensity drizzle rain:  //09d
            case 313:  //shower rain and drizzle:  //09d
            case 314:  //heavy shower rain and drizzle:  //09d
            case 321:  //shower drizzle:  //09d
            case 500:  //light rain:  //10d
            case 501:  //moderate rain:  //10d
                return 3;
//Group 5xx: Rain
            case 502:  //heavy intensity rain:  //10d
            case 503:  //very heavy rain:  //10d
            case 504:  //extreme rain:  //10d
            case 511:  //freezing rain:  //13d
            case 520:  //light intensity shower rain:  //09d
            case 521:  //shower rain:  //09d
            case 522:  //heavy intensity shower rain:  //09d
            case 531:  //ragged shower rain:  //09d
                return 4;
//Group 6xx: Snow
            case 600:  //light snow:  //[[file:13d.png]]
            case 601:  //snow:  //[[file:13d.png]]
            case 620:  //light shower snow:  //[[file:13d.png]]
                return 2;
            case 602:  //heavy snow:  //[[file:13d.png]]
            case 611:  //sleet:  //[[file:13d.png]]
            case 612:  //shower sleet:  //[[file:13d.png]]
            case 621:  //shower snow:  //[[file:13d.png]]
            case 622:  //heavy shower snow:  //[[file:13d.png]]
                return 5;
            case 615:  //light rain and snow:  //[[file:13d.png]]
            case 616:  //rain and snow:  //[[file:13d.png]]
                return 8;
//Group 7xx: Atmosphere
            case 701:  //mist:  //[[file:50d.png]]
            case 711:  //smoke:  //[[file:50d.png]]
            case 721:  //haze:  //[[file:50d.png]]
            case 731:  //sandcase  dust whirls:  //[[file:50d.png]]
            case 741:  //fog:  //[[file:50d.png]]
            case 751:  //sand:  //[[file:50d.png]]
            case 761:  //dust:  //[[file:50d.png]]
            case 762:  //volcanic ash:  //[[file:50d.png]]
            case 771:  //squalls:  //[[file:50d.png]]
            case 781:  //tornado:  //[[file:50d.png]]
            case 900:  //tornado
                return 6;
//Group 800: Clear
            case 800:  //clear sky:  //[[file:01d.png]] [[file:01n.png]]
                return 7;
//Group 80x: Clouds
            case 801:  //few clouds:  //[[file:02d.png]] [[file:02n.png]]
            case 802:  //scattered clouds:  //[[file:03d.png]] [[file:03d.png]]
            case 803:  //broken clouds:  //[[file:04d.png]] [[file:03d.png]]
            case 804:  //overcast clouds:  //[[file:04d.png]] [[file:04d.png]]
                return 0;
//Group 90x: Extreme
            case 901:  //tropical storm
            case 903:  //cold
            case 904:  //hot
            case 905:  //windy
            case 906:  //hail
//Group 9xx: Additional
            case 951:  //calm
            case 952:  //light breeze
            case 953:  //gentle breeze
            case 954:  //moderate breeze
            case 955:  //fresh breeze
            case 956:  //strong breeze
            case 957:  //high windcase  near gale
            case 958:  //gale
            case 959:  //severe gale
            case 960:  //storm
            case 961:  //violent storm
            case 902:  //hurricane
            case 962:  //hurricane
            default:
                return 6;

        }
    }
    public static int mapToYahooCondition(int openWeatherMapCondition) {
        // openweathermap.org conditions:
        // http://openweathermap.org/weather-conditions
        switch (openWeatherMapCondition) {
//Group 2xx: Thunderstorm
            case 200:  //thunderstorm with light rain:  //11d
            case 201:  //thunderstorm with rain:  //11d
            case 202:  //thunderstorm with heavy rain:  //11d
            case 210:  //light thunderstorm::  //11d
            case 211:  //thunderstorm:  //11d
            case 230:  //thunderstorm with light drizzle:  //11d
            case 231:  //thunderstorm with drizzle:  //11d
            case 232:  //thunderstorm with heavy drizzle:  //11d
                return 4;
            case 212:  //heavy thunderstorm:  //11d
            case 221:  //ragged thunderstorm:  //11d
                return 3;
//Group 3xx: Drizzle
            case 300:  //light intensity drizzle:  //09d
            case 301:  //drizzle:  //09d
            case 302:  //heavy intensity drizzle:  //09d
            case 310:  //light intensity drizzle rain:  //09d
            case 311:  //drizzle rain:  //09d
            case 312:  //heavy intensity drizzle rain:  //09d
                return 9;
            case 313:  //shower rain and drizzle:  //09d
            case 314:  //heavy shower rain and drizzle:  //09d
            case 321:  //shower drizzle:  //09d
                return 11;
//Group 5xx: Rain
            case 500:  //light rain:  //10d
            case 501:  //moderate rain:  //10d
            case 502:  //heavy intensity rain:  //10d
            case 503:  //very heavy rain:  //10d
            case 504:  //extreme rain:  //10d
            case 511:  //freezing rain:  //13d
                return 10;
            case 520:  //light intensity shower rain:  //09d
                return 40;
            case 521:  //shower rain:  //09d
            case 522:  //heavy intensity shower rain:  //09d
            case 531:  //ragged shower rain:  //09d
                return 12;
//Group 6xx: Snow
            case 600:  //light snow:  //[[file:13d.png]]
                return 7;
            case 601:  //snow:  //[[file:13d.png]]
                return 16;
            case 602:  //heavy snow:  //[[file:13d.png]]
                return 15;
            case 611:  //sleet:  //[[file:13d.png]]
            case 612:  //shower sleet:  //[[file:13d.png]]
                return 18;
            case 615:  //light rain and snow:  //[[file:13d.png]]
            case 616:  //rain and snow:  //[[file:13d.png]]
                return 5;
            case 620:  //light shower snow:  //[[file:13d.png]]
                return 14;
            case 621:  //shower snow:  //[[file:13d.png]]
                return 46;
            case 622:  //heavy shower snow:  //[[file:13d.png]]
//Group 7xx: Atmosphere
            case 701:  //mist:  //[[file:50d.png]]
            case 711:  //smoke:  //[[file:50d.png]]
                return 22;
            case 721:  //haze:  //[[file:50d.png]]
                return 21;
            case 731:  //sandcase  dust whirls:  //[[file:50d.png]]
                return 3200;
            case 741:  //fog:  //[[file:50d.png]]
                return 20;
            case 751:  //sand:  //[[file:50d.png]]
            case 761:  //dust:  //[[file:50d.png]]
                return 19;
            case 762:  //volcanic ash:  //[[file:50d.png]]
            case 771:  //squalls:  //[[file:50d.png]]
                return 3200;
            case 781:  //tornado:  //[[file:50d.png]]
            case 900:  //tornado
                return 0;
//Group 800: Clear
            case 800:  //clear sky:  //[[file:01d.png]] [[file:01n.png]]
                return 32;
//Group 80x: Clouds
            case 801:  //few clouds:  //[[file:02d.png]] [[file:02n.png]]
            case 802:  //scattered clouds:  //[[file:03d.png]] [[file:03d.png]]
                return 34;
            case 803:  //broken clouds:  //[[file:04d.png]] [[file:03d.png]]
            case 804:  //overcast clouds:  //[[file:04d.png]] [[file:04d.png]]
                return 44;
//Group 90x: Extreme
            case 901:  //tropical storm
                return 1;
            case 903:  //cold
                return 25;
            case 904:  //hot
                return 36;
            case 905:  //windy
                return 24;
            case 906:  //hail
                return 17;
//Group 9xx: Additional
            case 951:  //calm
            case 952:  //light breeze
            case 953:  //gentle breeze
            case 954:  //moderate breeze
            case 955:  //fresh breeze
                return 34;
            case 956:  //strong breeze
            case 957:  //high windcase  near gale
                return 24;
            case 958:  //gale
            case 959:  //severe gale
            case 960:  //storm
            case 961:  //violent storm
                return 3200;
            case 902:  //hurricane
            case 962:  //hurricane
                return 2;
            default:
                return 3200;

        }
    }

    public static String mapToOpenWeatherMapIcon(int openWeatherMapCondition) {
        //see https://openweathermap.org/weather-conditions
        String condition = "02d"; //generic "variable" icon

        if (openWeatherMapCondition >= 200 && openWeatherMapCondition < 300) {
            condition = "11d";
        } else if (openWeatherMapCondition >= 300 && openWeatherMapCondition < 500) {
            condition = "09d";
        } else if (openWeatherMapCondition >= 500 && openWeatherMapCondition < 510) {
            condition = "10d";
        } else if (openWeatherMapCondition >= 511 && openWeatherMapCondition < 600) {
            condition = "09d";
        } else if (openWeatherMapCondition >= 600 && openWeatherMapCondition < 700) {
            condition = "13d";
        } else if (openWeatherMapCondition >= 700 && openWeatherMapCondition < 800) {
            condition = "50d";
        } else if (openWeatherMapCondition == 800) {
            condition = "01d"; //TODO: night?
        } else if (openWeatherMapCondition == 801) {
            condition = "02d"; //TODO: night?
        } else if (openWeatherMapCondition == 802) {
            condition = "03d"; //TODO: night?
        } else if (openWeatherMapCondition == 803 || openWeatherMapCondition == 804) {
            condition = "04d"; //TODO: night?
        }

        return condition;
    }

    public static int mapToOpenWeatherMapCondition(int yahooCondition) {
        switch (yahooCondition) {
//yahoo weather conditions:
//https://developer.yahoo.com/weather/documentation.html
            case 0:  //tornado
                return 900;
            case 1:  //tropical storm
                return 901;
            case 2:  //hurricane
                return 962;
            case 3:  //severe thunderstorms
                return 212;
            case 4:  //thunderstorms
                return 211;
            case 5:  //mixed rain and snow
            case 6:  //mixed rain and sleet
                return 616;
            case 7:  //mixed snow and sleet
                return 600;
            case 8:  //freezing drizzle
            case 9:  //drizzle
                return 301;
            case 10:  //freezing rain
                return 511;
            case 11:  //showers
            case 12:  //showers
                return 521;
            case 13:  //snow flurries
            case 14:  //light snow showers
                return 620;
            case 15:  //blowing snow
            case 41:  //heavy snow
            case 42:  //scattered snow showers
            case 43:  //heavy snow
            case 46:  //snow showers
                return 602;
            case 16:  //snow
                return 601;
            case 17:  //hail
            case 35:  //mixed rain and hail
                return 906;
            case 18:  //sleet
                return 611;
            case 19:  //dust
                return 761;
            case 20:  //foggy
                return 741;
            case 21:  //haze
                return 721;
            case 22:  //smoky
                return 711;
            case 23:  //blustery
            case 24:  //windy
                return 905;
            case 25:  //cold
                return 903;
            case 26:  //cloudy
            case 27:  //mostly cloudy (night)
            case 28:  //mostly cloudy (day)
                return 804;
            case 29:  //partly cloudy (night)
            case 30:  //partly cloudy (day)
                return 801;
            case 31:  //clear (night)
            case 32:  //sunny
                return 800;
            case 33:  //fair (night)
            case 34:  //fair (day)
                return 801;
            case 36:  //hot
                return 904;
            case 37:  //isolated thunderstorms
            case 38:  //scattered thunderstorms
            case 39:  //scattered thunderstorms
                return 210;
            case 40:  //scattered showers
                return 520;
            case 44:  //partly cloudy
                return 801;
            case 45:  //thundershowers
            case 47:  //isolated thundershowers
                return 211;
            case 3200:  //not available
            default:
                return -1;
        }
    }

    public static String getConditionString(int openWeatherMapCondition) {
        Prefs prefs = GBApplication.getPrefs ();
        String lang = prefs.getString("weather_language", "en");
        boolean shortPhrases = prefs.getBoolean ("weather_short_phrases", false);

        if ("ru".equals (lang)) {
            if (!shortPhrases)
            switch (openWeatherMapCondition) {
                case 200:
                    return "гроза с небольшим дождём";
                case 201:
                    return "гроза с дождем";
                case 202:
                    return "гроза с проливным дождём";
                case 210:
                    return "легкая гроза";
                case 211:
                    return "гроза";
                case 230:
                    return "гроза с лёгкой моросью";
                case 231:
                    return "гроза с моросью";
                case 232:
                    return "гроза с сильным моросящим дождём";
                case 212:
                    return "сильная гроза";
                case 221:
                    return "рваная гроза";
                //Group 3xx: Drizzle
                case 300:
                    return "лёгкая морось";
                case 301:
                    return "морось";
                case 302:
                    return "сильная морось";
                case 310:
                    return "лёгкий моросящий дождь";
                case 311:
                    return "моросящий дождь";
                case 312:
                    return "сильный моросящий дождь";
                case 313:
                    return "ливень и морось";
                case 314:
                    return "сильный ливень и морось";
                case 321:
                    return "моросящий дождь";
                //Group 5xx: Rain
                case 500:
                    return "лёгкий дождь";
                case 501:
                    return "дождь";
                case 502:
                    return "сильный дождь";
                case 503:
                    return "очень сильный дождь";
                case 504:
                    return "экстримальный дождь";
                case 511:
                    return "холодный дождь";
                case 520:
                    return "лёгкий дождь";
                case 521:
                    return "ливень";
                case 522:
                    return "сильный ливень";
                case 531:
                    return "рваный ливень";
                //Group 6xx: Snow
                case 600:
                    return "небольшой снег";
                case 601:
                    return "снег";
                case 620:
                    return "лёгкий снегопад";
                case 602:
                    return "сильный снег";
                case 611:
                    return "дождь со снегом";
                case 612:
                    return "ливень со снегом";
                case 621:
                    return "снегопад";
                case 622:
                    return "сильный снегопад";
                case 615:
                    return "лёгкий дождь со снегом";
                case 616:
                    return "дождь со снегом";
                //Group 7xx: Atmosphere
                case 701:
                    return "туман";
                case 711:
                    return "туман";
                case 721:
                    return "густой туман";
                case 731:
                    return "песчанная буря";
                case 741:
                    return "густой туман";
                case 751:
                    return "песок";
                case 761:
                    return "пыль";
                case 762:
                    return "вулканический пепел";
                case 771:
                    return "шквал";
                case 781:
                    return "торнадо";
                case 900:
                    return "торнадо";
                case 800:
                    return "чистое небо";
                //Group 80x: Clouds
                case 801:
                    return "малооблачно";
                case 802:
                    return "рассеянные облака";
                case 803:
                    return "облачность";
                case 804:
                    return "пасмурно";
                //Group 90x: Extreme
                case 901:
                    return "тропический шторм";
                case 903:
                    return "холодно";
                case 904:
                    return "жарко";
                case 905:
                    return "ветренно";
                case 906:
                    return "град";
                //Group 9xx: Additional
                case 951:
                    return "штиль";
                case 952:
                    return "лёгкий ветер";
                case 953:
                    return "слабый ветер";
                case 954:
                    return "ветер";
                case 955:
                    return "свежий бриз";
                case 956:
                    return "сильный ветер";
                case 957:
                    return "штормовой ветер";
                case 958:
                    return "шторм";
                case 959:
                    return "сильный шторм";
                case 960:
                    return "шторм";
                case 961:
                    return "сильный шторм";
                case 902:
                    return "ураган";
                case 962:
                    return "ураган";
            }

            switch (openWeatherMapCondition)
            {
                case 200:
                    return "ГPO3A,ДOЖДb";
                case 201:
                    return "ГPO3A,ДOЖДb";
                case 202:
                    return "ГPO3A,ДOЖДb";
                case 210:
                    return "ГPO3A";
                case 211:
                    return "ГPO3A";
                case 230:
                    return "ГPO3A,MOPOCb";
                case 231:
                    return "ГPO3A,MOPOCb";
                case 232:
                    return "ГPO3A,ДOЖДb";
                case 212:
                    return "CИЛH.ГPO3A";
                case 221:
                    return "CИЛH.ГPO3A";
                //Group 3xx: Drizzle
                case 300:
                    return "ЛEГK.MOPOCb";
                case 301:
                    return "MOPOCb";
                case 302:
                    return "CИЛH.MOPOCb";
                case 310:
                    return "ЛEГK.ДOЖДb";
                case 311:
                    return "ДOЖДb";
                case 312:
                    return "CИЛH.ДOЖДb";
                case 313:
                    return "ЛИBEHb";
                case 314:
                    return "CИЛH.ЛИBEHb";
                case 321:
                    return "ДOЖДb";
                //Group 5xx: Rain
                case 500:
                    return "ЛEГK.ДOЖДb";
                case 501:
                    return "ДOЖДb";
                case 502:
                    return "CИЛH.ДOЖДb";
                case 503:
                    return "OЧ.CИЛ.ДOЖДb";
                case 504:
                    return "ЭKCTP.ДOЖДb";
                case 511:
                    return "XOЛ.ДOЖДb";
                case 520:
                    return "ЛEГK.ДOЖДb";
                case 521:
                    return "ЛИBEHb";
                case 522:
                    return "CИЛH.ЛИBEHb";
                case 531:
                    return "CИЛH.ЛИBEHb";
                //Group 6xx: Snow
                case 600:
                    return "HEБOЛ.CHEГ";
                case 601:
                    return "CHEГ";
                case 620:
                    return "ЛEГK.CHEГOПAД";
                case 602:
                    return "CИЛH.CHEГ";
                case 611:
                    return "ДOЖДb-CHEГ";
                case 612:
                    return "ЛИBEHb-CHEГ";
                case 621:
                    return "CHEГOПAД";
                case 622:
                    return "CИЛH.CHEГOПAД";
                case 615:
                    return "ДOЖДb-CHEГ";
                case 616:
                    return "ДOЖДb-CHEГ";
                //Group 7xx: Atmosphere
                case 701:
                    return "TУMAH";
                case 711:
                    return "TУMAH";
                case 721:
                    return "ГУCT.TУMAH";
                case 731:
                    return "ПECЧAH.БУPЯ";
                case 741:
                    return "ГУCTOЙ TУMAH";
                case 751:
                    return "ПECOK";
                case 761:
                    return "ПЫЛb";
                case 762:
                    return "BУЛK.ПEПEЛ";
                case 771:
                    return "ШKBAЛ";
                case 781:
                    return "TOPHAДO";
                case 900:
                    return "TOPHAДO";
                case 800:
                    return "ЧИCTOE HEБO";
                //Group 80x: Clouds
                case 801:
                    return "MAЛOOБЛAЧHO";
                case 802:
                    return "PACCEЯH.OБЛ";
                case 803:
                    return "OБЛAЧHOCTB";
                case 804:
                    return "ПACMУPHO";
                //Group 90x: Extreme
                case 901:
                    return "TPOПИЧ.ШTOPM";
                case 903:
                    return "XOЛOДHO";
                case 904:
                    return "ЖAPKO";
                case 905:
                    return "BETPEHHO";
                case 906:
                    return "ГPAД";
                //Group 9xx: Additional
                case 951:
                    return "ШTИЛb";
                case 952:
                    return "ЛEГK.BETEP";
                case 953:
                    return "CЛAБ.BETEP";
                case 954:
                    return "BETEP";
                case 955:
                    return "CBEЖ.БPИ3";
                case 956:
                    return "CИЛH.BETEP";
                case 957:
                    return "ШTOPM.BETEP";
                case 958:
                    return "ШTOPM";
                case 959:
                    return "CИЛH.ШTOPM";
                case 960:
                    return "ШTOPM";
                case 961:
                    return "CИЛH.ШTOPM";
                case 902:
                    return "УPAГAH";
                case 962:
                    return "УPAГAH";
            }
        }
        else if ("es".equals (lang))
        {
            switch (openWeatherMapCondition)
            {
                case 200:
                    return "tormenta con lluvias ligeras";
                case 201:
                    return "tormenta con lluvias";
                case 202:
                    return "tormenta con fuertes lluvias";
                case 210:
                    return "tormenta ligera";
                case 211:
                    return "tormenta eléctrica";
                case 230:
                    return "tormenta con lloviznas ligeras";
                case 231:
                    return "tormenta con lloviznas";
                case 232:
                    return "tormenta con fuertes lloviznas";
                case 212:
                    return "tormenta fuerte";
                case 221:
                    return "tormenta irregular";
                //Group 3xx: Drizzle
                case 300:
                    return "llovizna ligera";
                case 301:
                    return "llovizna";
                case 302:
                    return "llovizna intensa";
                case 310:
                    return "lluvia y llovizna ligera";
                case 311:
                    return "lluvia con llovizna";
                case 312:
                    return "lluvia y llovizna intensa";
                case 313:
                    return "lluvia y llovizna breve";
                case 314:
                    return "lluvia y llovizna breve e intensa";
                case 321:
                    return "llovizna breve";
                //Group 5xx: Rain
                case 500:
                    return "lluvia ligera";
                case 501:
                    return "lluvia moderada";
                case 502:
                    return "lluvia intensa";
                case 503:
                    return "lluvia muy intensa";
                case 504:
                    return "lluvia extrema";
                case 511:
                    return "lluvia helada";
                case 520:
                    return "lluvia breve y ligera";
                case 521:
                    return "lluvia breve";
                case 522:
                    return "lluvia breve e intensa";
                case 531:
                    return "lluvia breve e irregular";
                //Group 6xx: Snow
                case 600:
                    return "nevada ligera";
                case 601:
                    return "nevada";
                case 620:
                    return "nevada breve y ligera";
                case 602:
                    return "nevada intensa";
                case 611:
                    return "aguanieve";
                case 612:
                    return "aguanieve breve";
                case 621:
                    return "nevada breve";
                case 622:
                    return "nevada breve e intensa";
                case 615:
                    return "lluvia y nieve ligera";
                case 616:
                    return "lluvia y nieve";
                //Group 7xx: Atmosphere
                case 701:
                    return "neblina";
                case 711:
                    return "humo";
                case 721:
                    return "neblina";
                case 731:
                    return "remolinos de polvo y arena";
                case 741:
                    return "niebla";
                case 751:
                    return "arena";
                case 761:
                    return "polvo";
                case 762:
                    return "ceniza volcánica";
                case 771:
                    return "chubascos";
                case 781:
                    return "tornado";
                case 900:
                    return "tornado";
                case 800:
                    return "cielo despejado";
                //Group 80x: Clouds
                case 801:
                    return "pocas nubes";
                case 802:
                    return "nubes dispersas";
                case 803:
                    return "nubes separadas";
                case 804:
                    return "cielos cubiertos";
                //Group 90x: Extreme
                case 901:
                    return "tormenta tropical";
                case 903:
                    return "frio";
                case 904:
                    return "caluroso";
                case 905:
                    return "ventoso";
                case 906:
                    return "granizo";
                //Group 9xx: Additional
                case 951:
                    return "calmado";
                case 952:
                    return "brisa leve";
                case 953:
                    return "brisa suave";
                case 954:
                    return "brisa moderada";
                case 955:
                    return "brisa fresca";
                case 956:
                    return "brisa fuerte";
                case 957:
                    return "viento intenso";
                case 958:
                    return "vendaval";
                case 959:
                    return "vendaval severo";
                case 960:
                    return "tormenta";
                case 961:
                    return "tormenta violenta";
                case 902:
                    return "huracán";
                case 962:
                    return "huracán";
            }
        }
        else if ("pl".equals (lang))
        {
            switch (openWeatherMapCondition)
            {
                case 200:
                    return "burza";
                case 201:
                    return "burza";
                case 202:
                    return "burza";
                case 210:
                    return "burza:";
                case 211:
                    return "burza";
                case 230:
                    return "burza";
                case 231:
                    return "burza";
                case 232:
                    return "burza";
                case 212:
                    return "burza";
                case 221:
                    return "burza";
                //Group 3xx: Drizzle
                case 300:
                    return "mżawka";
                case 301:
                    return "mżawka";
                case 302:
                    return "mżawka";
                case 310:
                    return "mżawka";
                case 311:
                    return "mżawka";
                case 312:
                    return "mżawka";
                case 313:
                    return "mżawka";
                case 314:
                    return "mżawka";
                case 321:
                    return "mżawka";
                //Group 5xx: Rain
                case 500:
                    return "deszcz";
                case 501:
                    return "deszcz";
                case 502:
                    return "deszcz";
                case 503:
                    return "deszcz";
                case 504:
                    return "deszcz";
                case 511:
                    return "deszcz";
                case 520:
                    return "deszcz";
                case 521:
                    return "deszcz";
                case 522:
                    return "deszcz";
                case 531:
                    return "deszcz";
                //Group 6xx: Snow
                case 600:
                    return "Śnieg";
                case 601:
                    return "Śnieg";
                case 620:
                    return "Śnieg";
                case 602:
                    return "Śnieg";
                case 611:
                    return "Śnieg";
                case 612:
                    return "Śnieg";
                case 621:
                    return "Śnieg";
                case 622:
                    return "Śnieg";
                case 615:
                    return "Śnieg";
                case 616:
                    return "Śnieg";
                //Group 7xx: Atmosphere
                case 701:
                    return "mgła";
                case 711:
                    return "mgła";
                case 721:
                    return "mgła";
                case 731:
                    return "burza piaskowa";
                case 741:
                    return "mgła";
                case 751:
                    return "piasek";
                case 761:
                    return "pył";
                case 762:
                    return "pył wulkaniczny";
                case 771:
                    return "szkwały";
                case 781:
                    return "tornado";
                case 900:
                    return "tornado";
                case 800:
                    return "czyste niebo";
                //Group 80x: Clouds
                case 801:
                    return "pochmurnie";
                case 802:
                    return "pochmurnie";
                case 803:
                    return "pochmurnie";
                case 804:
                    return "pochmurnie";
                //Group 90x: Extreme
                case 901:
                    return "tropical storm";
                case 903:
                    return "cold";
                case 904:
                    return "hot";
                case 905:
                    return "windy";
                case 906:
                    return "hail";
                //Group 9xx: Additional
                case 951:
                    return "spokojnie";
                case 952:
                    return "bryza";
                case 953:
                    return "bryza";
                case 954:
                    return "bryza";
                case 955:
                    return "bryza";
                case 956:
                    return "bryza";
                case 957:
                    return "wichura";
                case 958:
                    return "wichura";
                case 959:
                    return "wichura";
                case 960:
                    return "sztorm";
                case 961:
                    return "sztorm";
                case 902:
                    return "huragan";
                case 962:
                    return "huragan";
            }
        }

        switch (openWeatherMapCondition) {
            case 200:
                return "thunderstorm with light rain";
            case 201:
                return "thunderstorm with rain";
            case 202:
                return "thunderstorm with heavy rain";
            case 210:
                return "light thunderstorm:";
            case 211:
                return "thunderstorm";
            case 230:
                return "thunderstorm with light drizzle";
            case 231:
                return "thunderstorm with drizzle";
            case 232:
                return "thunderstorm with heavy drizzle";
            case 212:
                return "heavy thunderstorm";
            case 221:
                return "ragged thunderstorm";
            //Group 3xx: Drizzle
            case 300:
                return "light intensity drizzle";
            case 301:
                return "drizzle";
            case 302:
                return "heavy intensity drizzle";
            case 310:
                return "light intensity drizzle rain";
            case 311:
                return "drizzle rain";
            case 312:
                return "heavy intensity drizzle rain";
            case 313:
                return "shower rain and drizzle";
            case 314:
                return "heavy shower rain and drizzle";
            case 321:
                return "shower drizzle";
            //Group 5xx: Rain
            case 500:
                return "light rain";
            case 501:
                return "moderate rain";
            case 502:
                return "heavy intensity rain";
            case 503:
                return "very heavy rain";
            case 504:
                return "extreme rain";
            case 511:
                return "freezing rain";
            case 520:
                return "light intensity shower rain";
            case 521:
                return "shower rain";
            case 522:
                return "heavy intensity shower rain";
            case 531:
                return "ragged shower rain";
            //Group 6xx: Snow
            case 600:
                return "light snow";
            case 601:
                return "snow";
            case 620:
                return "light shower snow";
            case 602:
                return "heavy snow";
            case 611:
                return "sleet";
            case 612:
                return "shower sleet";
            case 621:
                return "shower snow";
            case 622:
                return "heavy shower snow";
            case 615:
                return "light rain and snow";
            case 616:
                return "rain and snow";
            //Group 7xx: Atmosphere
            case 701:
                return "mist";
            case 711:
                return "smoke";
            case 721:
                return "haze";
            case 731:
                return "sandcase dust whirls";
            case 741:
                return "fog";
            case 751:
                return "sand";
            case 761:
                return "dust";
            case 762:
                return "volcanic ash";
            case 771:
                return "squalls";
            case 781:
                return "tornado";
            case 900:
                return "tornado";
            case 800:
                return "clear sky";
            //Group 80x: Clouds
            case 801:
                return "few clouds";
            case 802:
                return "scattered clouds";
            case 803:
                return "broken clouds";
            case 804:
                return "overcast clouds";
            //Group 90x: Extreme
            case 901:
                return "tropical storm";
            case 903:
                return "cold";
            case 904:
                return "hot";
            case 905:
                return "windy";
            case 906:
                return "hail";
            //Group 9xx: Additional
            case 951:
                return "calm";
            case 952:
                return "light breeze";
            case 953:
                return "gentle breeze";
            case 954:
                return "moderate breeze";
            case 955:
                return "fresh breeze";
            case 956:
                return "strong breeze";
            case 957:
                return "high windcase near gale";
            case 958:
                return "gale";
            case 959:
                return "severe gale";
            case 960:
                return "storm";
            case 961:
                return "violent storm";
            case 902:
                return "hurricane";
            case 962:
                return "hurricane";
            default:
                return "";
        }
    }

    public static byte mapToZeTimeConditionOld(int openWeatherMapCondition) {
/* deducted values:
    0 = partly cloudy
    1 = cloudy
    2 = sunny
    3 = windy/gale
    4 = heavy rain
    5 = snowy
    6 = storm
 */
        switch (openWeatherMapCondition) {
//Group 2xx: Thunderstorm
            case 200:  //thunderstorm with light rain:  //11d
            case 201:  //thunderstorm with rain:  //11d
            case 202:  //thunderstorm with heavy rain:  //11d
            case 210:  //light thunderstorm::  //11d
            case 211:  //thunderstorm:  //11d
            case 230:  //thunderstorm with light drizzle:  //11d
            case 231:  //thunderstorm with drizzle:  //11d
            case 232:  //thunderstorm with heavy drizzle:  //11d
            case 212:  //heavy thunderstorm:  //11d
            case 221:  //ragged thunderstorm:  //11d
//Group 7xx: Atmosphere
            case 771:  //squalls:  //[[file:50d.png]]
            case 781:  //tornado:  //[[file:50d.png]]
//Group 90x: Extreme
            case 900:  //tornado
            case 901:  //tropical storm
//Group 9xx: Additional
            case 960:  //storm
            case 961:  //violent storm
            case 902:  //hurricane
            case 962:  //hurricane
                return 6;
//Group 3xx: Drizzle
            case 300:  //light intensity drizzle:  //09d
            case 301:  //drizzle:  //09d
            case 302:  //heavy intensity drizzle:  //09d
            case 310:  //light intensity drizzle rain:  //09d
            case 311:  //drizzle rain:  //09d
            case 312:  //heavy intensity drizzle rain:  //09d
            case 313:  //shower rain and drizzle:  //09d
            case 314:  //heavy shower rain and drizzle:  //09d
            case 321:  //shower drizzle:  //09d
//Group 5xx: Rain
            case 500:  //light rain:  //10d
            case 501:  //moderate rain:  //10d
            case 502:  //heavy intensity rain:  //10d
            case 503:  //very heavy rain:  //10d
            case 504:  //extreme rain:  //10d
            case 511:  //freezing rain:  //13d
            case 520:  //light intensity shower rain:  //09d
            case 521:  //shower rain:  //09d
            case 522:  //heavy intensity shower rain:  //09d
            case 531:  //ragged shower rain:  //09d
//Group 90x: Extreme
            case 906:  //hail
                return 4;
//Group 6xx: Snow
            case 600:  //light snow:  //[[file:13d.png]]
            case 601:  //snow:  //[[file:13d.png]]
            case 620:  //light shower snow:  //[[file:13d.png]]
            case 602:  //heavy snow:  //[[file:13d.png]]
            case 611:  //sleet:  //[[file:13d.png]]
            case 612:  //shower sleet:  //[[file:13d.png]]
            case 621:  //shower snow:  //[[file:13d.png]]
            case 622:  //heavy shower snow:  //[[file:13d.png]]
            case 615:  //light rain and snow:  //[[file:13d.png]]
            case 616:  //rain and snow:  //[[file:13d.png]]
//Group 90x: Extreme
            case 903:  //cold
                return 5;
//Group 7xx: Atmosphere
            case 701:  //mist:  //[[file:50d.png]]
            case 711:  //smoke:  //[[file:50d.png]]
            case 721:  //haze:  //[[file:50d.png]]
            case 731:  //sandcase  dust whirls:  //[[file:50d.png]]
            case 741:  //fog:  //[[file:50d.png]]
            case 751:  //sand:  //[[file:50d.png]]
            case 761:  //dust:  //[[file:50d.png]]
            case 762:  //volcanic ash:  //[[file:50d.png]]
                return 1;
//Group 800: Clear
            case 800:  //clear sky:  //[[file:01d.png]] [[file:01n.png]]
//Group 90x: Extreme
            case 904:  //hot
                return 2;
//Group 80x: Clouds
            case 801:  //few clouds:  //[[file:02d.png]] [[file:02n.png]]
            case 802:  //scattered clouds:  //[[file:03d.png]] [[file:03d.png]]
            case 803:  //broken clouds:  //[[file:04d.png]] [[file:03d.png]]
            case 804:  //overcast clouds:  //[[file:04d.png]] [[file:04d.png]]
            default:
                return 0;

//Group 9xx: Additional
            case 905:  //windy
            case 951:  //calm
            case 952:  //light breeze
            case 953:  //gentle breeze
            case 954:  //moderate breeze
            case 955:  //fresh breeze
            case 956:  //strong breeze
            case 957:  //high windcase  near gale
            case 958:  //gale
            case 959:  //severe gale
                return 3;
        }
    }

    public static byte mapToZeTimeCondition(int openWeatherMapCondition) {
/* deducted values:
    0 = tornado
    1 = typhoon
    2 = hurricane
    3 = thunderstorm
    4 = rain and snow
    5 = unavailable
    6 = freezing rain
    7 = drizzle
    8 = showers
    9 = snow flurries
    10 = blowing snow
    11 = snow
    12 = sleet
    13 = foggy
    14 = windy
    15 = cloudy
    16 = partly cloudy (night)
    17 = partly cloudy (day)
    18 = clear night
    19 = sunny
    20 = thundershower
    21 = hot
    22 = scattered thunders
    23 = snow showers
    24 = heavy snow
 */
        switch (openWeatherMapCondition) {
//Group 2xx: Thunderstorm
            case 210:  //light thunderstorm::  //11d
                return 22;

//Group 2xx: Thunderstorm
            case 200:  //thunderstorm with light rain:  //11d
            case 201:  //thunderstorm with rain:  //11d
            case 202:  //thunderstorm with heavy rain:  //11d
            case 230:  //thunderstorm with light drizzle:  //11d
            case 231:  //thunderstorm with drizzle:  //11d
            case 232:  //thunderstorm with heavy drizzle:  //11d
                return 20;

//Group 2xx: Thunderstorm
            case 211:  //thunderstorm:  //11d
            case 212:  //heavy thunderstorm:  //11d
            case 221:  //ragged thunderstorm:  //11d
                return 3;

//Group 7xx: Atmosphere
            case 781:  //tornado:  //[[file:50d.png]]
//Group 90x: Extreme
            case 900:  //tornado
                return 0;

//Group 90x: Extreme
            case 901:  //tropical storm
                return 1;

// Group 7xx: Atmosphere
            case 771:  //squalls:  //[[file:50d.png]]
//Group 9xx: Additional
            case 960:  //storm
            case 961:  //violent storm
            case 902:  //hurricane
            case 962:  //hurricane
                return 2;

//Group 3xx: Drizzle
            case 300:  //light intensity drizzle:  //09d
            case 301:  //drizzle:  //09d
            case 302:  //heavy intensity drizzle:  //09d
            case 310:  //light intensity drizzle rain:  //09d
            case 311:  //drizzle rain:  //09d
            case 312:  //heavy intensity drizzle rain:  //09d
            case 313:  //shower rain and drizzle:  //09d
            case 314:  //heavy shower rain and drizzle:  //09d
            case 321:  //shower drizzle:  //09d
                return 7;

//Group 5xx: Rain
            case 500:  //light rain:  //10d
            case 501:  //moderate rain:  //10d
            case 502:  //heavy intensity rain:  //10d
            case 503:  //very heavy rain:  //10d
            case 504:  //extreme rain:  //10d
            case 520:  //light intensity shower rain:  //09d
            case 521:  //shower rain:  //09d
            case 522:  //heavy intensity shower rain:  //09d
            case 531:  //ragged shower rain:  //09d
//Group 90x: Extreme
            case 906:  //hail
                return 8;

//Group 5xx: Rain
            case 511:  //freezing rain:  //13d
                return 6;

//Group 6xx: Snow
            case 620:  //light shower snow:  //[[file:13d.png]]
            case 621:  //shower snow:  //[[file:13d.png]]
            case 622:  //heavy shower snow:  //[[file:13d.png]]
                return 23;

//Group 6xx: Snow
            case 615:  //light rain and snow:  //[[file:13d.png]]
            case 616:  //rain and snow:  //[[file:13d.png]]
                return 4;

//Group 6xx: Snow
            case 611:  //sleet:  //[[file:13d.png]]
            case 612:  //shower sleet:  //[[file:13d.png]]
                return 12;

//Group 6xx: Snow
            case 600:  //light snow:  //[[file:13d.png]]
            case 601:  //snow:  //[[file:13d.png]]
                return 11;
//Group 6xx: Snow
            case 602:  //heavy snow:  //[[file:13d.png]]
                return 24;

//Group 7xx: Atmosphere
            case 701:  //mist:  //[[file:50d.png]]
            case 711:  //smoke:  //[[file:50d.png]]
            case 721:  //haze:  //[[file:50d.png]]
            case 731:  //sandcase  dust whirls:  //[[file:50d.png]]
            case 741:  //fog:  //[[file:50d.png]]
            case 751:  //sand:  //[[file:50d.png]]
            case 761:  //dust:  //[[file:50d.png]]
            case 762:  //volcanic ash:  //[[file:50d.png]]
                return 13;

//Group 800: Clear
            case 800:  //clear sky:  //[[file:01d.png]] [[file:01n.png]]
                return 19;

//Group 90x: Extreme
            case 904:  //hot
                return 21;

//Group 80x: Clouds
            case 801:  //few clouds:  //[[file:02d.png]] [[file:02n.png]]
            case 802:  //scattered clouds:  //[[file:03d.png]] [[file:03d.png]]
            case 803:  //broken clouds:  //[[file:04d.png]] [[file:03d.png]]
                return 17;

//Group 80x: Clouds
            case 804:  //overcast clouds:  //[[file:04d.png]] [[file:04d.png]]
                return 15;

//Group 9xx: Additional
            case 905:  //windy
            case 951:  //calm
            case 952:  //light breeze
            case 953:  //gentle breeze
            case 954:  //moderate breeze
            case 955:  //fresh breeze
            case 956:  //strong breeze
            case 957:  //high windcase  near gale
            case 958:  //gale
            case 959:  //severe gale
                return 14;

            default:
//Group 90x: Extreme
            case 903:  //cold
                return 5;
        }
    }
}
