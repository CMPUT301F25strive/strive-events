package com.example.eventlottery.model;

import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.google.zxing.integration.android.IntentIntegrator;

public class QRScanner {
    private EventRepository eventRepo;
    public static void startScanner(Fragment fragment) {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(fragment);
        integrator.setPrompt("Scan a QR Code");
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.initiateScan();
    }

    public Event extractEvent(String scanned) {
        if (scanned == null) return null;
        // Only getting valid QR codes
        eventRepo = RepositoryProvider.getEventRepository();
        if (scanned.startsWith("event/")) {
            return eventRepo.findEventById(scanned.substring("event/".length()).trim());
        }
        return null;
    }
}
