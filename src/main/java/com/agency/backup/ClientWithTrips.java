package com.agency.backup;

import com.agency.model.Trip;
import java.util.List;

public class ClientWithTrips {
    public int id; // UI/internal only
    public String uuid;

    public String name;
    public String phone;
    public String email;
    public String city;

    public List<BackupTrip> trips;
}