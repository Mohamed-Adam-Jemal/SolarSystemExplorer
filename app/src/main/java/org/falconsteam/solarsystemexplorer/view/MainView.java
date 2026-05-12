package org.falconsteam.solarsystemexplorer.view;

import org.falconsteam.solarsystemexplorer.model.CelestialBody;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final ListView<CelestialBody> listView    = new ListView<>();
    private final TextField               searchField = new TextField();
    private final Button                  calcButton  = new Button("⟁  DISTANCE CALCULATOR");

    public MainView() {
        getStyleClass().add("sidebar");

        // ── Header ──────────────────────────────────────────
        Label icon = new Label("🌌");
        icon.setStyle("-fx-font-size: 30px;");

        Label title = new Label("Solar System");
        title.getStyleClass().add("app-title");

        Label sub = new Label("Explorer");
        sub.getStyleClass().add("app-subtitle");

        VBox titleBox = new VBox(1, title, sub);
        HBox header = new HBox(10, icon, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 16, 16, 16));

        // ── Search ───────────────────────────────────────────
        searchField.setPromptText("🔍  Search bodies...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(Double.MAX_VALUE);

        HBox searchWrap = new HBox(searchField);
        searchWrap.setPadding(new Insets(0, 14, 10, 14));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // ── Section label ────────────────────────────────────
        Label section = new Label("ALL BODIES");
        section.getStyleClass().add("section-label");
        section.setPadding(new Insets(0, 16, 6, 16));

        // ── List ─────────────────────────────────────────────
        listView.getStyleClass().add("body-list");
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CelestialBody item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buildCard(item, isSelected()));
                }
            }

            @Override
            public void updateSelected(boolean sel) {
                super.updateSelected(sel);
                if (getItem() != null) setGraphic(buildCard(getItem(), sel));
            }
        });

        // ── Distance Calculator button ────────────────────────
        String btnBase =
            "-fx-background-color: rgba(79,195,247,0.08);" +
            "-fx-text-fill: #4fc3f7;" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-padding: 10 14 10 14;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(79,195,247,0.30);" +
            "-fx-border-width: 1; -fx-border-radius: 6;" +
            "-fx-cursor: hand; -fx-letter-spacing: 0.8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(79,195,247,0.25), 8, 0.30, 0, 0);" +
            "-fx-alignment: CENTER;";
        String btnHover =
            "-fx-background-color: rgba(79,195,247,0.16);" +
            "-fx-text-fill: #4fc3f7;" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-padding: 10 14 10 14;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(79,195,247,0.70);" +
            "-fx-border-width: 1; -fx-border-radius: 6;" +
            "-fx-cursor: hand; -fx-letter-spacing: 0.8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(79,195,247,0.50), 14, 0.45, 0, 0);" +
            "-fx-alignment: CENTER;";
        calcButton.setMaxWidth(Double.MAX_VALUE);
        calcButton.setStyle(btnBase);
        calcButton.setOnMouseEntered(e -> calcButton.setStyle(btnHover));
        calcButton.setOnMouseExited(e  -> calcButton.setStyle(btnBase));

        HBox calcWrap = new HBox(calcButton);
        calcWrap.setPadding(new Insets(8, 14, 14, 14));
        HBox.setHgrow(calcButton, Priority.ALWAYS);

        // ── Layout ────────────────────────────────────────────
        VBox top = new VBox(header, searchWrap, section);
        top.getStyleClass().add("sidebar");
        setTop(top);
        setCenter(listView);
        setBottom(calcWrap);
        setPrefWidth(240);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private HBox buildCard(CelestialBody item, boolean selected) {
        ImageView img = new ImageView();
        img.setFitWidth(34);
        img.setFitHeight(34);
        img.setPreserveRatio(true);
        try {
            Image image = PlanetAssets.loadImage(item.getName(), 44, 44);
            if (image != null) img.setImage(image);
        } catch (Exception ignored) {}

        StackPane imgWrap = new StackPane(img);
        imgWrap.setMinSize(44, 44);
        imgWrap.setMaxSize(44, 44);
        imgWrap.setPrefSize(44, 44);
        imgWrap.setStyle("-fx-background-color: transparent;");
        imgWrap.setAlignment(Pos.CENTER);

        Label name = new Label(item.getName());
        name.getStyleClass().add(selected ? "card-name-selected" : "card-name");

        Label badge = new Label(item.getType());
        badge.getStyleClass().addAll("badge", PlanetAssets.getBadgeClass(item));

        VBox info = new VBox(4, name, badge);
        info.setAlignment(Pos.CENTER_LEFT);

        HBox card = new HBox(12, imgWrap, info);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add(selected ? "planet-card-selected" : "planet-card");

        return card;
    }

    public ListView<CelestialBody> getListView()  { return listView; }
    public TextField               getSearchField() { return searchField; }
    public Button                  getCalcButton() { return calcButton; }
}