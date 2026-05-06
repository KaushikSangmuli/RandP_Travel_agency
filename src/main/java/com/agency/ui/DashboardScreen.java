package com.agency.ui;

import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import com.agency.util.AppLogger;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardScreen {

    private static final int DASHBOARD_PAGE_SIZE = 4;

    private static VBox content;
    private static Button dashboardBtn, clientsBtn, tripsBtn, calendarBtn, documentsBtn, reportsBtn, settingsBtn;
    private static Button activeMenuButton;
    private static HBox mainRoot;
    private static Label userLabel;

    private static List<Trip> cachedTrips = new ArrayList<>();
    private static List<Client> cachedClients = new ArrayList<>();

    public static void show(Stage stage) {
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(230);
        sidebar.getStyleClass().add("sidebar");
        Image image = new Image(DashboardScreen.class.getResourceAsStream("/icons/tour.png"));

        ImageView icon = new ImageView(image);
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        icon.setPreserveRatio(true);
        Label logo = new Label("KP TOURS & TRAVELS",icon);
        logo.setMaxWidth(220);
        logo.setWrapText(false);
        logo.getStyleClass().add("logo");

        dashboardBtn = createMenuButton("Dashboard", "/icons/dashboard.png", true);
        clientsBtn = createMenuButton("Clients", "/icons/clients.png", false);
        tripsBtn = createMenuButton("Trips", "/icons/travelling.png", false);
        calendarBtn = createMenuButton("Calendar", "/icons/calendar.png", false);
        documentsBtn = createMenuButton("Documents", "/icons/document.png", false);
        reportsBtn = createMenuButton("Reports", "/icons/reports.png", false);
        settingsBtn = createMenuButton("Settings", "/icons/settings.png", false);

        sidebar.getChildren().addAll(
                logo, dashboardBtn, clientsBtn, tripsBtn,
                calendarBtn, documentsBtn, reportsBtn, settingsBtn
        );

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
            content.getChildren().setAll(CalendarScreen.getView());
        });

        documentsBtn.setOnAction(e -> {
            setActive(documentsBtn);
            content.getChildren().setAll(DocumentScreen.getView());
        });

        reportsBtn.setOnAction(e -> {
            setActive(reportsBtn);
            content.getChildren().setAll(ReportsScreen.getView());
        });

        settingsBtn.setOnAction(e -> {
            setActive(settingsBtn);
            content.getChildren().setAll(SettingsScreen.getView());
        });

        BorderPane rightSide = new BorderPane();
        rightSide.setTop(createTopbar());
        rightSide.setCenter(content);

        mainRoot = new HBox(sidebar, rightSide);
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        Scene oldScene = stage.getScene();

        double width = oldScene != null ? oldScene.getWidth() : stage.getWidth();
        double height = oldScene != null ? oldScene.getHeight() : stage.getHeight();

        Scene scene = new Scene(mainRoot, width, height);
        scene.getStylesheets().add(DashboardScreen.class.getResource("/css/dashboard.css").toExternalForm());
        scene.getStylesheets().add(DashboardScreen.class.getResource("/css/client.css").toExternalForm());

        boolean wasMaximized = stage.isMaximized();

        stage.setScene(scene);
        stage.setMaximized(wasMaximized);
        stage.show();
    }

    private static void loadDashboardContent() {
        content.getChildren().clear();

        refreshCache();

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        HBox cards = new HBox(18);

        VBox c1 = createCard(
                "/icons/client.png",
                "Total Clients",
                String.valueOf(cachedClients.size()),
                "All Registered Clients"
        );

        VBox c2 = createCard(
                "/icons/trips.png",
                "Total Trips",
                String.valueOf(cachedTrips.size()),
                "All Trips Stored"
        );

        VBox c3 = createCard(
                "/icons/upcomingTrip.png",
                "Upcoming Trips",
                String.valueOf(getUpcomingTripCount()),
                "Next 7 Days"
        );

        VBox c4 = createCard(
                "/icons/thisMonthTrip.png",
                "This Month Trips",
                String.valueOf(getThisMonthTripCount()),
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );

        c1.setOnMouseClicked(e -> {
            setActive(clientsBtn);
            loadClientsScreen();
        });

        c2.setOnMouseClicked(e -> {
            setActive(tripsBtn);
            loadTripsScreen();
        });

        c3.setOnMouseClicked(e -> {
            setActive(tripsBtn);
            loadTripsScreen();
        });

        c4.setOnMouseClicked(e -> {
            setActive(calendarBtn);
            content.getChildren().setAll(CalendarScreen.getView());
        });

        cards.getChildren().addAll(c1, c2, c3, c4);

        for (VBox card : new VBox[]{c1, c2, c3, c4}) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        HBox mainArea = new HBox(18);
        mainArea.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox trips = createUpcomingTripsPanel();
        VBox actions = createQuickActions();

        HBox.setHgrow(trips, Priority.ALWAYS);
        trips.setMaxHeight(Double.MAX_VALUE);
        actions.setMaxHeight(Double.MAX_VALUE);

        mainArea.getChildren().addAll(trips, actions);
        content.getChildren().addAll(title, cards, mainArea);
    }

    private static void refreshCache() {
        cachedClients = ClientRepository.getAllClients();
        cachedTrips = TripRepository.getAllTrips();
    }

    private static VBox createUpcomingTripsPanel() {
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

        HBox header = new HBox(title, spacer, viewAll);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox rows = new VBox(10);

        Button prev = new Button("Prev",loadIcon("left.png",10));
        Button next = new Button("Next",loadIcon("right.png",10));
        next.setContentDisplay(ContentDisplay.RIGHT);
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

            page[0] = Math.max(0, Math.min(page[0], pageCount - 1));

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
        return tripRowBase(
                trip.getDate(),
                trip.getClientName(),
                trip.getDestination(),
                trip.getType(),
                trip.getStatus(),
                getClientPhone(trip.getClientId()),
                "View",
                false,
                trip
        );
    }

    private static HBox tripRowBase(String date, String client, String destination, String type,
                                    String status, String contact, String details, boolean header, Trip trip) {

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add(header ? "trip-header-row" : "trip-row");

        Label l1 = dashCell(date);
        Label l2 = dashCell(client);
        Label l3 = dashCell(destination);
        Label l4 = dashCell(type);
        Label l6 = dashCell(contact);

        HBox statusBox = header ? centerBox(dashCell(status)) : centerBox(statusPill(status));

        if (header) {
            Label l7 = dashCell(details);

            for (Label l : new Label[]{l1, l2, l3, l4, l6, l7}) {
                l.getStyleClass().add("trip-header-text");
            }

            row.getChildren().addAll(l1, l2, l3, l4, statusBox, l6, l7);
        } else {
            Button view = new Button("View");
            view.getStyleClass().add("view-details-btn");
            view.setOnAction(e -> {
                content.getChildren().clear();
                TripScreen.showTripDetailsScreen(content, trip);
            });

            HBox actionBox = centerBox(view);
            row.getChildren().addAll(l1, l2, l3, l4, statusBox, l6, actionBox);
        }

        for (javafx.scene.Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);

            if (node instanceof Region) {
                ((Region) node).setMaxWidth(Double.MAX_VALUE);
            }
        }

        return row;
    }

    private static Label statusPill(String status) {
        Label pill = new Label(status == null ? "-" : status);
        pill.getStyleClass().add("status-pill");

        if ("Confirmed".equalsIgnoreCase(status)) {
            pill.getStyleClass().add("status-confirmed");
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            pill.getStyleClass().add("status-cancelled");
        } else {
            pill.getStyleClass().add("status-pending");
        }

        return pill;
    }

    private static HBox centerBox(javafx.scene.Node node) {
        HBox box = new HBox(node);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(0);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Label dashCell(String text) {
        String value = text == null || text.isEmpty() ? "-" : text;

        Label label = new Label(value);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(false);
        label.setEllipsisString("...");

        Tooltip.install(label, new Tooltip(value));
        return label;
    }

    private static List<Trip> getUpcomingTripsList() {
        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);

        return cachedTrips.stream()
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

        return (int) cachedTrips.stream()
                .filter(t -> parseDate(t.getDate()) != null)
                .filter(t -> {
                    LocalDate d = parseDate(t.getDate());
                    return d.getMonth() == now.getMonth()
                            && d.getYear() == now.getYear();
                })
                .count();
    }

    private static LocalDate parseDate(String date) {
        try {
            return date == null || date.isEmpty() ? null : LocalDate.parse(date);
        } catch (Exception e) {
            AppLogger.logError(e, "Failed while Parsing the date = "+ date);
            return null;
        }
    }

    private static Client getClientById(int clientId) {
        return cachedClients.stream()
                .filter(c -> c.getId() == clientId)
                .findFirst()
                .orElseGet(() -> ClientRepository.getClientById(clientId));
    }

    private static String getClientPhone(int clientId) {
        Client client = getClientById(clientId);
        return client == null ? "-" : client.getPhone();
    }

    private static VBox createQuickActions() {
        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        Button addClient = createActionButton("Add New Client", "blue-btn");
        addClient.setGraphic(loadIcon("plus.png",12));
        addClient.setOnAction(e -> {
            setActive(clientsBtn);
            content.getChildren().setAll(ClientsScreen.getAddClientViewDirect());
        });

        Button addTrip = createActionButton("Add New Trip", "green-btn");
        addTrip.setGraphic(loadIcon("plus.png",12));
        addTrip.setOnAction(e -> loadAddTripScreen());

        Button upload = createActionButton("Upload Document", "purple-btn");
        upload.setGraphic(loadIcon("upload.png",12));
        upload.setOnAction(e -> {
            setActive(documentsBtn);
            content.getChildren().setAll(DocumentScreen.getView());
        });

        Button backup = createActionButton("Backup Now", "orange-btn");
        backup.setGraphic(loadIcon("backup.png",24));
        backup.setOnAction(e -> {
            setActive(settingsBtn);
            content.getChildren().setAll(SettingsScreen.getView());
        });

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
        search.setPromptText("Search clients, trips, destination...");
        search.setPrefWidth(320);
        search.getStyleClass().add("search");
        HBox.setMargin(search, new Insets(0, 0, 0, 25));

        search.setOnAction(e -> performTopSearch(search.getText()));
        search.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                setActive(dashboardBtn);
                loadDashboardContent();
                return;
            }

            if (newVal.trim().length() >= 2) {
                setActive(dashboardBtn);
                loadSearchResults(newVal.trim());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        date.getStyleClass().add("date-label");

        userLabel = new Label("Admin");
        userLabel.getStyleClass().add("user-label");

        topbar.getChildren().addAll(search, spacer, date, userLabel);
        return topbar;
    }

    private static void loadSearchResults(String keyword) {
        content.getChildren().clear();

        refreshCache();

        String value = keyword == null ? "" : keyword.toLowerCase().trim();

        Label title = new Label("Search Results");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Showing results for: " + keyword);
        subtitle.getStyleClass().add("section-title");

        VBox resultPanel = new VBox(14);
        resultPanel.getStyleClass().add("panel");

        List<Client> matchedClients = cachedClients.stream()
                .filter(c ->
                        safe(c.getName()).toLowerCase().contains(value)
                                || safe(c.getPhone()).toLowerCase().contains(value)
                                || safe(c.getEmail()).toLowerCase().contains(value)
                                || safe(c.getCity()).toLowerCase().contains(value)
                )
                .collect(Collectors.toList());

        List<Trip> matchedTrips = cachedTrips.stream()
                .filter(t ->
                        safe(t.getClientName()).toLowerCase().contains(value)
                                || safe(t.getDestination()).toLowerCase().contains(value)
                                || safe(t.getDate()).toLowerCase().contains(value)
                                || safe(t.getType()).toLowerCase().contains(value)
                                || safe(t.getStatus()).toLowerCase().contains(value)
                                || safe(t.getAirlineName()).toLowerCase().contains(value)
                )
                .collect(Collectors.toList());

        if (matchedClients.isEmpty() && matchedTrips.isEmpty()) {
            Label empty = new Label("No matching client or trip found.");
            empty.getStyleClass().add("card-subtitle");
            resultPanel.getChildren().add(empty);
        }

        if (!matchedClients.isEmpty()) {
            Label clientTitle = new Label("Clients");
            clientTitle.getStyleClass().add("section-title");
            resultPanel.getChildren().add(clientTitle);

            for (Client c : matchedClients) {
                HBox row = new HBox(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("card");

                Label info = new Label(
                        c.getId() + " - " +
                                safe(c.getName()) + " | " +
                                safe(c.getPhone()) + " | " +
                                safe(c.getEmail()) + " | " +
                                safe(c.getCity())
                );
                info.getStyleClass().add("card-title");

                Button open = new Button("Open Clients");
                open.getStyleClass().add("blue-btn");
                open.setOnAction(e -> {
                    setActive(clientsBtn);
                    loadClientsScreen();
                });

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(info, spacer, open);
                resultPanel.getChildren().add(row);
            }
        }

        if (!matchedTrips.isEmpty()) {
            Label tripTitle = new Label("Trips");
            tripTitle.getStyleClass().add("section-title");
            resultPanel.getChildren().add(tripTitle);

            resultPanel.getChildren().add(tripHeaderRow());

            for (Trip t : matchedTrips) {
                resultPanel.getChildren().add(tripRow(t));
            }
        }

        content.getChildren().addAll(title, subtitle, resultPanel);
    }
    private static void performTopSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            setActive(dashboardBtn);
            loadDashboardContent();
            return;
        }

        setActive(dashboardBtn);
        loadSearchResults(keyword.trim());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void loadClientsScreen() {
        content.getChildren().setAll(ClientsScreen.getView());
    }

    private static void loadTripsScreen() {
        content.getChildren().setAll(TripScreen.getView());
    }

    private static void loadAddTripScreen() {
        setActive(tripsBtn);
        content.getChildren().setAll(TripScreen.getAddTripViewDirect());
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

        HBox cardContent = new HBox(18, iconBox, new VBox(5, t, v, s));
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

    public static void setDarkMode(boolean enabled) {
        if (mainRoot == null) return;

        if (enabled) {
            if (!mainRoot.getStyleClass().contains("dark-mode")) {
                mainRoot.getStyleClass().add("dark-mode");
            }
        } else {
            mainRoot.getStyleClass().remove("dark-mode");
        }
    }

    public static void updateUserName(String name) {
        if (userLabel != null && name != null && !name.isEmpty()) {
            userLabel.setText(name);
        }
    }
    public static ImageView loadIcon(String name, double size) {
        ImageView icon = new ImageView(
                new Image(DashboardScreen.class.getResourceAsStream("/icons/" + name))
        );
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        return icon;
    }
}