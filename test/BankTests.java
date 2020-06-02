import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class BankTests {
    public static void main(String[] args) {
        Result result = new JUnitCore().run(ServerTest.class, ClientTest.class);

        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }
}
