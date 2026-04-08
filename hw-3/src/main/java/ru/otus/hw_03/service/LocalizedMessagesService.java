package ru.otus.hw_03.service;

public interface LocalizedMessagesService {
    String getMessage(String code, Object ...args);
}
