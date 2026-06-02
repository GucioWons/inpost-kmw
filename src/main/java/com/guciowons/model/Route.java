package com.guciowons.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private final StartingPoint startingPoint;
    private final List<RoutePoint> points = new ArrayList<>();
    private BigDecimal distance = BigDecimal.ZERO;
    private BigDecimal returnDistance;

    public Route(StartingPoint startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Route(StartingPoint startingPoint, List<RoutePoint> points) {
        this.startingPoint = startingPoint;
        addAll(points);
    }

    public void add(RoutePoint point) {
        points.add(point);
        addDistance(point.distanceTo());
    }

    public void addAll(List<RoutePoint> points) {
        this.points.addAll(points);
    }

    private void addDistance(BigDecimal distance) {
        this.distance = this.distance.add(distance);
    }

    public StartingPoint getStartingPoint() {
        return startingPoint;
    }

    public List<RoutePoint> getPoints() {
        return points;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public BigDecimal getReturnDistance() {
        return returnDistance;
    }

    public void setReturnDistance(BigDecimal returnDistance) {
        this.returnDistance = returnDistance;
        addDistance(returnDistance);
    }

    public record RoutePoint(ParcelLocker locker, BigDecimal distanceTo) {}
}
