package com.ssafy.enjoytrip.web.mapper;

import com.ssafy.enjoytrip.domain.Point;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import java.util.ArrayList;
import java.util.List;

public final class RoutePointQueryParser {

    private RoutePointQueryParser() {
    }

    public static List<Point> parse(String raw, ErrorType errorType) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return List.of();
        }

        String[] chunks = value.split("\\|");
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < chunks.length; i++) {
            points.add(parsePair(chunks[i], i, errorType));
        }
        return points;
    }

    private static Point parsePair(String rawPair, int index, ErrorType errorType) {
        String[] pair = rawPair.split(",");
        if (pair.length != 2) {
            throw new CoreException(errorType);
        }

        double latitude = parseFiniteDouble(pair[0], errorType);
        double longitude = parseFiniteDouble(pair[1], errorType);
        return new Point(latitude, longitude, index);
    }

    private static double parseFiniteDouble(String raw, ErrorType errorType) {
        try {
            double value = Double.parseDouble(raw.strip());
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("not finite");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new CoreException(errorType);
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }
}
