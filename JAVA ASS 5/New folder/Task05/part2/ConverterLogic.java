// Shared conversion rules for the four styles (A-D). No GUI classes here.
// Rates are "units of this currency per 1 INR".
public class ConverterLogic {
    static double rate(String currency) {
        switch (currency) {
            case "USD": return 0.012;
            case "EUR": return 0.011;
            default:    return 1.0;      // INR
        }
    }

    static String convert(String amountText, String from, String to) {
        try {
            double a = Double.parseDouble(amountText.trim());
            if (Double.isNaN(a) || Double.isInfinite(a)) return "Invalid amount";
            double value = a / rate(from) * rate(to);
            return String.format("%.2f %s", value, to);
        } catch (NumberFormatException e) {
            return "Invalid amount";
        }
    }
}
