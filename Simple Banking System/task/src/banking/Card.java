package banking;
public class Card {
    private String  pin;

    public Card(String pin, double balance) {
        this.pin = pin;
        this.balance = balance;
    }

    public String getPin() {
        return pin;
    }

    private double balance;

    public Card() {
        this.pin = generatePIN();
        this.balance = 0.0;
    }

    public double getBalance() {
        return balance;
    }


    private String generatePIN(){
        return Main.generate(4);
    }

    protected void setBalance(int income) {
        balance += income;
    }
}
