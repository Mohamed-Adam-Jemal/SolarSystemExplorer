package org.falconsteam.solarsystemexplorer.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;
import org.falconsteam.solarsystemexplorer.model.DwarfPlanet;
import org.falconsteam.solarsystemexplorer.model.Planet;
import org.falconsteam.solarsystemexplorer.model.Star;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlanetDataLoader {

    public List<CelestialBody> loadAll() {
        List<CelestialBody> bodies = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/data/planets.json")) {
            if (is == null) throw new RuntimeException("planets.json not found");

            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonArray();

            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();

                String name          = obj.get("name").getAsString();
                String type          = obj.get("type").getAsString();
                double dist          = obj.get("distanceFromSun").getAsDouble();
                double radius        = obj.get("radiusKm").getAsDouble();
                double mass          = obj.get("massKg").getAsDouble();
                String desc          = obj.get("description").getAsString();
                int    period        = obj.get("orbitalPeriodDays").getAsInt();
                double rotation      = obj.get("rotationPeriodDays").getAsDouble();
                double speedFactor   = obj.get("orbitalSpeedFactor").getAsDouble();
                String color         = obj.get("color").getAsString();
                int    displayRadius = obj.get("displayRadius").getAsInt();
                int    moons         = obj.get("moons").getAsInt();
                boolean hasRings     = obj.get("hasRings").getAsBoolean();
                double  gravity      = obj.get("gravity").getAsDouble();

                // Nested temperature
                JsonObject tempObj = obj.getAsJsonObject("surfaceTemperatureC");
                int tempMin = tempObj.get("min").getAsInt();
                int tempMax = tempObj.get("max").getAsInt();

                // Atmosphere composition
                Map<String, Integer> atmosphere = new LinkedHashMap<>();
                JsonObject atmoObj = obj.getAsJsonObject("atmosphereComposition");
                if (atmoObj != null) {
                    for (Map.Entry<String, JsonElement> entry : atmoObj.entrySet()) {
                        atmosphere.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                }

                // Notable features
                List<String> features = new ArrayList<>();
                JsonArray featArr = obj.getAsJsonArray("notableFeatures");
                if (featArr != null)
                    for (JsonElement f : featArr) features.add(f.getAsString());

                // Exploration missions
                List<String> missions = new ArrayList<>();
                JsonArray missArr = obj.getAsJsonArray("explorationMissions");
                if (missArr != null)
                    for (JsonElement m : missArr) missions.add(m.getAsString());

                // Fun facts
                List<String> funFacts = new ArrayList<>();
                JsonArray factsArr = obj.getAsJsonArray("funFacts");
                if (factsArr != null)
                    for (JsonElement f : factsArr) funFacts.add(f.getAsString());

                bodies.add(switch (type) {
                    case "Star"        -> new Star(name, dist, radius, mass, desc, period, rotation,
                                            speedFactor, color, displayRadius, tempMin, tempMax,
                                            moons, hasRings, gravity, atmosphere, features, missions, funFacts);
                    case "DwarfPlanet" -> new DwarfPlanet(name, dist, radius, mass, desc, period, rotation,
                                            speedFactor, color, displayRadius, tempMin, tempMax,
                                            moons, hasRings, gravity, atmosphere, features, missions, funFacts);
                    default            -> new Planet(name, dist, radius, mass, desc, period, rotation,
                                            speedFactor, color, displayRadius, tempMin, tempMax,
                                            moons, hasRings, gravity, atmosphere, features, missions, funFacts);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodies;
    }
}