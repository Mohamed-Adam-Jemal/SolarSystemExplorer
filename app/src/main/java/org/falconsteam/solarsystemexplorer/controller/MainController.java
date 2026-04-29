package org.falconsteam.solarsystemexplorer.controller;

import java.util.List;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;
import org.falconsteam.solarsystemexplorer.view.DetailView;
import org.falconsteam.solarsystemexplorer.view.MainView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class MainController {

    private final MainView mainView;
    private final DetailView detailView;
    private final ObservableList<CelestialBody> allBodies;

    public MainController(MainView mainView, DetailView detailView, List<CelestialBody> loaded) {
        this.mainView   = mainView;
        this.detailView = detailView;
        
        detailView.setAllBodies(loaded);

        allBodies = FXCollections.observableArrayList(loaded);
        FilteredList<CelestialBody> filtered = new FilteredList<>(allBodies, b -> true);
        mainView.getListView().setItems(filtered);

        // Search filter
        mainView.getSearchField().textProperty().addListener((obs, oldVal, newVal) ->
            filtered.setPredicate(body ->
                newVal == null || newVal.isBlank() ||
                body.getName().toLowerCase().contains(newVal.toLowerCase())
            )
        );

        // Selection → detail
        mainView.getListView().getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) detailView.display(newVal);
            }
        );
    }
}