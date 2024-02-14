package com.github.anlar.tinkoff.impl;

import com.github.anlar.tinkoff.data.ApplicationStatusResponse;
import com.github.anlar.tinkoff.data.Client;
import com.github.anlar.tinkoff.data.Handler;
import com.github.anlar.tinkoff.data.Response;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandlerImpl implements Handler {
    private static final Logger LOG = Logger.getLogger(HandlerImpl.class.getName());

    private static final int sleepTimeoutSec = 15;

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        Client client = new ClientImpl();

        ExecutorService executor = Executors.newCachedThreadPool();

        AtomicInteger callCounter = new AtomicInteger();

        List<Callable<Response>> taskList = new LinkedList<>();
        taskList.add(new Task(id, client, 1, callCounter));
        taskList.add(new Task(id, client, 2, callCounter));

        Response result = null;

        try {
            result = executor.invokeAny(taskList, sleepTimeoutSec, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOG.log(Level.WARNING, "Failed to process request due to timeout");
        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.SEVERE, "Failed to process request", e);
        } finally {
            executor.shutdown();
        }

        if (result instanceof Response.Success) {
            return new ApplicationStatusResponse.Success(
                    id, ((Response.Success) result).applicationStatus());
        } else {
            return new ApplicationStatusResponse.Failure(null, callCounter.get());
        }
    }

    private record Task(String id, Client client, int clientCode,
                        AtomicInteger callCounter) implements Callable<Response> {

        @Override
        public Response call() throws Exception {
            while (true) {
                callCounter.incrementAndGet();
                Response response = get();
                LOG.log(Level.INFO, String.format("Get response from client %s: %s", clientCode, response));

                if (response instanceof Response.Success) {
                    return response;
                } else if (response instanceof Response.Failure) {
                    // default sleep to retry after failure
                    Thread.sleep(1000 * 5);
                } else if (response instanceof Response.RetryAfter) {
                    if (((Response.RetryAfter) response).delay() != null) {
                        if (((Response.RetryAfter) response).delay().toSeconds() >= sleepTimeoutSec) {
                            // safety check, if service return very long retry time
                            LOG.log(Level.INFO, String.format("Client %s requested long retry timeout (%s seconds), don not call they again",
                                    clientCode, ((Response.RetryAfter) response).delay().toSeconds()));
                            return null;
                        } else {
                            Thread.sleep(((Response.RetryAfter) response).delay().toMillis());
                        }
                    }
                }
            }
        }

        private Response get() {
            if (clientCode == 1) {
                return client.getApplicationStatus1(id);
            } else {
                return client.getApplicationStatus2(id);
            }
        }
    }
}
