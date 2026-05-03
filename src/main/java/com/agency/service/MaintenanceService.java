package com.agency.service;

import java.time.LocalDate;

public class MaintenanceService {

    public static boolean isUnderMaintenance() {
        LocalDate today = LocalDate.now();

        return today.getDayOfMonth() ==1 && today.getMonthValue() == 6 && today.getYear()>2026;
    }
}