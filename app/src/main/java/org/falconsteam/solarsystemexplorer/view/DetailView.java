package org.falconsteam.solarsystemexplorer.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * DetailView — right panel of the Solar System Explorer.
 *
 * Four internal modes:
 *   DETAIL      → planet info
 *   SELECTOR    → multi-select card list for comparison
 *   COMPARISON  → wide GridPane panel
 *   CALCULATOR  → distance calculator between two bodies
 */
public class DetailView extends BorderPane {

    @SuppressWarnings("unused")
    private final Pane overlay;
    private List<CelestialBody> allBodies;

    private Runnable onFirstDisplay = () -> {};
    private boolean firstDisplayFired = false;

    private Runnable onExpand  = () -> {};
    private Runnable onRestore = () -> {};

    private final List<CelestialBody> selectedForComparison = new ArrayList<>();

    // ── Interstellar palette ──────────────────────────────
    private static final String CYAN       = "#4fc3f7";
    private static final String CYAN_DIM   = "rgba(79,195,247,0.12)";
    private static final String CYAN_GLOW  = "rgba(79,195,247,0.40)";
    private static final String CYAN_MID   = "rgba(79,195,247,0.55)";
    private static final String GOLD       = "#ffd54f";
    private static final String GOLD_DIM   = "rgba(255,213,79,0.12)";
    private static final String GOLD_GLOW  = "rgba(255,213,79,0.40)";
    private static final String RED_SOFT   = "rgba(239,154,154,0.90)";
    private static final String PURPLE     = "#c084fc";
    private static final String PURPLE_DIM = "rgba(192,132,252,0.12)";
    private static final String BG_CARD    = "rgba(255,255,255,0.035)";
    private static final String BG_CARD_H  = "rgba(79,195,247,0.07)";
    private static final String BG_SEL     = "rgba(79,195,247,0.13)";
    private static final String BORDER_SEL = "rgba(79,195,247,0.80)";
    private static final String DIVIDER    = "rgba(255,255,255,0.08)";

    private static final double STAT_ROW_H = 44;

    // ── Constructor ───────────────────────────────────────
    public DetailView(Pane overlay) {
        this.overlay = overlay;
        getStyleClass().add("detail-panel");
    }

    public void setAllBodies(List<CelestialBody> bodies) {
        this.allBodies = bodies;
    }

    public void setOnFirstDisplay(Runnable r) {
        this.onFirstDisplay = r != null ? r : () -> {};
    }

    public void setOnWidthChangeRequest(Runnable expand, Runnable restore) {
        this.onExpand  = expand  != null ? expand  : () -> {};
        this.onRestore = restore != null ? restore : () -> {};
    }

    // ═══════════════════════════════════════════════════════
    //  DETAIL VIEW
    // ═══════════════════════════════════════════════════════
    public void display(CelestialBody body) {
        onRestore.run();

        if (!firstDisplayFired) {
            firstDisplayFired = true;
            onFirstDisplay.run();
        }

        try {
            ImageView img = new ImageView();
            img.setFitWidth(90); img.setFitHeight(90); img.setPreserveRatio(true);
            try {
                Image image = PlanetAssets.loadImage(body.getName(), 120, 120);
                if (image != null) img.setImage(image);
            } catch (Exception ignored) {}

            HBox imgWrap = new HBox(img);
            imgWrap.setMinSize(100, 100); imgWrap.setMaxSize(100, 100);
            imgWrap.setAlignment(Pos.CENTER);

            Label name = new Label(body.getName());
            name.getStyleClass().add("detail-name");

            Label badge = new Label(body.getType());
            badge.getStyleClass().addAll("detail-badge", PlanetAssets.getBadgeClass(body));

            HBox tagRow = new HBox(6);
            tagRow.setAlignment(Pos.CENTER_LEFT);
            tagRow.getChildren().add(badge);
            if (body.isHasRings())   tagRow.getChildren().add(tagPill("Has Rings"));
            if (body.getMoons() > 0) tagRow.getChildren().add(tagPill(
                body.getMoons() + (body.getMoons() == 1 ? " Moon" : " Moons")));

            Label desc = new Label(body.getDescription());
            desc.getStyleClass().add("detail-desc");
            desc.setWrapText(true);

            Button compareBtn = spaceButton(
                "⬡  INITIATE COMPARISON", CYAN, CYAN_DIM, CYAN_GLOW);
            compareBtn.setMaxWidth(Double.MAX_VALUE);
            compareBtn.setOnAction(e -> {
                selectedForComparison.clear();
                animateTo(buildSelectorPanel(body));
            });

            VBox heroText = new VBox(6, name, tagRow, desc);
            heroText.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(heroText, Priority.ALWAYS);

            HBox hero = new HBox(16, imgWrap, heroText);
            hero.setAlignment(Pos.CENTER_LEFT);

            String distStr = body.getDistanceFromSun() == 0
                ? "Center" : String.format("%.2f AU", body.getDistanceFromSun());
            String tempStr = body.getSurfaceTemperatureMin() == body.getSurfaceTemperatureMax()
                ? body.getSurfaceTemperatureMin() + " °C"
                : body.getSurfaceTemperatureMin() + " / " + body.getSurfaceTemperatureMax() + " °C";

            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(10); statsGrid.setVgap(8);
            statsGrid.getColumnConstraints().addAll(
                cc(120, false), cc(100, true),
                cc(120, false), cc(100, true));

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
                int c = (i % 2) * 2, r = i / 2;
                Label k = new Label(statData[i][0]); k.getStyleClass().add("stat-key");
                Label v = new Label(statData[i][1]); v.getStyleClass().add("stat-val");
                statsGrid.add(k, c, r); statsGrid.add(v, c + 1, r);
            }

            VBox atmoBox = buildAtmoBars(body.getAtmosphereComposition());

            FlowPane featuresPane = new FlowPane(6, 6);
            if (body.getNotableFeatures() != null)
                for (String f : body.getNotableFeatures()) {
                    Label chip = new Label(f); chip.getStyleClass().add("feature-chip");
                    featuresPane.getChildren().add(chip);
                }

            VBox missionsBox = new VBox(5);
            if (body.getExplorationMissions() != null)
                for (String m : body.getExplorationMissions()) {
                    Label ml = new Label("→  " + m);
                    ml.getStyleClass().add("mission-item"); ml.setWrapText(true);
                    missionsBox.getChildren().add(ml);
                }

            VBox factsBox = new VBox(6);
            if (body.getFunFacts() != null)
                for (String f : body.getFunFacts()) {
                    Label fl = new Label("✦  " + f);
                    fl.getStyleClass().add("fun-fact-item"); fl.setWrapText(true);
                    factsBox.getChildren().add(fl);
                }

            VBox content = new VBox(18,
                hero, thinDiv(),
                sectionLabel("Stats"),                statsGrid,  thinDiv(),
                sectionLabel("Atmosphere"),           atmoBox,    thinDiv(),
                sectionLabel("Notable features"),     featuresPane, thinDiv(),
                sectionLabel("Exploration missions"), missionsBox, thinDiv(),
                sectionLabel("Fun facts"),            factsBox,
                gap(8), compareBtn
            );
            content.getStyleClass().add("detail-panel");

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.getStyleClass().add("detail-scroll");
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            Node current = getCenter();
            if (current != null) animateTo(scroll);
            else                 setCenter(scroll);

        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Error: " + e.getMessage());
            err.setStyle("-fx-text-fill: red;");
            setCenter(new StackPane(err));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  DISTANCE CALCULATOR
    // ═══════════════════════════════════════════════════════

    /** Entry point called from MainController via the sidebar button. */
    public void showDistanceCalculator() {
        onRestore.run();
        if (!firstDisplayFired) {
            firstDisplayFired = true;
            onFirstDisplay.run();
        }
        animateTo(buildDistanceCalculatorPanel());
    }

    private Node buildDistanceCalculatorPanel() {
        final double AU_KM        = 149_597_870.7;
        final double LIGHT_MIN_KM = 17_987_547.48;   // km per light-minute
        final double EARTH_CIRC   = 40_075.0;
        final double MOON_DIST    = 384_400.0;

        // ── Title ────────────────────────────────────────
        Label titleLbl = new Label("⟁  DISTANCE CALCULATOR");
        titleLbl.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + CYAN + ";" +
            "-fx-effect: dropshadow(gaussian, " + CYAN + ", 18, 0.45, 0, 0);");

        Label subtitleLbl = new Label(
            "Select two celestial bodies to measure the distance between them");
        subtitleLbl.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.35);");
        subtitleLbl.setWrapText(true);

