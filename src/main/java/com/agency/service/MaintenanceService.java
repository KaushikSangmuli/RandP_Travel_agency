package com.agency.service;

import java.time.LocalDate;

public class MaintenanceService {

    public static boolean isUnderMaintenance() {
        LocalDate today = LocalDate.now();

        return today.getDayOfMonth() == 26 && today.getMonthValue() == 4 && today.getYear()>2026;
    }
}