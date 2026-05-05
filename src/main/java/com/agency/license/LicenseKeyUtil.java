package com.agency.license;

import java.io.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Properties;

public class LicenseKeyUtil {

    public static String generateLicenseKey(String clientName, String machineId, LocalDate expiryDate) {
        try {
            String raw = clientName + "|" + machineId + "|" + expiryDate;
            String signature = LicenseCrypto.sign(raw);

            Properties props = new Properties();
            props.setProperty("clientName", clientName);
            props.setProperty("machineId", machineId);
            props.setProperty("expiryDate", expiryDate.toString());
            props.setProperty("signature", signature);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            props.store(out, null);

            return Base64.getEncoder().encodeToString(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed");
        }
    }

    public static boolean validateLicenseKey(String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(key);

            Properties props = new Properties();
            props.load(new ByteArrayInputStream(decoded));

            String client = props.getProperty("clientName");
            String machine = props.getProperty("machineId");
            String expiry = props.getProperty("expiryDate");
            String signature = props.getProperty("signature");

            if (client == null || machine == null || expiry == null || signature == null) {
                return false;
            }

            String currentMachine = MachineFingerprint.generate();

            if (!currentMachine.equals(machine)) {
                return false;
            }

            if (LocalDate.now().isAfter(LocalDate.parse(expiry))) {
                return false;
            }

            String raw = client + "|" + machine + "|" + expiry;
            String expected = LicenseCrypto.sign(raw);

            return expected.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }
}