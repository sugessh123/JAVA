public class ConverterLogicDemo {
    public static void main(String[] args) {
        System.out.println(ConverterLogic.convert("1000", "INR", "USD"));
        System.out.println(ConverterLogic.convert("1000", "INR", "EUR"));
        System.out.println(ConverterLogic.convert("12", "USD", "INR"));
        System.out.println(ConverterLogic.convert("11", "EUR", "USD"));
        System.out.println(ConverterLogic.convert("abc", "INR", "USD"));
        System.out.println(ConverterLogic.convert("", "INR", "USD"));
    }
}
