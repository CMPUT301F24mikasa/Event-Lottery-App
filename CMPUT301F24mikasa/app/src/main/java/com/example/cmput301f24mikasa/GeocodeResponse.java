package com.example.cmput301f24mikasa;

import java.util.List;

/**
 * GeocodeResponse class represents the response from a geocoding API, containing a list of results with location coordinates.
 */
public class GeocodeResponse {
    public List<Result> results;

    /**
     * represents an individual geocoding result containing geometry information (latitude and longitude).
     */
    public static class Result {
        public Geometry geometry;

        /**
         * Represents the geometry of a location, including latitude and longitude.
         */
        public static class Geometry {
            public double lat;
            public double lng;
        }
    }
}
