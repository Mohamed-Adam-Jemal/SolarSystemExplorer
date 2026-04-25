package org.falconsteam.solarsystemexplorer.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;
import org.falconsteam.solarsystemexplorer.utils.OrbitCalculator;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class OrbitAnimationPane extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private OrbitCalculator calculator;
    private List<CelestialBody> bodies;

    private final Map<String, Double> angles = new HashMap<>();
    private AnimationTimer timer;
    private double speedMultiplier = 0.1;

    // Zoom
    private double zoomFactor = 1.0;
    private static final double ZOOM_MIN = 1.0;
    private static final double ZOOM_MAX = 20.0;

    // Stars
    private double[] starX, starY, starSize;
    private static final int STAR_COUNT = 200;

    public OrbitAnimationPane() {
        canvas = new Canvas();

        // ── Zoom Buttons ─────────────────────────────────────
        Button zoomIn    = new Button("+");
        Button zoomOut   = new Button("−");
        Button zoomReset = new Button("⟳");

        String btnStyle = """
            -fx-background-color: #ffffff22;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-cursor: hand;
            -fx-background-radius: 6;
            -fx-min-width: 40px;
            -fx-min-height: 40px;
        """;

        zoomIn.setStyle(btnStyle);
        zoomOut.setStyle(btnStyle);
        zoomReset.setStyle(btnStyle);

        zoomIn.setOnAction(e    -> zoomFactor = Math.min(ZOOM_MAX, zoomFactor * 1.5));
        zoomOut.setOnAction(e   -> zoomFactor = Math.max(ZOOM_MIN, zoomFactor / 1.5));
        zoomReset.setOnAction(e -> zoomFactor = 1.0);

        // ── Speed Control ─────────────────────────────────────
        Label speedLabel = new Label("Speed");
        speedLabel.setStyle("-fx-text-fill: #ffffff99; -fx-font-size: 11px;");

        Slider speedSlider = new Slider(0.01, 1.0, 0.1);
        speedSlider.setOrientation(javafx.geometry.Orientation.VERTICAL);
        speedSlider.setPrefHeight(100);
        speedSlider.setStyle("-fx-control-inner-background: #ffffff22;");
        speedSlider.valueProperty().addListener((obs, o, n) -> setSpeedMultiplier(n.doubleValue()));

        Label speedValue = new Label("1×");
        speedValue.setStyle("-fx-text-fill: #ffffff99; -fx-font-size: 10px;");
        speedSlider.valueProperty().addListener((obs, o, n) ->
            speedValue.setText(String.format("%.1f×", n.doubleValue() / 0.1))
        );

        // ── Layout ────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #ffffff22;");
        sep.setPrefWidth(30);

        VBox controls = new VBox(6, zoomIn, zoomOut, zoomReset, sep, speedLabel, speedSlider, speedValue);
        controls.setAlignment(Pos.CENTER);

        getChildren().addAll(canvas, controls);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        gc = canvas.getGraphicsContext2D();

        // Pin controls to top-right corner
        controls.layoutXProperty().bind(widthProperty().subtract(50));
        controls.setLayoutY(10);

        widthProperty().addListener((obs, o, n) -> redraw());
        heightProperty().addListener((obs, o, n) -> redraw());

        // Generate static star field once
        generateStars();
    }

    // ── Star Generation ───────────────────────────────────────
    private void generateStars() {
        starX    = new double[STAR_COUNT];
        starY    = new double[STAR_COUNT];
        starSize = new double[STAR_COUNT];
        Random rand = new Random(42);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]    = rand.nextDouble();
            starY[i]    = rand.nextDouble();
            starSize[i] = 0.8 + rand.nextDouble() * 1.5;
        }
    }

    public void init(List<CelestialBody> bodies) {
        this.bodies = bodies;
        for (CelestialBody body : bodies) {
            if (!body.getType().equals("Star")) {
                angles.put(body.getName(), Math.random() * 360);
            }
        }
        startAnimation();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                redraw();
            }
        };
        timer.start();
    }

    private void update() {
        if (bodies == null || calculator == null) return;
        for (CelestialBody body : bodies) {
            if (body.getType().equals("Star")) continue;
            double current = angles.getOrDefault(body.getName(), 0.0);
            double next    = calculator.nextAngle(current, body.getOrbitalSpeedFactor(), speedMultiplier);
            angles.put(body.getName(), next);
        }
    }

    private void redraw() {
        if (bodies == null) return;

        double w  = canvas.getWidth();
        double h  = canvas.getHeight();
        double cx = w / 2;
        double cy = h / 2;

        // Auto-scale + zoom
        double maxDistance = bodies.stream()
            .mapToDouble(CelestialBody::getDistanceFromSun)
            .max()
            .orElse(40.0);
        double auToPixels = (Math.min(w, h) / 2.0 / (maxDistance * 1.3)) * zoomFactor;

        calculator = new OrbitCalculator(cx, cy, auToPixels);

        // ── Background ────────────────────────────────────────
        gc.setFill(Color.web("#0a0a1a"));
        gc.fillRect(0, 0, w, h);

        // ── Background Stars ──────────────────────────────────
        for (int i = 0; i < STAR_COUNT; i++) {
            double brightness = (i % 3 == 0) ? 0.9 : (i % 3 == 1) ? 0.6 : 0.4;
            gc.setFill(Color.color(1, 1, 1, brightness));
            gc.fillOval(starX[i] * w, starY[i] * h, starSize[i], starSize[i]);
        }

        // ── Draw Bodies ───────────────────────────────────────
        for (CelestialBody body : bodies) {
            if (body.getType().equals("Star")) {
                drawSun(cx, cy, body);
            } else {
                // Orbit path
                double orbitR = calculator.getOrbitRadius(body.getDistanceFromSun());
                gc.setStroke(Color.web("#ffffff22"));
                gc.setLineWidth(0.5);
                gc.strokeOval(cx - orbitR, cy - orbitR, orbitR * 2, orbitR * 2);

                // Planet position
                double angle = angles.getOrDefault(body.getName(), 0.0);
                double px    = calculator.calculateX(body.getDistanceFromSun(), angle);
                double py    = calculator.calculateY(body.getDistanceFromSun(), angle);

                int r = body.getDisplayRadius();

                // Planet body
                gc.setFill(Color.web(body.getColor()));
                gc.fillOval(px - r, py - r, r * 2, r * 2);

                // Glow
                gc.setFill(Color.web(body.getColor() + "44"));
                gc.fillOval(px - r * 1.8, py - r * 1.8, r * 3.6, r * 3.6);

                // Label
                if (r >= 3) {
                    gc.setFill(Color.web("#ffffff99"));
                    gc.setFont(Font.font(10));
                    gc.fillText(body.getName(), px + r + 3, py + 4);
                }
            }
        }
    }

    private void drawSun(double cx, double cy, CelestialBody sun) {
        int r = sun.getDisplayRadius();

        gc.setFill(Color.web("#FFD70011"));
        gc.fillOval(cx - r * 3, cy - r * 3, r * 6, r * 6);
        gc.setFill(Color.web("#FFD70033"));
        gc.fillOval(cx - r * 2, cy - r * 2, r * 4, r * 4);

        gc.setFill(Color.web(sun.getColor()));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    public void setSpeedMultiplier(double m) { this.speedMultiplier = m; }
    public void stop()  { if (timer != null) timer.stop(); }
    public void start() { if (timer != null) timer.start(); }
}