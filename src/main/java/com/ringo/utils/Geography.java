package com.ringo.utils;

import com.ringo.dto.common.Coordinates;

public class Geography {
    public static int getDistance(Coordinates c1, Coordinates c2) {
        final double R = 6_371_000;

        double phi1 = c1.latitude() * Math.PI / 180;
        double phi2 = c2.latitude() * Math.PI / 180;
        double deltaPhi = (c2.latitude() - c1.latitude()) * Math.PI / 180;
        double deltaLambda = (c2.longitude() - c1.longitude()) * Math.PI / 180;

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);

        return (int)(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
