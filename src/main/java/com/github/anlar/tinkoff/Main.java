package com.github.anlar.tinkoff;

import com.github.anlar.tinkoff.data.ApplicationStatusResponse;
import com.github.anlar.tinkoff.data.Handler;
import com.github.anlar.tinkoff.impl.HandlerImpl;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String id = String.valueOf(new Random().nextInt(1000, 9999));

        LOG.log(Level.INFO, String.format("Send request with ID: %s", id));

        Handler handler = new HandlerImpl();
        ApplicationStatusResponse response = handler.performOperation(id);

        LOG.log(Level.INFO, String.format("Get request: %s", response));
    }
}