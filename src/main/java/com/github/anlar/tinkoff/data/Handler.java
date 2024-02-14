package com.github.anlar.tinkoff.data;

public interface Handler {
    ApplicationStatusResponse performOperation(String id);
}
