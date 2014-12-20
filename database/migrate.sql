--- Migrations ---
--- Every migration in this file should be able to be run an arbitrary number of times
--- This may require a table to track which migrations have been run


CREATE OR REPLACE FUNCTION create_types ()
  RETURNS void AS
$_$
BEGIN

    IF NOT EXISTS ( SELECT 1 FROM pg_type where typname = 'flag_color' )
    THEN
        CREATE TYPE flag_color AS ENUM ('GREEN', 'YELLOW', 'RED', 'CLOSED');
    END IF;

    IF NOT EXISTS( SELECT 1 FROM pg_type where typname = 'wind_direction' )
    THEN
        CREATE TYPE wind_direction AS ENUM ('NORTH', 'NORTHEAST', 'EAST', 'SOUTHEAST', 'SOUTH', 'SOUTHWEST', 'WEST', 'NORTHWEST');
    END IF;

END;
$_$ LANGUAGE plpgsql;

BEGIN;

    SELECT create_types();

END;

BEGIN;

    CREATE TABLE IF NOT EXISTS conditions (
        condition_id serial PRIMARY KEY,
        recorded_datetime timestamp NOT NULL,
        current_color flag_color NOT NULL,
        wind_speed real NOT NULL,
        wind_direction wind_direction NOT NULL
    );

END;
