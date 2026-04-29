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
 * Three internal modes:
 *   DETAIL     → planet info (slides in from right when a body is first selected)
 *   SELECTOR   → multi-select card list for comparison
 *   COMPARISON → wide GridPane panel where every row aligns perfectly
 *
 * Changes vs original:
 *  1. NO placeholder screen — panel is invisible until a body is selected.
 *     App.java controls visibility.  setOnFirstDisplay(Runnable) is called
 *     exactly once to let App know it should slide the panel in.
 *  2. Comparison view uses a single GridPane instead of parallel VBox columns,
 *     so stat rows are pixel-perfect aligned across all planet columns.
 *  3. setOnWidthChangeRequest callbacks still work exactly as documented.
 */
public class DetailView extends BorderPane {

    @SuppressWarnings("unused")
    private final Pane overlay;
    private List<CelestialBody> allBodies;

    // Called once when the very first body is displayed (App uses it to slide panel in)
    private Runnable onFirstDisplay = () -> {};
    private boolean firstDisplayFired = false;

    // Callbacks to ask the parent layout to expand/restore this panel
    private Runnable onExpand  = () -> {};
    private Runnable onRestore = () -> {};

    // Multi-select state
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
    private static final String BG_CARD    = "rgba(255,255,255,0.035)";
    private static final String BG_CARD_H  = "rgba(79,195,247,0.07)";
    private static final String BG_SEL     = "rgba(79,195,247,0.13)";
    private static final String BORDER_SEL = "rgba(79,195,247,0.80)";
    private static final String DIVIDER    = "rgba(255,255,255,0.08)";

    // Fixed row height for comparison grid (stats rows)
    private static final double STAT_ROW_H = 44;

    // ── Constructor ───────────────────────────────────────
    public DetailView(Pane overlay) {
        this.overlay = overlay;
        getStyleClass().add("detail-panel");
        // No placeholder — panel starts empty and invisible (App controls translate)
    }

    public void setAllBodies(List<CelestialBody> bodies) {
        this.allBodies = bodies;
    }

    /** Called once when the very first body is shown, so App can slide panel in. */
    public void setOnFirstDisplay(Runnable r) {
        this.onFirstDisplay = r != null ? r : () -> {};
    }

    /** Wire parent layout callbacks so DetailView can request expand/shrink. */
    public void setOnWidthChangeRequest(Runnable expand, Runnable restore) {
        this.onExpand  = expand  != null ? expand  : () -> {};
        this.onRestore = restore != null ? restore : () -> {};
    }

