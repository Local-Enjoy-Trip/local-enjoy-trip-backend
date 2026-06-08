package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Point;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RouteOptimizationService {

    public int[] optimizeOrder(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new int[0];
        }
        if (points.size() == 1) {
            return new int[]{0};
        }

        int[] order = buildInitialNearestNeighborOrder(points);
        improveOrderWithTwoOpt(order, points);
        return order;
    }

    public double estimateTotalDistanceKm(List<Point> points, int[] order) {
        if (points == null || points.size() < 2 || order == null || order.length < 2) {
            return 0.0d;
        }

        double total = 0.0d;
        for (int i = 1; i < order.length; i++) {
            total += points.get(order[i - 1]).distanceKmTo(points.get(order[i]));
        }
        return total;
    }

    public SplitResult splitByLargestGap(List<Point> orderedPoints, int requestDays) {
        if (orderedPoints == null || orderedPoints.isEmpty()) {
            return emptySplitResult();
        }

        int pointCount = orderedPoints.size();
        int days = clampRequestedDays(requestDays, pointCount);
        if (pointCount == 1) {
            return splitSinglePointIntoOneDay();
        }

        double[] edgeDistances = measureEdgeDistances(orderedPoints);
        Set<Integer> cutIndexes = chooseLargestGapCutIndexes(edgeDistances, days);

        return splitPointsAtCutIndexes(edgeDistances, cutIndexes);
    }

    public List<Point> parsePoints(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return parseCoordinatePairs(raw);
    }

    public String formatDouble(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private static SplitResult emptySplitResult() {
        return new SplitResult(Collections.emptyList(), Collections.emptyList());
    }

    private static int clampRequestedDays(int requestDays, int pointCount) {
        return Math.max(1, Math.min(requestDays, pointCount));
    }

    private static SplitResult splitSinglePointIntoOneDay() {
        List<List<Integer>> oneDay = new ArrayList<>();
        oneDay.add(Collections.singletonList(0));
        List<Double> oneDistance = new ArrayList<>();
        oneDistance.add(0.0d);
        return new SplitResult(oneDay, oneDistance);
    }

    private double[] measureEdgeDistances(List<Point> orderedPoints) {
        int pointCount = orderedPoints.size();
        double[] edgeDistances = new double[pointCount - 1];
        for (int i = 1; i < pointCount; i++) {
            edgeDistances[i - 1] = orderedPoints.get(i - 1).distanceKmTo(orderedPoints.get(i));
        }
        return edgeDistances;
    }

    private Set<Integer> chooseLargestGapCutIndexes(double[] edgeDistances, int days) {
        List<EdgeGap> edgeGaps = new ArrayList<>();
        for (int i = 0; i < edgeDistances.length; i++) {
            edgeGaps.add(new EdgeGap(i + 1, edgeDistances[i]));
        }

        edgeGaps.sort(Comparator.comparingDouble(EdgeGap::distance).reversed());
        Set<Integer> cutIndexes = new HashSet<>();
        for (int i = 0; i < days - 1 && i < edgeGaps.size(); i++) {
            cutIndexes.add(edgeGaps.get(i).startIndex());
        }
        return cutIndexes;
    }

    private SplitResult splitPointsAtCutIndexes(double[] edgeDistances, Set<Integer> cutIndexes) {
        List<List<Integer>> resultDays = new ArrayList<>();
        List<Double> resultDistances = new ArrayList<>();

        List<Integer> current = new ArrayList<>();
        current.add(0);
        double currentDistance = 0.0d;

        for (int i = 1; i <= edgeDistances.length; i++) {
            double edgeDistance = edgeDistances[i - 1];
            if (cutIndexes.contains(i)) {
                resultDays.add(current);
                resultDistances.add(currentDistance);
                current = new ArrayList<>();
                current.add(i);
                currentDistance = 0.0d;
            } else {
                current.add(i);
                currentDistance += edgeDistance;
            }
        }

        resultDays.add(current);
        resultDistances.add(currentDistance);

        return new SplitResult(resultDays, resultDistances);
    }

    private List<Point> parseCoordinatePairs(String raw) {
        List<Point> points = new ArrayList<>();
        String[] chunks = raw.split("\\|");
        for (int i = 0; i < chunks.length; i++) {
            points.add(parseCoordinatePair(chunks[i], i));
        }
        return points;
    }

    private Point parseCoordinatePair(String rawPair, int index) {
        String[] pair = rawPair.split(",");
        if (pair.length != 2) {
            throw new IllegalArgumentException("invalid coordinate pair");
        }
        double lat = Double.parseDouble(pair[0].trim());
        double lng = Double.parseDouble(pair[1].trim());
        return new Point(lat, lng, index);
    }

    private int[] buildInitialNearestNeighborOrder(List<Point> points) {
        int count = points.size();
        int[] order = new int[count];
        boolean[] visited = new boolean[count];

        order[0] = 0;
        visited[0] = true;

        for (int step = 1; step < count; step++) {
            int currentIndex = order[step - 1];
            double bestDistance = Double.POSITIVE_INFINITY;
            int bestIndex = -1;

            for (int candidate = 0; candidate < count; candidate++) {
                if (visited[candidate]) {
                    continue;
                }
                double distance = points.get(currentIndex).distanceKmTo(points.get(candidate));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = candidate;
                }
            }

            if (bestIndex == -1) {
                break;
            }
            visited[bestIndex] = true;
            order[step] = bestIndex;
        }

        return order;
    }

    private void improveOrderWithTwoOpt(int[] order, List<Point> points) {
        if (order.length < 4) {
            return;
        }

        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < order.length - 2; i++) {
                for (int k = i + 1; k < order.length - 1; k++) {
                    double currentCost = segmentDistance(order[i - 1], order[i], points)
                            + segmentDistance(order[k], order[k + 1], points);
                    double swappedCost = segmentDistance(order[i - 1], order[k], points)
                            + segmentDistance(order[i], order[k + 1], points);

                    if (swappedCost + 1e-9d < currentCost) {
                        reverse(order, i, k);
                        improved = true;
                    }
                }
            }
        }
    }

    private double segmentDistance(int from, int to, List<Point> points) {
        return points.get(from).distanceKmTo(points.get(to));
    }

    private void reverse(int[] order, int left, int right) {
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

    private record EdgeGap(int startIndex, double distance) {
    }

    public record SplitResult(List<List<Integer>> days, List<Double> dayDistances) {
    }
}
