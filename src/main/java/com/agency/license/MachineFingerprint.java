package com.agency.license;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;

public class MachineFingerprint {

    public static String generate() {
        try {
            StringBuilder raw = new StringBuilder();

            raw.append(System.getProperty("os.name")).append("|");
            raw.append(System.getProperty("os.arch")).append("|");
            raw.append(System.getProperty("user.name")).append("|");

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        raw.append(String.format("%02X", b));
                    }
                    break;
                }
            }

            return sha256(raw.toString());

        } catch (Exception e) {
            throw new RuntimeException("Fingerprint error", e);
        }
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}