package io.github.kkysen.quicktrip.app;

import io.github.kkysen.quicktrip.apis.google.maps.directions.order.WaypointOrderRequest;
import io.github.kkysen.quicktrip.io.MyFiles;
import io.github.kkysen.quicktrip.optimization.simulatedAnnealing.SimulatedAnnealer;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * 
 * 
 * @author Khyber Sen
 */
public class ItineraryScreenModel {
    
    private final List<NoDateDestination> noDateDests;
    
    private @Getter final int numPeople;
    private @Getter final int budget;
    private @Getter final LocalDate startDate;
    private @Getter final String origin;
    private @Getter final List<Destination> destinations;
    private final List<Hotel> hotels;
    private @Getter final int cost;
    
    private SearchArgs deserializeSearchArgs() {
        String json;
        try {
            json = MyFiles.read(Paths.get(QuickTripConstants.SEARCH_ARGS_PATH));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return QuickTripConstants.GSON.fromJson(json, SearchArgs.class);
    }
    
    private List<Destination> orderDestinations() {
        List<NoDateDestination> orderedDestinations;
        try {
            orderedDestinations = WaypointOrderRequest
                    .orderedDestinations(origin, noDateDests, NoDateDestination::getAddress);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final List<Destination> dests = new ArrayList<>(orderedDestinations.size());
        int numDaysSinceStart = 0;
        for (final NoDateDestination noDateDest : orderedDestinations) {
            final LocalDate startDate = this.startDate.plusDays(numDaysSinceStart);
            numDaysSinceStart += noDateDest.getNumDays();
            final LocalDate endDate = this.startDate.plusDays(numDaysSinceStart);
            dests.add(new Destination(noDateDest, startDate, endDate, numPeople));
        }
        return dests;
    }
    
    private Hotels findOptimalHotels() {
        System.out.println("scraping hotels");
        final Hotels originalHotels = new Hotels(destinations, budget);
        System.out.println("annealing");
        final SimulatedAnnealer<Hotels> annealer = new SimulatedAnnealer<>(originalHotels); // FIXME add tuning args
        annealer.search(); // FIXME add numIters
        return annealer.getMinState();
    }
    
    public ItineraryScreenModel() {
        final SearchArgs searchArgs = deserializeSearchArgs();
        numPeople = searchArgs.getNumPeople();
        budget = searchArgs.getBudget();
        startDate = searchArgs.getDate();
        origin = searchArgs.getOrigin();
        noDateDests = searchArgs.getDestinations();
        
        destinations = orderDestinations();
        
        final Hotels optimalHotels = findOptimalHotels();
        cost = optimalHotels.totalPrice();
        hotels = optimalHotels.getHotels();
        for (int i = 0; i < hotels.size(); i++) {
            destinations.get(i).setHotel(hotels.get(i));
        }
        
        hotels.forEach(System.out::println);
    }
    
}