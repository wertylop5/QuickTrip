package io.github.kkysen.quicktrip.apis.hotels;

import io.github.kkysen.quicktrip.apis.RenderedHtmlRequest;
import io.github.kkysen.quicktrip.app.Destination;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;

import lombok.RequiredArgsConstructor;

/**
 * 
 * 
 * @author Khyber Sen
 */
@RequiredArgsConstructor
public class HotelsScrapeRequest extends RenderedHtmlRequest<List<Hotel>> {
    
    private static final String BASE_URL = "https://www.hotels.com/search.do";
    
    private final String destination;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int numPeople;
    private final int numRooms;
    
    private Map<String, String> query;
    
    public HotelsScrapeRequest(final Destination dest) {
        this(
                dest.getAddress(), 
                dest.getStartDate(), 
                dest.getEndDate(), 
                dest.getNumPeople(), 
                dest.getNumRooms()
                );
    }
    
    private void addQuery(final String name, final String value) {
        query.put("q-" + name, value);
    }
    
    private void addRooms() {
        addQuery("rooms", String.valueOf(numRooms));
        for (int roomNum = 0; roomNum < numRooms; roomNum++) {
            final String room = "room-" + roomNum + "-";
            addQuery(room + "adults", "2");
            addQuery(room + "children", "0"); // not dealing w/ children yet
        }
        // if odd numPeople, last room should have only 1 adult
        if ((numPeople & 1) == 1) {
            addQuery("room-" + (numRooms - 1) + "-adults", "1");
        }
    }
    
    @Override
    protected void modifyQuery(final Map<String, String> query) {
        super.modifyQuery(query);
        this.query = query;
        addQuery("destination", destination);
        addQuery("check-in", startDate.toString());
        addQuery("check-out", endDate.toString());
        addRooms();
        query.put("sort-order", "DISTANCE_FROM_LANDMARK");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends List<Hotel>> getPojoClass() {
        return (Class<List<Hotel>>) (Class<?>) List.class;
    }
    
    @Override
    protected String getBaseUrl() {
        return BASE_URL;
    }
    
    @Override
    protected Path getRelativeCachePath() {
        return Paths.get("hotels");
    }
    
    @Override
    protected List<Hotel> parseHtml(final Document doc) {
        return doc.getElementsByClass("hotel-wrap")
                .parallelStream()
                .map(Hotel::new)
                .collect(Collectors.toList());
    }
    
}
