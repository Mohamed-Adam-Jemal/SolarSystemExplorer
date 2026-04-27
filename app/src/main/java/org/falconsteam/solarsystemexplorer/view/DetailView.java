package org.falconsteam.solarsystemexplorer.view;

import java.util.Map;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DetailView extends BorderPane {

    public DetailView() {
        getStyleClass().add("detail-panel");
        showPlaceholder();
    }

    private void showPlaceholder() {
        Label lbl = new Label("✦  Select a body to explore");
        lbl.getStyleClass().add("placeholder-text");
        StackPane p = new StackPane(lbl);
        p.getStyleClass().add("detail-panel");
        setCenter(p);
    }

    public void display(CelestialBody body) {
        try {

            // ── Hero ──────────────────────────────────────────
            ImageView img = new ImageView();
            img.setFitWidth(90);
            img.setFitHeight(90);
            img.setPreserveRatio(true);
            try {
                Image image = PlanetAssets.loadImage(body.getName(), 120, 120);
                if (image != null) img.setImage(image);
            } catch (Exception ignored) {}

            // No background circle — plain wrapper, no style
            HBox imgWrap = new HBox(img);
            imgWrap.setMinSize(100, 100);
            imgWrap.setMaxSize(100, 100);
            imgWrap.setAlignment(Pos.CENTER);

            Label name = new Label(body.getName());
            name.getStyleClass().add("detail-name");

            Label badge = new Label(body.getType());
            badge.getStyleClass().addAll("detail-badge", PlanetAssets.getBadgeClass(body));

            HBox tagRow = new HBox(6);
            tagRow.setAlignment(Pos.CENTER_LEFT);
            tagRow.getChildren().add(badge);
            if (body.isHasRings())
                tagRow.getChildren().add(tagPill("Has Rings"));
            if (body.getMoons() > 0)
                tagRow.getChildren().add(tagPill(body.getMoons() + (body.getMoons() == 1 ? " Moon" : " Moons")));

            Label desc = new Label(body.getDescription());
            desc.getStyleClass().add("detail-desc");
            desc.setWrapText(true);

            VBox heroText = new VBox(6, name, tagRow, desc);
            heroText.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(heroText, Priority.ALWAYS);

            HBox hero = new HBox(16, imgWrap, heroText);
            hero.setAlignment(Pos.CENTER_LEFT);
            hero.setPadding(new Insets(0, 0, 4, 0));

            // ── Stats — 2-column grid ─────────────────────────
            String distStr = body.getDistanceFromSun() == 0
                    ? "Center" : String.format("%.2f AU", body.getDistanceFromSun());
            String tempStr = body.getSurfaceTemperatureMin() == body.getSurfaceTemperatureMax()
                    ? body.getSurfaceTemperatureMin() + " °C"
                    : body.getSurfaceTemperatureMin() + " / " + body.getSurfaceTemperatureMax() + " °C";

            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(10);
            statsGrid.setVgap(8);

            ColumnConstraints keyCol1 = new ColumnConstraints();
            keyCol1.setMinWidth(100);
            keyCol1.setPrefWidth(120);
            ColumnConstraints valCol1 = new ColumnConstraints();
            valCol1.setMinWidth(100);
            valCol1.setHgrow(Priority.ALWAYS);
            ColumnConstraints keyCol2 = new ColumnConstraints();
            keyCol2.setMinWidth(100);
            keyCol2.setPrefWidth(120);
            ColumnConstraints valCol2 = new ColumnConstraints();
            valCol2.setMinWidth(100);
            valCol2.setHgrow(Priority.ALWAYS);
            statsGrid.getColumnConstraints().addAll(keyCol1, valCol1, keyCol2, valCol2);

            String[][] statData = {
                { "Distance",        distStr },
                { "Radius",          String.format("%,.0f km", body.getRadiusKm()) },
                { "Mass",            String.format("%.2e kg", body.getMassKg()) },
                { "Gravity",         String.format("%.2f m/s²", body.getGravity()) },
                { "Orbital period",  String.format("%,d days", body.getOrbitalPeriodDays()) },
                { "Rotation period", String.format("%.2f days", body.getRotationPeriodDays()) },
                { "Surface temp",    tempStr },
                { "Moons",           String.valueOf(body.getMoons()) }
            };

            for (int i = 0; i < statData.length; i++) {
                int col = (i % 2) * 2;
                int row = i / 2;

                Label k = new Label(statData[i][0]);
                k.getStyleClass().add("stat-key");

                Label v = new Label(statData[i][1]);
                v.getStyleClass().add("stat-val");

                statsGrid.add(k, col,     row);
                statsGrid.add(v, col + 1, row);
            }

            // ── Atmosphere bars ───────────────────────────────
            // KEY FIX: fill's maxWidth is also bound so JavaFX cannot stretch it
            // beyond the computed value. Track uses a dark color, fill uses blue —
            // two visually distinct colors so the proportion is obvious.
            VBox atmoBox = new VBox(10);
            Map<String, Integer> atmo = body.getAtmosphereComposition();
            if (atmo != null && !atmo.isEmpty()) {
                for (Map.Entry<String, Integer> e : atmo.entrySet()) {
                    int pct = Math.max(0, Math.min(100, e.getValue()));

                    Label gasLbl = new Label(e.getKey());
                    gasLbl.getStyleClass().add("stat-key");
                    gasLbl.setMinWidth(70);
                    gasLbl.setPrefWidth(70);

                    // Full-width dark track
                    Region track = new Region();
                    track.getStyleClass().add("atmo-bar-track");
                    track.setPrefHeight(8);
                    track.setMinHeight(8);
                    track.setMaxHeight(8);
                    track.setMaxWidth(Double.MAX_VALUE);

                    // Blue fill — width strictly = pct% of track, never more
                    Region fill = new Region();
                    fill.getStyleClass().add("atmo-bar-fill");
                    fill.setPrefHeight(8);
                    fill.setMinHeight(8);
                    fill.setMaxHeight(8);
                    // Bind both pref AND max so the fill cannot overflow
                    fill.prefWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));
                    fill.maxWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));

                    StackPane barStack = new StackPane(track, fill);
                    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                    barStack.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(barStack, Priority.ALWAYS);

                    Label pctLbl = new Label(pct + "%");
                    pctLbl.getStyleClass().add("atmo-pct-label");
                    pctLbl.setMinWidth(36);
                    pctLbl.setAlignment(Pos.CENTER_RIGHT);

                    HBox row = new HBox(8, gasLbl, barStack, pctLbl);
                    row.setAlignment(Pos.CENTER_LEFT);
                    atmoBox.getChildren().add(row);
                }
            }

            // ── Notable features ──────────────────────────────
            FlowPane featuresPane = new FlowPane(6, 6);
            if (body.getNotableFeatures() != null)
                for (String f : body.getNotableFeatures()) {
                    Label chip = new Label(f);
                    chip.getStyleClass().add("feature-chip");
                    featuresPane.getChildren().add(chip);
                }

            // ── Missions ──────────────────────────────────────
            VBox missionsBox = new VBox(5);
            if (body.getExplorationMissions() != null)
                for (String m : body.getExplorationMissions()) {
                    Label ml = new Label("→  " + m);
                    ml.getStyleClass().add("mission-item");
                    ml.setWrapText(true);
                    missionsBox.getChildren().add(ml);
                }

            // ── Fun facts ─────────────────────────────────────
            VBox factsBox = new VBox(6);
            if (body.getFunFacts() != null)
                for (String f : body.getFunFacts()) {
                    Label fl = new Label("✦  " + f);
                    fl.getStyleClass().add("fun-fact-item");
                    fl.setWrapText(true);
                    factsBox.getChildren().add(fl);
                }

            // ── Assemble ──────────────────────────────────────
            VBox content = new VBox(18,
                hero,
                divider(),
                sectionLabel("Stats"),       statsGrid,
                divider(),
                sectionLabel("Atmosphere"),  atmoBox,
                divider(),
                sectionLabel("Notable features"),    featuresPane,
                divider(),
                sectionLabel("Exploration missions"), missionsBox,
                divider(),
                sectionLabel("Fun facts"),   factsBox
            );
            content.getStyleClass().add("detail-panel");

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.getStyleClass().add("detail-scroll");
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            setCenter(scroll);

        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Error: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            setCenter(new StackPane(err));
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private Region divider() {
        Region d = new Region();
        d.getStyleClass().add("divider");
        d.setMaxWidth(Double.MAX_VALUE);
        d.setPrefHeight(1);
        return d;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("detail-about-label");
        return l;
    }

    private Label tagPill(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("tag-pill");
        return l;
    }
}