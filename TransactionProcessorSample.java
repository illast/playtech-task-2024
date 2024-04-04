package com.playtech.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {
        List<User> users = readObjects(args[0], User::new);
        List<Transaction> transactions = readObjects(args[1], Transaction::new);
        List<BinMapping> binMappings = readObjects(args[2], BinMapping::new);

        List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings);

//        TransactionProcessorSample.writeBalances(Paths.get(args[3]), users);
//        TransactionProcessorSample.writeEvents(Paths.get(args[4]), events);
    }

    private static <T> List<T> readObjects(final String filePath, Function<String[], T> constructor) throws IOException {
        List<T> objects = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));
        for (int i = 1; i < lines.size(); i++) {
            String[] line = lines.get(i).split(",");
            T object = constructor.apply(line);
            objects.add(object);
        }
        return objects;
    }

    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        List<Event> events = new ArrayList<>();

        for (Transaction transaction : transactions) {
            boolean isValid = validateTransaction(transaction, users, binMappings, events);
            if (isValid) {
                events.add(new Event(transaction.transactionId, Event.STATUS_APPROVED, "OK"));
            }
        }

        for (Event event : events) {
            System.out.println(event);
        }

        return events;
    }

    private static boolean validateTransaction(Transaction transaction, List<User> users, List<BinMapping> binMappings, List<Event> events) {
        return isTransactionIdUnique(transaction, events) && isUserValid(transaction, users, events) && isPaymentMethodValid(transaction, binMappings, events);
    }

    private static boolean isTransactionIdUnique(Transaction transaction, List<Event> events) {
        for (Event event : events) {
            if (event.transactionId.equals(transaction.transactionId)) {
                events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Transaction " + transaction.transactionId + " already processed (id non-unique)"));
                return false;
            }
        }
        return true;
    }

    private static User findUser(String userId, List<User> users) {
        for (User user : users) {
            if (user.userId.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private static boolean isUserValid(Transaction transaction, List<User> users, List<Event> events) {
        User user = findUser(transaction.userId, users);
        if (user == null || user.frozen.equals("1")) {
            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "User " + transaction.userId + " not found in Users"));
            return false;
        }
        return true;
    }

    private static boolean isIBANValid(String IBAN) {
        if (IBAN.length() != 22) {
            return false;
        }

        IBAN = IBAN.substring(4) + IBAN.substring(0, 4);

        StringBuilder expandedIBAN = new StringBuilder();
        for (int i = 0; i < IBAN.length(); i++) {
            char c = IBAN.charAt(i);
            if (Character.isLetter(c)) {
                int value = c - 'A' + 10;
                expandedIBAN.append(value);
            }
            else {
                expandedIBAN.append(c);
            }
        }

        BigInteger bigInteger = new BigInteger(expandedIBAN.toString());
        BigInteger modulus = new BigInteger("97");
        return bigInteger.mod(modulus).intValue() == 1;
    }

    private static boolean isDebitCard(String accountNumber, List<BinMapping> binMappings) {
        for (BinMapping binMapping : binMappings) {
            if (binMapping.type.equals("DC") && (isAccountNumberInRange(accountNumber, binMapping))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAccountNumberInRange(String accountNumber, BinMapping binMapping) {
        long accountNumberStart = Long.parseLong(accountNumber.substring(0, binMapping.rangeFrom.length()));
        long rangeFrom = Long.parseLong(binMapping.rangeFrom);
        long rangeTo = Long.parseLong(binMapping.rangeTo);
        return accountNumberStart >= rangeFrom && accountNumberStart <= rangeTo;
    }

    private static boolean isPaymentMethodValid(Transaction transaction, List<BinMapping> binMappings, List<Event> events) {
        if ("TRANSFER".equals(transaction.method)) {
            if (!isIBANValid(transaction.accountNumber)) {
                events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Invalid iban " + transaction.accountNumber));
                return false;
            }
        }
        else if ("CARD".equals(transaction.method)) {
            if(!isDebitCard(transaction.accountNumber, binMappings)) {
                events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Only DC cards allowed; got CC"));
                return false;
            }
        } else {
            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Invalid payment method " + transaction.method));
            return false;
        }
        return true;
    }


//    private static void writeBalances(final Path filePath, final List<User> users) {
//    }
//
//    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
//        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
//            writer.append("transaction_id,status,message\n");
//            for (final var event : events) {
//                writer.append(event.transactionId).append(",").append(event.status).append(",").append(event.message).append("\n");
//            }
//        }
//    }
}

class User {
    public String userId;
    public String username;
    public String balance;
    public String country;
    public String frozen;
    public String depositMin;
    public String deposit_max;
    public String withdrawMin;
    public String withdrawMax;

    public User(String[] fields) {
        this.userId = fields[0];
        this.username = fields[1];
        this.balance = fields[2];
        this.country = fields[3];
        this.frozen = fields[4];
        this.depositMin = fields[5];
        this.deposit_max = fields[6];
        this.withdrawMin = fields[7];
        this.withdrawMax = fields[8];
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
    public String transactionId;
    public String userId;
    public String type;
    public String amount;
    public String method;
    public String accountNumber;

    public Transaction(String[] fields) {
        this.transactionId = fields[0];
        this.userId = fields[1];
        this.type = fields[2];
        this.amount = fields[3];
        this.method = fields[4];
        this.accountNumber = fields[5];
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
    public String name;
    public String rangeFrom;
    public String rangeTo;
    public String type;
    public String country;

    public BinMapping(String[] fields) {
        this.name = fields[0];
        this.rangeFrom = fields[1];
        this.rangeTo = fields[2];
        this.type = fields[3];
        this.country = fields[4];
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

    public Event(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return transactionId + ',' + status + ',' + message;
    }
}
