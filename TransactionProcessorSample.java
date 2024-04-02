package com.playtech.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {
        List<User> users = TransactionProcessorSample.readUsers(Paths.get(args[0]));
        System.out.println(users);
        List<Transaction> transactions = TransactionProcessorSample.readTransactions(Paths.get(args[1]));
        System.out.println(transactions);
        List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(Paths.get(args[2]));
        System.out.println(binMappings);

//        List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings);
//
//        TransactionProcessorSample.writeBalances(Paths.get(args[3]), users);
//        TransactionProcessorSample.writeEvents(Paths.get(args[4]), events);
    }

    private static List<User> readUsers(final Path filePath) {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i).split(",");
                User user = new User(line[0], line[1], line[2], line[3], line[4], line[5], line[6], line[7], line[8]);
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    private static List<Transaction> readTransactions(final Path filePath) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i).split(",");
                Transaction transaction = new Transaction(line[0], line[1], line[2], line[3], line[4], line[5]);
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private static List<BinMapping> readBinMappings(final Path filePath) {
        List<BinMapping> binMappings = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i).split(",");
                BinMapping binMapping = new BinMapping(line[0], line[1], line[2], line[3], line[4]);
                binMappings.add(binMapping);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return binMappings;
    }

    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        return null;
    }

    private static void writeBalances(final Path filePath, final List<User> users) {
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("transaction_id,status,message\n");
            for (final var event : events) {
                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
            }
        }
    }
}


class User {

    private final String userId;
    private final String username;
    private final String balance;
    private final String country;
    private final String frozen;
    private final String depositMin;
    private final String deposit_max;
    private final String withdrawMin;
    private final String withdrawMax;

    public User(String userId, String username, String balance, String country, String frozen, String depositMin, String deposit_max, String withdrawMin, String withdrawMax) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.country = country;
        this.frozen = frozen;
        this.depositMin = depositMin;
        this.deposit_max = deposit_max;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", balance='" + balance + '\'' +
                ", country='" + country + '\'' +
                ", frozen='" + frozen + '\'' +
                ", depositMin='" + depositMin + '\'' +
                ", deposit_max='" + deposit_max + '\'' +
                ", withdrawMin='" + withdrawMin + '\'' +
                ", withdrawMax='" + withdrawMax + '\'' +
                '}';
    }
}

class Transaction {

    private final String transactionId;
    private final String userId;
    private final String type;
    private final String amount;
    private final String method;
    private final String accountNumber;

    public Transaction(String transactionId, String userId, String type, String amount, String method, String accountNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", amount='" + amount + '\'' +
                ", method='" + method + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}

class BinMapping {

    private final String name;
    private final String rangeFrom;
    private final String rangeTo;
    private final String type;
    private final String country;

    BinMapping(String name, String rangeFrom, String rangeTo, String type, String country) {
        this.name = name;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
        this.type = type;
        this.country = country;
    }

    @Override
    public String toString() {
        return "BinMapping{" +
                "name='" + name + '\'' +
                ", rangeFrom='" + rangeFrom + '\'' +
                ", rangeTo='" + rangeTo + '\'' +
                ", type='" + type + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}

class Event {
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_APPROVED = "APPROVED";

    public String transactionId;
    public String status;
    public String message;
}
