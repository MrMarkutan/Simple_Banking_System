package banking;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Main {
        final static Scanner scanner = new Scanner(System.in);
        static Map<String, Card> cardMap;
        static Random generator;
        static boolean loggedIn, exit;
        static Card userCard;


    private static String dbFileName = "db";

    private static String inputCardNumber = null;
    private static String inputPin = null;

    public static String getDbFileName() {
        return dbFileName;
    }

//    public static String getUsername() {
//        return username;
//    }

//    public static String getPassword() {
//        return password;
//    }


    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-fileName")) dbFileName = args[i+1];
        }
        new Main().menu();
    }

    private void menu() {
        cardMap = new HashMap<>();
        generator = new Random();

        dbConnection.createConnection();
        dbConnection.createTable();
        loadFromDB();

        do{
            if (loggedIn) {
                showUserMenu();
                switch (scanner.nextInt()){
                    case 1: checkBalance(); break;
                    case 2: addIncome(); break;
                    case 3: doTransfer(); break;
                    case 4: closeAcc(); break;
                    case 5: logOut(); System.out.println("\nYou have successfully logged out!\n"); break;
                    case 0: exit(); break;
                    default: System.out.println("\nWrong input\n");
                }
            } else {
                showMenu();
                switch (scanner.nextInt()) {
                    case 1: createAccount(); break;
                    case 2: logIn(); break;
                    case 0: exit(); break;
                    default: System.out.println("\nWrong input\n");
                }
            }
        }while (!exit);
    }

    private void loadFromDB() {
        ResultSet dataFromDB = dbConnection.loadFromDB();
        try{
            while (dataFromDB.next()){
                String cardNumber = dataFromDB.getString("number");
                String pin = dataFromDB.getString("pin");
                double balance = dataFromDB.getDouble("balance");

                cardMap.put(cardNumber, new Card(pin, balance));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void addIncome() {
        System.out.println("\nEnter income:");
        int income = scanner.nextInt();
        userCard.setBalance(income);
        int upd = dbConnection.addIncome(inputCardNumber, income);
        if (upd > 0){
            System.out.println("Income was added!\n");
        } else {
            System.err.println("Db error!");
        }
    }

    private void doTransfer() {
        System.out.println("\nEnter card number:");
        scanner.nextLine();
        String cardNum = scanner.nextLine();
        System.out.println("Card Num " + cardNum);
        if(checkCardNumber(cardNum)){
            if(cardMap.containsKey(cardNum)){
                if(!cardNum.equals(inputCardNumber)){
                    System.out.println("Enter how much money you want to transfer:");
                    int transferMoney = scanner.nextInt();
                    if(userCard.getBalance() > transferMoney){
                        Card receiver = cardMap.get(cardNum);
                        receiver.setBalance(transferMoney);
                        userCard.setBalance(-transferMoney);
                        dbConnection.transferMoney(inputCardNumber,cardNum, transferMoney);
                        System.out.println("Success!\n");

                    } else {
                        System.out.println("Not enough money!\n");
                    }
                } else {
                    System.out.println("You can't transfer money to the same account!\n");
                }
            } else {
                System.out.println("Such a card does not exist.\n");
            }
        } else {
            System.out.println("Probably you made mistake in the card number. Please try again!\n");
        }
    }


    private void closeAcc() {
        dbConnection.deleteFromDB(inputCardNumber, inputPin);
        cardMap.remove(inputCardNumber);
        logOut();
        System.out.println("\nThe account has been closed!\n");
    }

    private void showMenu() {
        System.out.println("1. Create an account\n" +
                "2. Log into account\n" +
                "0. Exit");
    }

    private void showUserMenu() {
        System.out.println("1. Balance\n" +
                "2. Add income\n" +
                "3. Do transfer\n" +
                "4. Close account\n" +
                "5. Log out\n" +
                "0. Exit");
    }

    private void logOut() {
        loggedIn = false;
        inputCardNumber = null;
        inputPin = null;
        userCard = null;
    }

    private void exit() {
        exit = true;
        dbConnection.close();
        System.out.println("\nBye!");
    }


    private void createAccount() {

        String newNumber = generateCardNumber();
        if(cardMap.containsKey(newNumber)){
            newNumber = generateCardNumber();
        }
        cardMap.put(newNumber, new Card());

        System.out.println("\nYour card has been created\n" +
                "Your card number:\n" +
                newNumber +
                "\nYour card PIN:\n" +
                cardMap.get(newNumber).getPin() + "\n");
        dbConnection.addToDB(newNumber, cardMap.get(newNumber).getPin());

    }

    private String generateCardNumber(){
        StringBuilder cardNumber = new StringBuilder();
        String iin = "400000";
        cardNumber.append(iin);
        cardNumber.append(generate(9));
        cardNumber.append(generateChecksum(cardNumber.toString()));
        return cardNumber.toString();
    }
    protected static String generate(int digits){
        StringBuilder sb = new StringBuilder();
        while (digits > 0){
            sb.append(generator.nextInt(10));
            digits--;
        }
        return sb.toString();
    }

    private int generateChecksum(String cardNum) {
        return luhnAlg(cardNum);
    }

    private void logIn() {
        System.out.println("\nEnter your card number:");
        inputCardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        inputPin = scanner.next();
        if(checkCardNumber(inputCardNumber)){
            if (cardMap.containsKey(inputCardNumber)) {
                userCard = cardMap.get(inputCardNumber);
                if (inputPin.equals(userCard.getPin())) {
                    loggedIn = true;
                    System.out.println("\nYou have successfully logged in!\n");
                } else {
                    System.out.println("\nWrong card number or PIN!\n");
                }
            } else {
                System.out.println("\nWrong card number or PIN!\n");
            }
        } else {
            System.out.println("\nWrong card number or PIN!\n");
        }
    }

    private boolean checkCardNumber(String cardNumber){
        int checkSum = Character.getNumericValue(cardNumber.charAt(cardNumber.length()-1));
        int countLuhn = luhnAlg(cardNumber.substring(0, cardNumber.length()-1));
        return countLuhn == checkSum;

    }
    private void checkBalance() {

//        System.out.printf("\nBalance: %d \n", dbConnection.selectFromDB(inputCardNumber, inputPin));
        System.out.printf("\nBalance: %d \n", (int) userCard.getBalance());
        System.out.println();
    }

    private int luhnAlg(String id){
        int[]luhnArr = new int[id.length()];
        char[] chars = id.toCharArray();
        for (int i = 0; i < luhnArr.length; i++) {
            luhnArr[i] = Character.getNumericValue(chars[i]);
        }
        for (int i = 0; i < luhnArr.length; i++) {
            if(i % 2 == 0) luhnArr[i] *= 2;
        }
        for (int i = 0; i < luhnArr.length; i++) {
            if(luhnArr[i] > 9) luhnArr[i] -= 9;
        }
        int sum = 0;
        for (int i = 0; i < luhnArr.length; i++) {
            sum += luhnArr[i];
        }
        int checksum = (10 - (sum % 10)) % 10 == 0 ? 0 : (10 - (sum % 10)) % 10;
        return checksum;
    }
}