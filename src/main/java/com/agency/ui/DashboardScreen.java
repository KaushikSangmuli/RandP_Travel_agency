package com.agency.ui;

import com.agency.data.ClientData;
import com.agency.data.TripData;
import com.agency.model.Client;
import com.agency.model.Trip;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardScreen {
    private static final int DASHBOARD_PAGE_SIZE = 4;

    private static VBox content;
    private static Button dashboardBtn;
    private static Button clientsBtn;
    private static Button tripsBtn;
    private static Button activeMenuButton;
    private static Button calendarBtn;
    private static Button documentsBtn;
    private static Button reportsBtn;
    private static Button settingsBtn;

    public static void show(Stage stage) {
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(230);
        sidebar.getStyleClass().add("sidebar");

        Label logo = new Label("✈ KP TOURS & TRAVELS");
        logo.getStyleClass().add("logo");

        dashboardBtn = createMenuButton("Dashboard", "/icons/dashboard.png", true);
        clientsBtn = createMenuButton("Clients", "/icons/clients.png", false);
        tripsBtn = createMenuButton("Trips", "/icons/travelling.png", false);

        calendarBtn = createMenuButton("Calendar", "/icons/calendar.png", false);
        documentsBtn = createMenuButton("Documents", "/icons/document.png", false);
        reportsBtn = createMenuButton("Reports", "/icons/reports.png", false);
        settingsBtn = createMenuButton("Settings", "/icons/settings.png", false);

        sidebar.getChildren().addAll(logo, dashboardBtn, clientsBtn, tripsBtn,
                calendarBtn, documentsBtn, reportsBtn, settingsBtn);

        content = new VBox(22);
        content.getStyleClass().add("content");

        setActive(dashboardBtn);
        loadDashboardContent();

        dashboardBtn.setOnAction(e -> {
            setActive(dashboardBtn);
            loadDashboardContent();
        });

        clientsBtn.setOnAction(e -> {
            setActive(clientsBtn);
            loadClientsScreen();
        });

        tripsBtn.setOnAction(e -> {
            setActive(tripsBtn);
            loadTripsScreen();
        });
        calendarBtn.setOnAction(e -> {
            setActive(calendarBtn);
            content.getChildren().clear();
            content.getChildren().add(CalendarScreen.getView());
        });

        documentsBtn.setOnAction(e -> {
            setActive(documentsBtn);
            content.getChildren().clear();
            content.getChildren().add(DocumentScreen.getView());
        });

        settingsBtn.setOnAction(e -> {
            setActive(settingsBtn);
            content.getChildren().clear();
            content.getChildren().add(SettingsScreen.getView());
        });
        BorderPane rightSide = new BorderPane();
        rightSide.setTop(createTopbar());
        rightSide.setCenter(content);

        HBox root = new HBox(sidebar, rightSide);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(DashboardScreen.class.getResource("/css/dashboard.css").toExternalForm());
        scene.getStylesheets().add(DashboardScreen.class.getResource("/css/client.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private static void loadDashboardContent() {
        content.getChildren().clear();

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        HBox cards = new HBox(18);

        VBox c1 = createCard("/icons/client.png", "Total Clients",
                String.valueOf(ClientData.clients.size()), "All Registered Clients");

        VBox c2 = createCard("/icons/trips.png", "Total Trips",
                String.valueOf(TripData.trips.size()), "All Trips Stored");

        VBox c3 = createCard("/icons/upcomingTrip.png", "Upcoming Trips",
                String.valueOf(getUpcomingTripCount()), "Next 7 Days");

        VBox c4 = createCard("/icons/thisMonthTrip.png", "This Month Trips",
                String.valueOf(getThisMonthTripCount()),
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        c1.setOnMouseClicked(e -> {
            setActive(clientsBtn);
            loadClientsScreen();
        });

        cards.getChildren().addAll(c1, c2, c3, c4);
        for (VBox card : new VBox[]{c1, c2, c3, c4}) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        HBox mainArea = new HBox(18);
        mainArea.setAlignment(Pos.TOP_LEFT);
        mainArea.setFillHeight(true);
        mainArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox trips = createUpcomingTripsPanel();
        VBox actions = createQuickActions();

        HBox.setHgrow(trips, Priority.ALWAYS);
        trips.setMaxHeight(Double.MAX_VALUE);
        actions.setMaxHeight(Double.MAX_VALUE);

        trips.prefHeightProperty().bind(mainArea.heightProperty());
        actions.prefHeightProperty().bind(mainArea.heightProperty());

        mainArea.getChildren().addAll(trips, actions);

        content.getChildren().addAll(title, cards, mainArea);
    }

    private static VBox createUpcomingTripsPanel() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Upcoming Trips");
        title.getStyleClass().add("section-title");

        Button viewAll = new Button("View All");
        viewAll.getStyleClass().add("view-all-btn");
        viewAll.setOnAction(e -> {
            setActive(tripsBtn);
            loadTripsScreen();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, viewAll);

        VBox rows = new VBox(10);

        Button prev = new Button("‹ Prev");
        Button next = new Button("Next ›");
        Label pageInfo = new Label();

        prev.getStyleClass().add("page-btn");
        next.getStyleClass().add("page-btn");
        pageInfo.getStyleClass().add("page-info");

        HBox pagination = new HBox(12, prev, pageInfo, next);
        pagination.setAlignment(Pos.CENTER_RIGHT);

        List<Trip> upcoming = getUpcomingTripsList();
        int[] page = {0};

        Runnable refresh = () -> {
            rows.getChildren().clear();
            rows.getChildren().add(tripHeaderRow());

            int total = upcoming.size();
            int pageCount = Math.max(1, (int) Math.ceil((double) total / DASHBOARD_PAGE_SIZE));

            if (page[0] < 0) page[0] = 0;
            if (page[0] >= pageCount) page[0] = pageCount - 1;

            int from = page[0] * DASHBOARD_PAGE_SIZE;
            int to = Math.min(from + DASHBOARD_PAGE_SIZE, total);

            if (total == 0) {
                Label empty = new Label("No upcoming trips in next 7 days");
                empty.getStyleClass().add("card-subtitle");
                rows.getChildren().add(empty);
            } else {
                for (int i = from; i < to; i++) {
                    rows.getChildren().add(tripRow(upcoming.get(i)));
                }
            }

            pageInfo.setText("Page " + (page[0] + 1) + " of " + pageCount);
            prev.setDisable(page[0] == 0);
            next.setDisable(page[0] >= pageCount - 1);
        };

        prev.setOnAction(e -> {
            page[0]--;
            refresh.run();
        });

        next.setOnAction(e -> {
            page[0]++;
            refresh.run();
        });

        refresh.run();

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        VBox panel = new VBox(15, header, rows, push, pagination);
        panel.getStyleClass().add("panel");
        panel.setMaxHeight(Double.MAX_VALUE);

        return panel;
    }

    private static HBox tripHeaderRow() {
        return tripRowBase("Date", "Client", "Destination", "Type", "Status", "Contact", "Details", true, null);
    }

    private static HBox tripRow(Trip trip) {
        return tripRowBase(trip.getDate(), trip.getClientName(), trip.getDestination(), trip.getType(),
                trip.getStatus(), getClientPhone(trip.getClientId()), "View", false, trip);
    }

    private static HBox tripRowBase(String date, String client, String destination, String type,
                                    String status, String contact, String details, boolean header, Trip trip) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(header ? "trip-header-row" : "trip-row");

        Label l1 = dashCell(date);
        Label l2 = dashCell(client);
        Label l3 = dashCell(destination);
        Label l4 = dashCell(type);
        Label l5 = dashCell(status);
        Label l6 = dashCell(contact);

        for (Label l : new Label[]{l1, l2, l3, l4, l5, l6}) {
            HBox.setHgrow(l, Priority.ALWAYS);
        }

        if (header) {
            Label l7 = dashCell(details);
            HBox.setHgrow(l7, Priority.ALWAYS);

            for (Label l : new Label[]{l1, l2, l3, l4, l5, l6, l7}) {
                l.getStyleClass().add("trip-header-text");
            }

            row.getChildren().addAll(l1, l2, l3, l4, l5, l6, l7);
        } else {
            l5.getStyleClass().add(status.equalsIgnoreCase("Confirmed") ? "status-confirmed" : "status-pending");

            Button view = new Button("View");
            view.getStyleClass().add("view-details-btn");
            view.setOnAction(e -> {
                content.getChildren().clear();
                TripScreen.showTripDetailsScreen(content, trip);
            });
            HBox actionBox = new HBox(view);
            actionBox.setAlignment(Pos.CENTER_LEFT);
            actionBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(actionBox, Priority.ALWAYS);

            row.getChildren().addAll(l1, l2, l3, l4, l5, l6, actionBox);
        }

        return row;
    }

    private static Label dashCell(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static List<Trip> getUpcomingTripsList() {
        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);

        return TripData.trips.stream()
                .filter(t -> parseDate(t.getDate()) != null)
                .filter(t -> {
                    LocalDate d = parseDate(t.getDate());
                    return !d.isBefore(today) && !d.isAfter(next7Days);
                })
                .sorted(Comparator.comparing(t -> parseDate(t.getDate())))
                .collect(Collectors.toList());
    }

    private static int getUpcomingTripCount() {
        return getUpcomingTripsList().size();
    }

    private static int getThisMonthTripCount() {
        LocalDate now = LocalDate.now();

        return (int) TripData.trips.stream()
                .filter(t -> parseDate(t.getDate()) != null)
                .filter(t -> parseDate(t.getDate()).getMonth() == now.getMonth())
                .filter(t -> parseDate(t.getDate()).getYear() == now.getYear())
                .count();
    }

    private static LocalDate parseDate(String date) {
        try {
            if (date == null || date.isEmpty()) return null;
            return LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    private static Client getClientById(int clientId) {
        int index = clientId - 1;
        if (index >= 0 && index < ClientData.clients.size()) {
            return ClientData.clients.get(index);
        }
        return null;
    }

    private static String getClientPhone(int clientId) {
        Client client = getClientById(clientId);
        return client == null ? "-" : client.getPhone();
    }

    private static VBox createQuickActions() {
        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        Button addClient = createActionButton("+ Add New Client", "blue-btn");
        addClient.setOnAction(e -> {
            setActive(clientsBtn);
            content.getChildren().clear();
            content.getChildren().add(ClientsScreen.getView());
        });

        Button addTrip = createActionButton("+ Add New Trip", "green-btn");
        addTrip.setOnAction(e -> loadAddTripScreen());

        Button upload = createActionButton("⇧ Upload Document", "purple-btn");
        Button backup = createActionButton("⛁ Backup Now", "orange-btn");

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        VBox box = new VBox(25, title, addClient, addTrip, upload, backup, push);
        box.getStyleClass().add("panel");
        box.setPrefWidth(300);
        box.setMaxHeight(Double.MAX_VALUE);

        return box;
    }

    private static HBox createTopbar() {
        HBox topbar = new HBox(15);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.getStyleClass().add("topbar");

        TextField search = new TextField();
        search.setPromptText("Search anything...");
        search.setPrefWidth(320);
        search.getStyleClass().add("search");
        HBox.setMargin(search, new Insets(0, 0, 0, 25));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        date.getStyleClass().add("date-label");

        Label user = new Label("Admin");
        user.getStyleClass().add("user-label");

        topbar.getChildren().addAll(search, spacer, date, user);
        return topbar;
    }

    private static void loadClientsScreen() {
        content.getChildren().clear();
        content.getChildren().add(ClientsScreen.getView());
    }

    private static void loadTripsScreen() {
        content.getChildren().clear();
        content.getChildren().add(TripScreen.getView());
    }

    private static void loadAddTripScreen() {
        setActive(tripsBtn);
        content.getChildren().clear();
        content.getChildren().add(TripScreen.getAddTripViewDirect());
    }

    private static Button createMenuButton(String text, String iconPath, boolean active) {
        ImageView icon = new ImageView(new Image(DashboardScreen.class.getResource(iconPath).toExternalForm()));
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setPreserveRatio(true);

        Button btn = new Button(text, icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setGraphicTextGap(12);
        btn.getStyleClass().add(active ? "sidebar-button-active" : "sidebar-button");
        return btn;
    }

    private static VBox createCard(String iconPath, String title, String value, String subtitle) {
        ImageView icon = new ImageView(new Image(DashboardScreen.class.getResource(iconPath).toExternalForm()));
        icon.setFitWidth(54);
        icon.setFitHeight(54);
        icon.setPreserveRatio(true);

        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("dashboard-icon-box");

        Label t = new Label(title);
        t.getStyleClass().add("card-title");

        Label v = new Label(value);
        v.getStyleClass().add("card-value");

        Label s = new Label(subtitle);
        s.getStyleClass().add("card-subtitle");

        VBox textBox = new VBox(5, t, v, s);
        HBox cardContent = new HBox(18, iconBox, textBox);
        cardContent.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(cardContent);
        box.getStyleClass().add("card");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefHeight(130);

        return box;
    }

    private static Button createActionButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(styleClass);
        return btn;
    }
    private static void setActive(Button selected) {
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("sidebar-button-active");

            if (!activeMenuButton.getStyleClass().contains("sidebar-button")) {
                activeMenuButton.getStyleClass().add("sidebar-button");
            }
        }

        selected.getStyleClass().remove("sidebar-button");

        if (!selected.getStyleClass().contains("sidebar-button-active")) {
            selected.getStyleClass().add("sidebar-button-active");
        }

        activeMenuButton = selected;
    }
}