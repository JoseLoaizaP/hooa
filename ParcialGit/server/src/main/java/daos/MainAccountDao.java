package daos;

import java.sql.Connection;
import DBConfig.ConnectionManager;
import model.MainAccount;

public class MainAccountDao {

    static {
        try {
            System.out.println("Initializing main account table...");
            Connection conn = ConnectionManager.getInstance().getConnection();
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS main_account (id INT PRIMARY KEY, available_balance DOUBLE, total_balance DOUBLE)");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainAccount find() {
        MainAccount account = null;
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var rs = conn.createStatement().executeQuery("SELECT * FROM main_account WHERE id = 1");
            if (rs.next()) {
                account = new MainAccount();
                account.setAvailableBalance(rs.getDouble("available_balance"));
                account.setTotalBalance(rs.getDouble("total_balance"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

    public void save(MainAccount account) {
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var ps = conn.prepareStatement("INSERT INTO main_account (id, available_balance, total_balance) VALUES (1, ?, ?)");
            ps.setDouble(1, account.getAvailableBalance());
            ps.setDouble(2, account.getTotalBalance());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(MainAccount account) {
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var ps = conn.prepareStatement("UPDATE main_account SET available_balance = ?, total_balance = ? WHERE id = 1");
            ps.setDouble(1, account.getAvailableBalance());
            ps.setDouble(2, account.getTotalBalance());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}