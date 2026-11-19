// Tests FeeService without any window or database (uses InMemoryFeeRepository).
public class FeeServiceDemo {
    static void tryPay(FeeService s, String reg, String amt) {
        try {
            System.out.println("OK    -> " + s.pay(reg, amt));
        } catch (FeeException e) {
            System.out.println("ERROR -> " + e.getMessage()
                    + (e.getCause() != null ? "  (cause: " + e.getCause().getClass().getSimpleName() + ")" : ""));
        }
    }

    public static void main(String[] args) throws Exception {
        InMemoryFeeRepository repo = new InMemoryFeeRepository();
        FeeService s = new FeeService(repo, 0);
        tryPay(s, "CSA2401001", "5000");
        tryPay(s, "", "5000");
        tryPay(s, "CSA2401001", "abc");
        tryPay(s, "CSA2401001", "-10");
        tryPay(s, "x'; DROP TABLE fees;--", "100");
        repo.failNext = true;
        tryPay(s, "CSA2401002", "2500.50");
        tryPay(s, "CSA2401002", "2500.50");
        System.out.println("Last receipt -> " + s.lastReceipt());
        System.out.println("Rows saved   -> " + repo.rows);
    }
}