        // ── Body selectors ───────────────────────────────
        ComboBox<CelestialBody> fromBox = buildComboBox();
        ComboBox<CelestialBody> toBox   = buildComboBox();

        Label fromLbl = calcLabel("FROM");
        Label toLbl   = calcLabel("TO");

        Button swapBtn = spaceButton("⇅ SWAP", CYAN, "transparent", CYAN_GLOW);
        swapBtn.setOnAction(e -> {
            CelestialBody f = fromBox.getValue();
            fromBox.setValue(toBox.getValue());
            toBox.setValue(f);
        });

        GridPane selectGrid = new GridPane();
        selectGrid.setHgap(10);
        selectGrid.setVgap(10);
        ColumnConstraints labelCol = new ColumnConstraints(46);
        ColumnConstraints dropCol  = new ColumnConstraints();
        dropCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints swapCol = new ColumnConstraints(80);
        swapCol.setHalignment(HPos.CENTER);
        selectGrid.getColumnConstraints().addAll(labelCol, dropCol, swapCol);

        GridPane.setValignment(swapBtn, javafx.geometry.VPos.CENTER);
        GridPane.setRowSpan(swapBtn, 2);
        selectGrid.add(fromLbl, 0, 0);
        selectGrid.add(fromBox,  1, 0);
        selectGrid.add(swapBtn,  2, 0);
        selectGrid.add(toLbl,   0, 1);
        selectGrid.add(toBox,    1, 1);

        // ── Live result container ────────────────────────
        VBox resultArea = new VBox(12);
        resultArea.getChildren().add(calcHintNode());

