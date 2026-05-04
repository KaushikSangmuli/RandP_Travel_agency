package com.agency.ui;

import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.agency.ui.DashboardScreen.loadIcon;

public class CalendarScreen {

    private enum ViewMode { MONTH, WEEK, DAY }

    private static YearMonth currentMonth = YearMonth.now();
    private static LocalDate selectedDate = LocalDate.now();
    private static ViewMode viewMode = ViewMode.MONTH;

    private static Label monthLabel;
    private static GridPane calendarGrid;
    private static VBox tripsPanel;

    private static Button monthBtn;
    private static Button weekBtn;
    private static Button dayBtn;

    public static VBox getView() {
        VBox root = new VBox(18);
        root.getStyleClass().add("client-root");

        Label title = new Label("Calendar");
        title.getStyleClass().add("client-title");

        HBox controls = createControls();

        calendarGrid = new GridPane();
        calendarGrid.setHgap(0);
        calendarGrid.setVgap(0);
        calendarGrid.getStyleClass().add("panel");
        calendarGrid.setMaxWidth(920);

        HBox calendarWrapper = new HBox(calendarGrid);
        calendarWrapper.setAlignment(Pos.CENTER);

        tripsPanel = new VBox(12);
        tripsPanel.getStyleClass().add("panel");
        tripsPanel.setMaxWidth(920);

        HBox tripsWrapper = new HBox(tripsPanel);
        tripsWrapper.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, controls, calendarWrapper, tripsWrapper);