    // ═══════════════════════════════════════════════════════
    //  DETAIL VIEW
    // ═══════════════════════════════════════════════════════
    public void display(CelestialBody body) {
        onRestore.run();

        // Fire first-display hook exactly once
        if (!firstDisplayFired) {
            firstDisplayFired = true;
            onFirstDisplay.run();
        }

        try {
            // Hero image
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

            // Stats
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
    //  COMPARISON VIEW  — uses a single GridPane for perfect row alignment
    //
    //  Grid layout:
    //    column 0         = property label column (fixed 130 px)
    //    column 1..N      = one column per planet (grow equally)
    //
    //  Row layout:
    //    row 0            = planet headers (image + name + type)
    //    rows 1..10       = stat rows (fixed height STAT_ROW_H)
    //    row 11           = "ATMOSPHERE" section banner
    //    row 12           = atmosphere bar cells (variable height, same per row)
    //    row 13           = "FEATURES" section banner
    //    row 14           = features content
    //    row 15           = "MISSIONS" section banner
    //    row 16           = missions content
    //    row 17           = "FUN FACTS" section banner
    //    row 18           = fun facts content
    // ═══════════════════════════════════════════════════════
    private Node buildComparisonView(List<CelestialBody> bodies, CelestialBody origin) {

        final String[] STAT_NAMES = {
            "Gravity", "Radius", "Mass", "Distance",
            "Orbital period", "Rotation period",
            "Temp min", "Temp max", "Moons", "Has rings"
        };
        final int N_STATS   = STAT_NAMES.length;
        final int N_PLANETS = bodies.size();

        // ── Pre-compute numeric values for MAX/MIN highlighting ──
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

        // ── Row indices ──────────────────────────────────────────
        final int ROW_HEADER  = 0;
        final int ROW_STATS_0 = 1;                          // stats rows: 1..N_STATS
        final int ROW_ATM_HDR = ROW_STATS_0 + N_STATS;     // atmosphere banner
        final int ROW_ATM_VAL = ROW_ATM_HDR + 1;           // atmosphere values
        final int ROW_FEA_HDR = ROW_ATM_VAL + 1;           // features banner
        final int ROW_FEA_VAL = ROW_FEA_HDR + 1;
        final int ROW_MIS_HDR = ROW_FEA_VAL + 1;
        final int ROW_MIS_VAL = ROW_MIS_HDR + 1;
        final int ROW_FAC_HDR = ROW_MIS_VAL + 1;
        final int ROW_FAC_VAL = ROW_FAC_HDR + 1;

        // ── Build the master GridPane ─────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Column constraints: label col fixed, planet cols grow equally
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setPrefWidth(130); labelCol.setMinWidth(110); labelCol.setMaxWidth(150);
        grid.getColumnConstraints().add(labelCol);

        for (int bi = 0; bi < N_PLANETS; bi++) {
            ColumnConstraints pc = new ColumnConstraints();
            pc.setHgrow(Priority.ALWAYS);
            pc.setMinWidth(120);
            if (bi < N_PLANETS - 1) {
                // Add a 1-px divider column after each planet except last
                grid.getColumnConstraints().add(pc);
                ColumnConstraints divCol = new ColumnConstraints();
                divCol.setPrefWidth(1); divCol.setMinWidth(1); divCol.setMaxWidth(1);
                grid.getColumnConstraints().add(divCol);
            } else {
                grid.getColumnConstraints().add(pc);
            }
        }

        // Helper: get grid column index for planet bi (accounting for divider cols)
        // label=0, planet0=1, div=2, planet1=3, div=4, planet2=5 ...
        // planet bi occupies column: 1 + bi*2

        // ── Row constraints ──────────────────────────────────────
        // Header row
        RowConstraints headerRC = new RowConstraints();
        headerRC.setMinHeight(170); headerRC.setPrefHeight(170);
        grid.getRowConstraints().add(headerRC);

        // Stat rows — fixed height so they always align
        for (int si = 0; si < N_STATS; si++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(STAT_ROW_H); rc.setPrefHeight(STAT_ROW_H);
            rc.setMaxHeight(STAT_ROW_H);
            grid.getRowConstraints().add(rc);
        }

        // Section banner + value rows (atmosphere, features, missions, facts)
        for (int i = 0; i < 8; i++) {
            RowConstraints rc = new RowConstraints();
            if (i % 2 == 0) { // banner row
                rc.setMinHeight(28); rc.setPrefHeight(28); rc.setMaxHeight(28);
            } else {           // content row
                rc.setMinHeight(40); rc.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }
            grid.getRowConstraints().add(rc);
        }

        // ── COLUMN 0: label cells ────────────────────────────────

        // Header spacer (col 0, row 0)
        Region headerSpacer = new Region();
        grid.add(headerSpacer, 0, ROW_HEADER);

        // Stat labels
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

        // Section banners in label col
        String[] sectionLabels = { "ATMOSPHERE", "FEATURES", "MISSIONS", "FUN FACTS" };
        int[] sectionBannerRows = { ROW_ATM_HDR, ROW_FEA_HDR, ROW_MIS_HDR, ROW_FAC_HDR };
        for (int i = 0; i < sectionLabels.length; i++) {
            grid.add(makeBannerCell(sectionLabels[i]), 0, sectionBannerRows[i]);
        }
        // Empty content cells in label col (atmosphere / features / missions / facts)
        int[] contentRows = { ROW_ATM_VAL, ROW_FEA_VAL, ROW_MIS_VAL, ROW_FAC_VAL };
        for (int r : contentRows) {
            grid.add(new Region(), 0, r);
        }

        // ── PLANET COLUMNS ───────────────────────────────────────
        for (int bi = 0; bi < N_PLANETS; bi++) {
            CelestialBody body    = bodies.get(bi);
            boolean isOrigin      = body.getName().equals(origin.getName());
            String  glowColor     = isOrigin ? GOLD : CYAN;
            String  glowDim       = isOrigin ? GOLD_DIM : CYAN_DIM;
            int     gridCol       = 1 + bi * 2; // planet grid column index

            // ── Header ──────────────────────────────────────────
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

            // ── Stat rows ────────────────────────────────────────
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

            // ── Atmosphere section banner (empty in planet cols) ──
            grid.add(makeBannerCell(""), gridCol, ROW_ATM_HDR);

            // ── Atmosphere bars ──────────────────────────────────
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

            // ── Features ─────────────────────────────────────────
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

            // ── Missions ──────────────────────────────────────────
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

            // ── Fun facts ─────────────────────────────────────────
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

            // ── Vertical divider after each planet except last ────
            if (bi < N_PLANETS - 1) {
                Region vDiv = new Region();
                vDiv.setMaxWidth(Double.MAX_VALUE); vDiv.setMaxHeight(Double.MAX_VALUE);
                vDiv.setStyle("-fx-background-color: " + DIVIDER + ";");
                // Span all rows
                int totalRows = ROW_FAC_VAL + 1;
                GridPane.setRowSpan(vDiv, totalRows);
                grid.add(vDiv, gridCol + 1, 0);
            }
        }

        // ── Top bar ───────────────────────────────────────────────
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

        // Wrap grid in scroll
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