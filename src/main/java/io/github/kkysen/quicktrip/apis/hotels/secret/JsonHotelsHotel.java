package io.github.kkysen.quicktrip.apis.hotels.secret;

import io.github.kkysen.quicktrip.app.data.Hotel;
import io.github.kkysen.quicktrip.json.Json;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * 
 * @author Khyber Sen
 */
@Json
@RequiredArgsConstructor
@Getter
public class JsonHotelsHotel implements Hotel {
    
    /*
     * FIXME this is unfinished
     * these are the main fields I want, but they're not all simple fields, but objects
     * JsonHotelsHotelsAdapter right now only does these fields w/o any error checking
     */
    
    private final String name;
    private final String phoneNumber;
    private final String address;
    private final String imgUrl;
    private final double rating;
    private final double distance;
    private final int price;
    
    @Override
    public String toString() {
        return "JsonHotelsHotel [name=" + name + ", phoneNumber=" + phoneNumber + ", address="
                + address + ", imgUrl=" + imgUrl + ", rating=" + rating + ", distance=" + distance
                + ", price=" + price + "]";
    }
    
}
