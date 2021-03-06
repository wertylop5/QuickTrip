package io.github.kkysen.quicktrip.apis.google;

import io.github.kkysen.quicktrip.json.Json;
import io.github.kkysen.quicktrip.json.PostProcessable;
import io.github.kkysen.quicktrip.reflect.Reflect;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * 
 * 
 * @author Khyber Sen
 */
@Json
@Getter
public class GoogleApiResponse implements PostProcessable {
    
    @SerializedName(value = "status", alternate = {"geocoder_status"}) // for Waypoint
    protected final String status;
    
    private boolean ok;
    private boolean impossible;
    
    public GoogleApiResponse(final String status) {
        this.status = status;
        //postDeserialize();
    }
    
    @Override
    public void postDeserialize() {
        PostProcessable.super.postDeserialize();
        System.out.println("post deserialized");
        ok = status.equals("OK");
        impossible = status.equals("ZERO_RESULTS");
    }
    
    @Override
    public String toString() {
        return Reflect.toString(this);
    }
    
    @Override
    public int hashCode() {
        return 31 * (1 + status.hashCode());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoogleApiResponse other = (GoogleApiResponse) obj;
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        return true;
    }
    
}
