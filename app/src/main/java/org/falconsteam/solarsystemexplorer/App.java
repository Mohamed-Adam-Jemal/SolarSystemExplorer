package org.falconsteam.solarsystemexplorer;

import java.util.List;

import org.falconsteam.solarsystemexplorer.controller.MainController;
import org.falconsteam.solarsystemexplorer.data.PlanetDataLoader;
import org.falconsteam.solarsystemexplorer.model.CelestialBody;
import org.falconsteam.solarsystemexplorer.view.DetailView;
import org.falconsteam.solarsystemexplorer.view.MainView;
import org.falconsteam.solarsystemexplorer.view.OrbitAnimationPane;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        MainView           mainView   = new MainView();
        DetailView         detailView = new DetailView();
        OrbitAnimationPane orbitPane  = new OrbitAnimationPane();

        List<CelestialBody> bodies = new PlanetDataLoader().loadAll();

        MainController controller = new MainController(mainView, detailView, bodies);
        orbitPane.init(bodies);

        SplitPane split = new SplitPane(mainView, orbitPane, detailView);
        split.setDividerPositions(0.22, 0.65);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(split, screenBounds.getWidth(), screenBounds.getHeight());
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

    public static void main(String[] args) { launch(args); }
}