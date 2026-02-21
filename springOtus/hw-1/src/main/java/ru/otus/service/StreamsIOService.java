package ru.otus.service;

import java.io.PrintStream;
import java.util.Scanner;

public class StreamsIOService implements IOService {
    private final PrintStream printStream;
    private final Scanner scanner;

    public StreamsIOService(PrintStream printStream, Scanner scanner) {
        this.printStream = printStream;
        this.scanner = scanner;
    }

    @Override
    public void printLine(String s) {
        printStream.println(s);
    }

    @Override
    public void printFormattedLine(String s, Object... args) {
        printStream.printf(s + "%n", args);
    }

    @Override
    public String readLine() {
        return scanner.nextLine();
    }
}
