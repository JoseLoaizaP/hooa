package model;

public class Pocket {

    private String name;
    private double balance;
    private MainAccount mainAccount;

    public MainAccount getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(MainAccount mainAccount) {
        this.mainAccount = mainAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    
}