        refreshCalendar();
        return root;
    }

    private static HBox createControls() {

        Button prev = new Button("",loadIcon("left.png",16));
        Button next = new Button("",loadIcon("right.png",16));
        Button today = new Button("Today");

        monthBtn = new Button("Month");
        weekBtn = new Button("Week");
        dayBtn = new Button("Day");

        prev.getStyleClass().add("page-btn");
        next.getStyleClass().add("page-btn");
        today.getStyleClass().add("page-btn");

        monthLabel = new Label();
        monthLabel.getStyleClass().add("section-title");
        monthLabel.setPrefWidth(180);
        monthLabel.setAlignment(Pos.CENTER);

        setActiveButton(monthBtn);

        prev.setOnAction(e -> {
            if (viewMode == ViewMode.MONTH) {
                currentMonth = currentMonth.minusMonths(1);
                selectedDate = currentMonth.atDay(1);
            } else if (viewMode == ViewMode.WEEK) {
                selectedDate = selectedDate.minusWeeks(1);
            } else {
                selectedDate = selectedDate.minusDays(1);
            }

            currentMonth = YearMonth.from(selectedDate);
            refreshCalendar();
        });

        next.setOnAction(e -> {
            if (viewMode == ViewMode.MONTH) {
                currentMonth = currentMonth.plusMonths(1);
                selectedDate = currentMonth.atDay(1);
            } else if (viewMode == ViewMode.WEEK) {
                selectedDate = selectedDate.plusWeeks(1);
            } else {
                selectedDate = selectedDate.plusDays(1);
            }

            currentMonth = YearMonth.from(selectedDate);
            refreshCalendar();
        });

        today.setOnAction(e -> {
            selectedDate = LocalDate.now();
            currentMonth = YearMonth.now();
            refreshCalendar();
        });

        monthBtn.setOnAction(e -> {
            viewMode = ViewMode.MONTH;
            setActiveButton(monthBtn);
            refreshCalendar();
        });

        weekBtn.setOnAction(e -> {
            viewMode = ViewMode.WEEK;
            setActiveButton(weekBtn);
            refreshCalendar();
        });

        dayBtn.setOnAction(e -> {
            viewMode = ViewMode.DAY;
            setActiveButton(dayBtn);
            refreshCalendar();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(12, prev, monthLabel, next, today, spacer, monthBtn, weekBtn, dayBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setMaxWidth(920);

        HBox wrapper = new HBox(controls);
        wrapper.setAlignment(Pos.CENTER);

        return wrapper;
    }

    private static void setActiveButton(Button active) {
        Button[] buttons = {monthBtn, weekBtn, dayBtn};

        for (Button btn : buttons) {
            btn.getStyleClass().remove("view-all-btn");

            if (!btn.getStyleClass().contains("page-btn")) {
                btn.getStyleClass().add("page-btn");
            }
        }

        active.getStyleClass().remove("page-btn");

        if (!active.getStyleClass().contains("view-all-btn")) {
            active.getStyleClass().add("view-all-btn");
        }
    }

    private static void refreshCalendar() {
        calendarGrid.getChildren().clear();

        if (viewMode == ViewMode.MONTH) {
            monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
            showMonthView();
        } else if (viewMode == ViewMode.WEEK) {
            monthLabel.setText("Week of " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            showWeekView();
        } else {
            monthLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            showDayView();
        }

        showTripsForDate(selectedDate);
    }

    private static void showMonthView() {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < 7; i++) {
            calendarGrid.add(header(days[i]), i, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int startCol = firstDay.getDayOfWeek().getValue() % 7;

        int row = 1;
        int col = startCol;

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            calendarGrid.add(createDateCell(date, 130, 82), col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private static void showWeekView() {
        LocalDate start = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() % 7);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < 7; i++) {
            calendarGrid.add(header(days[i]), i, 0);
            calendarGrid.add(createDateCell(start.plusDays(i), 130, 120), i, 1);
        }
    }

    private static void showDayView() {
        calendarGrid.add(createDateCell(selectedDate, 920, 160), 0, 0);
    }

    private static Label header(String text) {
        Label label = new Label(text);
        label.setPrefSize(130, 42);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("calendar-day-header");
        return label;
    }

    private static VBox createDateCell(LocalDate date, double width, double height) {
        VBox cell = new VBox(5);
        cell.setPrefSize(width, height);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.getStyleClass().add("calendar-cell");

        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));

        dateLabel.setStyle("-fx-font-weight:bold;");

        if (date.equals(selectedDate)) {
            dateLabel.getStyleClass().add("calendar-selected");
        } else if (date.equals(LocalDate.now())) {
            dateLabel.getStyleClass().add("calendar-today");
        }
        List<Trip> trips = getTripsByDate(date);

        if (!trips.isEmpty()) {
            Circle dot = new Circle(2);
            dot.setFill(Color.ORANGE);
            dot.getStyleClass().add("calendar-dot");

            Label count = new Label(trips.size() + " trip" + (trips.size() > 1 ? "s" : ""));
            count.getStyleClass().add("card-subtitle");

            cell.getChildren().addAll(dateLabel, dot, count);
        } else {
            cell.getChildren().add(dateLabel);
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            currentMonth = YearMonth.from(date);
            refreshCalendar();
        });

        return cell;
    }

    private static void showTripsForDate(LocalDate date) {
        tripsPanel.getChildren().clear();

        Label title = new Label("Trips on " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        title.getStyleClass().add("section-title");

        List<Trip> trips = getTripsByDate(date);

        if (trips.isEmpty()) {
            Label empty = new Label("No trips on this date");
            empty.getStyleClass().add("card-subtitle");
            tripsPanel.getChildren().addAll(title, empty);
            return;
        }

        GridPane table = new GridPane();
        table.setHgap(25);
        table.setVgap(14);

        addHeader(table, "Client Name", 0);
        addHeader(table, "Destination", 1);
        addHeader(table, "Trip Type", 2);
        addHeader(table, "Contact", 3);
        addHeader(table, "Status", 4);

        int row = 1;

        for (Trip trip : trips) {
            table.add(cell(trip.getClientName()), 0, row);
            table.add(cell(trip.getDestination()), 1, row);
            table.add(cell(trip.getType()), 2, row);
            table.add(cell(getClientPhone(trip.getClientId())), 3, row);

            Label status = cell(trip.getStatus());
            status.getStyleClass().add("status-pill");

            String tripStatus = trip.getStatus() == null ? "" : trip.getStatus();

            if (tripStatus.equalsIgnoreCase("Confirmed")) {
                status.getStyleClass().add("status-confirmed");
            } else if (tripStatus.equalsIgnoreCase("Cancelled")) {
                status.getStyleClass().add("status-cancelled");
            } else {
                status.getStyleClass().add("status-pending");
            }

            status.setPrefWidth(105);
            status.setMaxWidth(105);
            status.setAlignment(Pos.CENTER);
            table.add(status, 4, row);

            row++;
        }

        tripsPanel.getChildren().addAll(title, table);
    }

    private static void addHeader(GridPane table, String text, int col) {
        Label label = new Label(text);
        label.getStyleClass().add("trip-header-text");
        label.setPrefWidth(150);
        table.add(label, col, 0);
    }

    private static Label cell(String text) {
        String value = text == null || text.isEmpty() ? "-" : text;

        Label label = new Label(value);
        label.setPrefWidth(150);
        label.setWrapText(false);
        label.setEllipsisString("...");

        Tooltip.install(label, new Tooltip(value));

        return label;
    }

    private static List<Trip> getTripsByDate(LocalDate date) {
        return TripRepository.getAllTrips()
                .stream()
                .filter(t -> t.getDate() != null && t.getDate().equals(date.toString()))
                .collect(Collectors.toList());
    }

    private static String getClientPhone(int clientId) {
        Client client = ClientRepository.getClientById(clientId);
        return client == null ? "-" : client.getPhone();
    }
}