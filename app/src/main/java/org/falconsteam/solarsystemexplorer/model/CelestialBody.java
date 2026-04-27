package org.falconsteam.solarsystemexplorer.model;

import java.util.List;
import java.util.Map;

public abstract class CelestialBody {

    protected String name;
    protected double distanceFromSun;
    protected double radiusKm;
    protected double massKg;
    protected String description;

    // Animation
    protected int    orbitalPeriodDays;
    protected double rotationPeriodDays;
    protected double orbitalSpeedFactor;
    protected String color;
    protected int    displayRadius;

    // Detail
    protected int    surfaceTemperatureMin;
    protected int    surfaceTemperatureMax;
    protected int    moons;
    protected boolean hasRings;
    protected double gravity;
    protected Map<String, Integer> atmosphereComposition;
    protected List<String> notableFeatures;
    protected List<String> explorationMissions;
    protected List<String> funFacts;

    public CelestialBody(String name, double distanceFromSun, double radiusKm, double massKg,
                         String description, int orbitalPeriodDays, double rotationPeriodDays,
                         double orbitalSpeedFactor, String color, int displayRadius,
                         int surfaceTemperatureMin, int surfaceTemperatureMax,
                         int moons, boolean hasRings, double gravity,
                         Map<String, Integer> atmosphereComposition,
                         List<String> notableFeatures,
                         List<String> explorationMissions,
                         List<String> funFacts) {
        this.name                  = name;
        this.distanceFromSun       = distanceFromSun;
        this.radiusKm              = radiusKm;
        this.massKg                = massKg;
        this.description           = description;
        this.orbitalPeriodDays     = orbitalPeriodDays;
        this.rotationPeriodDays    = rotationPeriodDays;
        this.orbitalSpeedFactor    = orbitalSpeedFactor;
        this.color                 = color;
        this.displayRadius         = displayRadius;
        this.surfaceTemperatureMin = surfaceTemperatureMin;
        this.surfaceTemperatureMax = surfaceTemperatureMax;
        this.moons                 = moons;
        this.hasRings              = hasRings;
        this.gravity               = gravity;
        this.atmosphereComposition = atmosphereComposition;
        this.notableFeatures       = notableFeatures;
        this.explorationMissions   = explorationMissions;
        this.funFacts              = funFacts;
    }

    public String  getName()               { return name; }
    public double  getDistanceFromSun()    { return distanceFromSun; }
    public double  getRadiusKm()           { return radiusKm; }
    public double  getMassKg()             { return massKg; }
    public String  getDescription()        { return description; }
    public int     getOrbitalPeriodDays()  { return orbitalPeriodDays; }
    public double  getRotationPeriodDays() { return rotationPeriodDays; }
    public double  getOrbitalSpeedFactor() { return orbitalSpeedFactor; }
    public String  getColor()              { return color; }
    public int     getDisplayRadius()      { return displayRadius; }
    public int     getSurfaceTemperatureMin()        { return surfaceTemperatureMin; }
    public int     getSurfaceTemperatureMax()        { return surfaceTemperatureMax; }
    public int     getMoons()              { return moons; }
    public boolean isHasRings()            { return hasRings; }
    public double  getGravity()            { return gravity; }
    public Map<String, Integer> getAtmosphereComposition() { return atmosphereComposition; }
    public List<String> getNotableFeatures()    { return notableFeatures; }
    public List<String> getExplorationMissions(){ return explorationMissions; }
    public List<String> getFunFacts()      { return funFacts; }

    public abstract String getType();

    @Override
    public String toString() { return name; }
}