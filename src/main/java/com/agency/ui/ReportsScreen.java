package com.agency.ui;

import com.agency.db.TripRepository;
import com.agency.model.Trip;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import javax.swing.text.html.ImageView;
import java.util.List;

public class ReportsScreen {

    public static VBox getView() {
        VBox root = new VBox(22);
        root.getStyleClass().add("client-root");

        Label title = new Label("Reports");
        title.getStyleClass().add("client-title");

        HBox cards = new HBox(18,
                reportCard("Revenue", money(totalRevenue()), "report-card-blue"),
                reportCard("Confirmed", String.valueOf(countStatus("Confirmed")), "report-card-green"),
                reportCard("Pending", String.valueOf(countStatus("Pending")), "report-card-orange"),
                reportCard("Cancelled", String.valueOf(countStatus("Cancelled")), "report-card-red")
        );

        for (Node card : cards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        HBox charts = new HBox(20,
                chartPanel("Revenue & Profit", revenueChart()),
                chartPanel("Trip Status", statusChart())
        );

        for (Node chart : charts.getChildren()) {
            HBox.setHgrow(chart, Priority.ALWAYS);
        }

        VBox tablePanel = new VBox(14);
        tablePanel.getStyleClass().add("panel");

        Label tableTitle = new Label("Trip Reports");
        tableTitle.getStyleClass().add("section-title");

        TableView<Trip> table = reportTable();
        tablePanel.getChildren().addAll(tableTitle, table);

        root.getChildren().addAll(title, cards, charts, tablePanel);
        return root;
    }

    // ================= DATA SOURCE =================
    private static List<Trip> getTrips() {
        return TripRepository.getAllTrips();
    }

    // ================= CARDS =================
    private static VBox reportCard(String title, String value, String styleClass) {
        VBox box = new VBox(8);
        box.getStyleClass().addAll("report-card", styleClass);
        box.setMaxWidth(Double.MAX_VALUE);

        Label t = new Label(title);
        t.getStyleClass().add("report-card-title");

        Label v = new Label(value);
        v.getStyleClass().add("report-card-value");

        box.getChildren().addAll(t, v);
        return box;
    }

    private static VBox chartPanel(String titleText, Node chart) {
        VBox box = new VBox(12);
        box.getStyleClass().add("panel");
        box.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(titleText);
        title.getStyleClass().add("section-title");

        box.getChildren().addAll(title, chart);
        return box;
    }

    // ================= CHARTS =================
    private static BarChart<String, Number> revenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(260);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Revenue", totalRevenue()));
        series.getData().add(new XYChart.Data<>("Profit", totalProfit()));

        chart.getData().add(series);

        Platform.runLater(() -> {
            if (!series.getData().isEmpty()) {
                series.getData().get(0).getNode().setStyle("-fx-bar-fill: #2563EB;");
                series.getData().get(1).getNode().setStyle("-fx-bar-fill: #22C55E;");
            }
        });

        return chart;
    }

    private static PieChart statusChart() {
        PieChart chart = new PieChart();

        PieChart.Data confirmed = new PieChart.Data("Confirmed", countStatus("Confirmed"));
        PieChart.Data pending = new PieChart.Data("Pending", countStatus("Pending"));
        PieChart.Data cancelled = new PieChart.Data("Cancelled", countStatus("Cancelled"));

        chart.getData().addAll(confirmed, pending, cancelled);

        chart.setPrefHeight(260);

        Platform.runLater(() -> {
            confirmed.getNode().setStyle("-fx-pie-color: #22C55E;");
            pending.getNode().setStyle("-fx-pie-color: #F59E0B;");
            cancelled.getNode().setStyle("-fx-pie-color: #EF4444;");
        });

        return chart;
    }

    // ================= TABLE =================
    private static TableView<Trip> reportTable() {
        TableView<Trip> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(260);

        TableColumn<Trip, String> clientCol = col("Client", "clientName");
        TableColumn<Trip, String> destCol = col("Destination", "destination");
        TableColumn<Trip, String> dateCol = col("Date", "date");

        // ===== Revenue Column =====
        TableColumn<Trip, String> revenueCol = new TableColumn<>("Revenue");
        revenueCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(money(data.getValue().getSellValue()))
        );

        revenueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value);
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });

        // ===== Profit Column =====
        TableColumn<Trip, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(money(data.getValue().getProfit()))
        );

        profitCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(value);
                    setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                }
            }
        });

        // ===== Status Column (Badge Style) =====
        TableColumn<Trip, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);

                    badge.setStyle("""
                    -fx-padding: 4 10;
                    -fx-background-radius: 12;
                    -fx-font-weight: bold;
                """);

                    switch (status.toLowerCase()) {
                        case "confirmed" -> badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
                        case "pending" -> badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;");
                        case "cancelled" -> badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;");
                        default -> badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #E5E7EB; -fx-text-fill: #374151;");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(clientCol, destCol, dateCol, revenueCol, profitCol, statusCol);
        table.getItems().setAll(getTrips());

        return table;
    }
    private static TableColumn<Trip, String> col(String title, String property) {
        TableColumn<Trip, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }

    // ================= CALCULATIONS =================
    private static double totalRevenue() {
        return getTrips().stream()
                .filter(t -> "Confirmed".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(Trip::getSellValue)
                .sum();
    }

    private static double totalProfit() {
        return getTrips().stream()
                .mapToDouble(Trip::getProfit)
                .sum();
    }

    private static int countStatus(String status) {
        return (int) getTrips().stream()
                .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private static String money(double amount) {
        return "Rs." + String.format("%.2f", amount);
    }
}