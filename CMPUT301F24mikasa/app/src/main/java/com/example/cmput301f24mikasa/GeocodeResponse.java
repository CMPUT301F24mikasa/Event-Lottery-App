package com.example.cmput301f24mikasa;

import java.util.List;

public class GeocodeResponse {
    public List<Result> results;

    public static class Result {
        public Geometry geometry;

        public static class Geometry {
            public double lat;
            public double lng;
        }
    }
}
