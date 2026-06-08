package com.ssafy.enjoytrip.domain;

public record Point(double lat, double lng, int index) {
    private static final double EARTH_RADIUS_KM = 6_371.0d;

    public double distanceKmTo(Point other) {
        double dLat = Math.toRadians(other.lat - lat);
        double dLng = Math.toRadians(other.lng - lng);
        double thisLat = Math.toRadians(lat);
        double otherLat = Math.toRadians(other.lat);

        double haversine = Math.sin(dLat / 2.0d) * Math.sin(dLat / 2.0d)
                + Math.cos(thisLat) * Math.cos(otherLat) * Math.sin(dLng / 2.0d) * Math.sin(dLng / 2.0d);
        return EARTH_RADIUS_KM * (2.0d * Math.atan2(Math.sqrt(haversine), Math.sqrt(1.0d - haversine)));
    }
}
