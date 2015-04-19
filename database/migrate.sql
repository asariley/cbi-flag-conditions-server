--- Migrations ---
--- Every migration in this file should be able to be run an arbitrary number of times
--- This may require a table to track which migrations have been run


CREATE OR REPLACE FUNCTION create_types ()
  RETURNS void AS
$_$
BEGIN

    IF NOT EXISTS ( SELECT 1 FROM pg_type WHERE typname = 'flag_color' )
    THEN
        CREATE TYPE flag_color AS ENUM ('GREEN', 'YELLOW', 'RED', 'CLOSED');
    END IF;

    IF NOT EXISTS( SELECT 1 FROM pg_type WHERE typname = 'wind_direction' )
    THEN
        CREATE TYPE wind_direction AS ENUM ('NORTH', 'NORTHEAST', 'EAST', 'SOUTHEAST', 'SOUTH', 'SOUTHWEST', 'WEST', 'NORTHWEST');
    END IF;

    IF NOT EXISTS( SELECT 1 FROM pg_type WHERE typname = 'sky_condition' )
    THEN
        CREATE TYPE sky_condition AS ENUM ('SUN', 'OVERCAST', 'RAIN', 'THUNDERSTORM');
    END IF;

END;
$_$ LANGUAGE plpgsql;

BEGIN;

    SELECT create_types();

END;

BEGIN;

    CREATE TABLE IF NOT EXISTS condition (
        condition_id serial PRIMARY KEY,
        recorded_datetime timestamp NOT NULL,
        current_color flag_color NOT NULL,
        wind_speed real NOT NULL,
        wind_direction wind_direction NOT NULL,
        sunset varchar(5) NOT NULL,
        sky_condition sky_condition NOT NULL,
        temperature_farenheit real NOT NULL
    );

    CREATE TABLE IF NOT EXISTS device (
        device_id serial PRIMARY KEY,
        device_uuid varchar(36) UNIQUE NOT NULL,
        created_datetime timestamp DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS notification_pref (
        notification_pref_id serial PRIMARY KEY,
        device_id   integer NOT NULL REFERENCES device(device_id),
        address     varchar(255) NOT NULL,
        weekday     boolean NOT NULL,
        weekend     boolean NOT NULL,
        daytime     boolean NOT NULL,
        evening     boolean NOT NULL,
        red_flag    boolean NOT NULL,
        yellow_flag boolean NOT NULL,
        green_flag  boolean NOT NULL,
        closed_flag boolean NOT NULL
    );

END;






