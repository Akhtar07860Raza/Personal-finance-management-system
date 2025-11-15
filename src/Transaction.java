import java.time.LocalDate;

public class Transaction {
    private int transId;
    private int userId;
    private String type; // income or expense
    private String category;
    private double amount;
    private LocalDate date;
    private String note;

    public Transaction() {}

    public Transaction(int transId, int userId, String type, String category, double amount, LocalDate date, String note) {
        this.transId = transId;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public int getTransId() { return transId; }
    public void setTransId(int transId) { this.transId = transId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
