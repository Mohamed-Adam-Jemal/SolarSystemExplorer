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

        // ── SplitPane : mainView | orbitPane | detailView ──
        SplitPane split = new SplitPane(mainView, orbitPane, detailView);
        split.setDividerPositions(0.22, 0.65);

        // ── Wire expand/restore on the correct variable: split ──
        detailView.setOnWidthChangeRequest(
            // onExpand — comparison opens → detail gets wider
            () -> {
                Timeline expand = new Timeline(
                    new KeyFrame(Duration.millis(420),
                        new KeyValue(
                            split.getDividers().get(1).positionProperty(),
                            0.30,               // divider 2 moves LEFT → detail wider
                            Interpolator.EASE_BOTH))
                );
                expand.play();
            },
            // onRestore — back to detail/selector → normal width
            () -> {
                Timeline restore = new Timeline(
                    new KeyFrame(Duration.millis(380),
                        new KeyValue(
                            split.getDividers().get(1).positionProperty(),
                            0.65,               // divider 2 back to original position
                            Interpolator.EASE_BOTH))
                );
                restore.play();
            }
        );

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