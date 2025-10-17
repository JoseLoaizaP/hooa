package model;

import java.util.List;

public class MainAccount {

    private double availableBalance;
    private double totalBalance;
    private List<Pocket> pockets;

    public List<Pocket> getPockets() {
        if (pockets == null) {
            return List.of();
        }
        return pockets;
    }

    public void setPockets(List<Pocket> pockets) {
        this.pockets = pockets;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }
    public void addPocket(Pocket p) {
        if (pockets == null) {
            pockets = new java.util.ArrayList<>();
        } 
        pockets.add(p);
    }           
 
        
}

