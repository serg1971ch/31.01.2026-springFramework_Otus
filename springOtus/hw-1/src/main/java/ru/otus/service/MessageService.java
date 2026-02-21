package ru.otus.service;

import ru.otus.domain.Message;

public interface MessageService {
    String getNameForMessage();

    Message getMessage(int count);
}