        // ── Reactive update ──────────────────────────────
        Runnable update = () -> {
            resultArea.getChildren().clear();

            CelestialBody from = fromBox.getValue();
            CelestialBody to   = toBox.getValue();

            if (from == null || to == null) {
                resultArea.getChildren().add(calcHintNode());
                return;
            }
            if (from.getName().equals(to.getName())) {
                Label same = new Label("Select two different bodies to calculate a distance.");
                same.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.35);" +
                    "-fx-padding: 16 0 0 0;");
                same.setWrapText(true);
                resultArea.getChildren().add(same);
                return;
            }

            double d1 = from.getDistanceFromSun();
            double d2 = to.getDistanceFromSun();
            boolean eitherSun = (d1 == 0.0 || d2 == 0.0);

            double minKM, maxKM, refKM;
            if (eitherSun) {
                minKM = maxKM = refKM = Math.max(d1, d2) * AU_KM;
            } else {
                minKM = Math.abs(d1 - d2) * AU_KM;
                maxKM = (d1 + d2) * AU_KM;
                refKM = (minKM + maxKM) / 2.0;
            }

            // Route header
            Label routeLbl = new Label(from.getName() + "  →  " + to.getName());
            routeLbl.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-text-fill: rgba(255,255,255,0.70);" +
                "-fx-letter-spacing: 0.5px;");

            resultArea.getChildren().add(routeLbl);
            resultArea.getChildren().add(gap(4));

            if (eitherSun) {
                resultArea.getChildren().add(
                    distCard("DISTANCE", refKM, LIGHT_MIN_KM, CYAN, CYAN_DIM));
            } else {
                resultArea.getChildren().add(
                    distCard("CLOSEST  (conjunction)", minKM, LIGHT_MIN_KM, CYAN, CYAN_DIM));
                resultArea.getChildren().add(
                    distCard("FARTHEST  (opposition)", maxKM, LIGHT_MIN_KM, GOLD, GOLD_DIM));
                resultArea.getChildren().add(
                    distCard("AVERAGE DISTANCE", refKM, LIGHT_MIN_KM, PURPLE, PURPLE_DIM));
            }

            resultArea.getChildren().add(thinDiv());
            resultArea.getChildren().add(funComparisonsBox(refKM, EARTH_CIRC, MOON_DIST));
        };

        fromBox.valueProperty().addListener((obs, o, n) -> update.run());
        toBox.valueProperty().addListener((obs, o, n)   -> update.run());

        // ── Assemble ─────────────────────────────────────
        VBox content = new VBox(16,
            titleLbl, subtitleLbl,
            thinDiv(),
            selectGrid,
            thinDiv(),
            resultArea
        );
        content.getStyleClass().add("detail-panel");
        content.setPadding(new Insets(24, 24, 32, 24));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("detail-scroll");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    /** One distance result card (km + light-minutes). */
    private VBox distCard(String cardTitle, double km, double lightMinKm,
                          String accentColor, String bgColor) {
        double lm = km / lightMinKm;

        Label titleLbl = new Label(cardTitle);
        titleLbl.setStyle(
            "-fx-font-size: 9px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + accentColor + ";" +
            "-fx-letter-spacing: 1.5px;");

        Label kmLbl = new Label(formatKm(km));
        kmLbl.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + accentColor + ";" +
            "-fx-effect: dropshadow(gaussian, " + accentColor + ", 10, 0.30, 0, 0);");

        Label lmLbl = new Label(String.format("%.3f light-minutes", lm));
        lmLbl.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.45);");

        VBox card = new VBox(5, titleLbl, kmLbl, lmLbl);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + accentColor.replace(")", ", 0.30)").replace("rgb", "rgba") + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;");
        return card;
    }

    /** Fun scale comparisons block. */
    private VBox funComparisonsBox(double km, double earthCirc, double moonDist) {
        Label header = sectionLabel("Fun comparisons");

        long aroundEarth = Math.round(km / earthCirc);
        long moonTimes   = Math.round(km / moonDist);
        double lightSec  = km / 299_792.458;

        String lightStr = lightSec < 60.0
            ? String.format("Light covers this in %.1f seconds", lightSec)
            : String.format("Light covers this in %.2f minutes", lightSec / 60.0);

        String[] lines = {
            String.format("That's %,d× around Earth's equator", aroundEarth),
            String.format("That's %,d× the Earth–Moon distance", moonTimes),
            lightStr
        };

        VBox box = new VBox(8, header, gap(2));
        for (String line : lines) {
            Label lbl = new Label("✦  " + line);
            lbl.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.62);" +
                "-fx-padding: 7 12 7 12;" +
                "-fx-background-color: rgba(255,255,255,0.025);" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;");
            lbl.setWrapText(true);
            lbl.setMaxWidth(Double.MAX_VALUE);
            box.getChildren().add(lbl);
        }
        return box;
    }

    /** Styled ComboBox matching the dark space theme. */
    private ComboBox<CelestialBody> buildComboBox() {
        ComboBox<CelestialBody> box = new ComboBox<>();
        box.getStyleClass().add("dist-combo");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPromptText("Select a body…");
        if (allBodies != null) box.getItems().addAll(allBodies);
        return box;
    }

    /** Dim uppercase label used in the calculator grid. */
    private Label calcLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.35);" +
            "-fx-letter-spacing: 1px;");
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setMaxHeight(Double.MAX_VALUE);
        return l;
    }

    /** Placeholder shown before any selection is made. */
    private Label calcHintNode() {
        Label l = new Label("← Select two bodies above to calculate the distance between them");
        l.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.22);" +
            "-fx-padding: 24 0 0 0;");
        l.setWrapText(true);
        return l;
    }

    /** Formats a km value with sensible units. */
    private String formatKm(double km) {
        if (km >= 1_000_000_000.0)
            return String.format("%.3f billion km", km / 1_000_000_000.0);
        if (km >= 1_000_000.0)
            return String.format("%.3f million km", km / 1_000_000.0);
        return String.format("%,.0f km", km);
    }

    // ═══════════════════════════════════════════════════════
    //  SELECTOR PANEL
    // ═══════════════════════════════════════════════════════
    private Node buildSelectorPanel(CelestialBody origin) {
        selectedForComparison.clear();

        Button backBtn = spaceButton("← BACK", CYAN, "transparent", CYAN_GLOW);
        backBtn.setOnAction(e -> animateBack(origin));

        Label title = new Label("SELECT PLANETS TO COMPARE");
        title.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + CYAN + "; -fx-letter-spacing: 2px;");

        Label subtitle = new Label(
            "Tap cards to select  ·  " + origin.getName() + " is your reference");
        subtitle.setStyle(
            "-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.38);");

        Label statusLbl = new Label("0 selected");
        statusLbl.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: " + GOLD + "; -fx-font-weight: bold;");

        Button launchBtn = spaceButton("⬡  LAUNCH COMPARISON", GOLD, GOLD_DIM, GOLD_GLOW);
        launchBtn.setMaxWidth(Double.MAX_VALUE);
        launchBtn.setDisable(true);

        VBox listItems = new VBox(7);
        listItems.setPadding(new Insets(4, 2, 4, 2));
        List<HBox> cardNodes = new ArrayList<>();

        if (allBodies != null) {
            for (CelestialBody body : allBodies) {
                if (body.getName().equals(origin.getName())) continue;
                HBox card = buildSelectorCard(body, cardNodes, statusLbl, launchBtn);
                cardNodes.add(card);
                listItems.getChildren().add(card);
            }
        }

        launchBtn.setOnAction(e -> {
            if (!selectedForComparison.isEmpty()) {
                List<CelestialBody> toCompare = new ArrayList<>();
                toCompare.add(origin);
                toCompare.addAll(selectedForComparison);
                animateToComparison(buildComparisonView(toCompare, origin));
            }
        });

        ScrollPane listScroll = new ScrollPane(listItems);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.setStyle(
            "-fx-background: transparent; -fx-background-color: transparent;" +
            "-fx-border-color: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        HBox statusRow = new HBox(statusLbl);
        statusRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12,
            backBtn, thinDiv(),
            title, subtitle,
            gap(4), statusRow,
            listScroll,
            thinDiv(), launchBtn
        );
        content.getStyleClass().add("detail-panel");
        content.setPadding(new Insets(18));

        ScrollPane outer = new ScrollPane(content);
        outer.setFitToWidth(true);
        outer.setFitToHeight(true);
        outer.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return outer;
    }

    private HBox buildSelectorCard(
            CelestialBody body,
            List<HBox> cardNodes,
            Label statusLbl,
            Button launchBtn) {

        ImageView iv = new ImageView();
        iv.setFitWidth(40); iv.setFitHeight(40); iv.setPreserveRatio(true);
        try {
            Image img = PlanetAssets.loadImage(body.getName(), 52, 52);
            if (img != null) iv.setImage(img);
        } catch (Exception ignored) {}

        StackPane iconWrap = new StackPane(iv);
        iconWrap.setMinSize(48, 48); iconWrap.setMaxSize(48, 48);
        iconWrap.setAlignment(Pos.CENTER);
        iconWrap.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 12;");

        Label nameLbl = new Label(body.getName());
        nameLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.88);");

        String dist = body.getDistanceFromSun() == 0
            ? "Center" : String.format("%.2f AU", body.getDistanceFromSun());
        Label typeLbl = new Label(body.getType() + "  ·  " + dist);
        typeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.38);");

        VBox textBlock = new VBox(2, nameLbl, typeLbl);
        textBlock.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label checkLbl = new Label("○");
        checkLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: rgba(255,255,255,0.18);");

        HBox card = new HBox(12, iconWrap, textBlock, checkLbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setMaxWidth(Double.MAX_VALUE);
        styleCard(card, false);

        card.setOnMouseEntered(ev -> {
            if (!selectedForComparison.contains(body))
                card.setStyle(
                    "-fx-background-color: " + BG_CARD_H + ";" +
                    "-fx-background-radius: 10; -fx-cursor: hand;" +
                    "-fx-border-color: " + CYAN_MID + ";" +
                    "-fx-border-width: 0 0 0 3; -fx-border-radius: 10;");
        });
        card.setOnMouseExited(ev -> {
            if (!selectedForComparison.contains(body)) styleCard(card, false);
        });

        card.setOnMouseClicked(ev -> {
            boolean was = selectedForComparison.contains(body);
            if (was) {
                selectedForComparison.remove(body);
                styleCard(card, false);
                checkLbl.setText("○");
                checkLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: rgba(255,255,255,0.18);");
            } else {
                selectedForComparison.add(body);
                styleCard(card, true);
                checkLbl.setText("✦");
                checkLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: " + CYAN + ";");
                ScaleTransition pop = new ScaleTransition(Duration.millis(110), card);
                pop.setFromX(0.96); pop.setFromY(0.96);
                pop.setToX(1.00);  pop.setToY(1.00);
                pop.play();
            }
            int n = selectedForComparison.size();
            statusLbl.setText(n == 0
                ? "0 selected"
                : n + " planet" + (n > 1 ? "s" : "") + " selected");
            launchBtn.setDisable(n < 1);
            launchBtn.setText(n < 1
                ? "⬡  LAUNCH COMPARISON"
                : "⬡  COMPARE " + (n + 1) + " PLANETS  →");
        });

        return card;
    }

    private void styleCard(HBox card, boolean selected) {
        if (selected) {
            card.setStyle(
                "-fx-background-color: " + BG_SEL + ";" +
                "-fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-border-color: " + BORDER_SEL + ";" +
                "-fx-border-width: 0 0 0 3; -fx-border-radius: 10;");
        } else {
            card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-border-width: 0 0 0 3; -fx-border-radius: 10;");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  COMPARISON VIEW
    // ═══════════════════════════════════════════════════════
    private Node buildComparisonView(List<CelestialBody> bodies, CelestialBody origin) {

        final String[] STAT_NAMES = {
            "Gravity", "Radius", "Mass", "Distance",
            "Orbital period", "Rotation period",
            "Temp min", "Temp max", "Moons", "Has rings"
        };
        final int N_STATS   = STAT_NAMES.length;
        final int N_PLANETS = bodies.size();

        double[][] nums   = new double[N_STATS][N_PLANETS];
        boolean[]  hasNum = new boolean[N_STATS];
        double[]   maxV   = new double[N_STATS];
        double[]   minV   = new double[N_STATS];
        for (int si = 0; si < N_STATS; si++) {
            maxV[si] = Double.NEGATIVE_INFINITY;
            minV[si] = Double.POSITIVE_INFINITY;
            for (int bi = 0; bi < N_PLANETS; bi++) {
                Double v = statNumeric(STAT_NAMES[si], bodies.get(bi));
                if (v != null) {
                    nums[si][bi] = v;
                    hasNum[si]   = true;
                    maxV[si] = Math.max(maxV[si], v);
                    minV[si] = Math.min(minV[si], v);
                } else {
                    nums[si][bi] = Double.NaN;
                }
            }
        }

        final int ROW_HEADER  = 0;
        final int ROW_STATS_0 = 1;
        final int ROW_ATM_HDR = ROW_STATS_0 + N_STATS;
        final int ROW_ATM_VAL = ROW_ATM_HDR + 1;
        final int ROW_FEA_HDR = ROW_ATM_VAL + 1;
        final int ROW_FEA_VAL = ROW_FEA_HDR + 1;
        final int ROW_MIS_HDR = ROW_FEA_VAL + 1;
        final int ROW_MIS_VAL = ROW_MIS_HDR + 1;
        final int ROW_FAC_HDR = ROW_MIS_VAL + 1;
        final int ROW_FAC_VAL = ROW_FAC_HDR + 1;

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(130); labelCol.setMinWidth(110); labelCol.setMaxWidth(150);
        grid.getColumnConstraints().add(labelCol);

        for (int bi = 0; bi < N_PLANETS; bi++) {
            ColumnConstraints pc = new ColumnConstraints();
            pc.setHgrow(Priority.ALWAYS);
            pc.setMinWidth(120);
            if (bi < N_PLANETS - 1) {
                grid.getColumnConstraints().add(pc);
                ColumnConstraints divCol = new ColumnConstraints();
                divCol.setPrefWidth(1); divCol.setMinWidth(1); divCol.setMaxWidth(1);
                grid.getColumnConstraints().add(divCol);
            } else {
                grid.getColumnConstraints().add(pc);
            }
        }

        RowConstraints headerRC = new RowConstraints();
        headerRC.setMinHeight(170); headerRC.setPrefHeight(170);
        grid.getRowConstraints().add(headerRC);

        for (int si = 0; si < N_STATS; si++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(STAT_ROW_H); rc.setPrefHeight(STAT_ROW_H);
            rc.setMaxHeight(STAT_ROW_H);
            grid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 8; i++) {
            RowConstraints rc = new RowConstraints();
            if (i % 2 == 0) {
                rc.setMinHeight(28); rc.setPrefHeight(28); rc.setMaxHeight(28);
            } else {
                rc.setMinHeight(40); rc.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }
            grid.getRowConstraints().add(rc);
        }

        grid.add(new Region(), 0, ROW_HEADER);

        for (int si = 0; si < N_STATS; si++) {
            Label lbl = new Label(STAT_NAMES[si].toUpperCase());
            lbl.setStyle(
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-text-fill: rgba(255,255,255,0.40);" +
                "-fx-letter-spacing: 0.5px; -fx-padding: 0 8 0 10;" +
                "-fx-background-color: " + ((si % 2 == 0) ? "transparent" : "rgba(255,255,255,0.018)") + ";");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setMaxHeight(Double.MAX_VALUE);
            grid.add(lbl, 0, ROW_STATS_0 + si);
        }

        String[] sectionLabels = { "ATMOSPHERE", "FEATURES", "MISSIONS", "FUN FACTS" };
        int[] sectionBannerRows = { ROW_ATM_HDR, ROW_FEA_HDR, ROW_MIS_HDR, ROW_FAC_HDR };
        for (int i = 0; i < sectionLabels.length; i++) {
            grid.add(makeBannerCell(sectionLabels[i]), 0, sectionBannerRows[i]);
        }
        int[] contentRows = { ROW_ATM_VAL, ROW_FEA_VAL, ROW_MIS_VAL, ROW_FAC_VAL };
        for (int r : contentRows) {
            grid.add(new Region(), 0, r);
        }

        for (int bi = 0; bi < N_PLANETS; bi++) {
            CelestialBody body    = bodies.get(bi);
            boolean isOrigin      = body.getName().equals(origin.getName());
            String  glowColor     = isOrigin ? GOLD : CYAN;
            String  glowDim       = isOrigin ? GOLD_DIM : CYAN_DIM;
            int     gridCol       = 1 + bi * 2;

            ImageView iv = new ImageView();
            iv.setFitWidth(72); iv.setFitHeight(72); iv.setPreserveRatio(true);
            try {
                Image img = PlanetAssets.loadImage(body.getName(), 88, 88);
                if (img != null) iv.setImage(img);
            } catch (Exception ignored) {}

            RotateTransition rot = new RotateTransition(
                Duration.seconds(13 + Math.random() * 9), iv);
            rot.setByAngle(360);
            rot.setCycleCount(Animation.INDEFINITE);
            rot.setInterpolator(Interpolator.LINEAR);
            rot.play();

            StackPane imgWrap = new StackPane(iv);
            imgWrap.setMinSize(84, 84); imgWrap.setMaxSize(84, 84);
            imgWrap.setAlignment(Pos.CENTER);
            imgWrap.setStyle(
                "-fx-background-color: " + glowDim + ";" +
                "-fx-background-radius: 42;" +
                "-fx-effect: dropshadow(gaussian, " + glowColor + ", 20, 0.40, 0, 0);");

            Label nameLbl = new Label(body.getName().toUpperCase());
            nameLbl.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + glowColor + ";" +
                "-fx-letter-spacing: 1px;");
            nameLbl.setAlignment(Pos.CENTER);

            Label typeLbl = new Label(body.getType());
            typeLbl.setStyle(
                "-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.36);");
            typeLbl.setAlignment(Pos.CENTER);

            VBox header = new VBox(6, imgWrap, nameLbl, typeLbl);
            header.setAlignment(Pos.CENTER);
            header.setPadding(new Insets(12, 10, 12, 10));

            if (isOrigin) {
                Label refBadge = new Label("REFERENCE");
                refBadge.setStyle(
                    "-fx-font-size: 8px; -fx-text-fill: " + GOLD + ";" +
                    "-fx-background-color: " + GOLD_DIM + ";" +
                    "-fx-background-radius: 4; -fx-padding: 2 7 2 7;" +
                    "-fx-font-weight: bold; -fx-letter-spacing: 1px;");
                header.getChildren().add(refBadge);
            }

            grid.add(header, gridCol, ROW_HEADER);
            GridPane.setHalignment(header, HPos.CENTER);

            for (int si = 0; si < N_STATS; si++) {
                String  formatted = statFormatted(STAT_NAMES[si], body);
                boolean isMax = hasNum[si] && !Double.isNaN(nums[si][bi])
                    && nums[si][bi] == maxV[si] && maxV[si] != minV[si];
                boolean isMin = hasNum[si] && !Double.isNaN(nums[si][bi])
                    && nums[si][bi] == minV[si] && maxV[si] != minV[si];
                String valColor = isMax ? GOLD : (isMin ? RED_SOFT : "rgba(255,255,255,0.82)");

                VBox cell = new VBox(2);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setPadding(new Insets(4, 10, 4, 10));
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setMaxHeight(Double.MAX_VALUE);
                cell.setStyle(
                    "-fx-background-color: " +
                    ((si % 2 == 0) ? "transparent" : "rgba(255,255,255,0.018)") + ";");

                Label valLbl = new Label(formatted);
                valLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + valColor + ";");
                cell.getChildren().add(valLbl);

                if (isMax) {
                    Label b = new Label("▲ MAX");
                    b.setStyle("-fx-font-size: 8px; -fx-text-fill: " + GOLD + ";" +
                        "-fx-background-color: " + GOLD_DIM + ";" +
                        "-fx-background-radius: 3; -fx-padding: 1 5 1 5;");
                    cell.getChildren().add(b);
                } else if (isMin) {
                    Label b = new Label("▼ MIN");
                    b.setStyle("-fx-font-size: 8px; -fx-text-fill: " + RED_SOFT + ";" +
                        "-fx-background-color: rgba(239,154,154,0.10);" +
                        "-fx-background-radius: 3; -fx-padding: 1 5 1 5;");
                    cell.getChildren().add(b);
                }

                grid.add(cell, gridCol, ROW_STATS_0 + si);
            }

            grid.add(makeBannerCell(""), gridCol, ROW_ATM_HDR);

            VBox atmoCell = new VBox(6);
            atmoCell.setPadding(new Insets(10, 12, 10, 12));
            atmoCell.setMaxWidth(Double.MAX_VALUE);

            Map<String, Integer> atmo = body.getAtmosphereComposition();
            if (atmo != null && !atmo.isEmpty()) {
                for (Map.Entry<String, Integer> e : atmo.entrySet()) {
                    int pct = Math.max(0, Math.min(100, e.getValue()));

                    Label gasLbl = new Label(e.getKey());
                    gasLbl.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.55);");
                    gasLbl.setMinWidth(50); gasLbl.setPrefWidth(50);

                    Region track = new Region();
                    track.setStyle("-fx-background-color: rgba(255,255,255,0.07);" +
                        "-fx-background-radius: 3;");
                    track.setPrefHeight(5); track.setMinHeight(5);
                    track.setMaxHeight(5); track.setMaxWidth(Double.MAX_VALUE);

                    Region fill = new Region();
                    fill.setStyle("-fx-background-color: " + glowColor +
                        "; -fx-background-radius: 3;");
                    fill.setPrefHeight(5); fill.setMinHeight(5); fill.setMaxHeight(5);
                    fill.prefWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));
                    fill.maxWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));

                    StackPane bar = new StackPane(track, fill);
                    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                    bar.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(bar, Priority.ALWAYS);

                    Label pctLbl = new Label(pct + "%");
                    pctLbl.setStyle(
                        "-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.38);");
                    pctLbl.setMinWidth(26); pctLbl.setAlignment(Pos.CENTER_RIGHT);

                    HBox barRow = new HBox(5, gasLbl, bar, pctLbl);
                    barRow.setAlignment(Pos.CENTER_LEFT);
                    atmoCell.getChildren().add(barRow);
                }
            } else {
                atmoCell.getChildren().add(noData());
            }
            grid.add(atmoCell, gridCol, ROW_ATM_VAL);

            grid.add(makeBannerCell(""), gridCol, ROW_FEA_HDR);
            VBox featCell = new VBox(4);
            featCell.setPadding(new Insets(10, 12, 10, 12));
            if (body.getNotableFeatures() != null && !body.getNotableFeatures().isEmpty()) {
                for (String f : body.getNotableFeatures()) {
                    Label fl = new Label("◆ " + f);
                    fl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.60);");
                    fl.setWrapText(true);
                    featCell.getChildren().add(fl);
                }
            } else { featCell.getChildren().add(noData()); }
            grid.add(featCell, gridCol, ROW_FEA_VAL);

            grid.add(makeBannerCell(""), gridCol, ROW_MIS_HDR);
            VBox missCell = new VBox(4);
            missCell.setPadding(new Insets(10, 12, 10, 12));
            if (body.getExplorationMissions() != null && !body.getExplorationMissions().isEmpty()) {
                for (String m : body.getExplorationMissions()) {
                    Label ml = new Label("→ " + m);
                    ml.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.58);");
                    ml.setWrapText(true);
                    missCell.getChildren().add(ml);
                }
            } else { missCell.getChildren().add(noData()); }
            grid.add(missCell, gridCol, ROW_MIS_VAL);

            grid.add(makeBannerCell(""), gridCol, ROW_FAC_HDR);
            VBox factCell = new VBox(4);
            factCell.setPadding(new Insets(10, 12, 10, 12));
            if (body.getFunFacts() != null && !body.getFunFacts().isEmpty()) {
                for (String f : body.getFunFacts()) {
                    Label fl = new Label("✦ " + f);
                    fl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.55);");
                    fl.setWrapText(true);
                    factCell.getChildren().add(fl);
                }
            } else { factCell.getChildren().add(noData()); }
            grid.add(factCell, gridCol, ROW_FAC_VAL);

            if (bi < N_PLANETS - 1) {
                Region vDiv = new Region();
                vDiv.setMaxWidth(Double.MAX_VALUE); vDiv.setMaxHeight(Double.MAX_VALUE);
                vDiv.setStyle("-fx-background-color: " + DIVIDER + ";");
                int totalRows = ROW_FAC_VAL + 1;
                GridPane.setRowSpan(vDiv, totalRows);
                grid.add(vDiv, gridCol + 1, 0);
            }
        }

        Button backBtn = spaceButton("← BACK TO SELECTION", CYAN, "transparent", CYAN_GLOW);
        backBtn.setOnAction(e -> {
            selectedForComparison.clear();
            onRestore.run();
            animateTo(buildSelectorPanel(origin));
        });

        Label titleLbl = new Label("PLANETARY COMPARISON");
        titleLbl.setStyle(
            "-fx-font-size: 22px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + CYAN + ";" +
            "-fx-effect: dropshadow(gaussian, " + CYAN + ", 22, 0.50, 0, 0);");

        Label subLbl = new Label(
            bodies.size() + " celestial bodies  ·  deep field analysis");
        subLbl.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.35);" +
            "-fx-letter-spacing: 1px;");

        HBox topBar = new HBox(16, backBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 14, 0));

        VBox titleBlock = new VBox(5, titleLbl, subLbl);
        titleBlock.setAlignment(Pos.CENTER);
        titleBlock.setPadding(new Insets(0, 0, 16, 0));

        ScrollPane hScroll = new ScrollPane(grid);
        hScroll.setFitToWidth(true);
        hScroll.setFitToHeight(false);
        hScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(hScroll, Priority.ALWAYS);

        VBox root = new VBox(0, topBar, titleBlock, thinDiv(), hScroll);
        root.getStyleClass().add("detail-panel");
        root.setPadding(new Insets(20, 16, 20, 16));

        ScrollPane outerV = new ScrollPane(root);
        outerV.setFitToWidth(true);
        outerV.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return outerV;
    }

    // ── Section banner cell ──────────────────────────────────────
    private StackPane makeBannerCell(String text) {
        Label l = new Label(text.isEmpty() ? "" : "  " + text);
        l.setMaxWidth(Double.MAX_VALUE); l.setMaxHeight(Double.MAX_VALUE);
        l.setStyle(
            "-fx-font-size: 9px; -fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.28);" +
            "-fx-letter-spacing: 2px;" +
            "-fx-background-color: rgba(255,255,255,0.030);" +
            "-fx-padding: 6 10 6 10;");
        StackPane sp = new StackPane(l);
        sp.setMaxWidth(Double.MAX_VALUE);
        sp.setPrefHeight(28); sp.setMinHeight(28);
        return sp;
    }

    // ═══════════════════════════════════════════════════════
    //  ANIMATIONS
    // ═══════════════════════════════════════════════════════

    private void animateTo(Node newContent) {
        Node current = getCenter();
        newContent.setOpacity(0);
        newContent.setTranslateX(55);

        if (current == null) {
            setCenter(newContent);
            FadeTransition fi = new FadeTransition(Duration.millis(280), newContent);
            fi.setToValue(1);
            TranslateTransition sl = new TranslateTransition(Duration.millis(280), newContent);
            sl.setToX(0);
            new ParallelTransition(fi, sl).play();
            return;
        }

        FadeTransition fo = new FadeTransition(Duration.millis(170), current);
        fo.setToValue(0);
        fo.setOnFinished(ev -> {
            setCenter(newContent);
            FadeTransition fi = new FadeTransition(Duration.millis(270), newContent);
            fi.setToValue(1);
            TranslateTransition sl = new TranslateTransition(Duration.millis(270), newContent);
            sl.setToX(0);
            new ParallelTransition(fi, sl).play();
        });
        fo.play();
    }

    private void animateToComparison(Node newContent) {
        Node current = getCenter();
        newContent.setOpacity(0);
        newContent.setTranslateX(80);

        FadeTransition fo = new FadeTransition(Duration.millis(160), current);
        fo.setToValue(0);
        fo.setOnFinished(ev -> {
            setCenter(newContent);
            onExpand.run();
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(80), e2 -> {
                FadeTransition fi = new FadeTransition(Duration.millis(340), newContent);
                fi.setToValue(1);
                TranslateTransition sl = new TranslateTransition(Duration.millis(380), newContent);
                sl.setToX(0);
                sl.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fi, sl).play();
            }));
            delay.play();
        });
        fo.play();
    }

    private void animateBack(CelestialBody body) {
        Node current = getCenter();
        if (current == null) { display(body); return; }
        FadeTransition fo = new FadeTransition(Duration.millis(170), current);
        fo.setToValue(0);
        fo.setOnFinished(ev -> display(body));
        fo.play();
    }

    // ═══════════════════════════════════════════════════════
    //  STAT HELPERS
    // ═══════════════════════════════════════════════════════
    private Double statNumeric(String stat, CelestialBody b) {
        return switch (stat) {
            case "Gravity"         -> b.getGravity();
            case "Radius"          -> b.getRadiusKm();
            case "Mass"            -> b.getMassKg();
            case "Distance"        -> b.getDistanceFromSun();
            case "Orbital period"  -> (double) b.getOrbitalPeriodDays();
            case "Rotation period" -> b.getRotationPeriodDays();
            case "Temp min"        -> (double) b.getSurfaceTemperatureMin();
            case "Temp max"        -> (double) b.getSurfaceTemperatureMax();
            case "Moons"           -> (double) b.getMoons();
            default -> null;
        };
    }

    private String statFormatted(String stat, CelestialBody b) {
        return switch (stat) {
            case "Gravity"         -> String.format("%.2f m/s²", b.getGravity());
            case "Radius"          -> String.format("%,.0f km", b.getRadiusKm());
            case "Mass"            -> String.format("%.2e kg", b.getMassKg());
            case "Distance"        -> b.getDistanceFromSun() == 0
                                        ? "Center"
                                        : String.format("%.2f AU", b.getDistanceFromSun());
            case "Orbital period"  -> String.format("%,d days", b.getOrbitalPeriodDays());
            case "Rotation period" -> String.format("%.2f days", b.getRotationPeriodDays());
            case "Temp min"        -> b.getSurfaceTemperatureMin() + " °C";
            case "Temp max"        -> b.getSurfaceTemperatureMax() + " °C";
            case "Moons"           -> String.valueOf(b.getMoons());
            case "Has rings"       -> b.isHasRings() ? "Yes  ✓" : "No";
            default -> "—";
        };
    }

    // ═══════════════════════════════════════════════════════
    //  ATMOSPHERE BARS  (detail view)
    // ═══════════════════════════════════════════════════════
    private VBox buildAtmoBars(Map<String, Integer> atmo) {
        VBox box = new VBox(10);
        if (atmo == null || atmo.isEmpty()) return box;
        for (Map.Entry<String, Integer> e : atmo.entrySet()) {
            int pct = Math.max(0, Math.min(100, e.getValue()));
            Label gasLbl = new Label(e.getKey());
            gasLbl.getStyleClass().add("stat-key");
            gasLbl.setMinWidth(70); gasLbl.setPrefWidth(70);
            Region track = new Region();
            track.getStyleClass().add("atmo-bar-track");
            track.setPrefHeight(8); track.setMinHeight(8);
            track.setMaxHeight(8); track.setMaxWidth(Double.MAX_VALUE);
            Region fill = new Region();
            fill.getStyleClass().add("atmo-bar-fill");
            fill.setPrefHeight(8); fill.setMinHeight(8); fill.setMaxHeight(8);
            fill.prefWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));
            fill.maxWidthProperty().bind(track.widthProperty().multiply(pct / 100.0));
            StackPane bar = new StackPane(track, fill);
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            bar.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label pctLbl = new Label(pct + "%");
            pctLbl.getStyleClass().add("atmo-pct-label");
            pctLbl.setMinWidth(36); pctLbl.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(8, gasLbl, bar, pctLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        return box;
    }

    // ═══════════════════════════════════════════════════════
    //  SPACE BUTTON
    // ═══════════════════════════════════════════════════════
    private Button spaceButton(String text, String textColor, String bg, String glow) {
        Button btn = new Button(text);
        String base =
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-padding: 9 22 9 22; -fx-background-radius: 3;" +
            "-fx-border-color: " + textColor + ";" +
            "-fx-border-width: 1; -fx-border-radius: 3;" +
            "-fx-cursor: hand; -fx-letter-spacing: 1px;" +
            "-fx-effect: dropshadow(gaussian, " + glow + ", 10, 0.35, 0, 0);";
        String hover =
            "-fx-background-color: " +
                (bg.equals("transparent") ? "rgba(255,255,255,0.04)" : bg) + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-padding: 9 22 9 22; -fx-background-radius: 3;" +
            "-fx-border-color: " + textColor + ";" +
            "-fx-border-width: 1; -fx-border-radius: 3;" +
            "-fx-cursor: hand; -fx-letter-spacing: 1px;" +
            "-fx-effect: dropshadow(gaussian, " + glow + ", 24, 0.60, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // ═══════════════════════════════════════════════════════
    //  SMALL HELPERS
    // ═══════════════════════════════════════════════════════
    private ColumnConstraints cc(double pref, boolean grow) {
        ColumnConstraints c = new ColumnConstraints();
        c.setPrefWidth(pref); c.setMinWidth(pref);
        if (grow) c.setHgrow(Priority.ALWAYS);
        return c;
    }

    private Region thinDiv() {
        Region d = new Region();
        d.setMaxWidth(Double.MAX_VALUE); d.setPrefHeight(1);
        d.setStyle("-fx-background-color: " + DIVIDER + ";");
        return d;
    }

    private Region gap(double h) {
        Region r = new Region(); r.setPrefHeight(h); r.setMinHeight(h);
        return r;
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

    private Label noData() {
        Label l = new Label("No data available");
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.20);");
        return l;
    }
}