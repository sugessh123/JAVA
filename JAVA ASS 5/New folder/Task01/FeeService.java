import java.sql.SQLException;

// All rules live here: validation, payment and receipt. No AWT or Swing classes are used,
// so this class can be tested without opening a window.
public class FeeService {
    private final FeeRepository repository;
    private final long confirmationDelayMs;      // simulates waiting for bank confirmation
    private String lastReceipt = null;

    public FeeService(FeeRepository repository, long confirmationDelayMs) {
        this.repository = repository;
        this.confirmationDelayMs = confirmationDelayMs;
    }

    public double validate(String regNo, String amountText) throws FeeException {
        if (regNo == null || regNo.trim().isEmpty()) throw new FeeException("Register number is required");
        if (!regNo.trim().matches("[A-Za-z0-9]{6,15}")) throw new FeeException("Register number must be 6-15 letters or digits");
        if (amountText == null || amountText.trim().isEmpty()) throw new FeeException("Amount is required");
        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            throw new FeeException("Amount must be a number", e);
        }
        if (amount <= 0) throw new FeeException("Amount must be greater than zero");
        if (amount > 100000) throw new FeeException("Amount cannot exceed 100000");
        return amount;
    }

    // May take several seconds, so the window must call it from a background thread.
    public String pay(String regNo, String amountText) throws FeeException {
        double amount = validate(regNo, amountText);
        try {
            Thread.sleep(confirmationDelayMs);
            repository.save(regNo.trim(), amount);
        } catch (SQLException e) {
            throw new FeeException("Payment could not be saved. Please try again.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FeeException("Payment was interrupted", e);
        }
        lastReceipt = "RECEIPT: " + regNo.trim() + " paid Rs " + String.format("%.2f", amount);
        return lastReceipt;
    }

    public String lastReceipt() throws FeeException {
        if (lastReceipt == null) throw new FeeException("No payment has been made yet");
        return lastReceipt;
    }
}
