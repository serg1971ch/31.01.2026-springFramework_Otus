package ru.otus.hw_4.domain;

public record Student(String firstName, String lastName) {
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}
