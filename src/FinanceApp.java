import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FinanceApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final TransactionDAO txnDAO = new TransactionDAO();

    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("=== Personal Finance Management System ===");
        while (true) {
            if (currentUser == null) showAuthMenu();
            else showUserMenu();
        }
    }

    /* ---------------- AUTH MENU ---------------- */

    private static void showAuthMenu() {
        System.out.println("\n1) Register");
        System.out.println("2) Login");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();

        switch (c) {
            case "1": register(); break;
            case "2": login(); break;
            case "0": System.out.println("Bye"); System.exit(0);
            default: System.out.println("Invalid choice.");
        }
    }

    private static void register() {
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        // Hash the password
        String hash = Utils.sha256(pass);

        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(hash);

        if (userDAO.register(u)) {
            System.out.println("Registered successfully! You can now login.");
        } else {
            System.out.println("Registration failed (email may already exist).");
        }
    }

    private static void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        User u = userDAO.login(email, pass);
        if (u != null) {
            currentUser = u;
            System.out.println("Welcome, " + currentUser.getName() + "!");
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    /* ---------------- USER MENU ---------------- */

    private static void showUserMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1) Add transaction (income/expense)");
        System.out.println("2) List all transactions");
        System.out.println("3) Edit a transaction");
        System.out.println("4) Delete a transaction");
        System.out.println("5) Monthly summary");
        System.out.println("6) Export transactions to CSV");
        System.out.println("9) Logout");
        System.out.println("0) Exit");
        System.out.print("Choose: ");

        String c = scanner.nextLine().trim();

        switch (c) {
            case "1": addTransaction(); break;
            case "2": listTransactions(); break;
            case "3": editTransaction(); break;
            case "4": deleteTransaction(); break;
            case "5": monthlySummary(); break;
            case "6": exportCSV(); break;
            case "9": currentUser = null; break;
            case "0": System.out.println("Bye"); System.exit(0);
            default: System.out.println("Invalid choice.");
        }
    }

    /* ---------------- TRANSACTION HANDLERS ---------------- */

    private static void addTransaction() {
        System.out.print("Type (income/expense): ");
        String type = scanner.nextLine().trim().toLowerCase();

        if (!type.equals("income") && !type.equals("expense")) {
            System.out.println("Type must be 'income' or 'expense'.");
            return;
        }

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Date (YYYY-MM-DD): ");
        LocalDate date;
        try {
            date = LocalDate.parse(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return;
        }

        System.out.print("Note (optional): ");
        String note = scanner.nextLine().trim();

        Transaction t = new Transaction();
        t.setUserId(currentUser.getUserId());
        t.setType(type);
        t.setCategory(category);
        t.setAmount(amount);
        t.setDate(date);
        t.setNote(note);

        if (txnDAO.addTransaction(t)) {
            System.out.println("Transaction added successfully.");
        } else {
            System.out.println("Failed to add transaction.");
        }
    }

    private static void listTransactions() {
        List<Transaction> list = txnDAO.getTransactionsByUser(currentUser.getUserId());

        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("ID | Type | Category | Amount | Date | Note");
        for (Transaction t : list) {
            System.out.printf("%d | %s | %s | %.2f | %s | %s%n",
                    t.getTransId(), t.getType(), t.getCategory(),
                    t.getAmount(), t.getDate(), t.getNote());
        }
    }

    private static void editTransaction() {
        System.out.print("Enter transaction ID to edit: ");
        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid ID.");
            return;
        }

        List<Transaction> list = txnDAO.getTransactionsByUser(currentUser.getUserId());
        Transaction found = null;

        for (Transaction t : list) {
            if (t.getTransId() == id) {
                found = t;
                break;
            }
        }

        if (found == null) {
            System.out.println("Transaction not found.");
            return;
        }

        System.out.print("Type [" + found.getType() + "]: ");
        String type = scanner.nextLine().trim();
        if (!type.isEmpty()) found.setType(type);

        System.out.print("Category [" + found.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) found.setCategory(category);

        System.out.print("Amount [" + found.getAmount() + "]: ");
        String amountStr = scanner.nextLine().trim();
        if (!amountStr.isEmpty()) {
            try {
                found.setAmount(Double.parseDouble(amountStr));
            } catch (Exception e) {
                System.out.println("Invalid amount.");
                return;
            }
        }

        System.out.print("Date [" + found.getDate() + "]: ");
        String dateStr = scanner.nextLine().trim();
        if (!dateStr.isEmpty()) {
            try {
                found.setDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                System.out.println("Invalid date.");
                return;
            }
        }

        System.out.print("Note [" + found.getNote() + "]: ");
        String note = scanner.nextLine().trim();
        if (!note.isEmpty()) found.setNote(note);

        if (txnDAO.updateTransaction(found)) {
            System.out.println("Transaction updated.");
        } else {
            System.out.println("Update failed.");
        }
    }

    private static void deleteTransaction() {
        System.out.print("Enter transaction ID to delete: ");
        int id;

        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid ID.");
            return;
        }

        if (txnDAO.deleteTransaction(id, currentUser.getUserId())) {
            System.out.println("Deleted successfully.");
        } else {
            System.out.println("Delete failed or transaction not found.");
        }
    }

    private static void monthlySummary() {
        System.out.print("Enter year (YYYY): ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid year.");
            return;
        }

        System.out.print("Enter month (1-12): ");
        int month;
        try {
            month = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid month.");
            return;
        }

        Map<String, Double> m = txnDAO.getMonthlySummary(currentUser.getUserId(), year, month);

        System.out.printf("Income: %.2f%n", m.get("income"));
        System.out.printf("Expense: %.2f%n", m.get("expense"));
        System.out.printf("Balance: %.2f%n", m.get("balance"));
    }

    private static void exportCSV() {
        System.out.print("Enter CSV filename (e.g. txns.csv): ");
        String fname = scanner.nextLine().trim();

        List<Transaction> list = txnDAO.getTransactionsByUser(currentUser.getUserId());

        if (list.isEmpty()) {
            System.out.println("No transactions to export.");
            return;
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(fname)) {
            pw.println("trans_id,type,category,amount,date,note");

            for (Transaction t : list) {
                String note = t.getNote() == null ? "" : t.getNote().replace(",", " ");
                pw.printf("%d,%s,%s,%.2f,%s,%s%n",
                        t.getTransId(), t.getType(), t.getCategory(),
                        t.getAmount(), t.getDate(), note);
            }

            System.out.println("Exported to " + fname);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }
}
