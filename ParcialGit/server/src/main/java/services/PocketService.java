package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.MainAccount;
import model.Pocket;

public class PocketService {

    private final MainAccount mainAccount;
    private final Map<String, Pocket> pockets = new HashMap<>();

    public PocketService(double initialAmount) {
        if (initialAmount < 0) throw new IllegalArgumentException("Initial amount must be >= 0");
        this.mainAccount = new MainAccount();
        this.mainAccount.setAvailableBalance(initialAmount);
        this.mainAccount.setTotalBalance(initialAmount);

        // Si tu MainAccount(initialAmount) no existe, usa:
        // this.mainAccount = new MainAccount();
        // this.mainAccount.setAvailableBalance(initialAmount);
        // this.mainAccount.setTotalBalance(initialAmount);
    }

    public synchronized Pocket addPocket(String name, double initialAmount) throws Exception {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Pocket name is required");
        if (initialAmount < 0) throw new IllegalArgumentException("Initial amount must be >= 0");
        if (mainAccount.getAvailableBalance() < initialAmount)
            throw new IllegalStateException("Insufficient funds in main account");

        Pocket p = pockets.get(name);
        if (p == null) {
            p = new Pocket();
            p.setName(name);
            p.setBalance(0.0);
            pockets.put(name, p);
            // refleja en la lista del MainAccount (si existe)
            if (mainAccount.getPockets() != null) {
                List<Pocket> list = new ArrayList<>(mainAccount.getPockets());
                list.add(p);
                // Si no tienes setPockets, agrega uno a uno:
                // mainAccount.addPocket(p);
                // Para mantener compatibilidad, intentamos addPocket si existe:
                try { mainAccount.addPocket(p); } catch (Throwable ignore) {}
            }
        }

        p.setBalance(p.getBalance() + initialAmount);
        mainAccount.setAvailableBalance(mainAccount.getAvailableBalance() - initialAmount);
        return attach(p);
    }

    public synchronized Pocket depositInPocket(String name, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        Pocket p = requirePocket(name);
        if (mainAccount.getAvailableBalance() < amount)
            throw new IllegalStateException("Insufficient funds in main account");

        p.setBalance(p.getBalance() + amount);
        mainAccount.setAvailableBalance(mainAccount.getAvailableBalance() - amount);
        return attach(p);
    }

    public synchronized Pocket withdrawFromPocket(String name, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        Pocket p = requirePocket(name);
        if (p.getBalance() < amount)
            throw new IllegalStateException("Insufficient funds in pocket");

        p.setBalance(p.getBalance() - amount);
        mainAccount.setAvailableBalance(mainAccount.getAvailableBalance() + amount);
        return attach(p);
    }

    public synchronized MainAccount depositInAccount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        mainAccount.setAvailableBalance(mainAccount.getAvailableBalance() + amount);
        mainAccount.setTotalBalance(mainAccount.getTotalBalance() + amount);
        return snapshot();
    }

    public synchronized MainAccount getMainAccount() {
        return snapshot();
    }

    public synchronized Pocket getPocket(String name) {
        Pocket p = requirePocket(name);
        return attach(p);
    }

    // ---- helpers ----
    private Pocket requirePocket(String name) {
        Pocket p = pockets.get(name);
        if (p == null) throw new IllegalArgumentException("Pocket not found: " + name);
        return p;
    }

    private Pocket attach(Pocket p) {
        // copia del bolsillo
        Pocket out = new Pocket();
        out.setName(p.getName());
        out.setBalance(p.getBalance());

        // snapshot de la cuenta
        MainAccount acc = new MainAccount();
        acc.setAvailableBalance(mainAccount.getAvailableBalance());
        acc.setTotalBalance(mainAccount.getTotalBalance());

        // clonar lista de pockets (sin depender de setPockets)
        if (mainAccount.getPockets() != null) {
            for (Pocket orig : mainAccount.getPockets()) {
                Pocket cp = new Pocket();
                cp.setName(orig.getName());
                cp.setBalance(orig.getBalance());
                try { acc.addPocket(cp); } catch (Throwable ignore) {}
            }
        }

        out.setMainAccount(acc);
        return out;
    }

    private MainAccount snapshot() {
        MainAccount acc = new MainAccount();
        acc.setAvailableBalance(mainAccount.getAvailableBalance());
        acc.setTotalBalance(mainAccount.getTotalBalance());

        if (mainAccount.getPockets() != null) {
            for (Pocket orig : mainAccount.getPockets()) {
                Pocket cp = new Pocket();
                cp.setName(orig.getName());
                cp.setBalance(orig.getBalance());
                try { acc.addPocket(cp); } catch (Throwable ignore) {}
            }
        }
        return acc;
    }
}
