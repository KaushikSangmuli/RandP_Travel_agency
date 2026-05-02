package com.agency.ui;

import com.agency.data.TripData;
import com.agency.model.Trip;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarScreen {

    private static YearMonth currentMonth = YearMonth.now();
    private static VBox root;
    private static GridPane calendarGrid;
    private static VBox tripListBox;
    private static LocalDate selectedDate = null;
    private static VBox selectedBox = null;

    public static VBox getView() {
        root = new VBox(20);
        root.getStyleClass().add("client-root");
//        root.getStylesheets().add(
//                CalendarScreen.class.getResource("/css/client.css").toExternalForm()
//        );
        buildUI();


        return root;
    }

    private static void buildUI() {

        // HEADER
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button prev = new Button("‹");
        Button next = new Button("›");
        Button todayBtn = new Button("Today");

        Label monthLabel = new Label();
        monthLabel.getStyleClass().add("client-title");

        prev.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refresh(monthLabel);
        });

        next.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refresh(monthLabel);
        });

        todayBtn.setOnAction(e -> {
            currentMonth = YearMonth.now();
            refresh(monthLabel);
        });

        header.getChildren().addAll(prev, next, monthLabel, todayBtn);

        // CALENDAR GRID
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.getStyleClass().add("panel");

        // TRIP LIST
        tripListBox = new VBox(10);
        tripListBox.getStyleClass().add("panel");

        root.getChildren().addAll(header, calendarGrid, tripListBox);

        refresh(monthLabel);
    }

    private static void refresh(Label monthLabel) {

        calendarGrid.getChildren().clear();
        tripListBox.getChildren().clear();

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Satu"};

        for (int i = 0; i < 7; i++) {
            Label day = new Label(days[i]);
            day.setPrefWidth(120);
            calendarGrid.add(day, i, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startDay = firstDay.getDayOfWeek().getValue() % 7;

        int totalDays = currentMonth.lengthOfMonth();

        int row = 1;
        int col = startDay;

        for (int day = 1; day <= totalDays; day++) {

            LocalDate date = currentMonth.atDay(day);

            VBox cell = createDateCell(date);

            calendarGrid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private static VBox createDateCell(LocalDate date) {

        VBox box = new VBox(5);
        box.setPrefSize(120, 90);
        box.getStyleClass().clear();
        box.getStyleClass().add("trip-row");

        Label day = new Label(String.valueOf(date.getDayOfMonth()));

//         highlight today
//        if (date.equals(LocalDate.now())) {
//            day.setStyle("-fx-background-color:#2563EB; -fx-text-fill:white; -fx-padding:4 8; -fx-background-radius:20;");
//        }

/*        KS
                added this logic in the update date */

        // check trips
        List<Trip> trips = TripData.trips.stream()
                .filter(t -> t.getDate().equals(date.toString()))
                .collect(Collectors.toList());

        if (!trips.isEmpty()) {
            Label dot = new Label("•");
            dot.setStyle("-fx-text-fill: orange;");
            box.getChildren().addAll(day, dot);
        } else {
            box.getChildren().add(day);
        }

//        box.setOnMouseClicked(e -> showTripsForDate(date));
        if (date.equals(selectedDate)) {
            box.getStyleClass().add("selected-date");
            selectedBox = box; // rebind after refresh
        } else if (date.equals(LocalDate.now())) {
            box.getStyleClass().add("today-date");
            selectedBox = box;
        }

        box.setOnMouseClicked(e -> {

            // remove old selection
            if (selectedBox != null) {
                selectedBox.getStyleClass().remove("selected-date");
                selectedBox.getStyleClass().add("trip-row"); // restore base style
            }
            // update new selection
            selectedDate = date;
            selectedBox = box;

            box.getStyleClass().add("selected-date");
            showTripsForDate(date);
        });





        return box;
    }

    public static void updateSelectedDate(LocalDate date , VBox box) {
        if (date.equals(selectedDate)) {
            box.setStyle("-fx-background-color:#2563EB; -fx-text-fill:white; -fx-padding:4 8; -fx-background-radius:20;");
//           box.getChildren().get(1);
        }
    }
    private static void showTripsForDate(LocalDate date) {


        tripListBox.getChildren().clear();

        Label title = new Label("Trips on " + date);
        title.getStyleClass().add("section-title");

        VBox rows = new VBox(8);

        List<Trip> trips = TripData.trips.stream()
                .filter(t -> t.getDate().equals(date.toString()))
                .collect(Collectors.toList());

        if (trips.isEmpty()) {
            rows.getChildren().add(new Label("No trips"));
        } else {
            for (Trip t : trips) {
                rows.getChildren().add(new Label(
                        t.getClientName() + " | " +
                                t.getDestination() + " | " +
                                t.getType() + " | " +
                                t.getStatus()
                ));
            }
        }

        tripListBox.getChildren().addAll(title, rows);
    }
}