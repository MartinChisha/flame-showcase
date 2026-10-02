package showcase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/**
 * Dependency-free test harness so the samples build with nothing but a JDK. Runs every public
 * static no-arg method whose name starts with "test" in the listed classes.
 */
public final class TestRunner {

  public static void main(String[] args) throws Exception {
    List<String> classes = List.of(
        "showcase.competition.RoundRobinSchedulerTest",
        "showcase.policy.ReadinessEngineTest",
        "showcase.match.LineupLockPolicyTest",
        "showcase.finance.SuspensionCalculatorTest");

    int passed = 0;
    int failed = 0;
    for (String name : classes) {
      for (Method m : Class.forName(name).getDeclaredMethods()) {
        if (!m.getName().startsWith("test") || !Modifier.isStatic(m.getModifiers())) continue;
        try {
          m.invoke(null);
          passed++;
          System.out.println("  PASS " + name.substring(name.lastIndexOf('.') + 1) + "." + m.getName());
        } catch (java.lang.reflect.InvocationTargetException e) {
          failed++;
          System.out.println("  FAIL " + name + "." + m.getName() + ": " + e.getCause());
        }
      }
    }
    System.out.printf("%n%d passed, %d failed%n", passed, failed);
    if (failed > 0) System.exit(1);
  }

  public static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  public static void equal(Object expected, Object actual, String what) {
    if (!Objects.equals(expected, actual)) {
      throw new AssertionError(what + ": expected <" + expected + "> but was <" + actual + ">");
    }
  }

  public static void throwsIllegalArgument(Runnable action, String what) {
    try {
      action.run();
    } catch (IllegalArgumentException expected) {
      return;
    }
    throw new AssertionError(what + ": expected IllegalArgumentException");
  }

  private TestRunner() {}
}
