package io.github.kkysen.quicktrip.apis.google.flights;

import io.github.kkysen.quicktrip.json.Json;
import io.github.kkysen.quicktrip.reflect.Reflect;

import lombok.Getter;

@Json
@Getter
public class Leg {
    
    private String id;
    private String arrivalTime;
    private String departureTime;
    private String origin;
    private String destination;
    private String originTerminal;
    private String destinationTerminal;
    private String duration;
    
    @Override
    public String toString() {
        return Reflect.toString(this);
    }
    
}
