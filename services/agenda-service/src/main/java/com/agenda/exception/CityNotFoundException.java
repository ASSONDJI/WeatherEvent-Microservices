package com.agenda.exception;

public class CityNotFoundException extends AgendaException {
    public CityNotFoundException(String city) {
        super(String.format("City not found: '%s'. Please check the city name and try again.", city),
              "CITY_NOT_FOUND");
    }
}
