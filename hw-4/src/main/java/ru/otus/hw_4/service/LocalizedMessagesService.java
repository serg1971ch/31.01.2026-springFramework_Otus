package ru.otus.hw_4.service;

public interface LocalizedMessagesService {
    String getMessage(String code, Object ...args);
}
