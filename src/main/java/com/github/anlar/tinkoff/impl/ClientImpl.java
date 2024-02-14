package com.github.anlar.tinkoff.impl;

import com.github.anlar.tinkoff.data.Client;
import com.github.anlar.tinkoff.data.Response;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dummy client implementation for testing purposes.
 */
public class ClientImpl implements Client {

    private static final Logger LOG = Logger.getLogger(ClientImpl.class.getName());

    @Override
    public Response getApplicationStatus1(String id) {
        sleep(1);
        return getRandomResponse(1, id);
    }

    @Override
    public Response getApplicationStatus2(String id) {
        sleep(2);
        return getRandomResponse(2, id);
    }

    private void sleep(int clientCode) {
        try {
            int sleepTimeSec = new Random().nextInt(10);
            LOG.log(Level.INFO, String.format("Client %s sleep for %s seconds...",
                    clientCode, sleepTimeSec));
            Thread.sleep(1000 * sleepTimeSec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Response getRandomResponse(int clientCode, String id) {
        int result = new Random().nextInt(0, 3);

        return switch (result) {
            case 0 -> new Response.Success(String.format("Success from %s", clientCode), id);
            case 1 -> new Response.RetryAfter(null);
            default -> new Response.Failure(null);
        };
    }
}
