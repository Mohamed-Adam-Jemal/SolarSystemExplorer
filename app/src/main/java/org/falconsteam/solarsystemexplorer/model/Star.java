package org.falconsteam.solarsystemexplorer.model;

import java.util.List;
import java.util.Map;

public class Star extends CelestialBody {
    public Star(String name, double distanceFromSun, double radiusKm, double massKg,
                String description, int orbitalPeriodDays, double rotationPeriodDays,
                double orbitalSpeedFactor, String color, int displayRadius,
                int surfaceTemperatureMin, int surfaceTemperatureMax,
                int moons, boolean hasRings, double gravity,
                Map<String, Integer> atmosphereComposition,
                List<String> notableFeatures,
                List<String> explorationMissions,
                List<String> funFacts) {
        super(name, distanceFromSun, radiusKm, massKg, description,
              orbitalPeriodDays, rotationPeriodDays, orbitalSpeedFactor, color, displayRadius,
              surfaceTemperatureMin, surfaceTemperatureMax, moons, hasRings, gravity,
              atmosphereComposition, notableFeatures, explorationMissions, funFacts);
    }

    @Override public String getType() { return "Star"; }
}