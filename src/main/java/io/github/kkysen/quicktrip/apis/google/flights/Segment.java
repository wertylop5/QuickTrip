package io.github.kkysen.quicktrip.apis.google.flights;

import io.github.kkysen.quicktrip.json.Json;
import io.github.kkysen.quicktrip.reflect.Reflect;

import java.util.List;

import lombok.Getter;

@Json
@Getter
public class Segment {
    
    private int duration;
    private OtherFlight flight;	//not the useful part
    private String id;
    private String cabin;
    private List<Leg> leg;	//almost always contains only one thing
    private int connectionDuration;
    
    @Override
    public String toString() {
        return Reflect.toString(this);
    }
    
}
