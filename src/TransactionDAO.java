import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {
    public boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, type, category, amount, date, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategory());
            ps.setDouble(4, t.getAmount());
            ps.setDate(5, Date.valueOf(t.getDate()));
            ps.setString(6, t.getNote());
            int affected = ps.executeUpdate();
            if (affected == 0) return false;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setTransId(rs.getInt(1));
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            return false;
        }
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        String sql = "SELECT trans_id, user_id, type, category, amount, date, note FROM transactions WHERE user_id = ? ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                        rs.getInt("trans_id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("note")
                    );
                    list.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    public boolean updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET type=?, category=?, amount=?, date=?, note=? WHERE trans_id=? AND user_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getType());
            ps.setString(2, t.getCategory());
            ps.setDouble(3, t.getAmount());
            ps.setDate(4, Date.valueOf(t.getDate()));
            ps.setString(5, t.getNote());
            ps.setInt(6, t.getTransId());
            ps.setInt(7, t.getUserId());
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTransaction(int transId, int userId) {
        String sql = "DELETE FROM transactions WHERE trans_id = ? AND user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, transId);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getMonthlySummary(int userId, int year, int month) {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT type, SUM(amount) AS total FROM transactions WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? GROUP BY type";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                double income = 0.0, expense = 0.0;
                while (rs.next()) {
                    String type = rs.getString("type");
                    double total = rs.getDouble("total");
                    if ("income".equalsIgnoreCase(type)) income = total;
                    else if ("expense".equalsIgnoreCase(type)) expense = total;
                }
                map.put("income", income);
                map.put("expense", expense);
                map.put("balance", income - expense);
            }
        } catch (Exception e) {
            System.err.println("Error getting monthly summary: " + e.getMessage());
        }
        return map;
    }
}
