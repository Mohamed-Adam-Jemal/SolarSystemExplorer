package org.falconsteam.solarsystemexplorer;

import java.util.List;

import org.falconsteam.solarsystemexplorer.controller.MainController;
import org.falconsteam.solarsystemexplorer.data.PlanetDataLoader;
import org.falconsteam.solarsystemexplorer.model.CelestialBody;
import org.falconsteam.solarsystemexplorer.view.DetailView;
import org.falconsteam.solarsystemexplorer.view.MainView;
import org.falconsteam.solarsystemexplorer.view.OrbitAnimationPane;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // ── Layout constants ─────────────────────────────────────────
    // Two-panel state (no detail): sidebar | orbit
    private static final double DIV_SIDEBAR_2P    = 0.18;

    // Three-panel state (detail open): sidebar | orbit | detail
    private static final double DIV_SIDEBAR_3P    = 0.15;
    private static final double DIV_DETAIL_3P     = 0.62;

    // Three-panel state during comparison (detail wider)
    private static final double DIV_DETAIL_EXPAND = 0.28;

    @Override
    public void start(Stage stage) {
        MainView mainView = new MainView();
        Pane overlay = new Pane();
        overlay.setPickOnBounds(false);
        DetailView detailView = new DetailView(overlay);
        OrbitAnimationPane orbitPane = new OrbitAnimationPane();

        List<CelestialBody> bodies = new PlanetDataLoader().loadAll();
        MainController controller = new MainController(mainView, detailView, bodies);
        orbitPane.init(bodies);

        // ── Start with only 2 panels: sidebar | orbit ────────────
        SplitPane split = new SplitPane(mainView, orbitPane);
        split.setDividerPositions(DIV_SIDEBAR_2P);

        // ── First selection → slide detail panel in from the right ──
        detailView.setOnFirstDisplay(() -> {
            if (!split.getItems().contains(detailView)) {
                split.getItems().add(detailView);
                // Park divider 1 at far right so it slides in visually
                split.getDividers().get(1).setPosition(1.0);
                Timeline slideIn = new Timeline(
                    new KeyFrame(Duration.millis(400),
                        new KeyValue(split.getDividers().get(0).positionProperty(),
                            DIV_SIDEBAR_3P, Interpolator.EASE_BOTH),
                        new KeyValue(split.getDividers().get(1).positionProperty(),
                            DIV_DETAIL_3P, Interpolator.EASE_BOTH)
                    )
                );
                slideIn.play();
            }
        });

        // ── Close button → slide detail panel back out ────────────
        detailView.setOnClose(() -> {
            if (split.getItems().contains(detailView)) {
                Timeline slideOut = new Timeline(
                    new KeyFrame(Duration.millis(350),
                        new KeyValue(split.getDividers().get(0).positionProperty(),
                            DIV_SIDEBAR_2P, Interpolator.EASE_BOTH),
                        new KeyValue(split.getDividers().get(1).positionProperty(),
                            1.0, Interpolator.EASE_BOTH)
                    )
                );
                slideOut.setOnFinished(ev -> {
                    split.getItems().remove(detailView);
                    split.setDividerPositions(DIV_SIDEBAR_2P);
                    detailView.resetFirstDisplay();
                    mainView.getListView().getSelectionModel().clearSelection();
                });
                slideOut.play();
            }
        });

        // ── Expand (comparison) / restore (back to detail) ───────
        detailView.setOnWidthChangeRequest(
            // onExpand — comparison panel → give detail more room
            () -> {
                Timeline expand = new Timeline(
                    new KeyFrame(Duration.millis(420),
                        new KeyValue(split.getDividers().get(1).positionProperty(),
                            DIV_DETAIL_EXPAND, Interpolator.EASE_BOTH))
                );
                expand.play();
            },
            // onRestore — back to detail/selector → normal layout
            () -> {
                if (split.getDividers().size() > 1) {
                    Timeline restore = new Timeline(
                        new KeyFrame(Duration.millis(380),
                            new KeyValue(split.getDividers().get(1).positionProperty(),
                                DIV_DETAIL_3P, Interpolator.EASE_BOTH))
                    );
                    restore.play();
                }
            }
        );

        // ── Sidebar compare button → open selector in detail panel ──
        mainView.getCompareButton().setOnAction(e -> {
            mainView.getListView().getSelectionModel().clearSelection();
            detailView.showCompareSelector();
        });

        StackPane root = new StackPane();
        root.getChildren().addAll(split, overlay);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(
            getClass().getResource("/styles/main.css").toExternalForm()
        );

        stage.setTitle("Solar System Explorer");
        stage.setMinWidth(900);
        stage.setMinHeight(500);
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}