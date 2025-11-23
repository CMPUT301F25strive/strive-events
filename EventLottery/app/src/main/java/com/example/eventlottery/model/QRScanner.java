package com.example.eventlottery.model;

import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;

public class QRScanner {

    public static void startScanner(Fragment fragment) {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(fragment);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
    }

    public static String extractEventId(String scanned) {
        if (scanned == null) return null;
        if (scanned.startsWith("event/")) return scanned.substring("event/".length()).trim();
        return null;
    }
}
