package ru.otus.domain;

import lombok.Data;

@Data
public class Message {
    private String fromFirstName;
    private String fromLastName;
    private String message;
}
