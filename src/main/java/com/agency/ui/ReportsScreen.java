package com.agency.ui;

import com.agency.data.TripData;
import com.agency.model.Trip;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

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

    private static BarChart<String, Number> revenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(260);
        chart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Revenue", totalRevenue()));
        series.getData().add(new XYChart.Data<>("Profit", totalProfit()));

        chart.getData().add(series);

        Platform.runLater(() -> {
            if (series.getData().size() > 0 && series.getData().get(0).getNode() != null) {
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
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefHeight(260);
        chart.setAnimated(true);

        Platform.runLater(() -> {
            confirmed.getNode().setStyle("-fx-pie-color: #22C55E;");
            pending.getNode().setStyle("-fx-pie-color: #F59E0B;");
            cancelled.getNode().setStyle("-fx-pie-color: #EF4444;");
        });

        return chart;
    }

    private static TableView<Trip> reportTable() {
        TableView<Trip> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(260);

        TableColumn<Trip, String> clientCol = col("Client", "clientName");
        TableColumn<Trip, String> destCol = col("Destination", "destination");
        TableColumn<Trip, String> dateCol = col("Date", "date");

        TableColumn<Trip, String> revenueCol = new TableColumn<>("Revenue");
        revenueCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(money(data.getValue().getSellValue())));

        TableColumn<Trip, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(money(data.getValue().getProfit())));

        TableColumn<Trip, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                HBox box = new HBox(statusPill(status));
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(clientCol, destCol, dateCol, revenueCol, profitCol, statusCol);
        table.getItems().setAll(TripData.trips);

        return table;
    }

    private static TableColumn<Trip, String> col(String title, String property) {
        TableColumn<Trip, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }

    private static Label statusPill(String status) {
        Label pill = new Label(status);
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

    private static double totalRevenue() {
        return TripData.trips.stream()
                .filter(t -> "Confirmed".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(Trip::getSellValue)
                .sum();
    }

    private static double totalProfit() {
        return TripData.trips.stream()
                .filter(t -> "Confirmed".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(Trip::getProfit)
                .sum();
    }

    private static int countStatus(String status) {
        return (int) TripData.trips.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private static String money(double amount) {
        return "₹" + String.format("%.2f", amount);
    }
}