import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class BaseTest {
    @Rule
    public TestRule watcher = watcher(description -> System.err.println("=== Running " + description.getMethodName()));

    protected static TestWatcher watcher(final Consumer<Description> watcher) {
        return new TestWatcher() {
            @Override
            protected void starting(final Description description) {
                watcher.accept(description);
            }
        };
    }
}
