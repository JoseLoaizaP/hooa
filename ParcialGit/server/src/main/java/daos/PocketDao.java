package daos;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import DBConfig.ConnectionManager;
import model.Pocket;

public class PocketDao implements Dao<Pocket, String> {

    static {
        try {
            System.out.println("Initializing database...");
            Connection conn = ConnectionManager.getInstance().getConnection();
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS pocket (name VARCHAR(255) PRIMARY KEY, balance DOUBLE)");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Pocket> findAll() {
        List<Pocket> pockets = new ArrayList<>();
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var rs = conn.createStatement().executeQuery("SELECT * FROM pocket");
            while (rs.next()) {
                Pocket pocket = new Pocket();
                pocket.setName(rs.getString("name"));
                pocket.setBalance(rs.getDouble("balance"));
                pockets.add(pocket);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pockets;
    }

    @Override
    public Pocket finById(String id) {
        Pocket pocket = null;
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var ps = conn.prepareStatement("SELECT * FROM pocket WHERE name = ?");
            ps.setString(1, id);
            var rs = ps.executeQuery();
            if (rs.next()) {
                pocket = new Pocket();
                pocket.setName(rs.getString("name"));
                pocket.setBalance(rs.getDouble("balance"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pocket;
    }

    @Override
    public Pocket update(Pocket newEntity) {
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var ps = conn.prepareStatement("UPDATE pocket SET balance = ? WHERE name = ?");
            ps.setDouble(1, newEntity.getBalance());
            ps.setString(2, newEntity.getName());
            ps.executeUpdate();
            conn.close();
            return newEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(Pocket entity) {
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            var ps = conn.prepareStatement("DELETE FROM pocket WHERE name = ?");
            ps.setString(1, entity.getName());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Pocket entity) {
        try ( Connection conn = ConnectionManager.getInstance().getConnection()) {
            var ps = conn.prepareStatement("INSERT INTO pocket (name, balance) VALUES (?, ?)");
            ps.setString(1, entity.getName());
            ps.setDouble(2, entity.getBalance());
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO implement save
    }

}
