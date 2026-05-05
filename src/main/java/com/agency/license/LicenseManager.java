package com.agency.license;

import java.time.LocalDate;
import java.util.Properties;

public class LicenseManager {

    public static boolean isApplicationAllowed() {
        try {
            Properties data = LicenseStore.read();

            if (data == null) return false;

            String key = data.getProperty("licenseKey");
            String machine = data.getProperty("machine");
            String lastRun = data.getProperty("lastRun");
            String signature = data.getProperty("signature");

            if (key == null || machine == null || lastRun == null || signature == null) {
                return false;
            }

            String currentMachine = MachineFingerprint.generate();

            if (!currentMachine.equals(machine)) return false;

            String expected = LicenseCrypto.sign(key + "|" + machine + "|" + lastRun);

            if (!expected.equals(signature)) return false;

            if (LocalDate.now().isBefore(LocalDate.parse(lastRun))) return false;

            if (!LicenseKeyUtil.validateLicenseKey(key)) return false;

            LicenseStore.save(key);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean activateLicense(String key) {
        if (!LicenseKeyUtil.validateLicenseKey(key)) return false;

        try {
            LicenseStore.save(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDeleted() {
        return LicenseStore.read() == null && LicenseStore.markerExists();
    }

    public static String getMachineId() {
        return MachineFingerprint.generate();
    }
}