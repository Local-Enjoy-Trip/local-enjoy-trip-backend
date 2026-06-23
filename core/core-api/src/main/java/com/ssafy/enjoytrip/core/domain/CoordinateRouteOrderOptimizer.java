package com.ssafy.enjoytrip.core.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.springframework.stereotype.Component;

@Component
public class CoordinateRouteOrderOptimizer {
    public <T> List<T> optimizeByDay(List<T> items,
                                     ToIntFunction<T> dayExtractor,
                                     Function<T, Double> latitudeExtractor,
                                     Function<T, Double> longitudeExtractor) {
        if (items == null || items.size() < 2) {
            return items == null ? List.of() : items;
        }

        Map<Integer, List<T>> itemsByDay = groupItemsByDay(items, dayExtractor);
        List<T> optimized = new ArrayList<>();
        for (List<T> dayItems : itemsByDay.values()) {
            optimized.addAll(optimizeDayItems(dayItems, latitudeExtractor, longitudeExtractor));
        }
        return optimized;
    }

    public <T> boolean hasOptimizableCoordinates(List<T> items,
                                                 Function<T, Double> latitudeExtractor,
                                                 Function<T, Double> longitudeExtractor) {
        if (items == null) {
            return true;
        }

        for (T item : items) {
            if (!isFiniteCoordinate(latitudeExtractor.apply(item), longitudeExtractor.apply(item))) {
                return false;
            }
        }
        return true;
    }

    private static <T> Map<Integer, List<T>> groupItemsByDay(List<T> items, ToIntFunction<T> dayExtractor) {
        Map<Integer, List<T>> itemsByDay = new LinkedHashMap<>();
        for (T item : items) {
            int day = Math.max(1, dayExtractor.applyAsInt(item));
            itemsByDay.computeIfAbsent(day, ignored -> new ArrayList<>()).add(item);
        }
        return itemsByDay;
    }

    private static <T> List<T> optimizeDayItems(List<T> items,
                                                Function<T, Double> latitudeExtractor,
                                                Function<T, Double> longitudeExtractor) {
        if (items.size() < 2 || !hasCompleteCoordinates(items, latitudeExtractor, longitudeExtractor)) {
            return items;
        }

        List<Point> points = toPoints(items, latitudeExtractor, longitudeExtractor);
        int[] optimizedOrder = optimizeVisitOrder(points);
        if (optimizedOrder.length != items.size()) {
            return items;
        }

        List<T> optimized = new ArrayList<>();
        for (int index : optimizedOrder) {
            optimized.add(items.get(index));
        }
        return optimized;
    }

    private static <T> boolean hasCompleteCoordinates(List<T> items,
                                                      Function<T, Double> latitudeExtractor,
                                                      Function<T, Double> longitudeExtractor) {
        for (T item : items) {
            if (!isFiniteCoordinate(latitudeExtractor.apply(item), longitudeExtractor.apply(item))) {
                return false;
            }
        }
        return true;
    }

    private static <T> List<Point> toPoints(List<T> items,
                                            Function<T, Double> latitudeExtractor,
                                            Function<T, Double> longitudeExtractor) {
        List<Point> points = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            T item = items.get(index);
            points.add(new Point(latitudeExtractor.apply(item), longitudeExtractor.apply(item), index));
        }
        return points;
    }

    private static boolean isFiniteCoordinate(Double latitude, Double longitude) {
        return latitude != null
                && longitude != null
                && Double.isFinite(latitude)
                && Double.isFinite(longitude);
    }

    private static int[] optimizeVisitOrder(List<Point> points) {
        if (points.isEmpty()) {
            return new int[0];
        }
        if (points.size() == 1) {
            return new int[]{0};
        }

        int[] order = buildNearestNeighborOrder(points);
        improveOrderWithTwoOpt(order, points);
        return order;
    }

    private static int[] buildNearestNeighborOrder(List<Point> points) {
        int count = points.size();
        int[] order = new int[count];
        boolean[] visited = new boolean[count];

        order[0] = 0;
        visited[0] = true;

        for (int step = 1; step < count; step++) {
            int currentIndex = order[step - 1];
            int bestIndex = findNearestUnvisitedPoint(currentIndex, points, visited);
            if (bestIndex == -1) {
                break;
            }

            visited[bestIndex] = true;
            order[step] = bestIndex;
        }
        return order;
    }

    private static int findNearestUnvisitedPoint(int currentIndex, List<Point> points, boolean[] visited) {
        double bestDistance = Double.POSITIVE_INFINITY;
        int bestIndex = -1;

        for (int candidate = 0; candidate < points.size(); candidate++) {
            if (visited[candidate]) {
                continue;
            }

            double distance = points.get(currentIndex).distanceKmTo(points.get(candidate));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = candidate;
            }
        }
        return bestIndex;
    }

    private static void improveOrderWithTwoOpt(int[] order, List<Point> points) {
        if (order.length < 4) {
            return;
        }

        boolean improved = true;
        while (improved) {
            improved = improveOnePass(order, points);
        }
    }

    private static boolean improveOnePass(int[] order, List<Point> points) {
        boolean improved = false;
        for (int left = 1; left < order.length - 2; left++) {
            for (int right = left + 1; right < order.length - 1; right++) {
                if (isSwapShorter(order, points, left, right)) {
                    reverse(order, left, right);
                    improved = true;
                }
            }
        }
        return improved;
    }

    private static boolean isSwapShorter(int[] order, List<Point> points, int left, int right) {
        double currentCost = segmentDistance(order[left - 1], order[left], points)
                + segmentDistance(order[right], order[right + 1], points);
        double swappedCost = segmentDistance(order[left - 1], order[right], points)
                + segmentDistance(order[left], order[right + 1], points);
        return swappedCost + 1e-9d < currentCost;
    }

    private static double segmentDistance(int from, int to, List<Point> points) {
        return points.get(from).distanceKmTo(points.get(to));
    }

    private static void reverse(int[] order, int left, int right) {
        int i = left;
        int j = right;
        while (i < j) {
            int temp = order[i];
            order[i] = order[j];
            order[j] = temp;
            i++;
            j--;
        }
    }
}
