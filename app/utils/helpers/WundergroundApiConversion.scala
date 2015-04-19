package utils.helpers

import models.{WindDirection, SkyCondition}

import SkyCondition.{SUNNY, CLOUDY, RAIN, TSTORM}
import WindDirection.{NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST}

object WundergroundApiConversion {
    /**
      * Produces Either a SkyCondition or returns the value that it could not convert
      * Source: http://www.wunderground.com/weather/api/d/docs?d=resources/phrase-glossary
      */
    def convertSkyCondition(weather: String): Either[String, SkyCondition.Value] = weather match {
        case w if w.startsWith("Heavy ") || w.startsWith("Light ") => convertSkyCondition(w.substring(6, w.length))
        case "Drizzle" => Right(RAIN)
        case "Rain" => Right(RAIN)
        case "Snow" => Right(RAIN)
        case "Snow Grains" => Right(RAIN)
        case "Ice Crystals" => Right(RAIN)
        case "Ice Pellets" => Right(RAIN)
        case "Hail" => Right(RAIN)
        case "Mist" => Right(RAIN)
        case "Fog" => Right(CLOUDY)
        case "Fog Patches" => Right(CLOUDY)
        case "Smoke" => Right(CLOUDY)
        case "Volcanic Ash" => Right(CLOUDY)
        case "Widespread Dust" => Right(CLOUDY)
        case "Sand" => Right(CLOUDY)
        case "Haze" => Right(CLOUDY)
        case "Spray" => Right(RAIN)
        case "Dust Whirls" => Right( CLOUDY)
        case "Sandstorm" => Right(CLOUDY)
        case "Low Drifting Snow" => Right(RAIN)
        case "Low Drifting Widespread Dust" => Right(CLOUDY)
        case "Low Drifting Sand" => Right(CLOUDY)
        case "Blowing Snow" => Right(RAIN)
        case "Blowing Widespread Dust" => Right(CLOUDY)
        case "Blowing Sand" => Right(CLOUDY)
        case "Rain Mist" => Right(RAIN)
        case "Rain Showers" => Right(RAIN)
        case "Snow Showers" => Right(RAIN)
        case "Snow Blowing Snow Mist" => Right(RAIN)
        case "Ice Pellet Showers" => Right(RAIN)
        case "Hail Showers" => Right(RAIN)
        case "Small Hail Showers" => Right(RAIN)
        case "Thunderstorm" => Right(TSTORM)
        case "Thunderstorms and Rain" => Right(TSTORM)
        case "Thunderstorms and Snow" => Right(TSTORM)
        case "Thunderstorms and Ice Pellets" => Right(TSTORM)
        case "Thunderstorms with Hail" => Right(TSTORM)
        case "Thunderstorms with Small Hail" => Right(TSTORM)
        case "Freezing Drizzle" => Right(RAIN)
        case "Freezing Rain" => Right(RAIN)
        case "Freezing Fog" => Right(CLOUDY)
        case "Patches of Fog" => Right(CLOUDY)
        case "Shallow Fog" => Right(CLOUDY)
        case "Partial Fog" => Right(CLOUDY)
        case "Overcast" => Right(CLOUDY)
        case "Clear" => Right(SUNNY)
        case "Partly Cloudy" => Right(SUNNY)
        case "Mostly Cloudy" => Right(CLOUDY)
        case "Scattered Clouds" => Right(SUNNY)
        case "Small Hail" => Right(RAIN)
        case "Squalls" => Right(RAIN)
        case "Funnel Cloud" => Right(CLOUDY)
        case "Unknown Precipitation" => Right(RAIN)
        case unknown => Left(s"can't convert $unknown to SkyCondition")
    }

    /** Produces Either a WindDirection or returns the value as a string that it could not convert */
    def convertWindDegreesToWindDirection(degrees: Double): Either[String, WindDirection.Value] = degrees match {
        case d if (0 <= d && d < 22.5) => Right(NORTH)
        case d if (22.5 <= d && d < 67.5) => Right(NORTHEAST)
        case d if (67.5 <= d && d < 112.5) => Right(EAST)
        case d if (112.5 <= d && d < 157.5) => Right(SOUTHEAST)
        case d if (157.5 <= d && d < 202.5) => Right(SOUTH)
        case d if (202.5 <= d && d < 247.5) => Right(SOUTHWEST)
        case d if (247.5 <= d && d < 292.5) => Right(WEST)
        case d if (292.5 <= d && d < 337.5) => Right(NORTHWEST)
        case d if (337.5 <= d && d <= 360) => Right(NORTH)
        case d => Left(s"can't convert $d degrees to cardinal direction")
    }

}