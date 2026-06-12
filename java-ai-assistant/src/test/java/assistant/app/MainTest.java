package assistant.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void demoDataSwitchDefaultsToEnabledWhenUnset() {
        assertTrue(Main.isDemoDataEnabled(null, null));
    }

    @Test
    void demoDataSwitchDisablesForExplicitFalseValues() {
        assertAll(
                () -> assertFalse(Main.isDemoDataEnabled("false", null)),
                () -> assertFalse(Main.isDemoDataEnabled("0", null)),
                () -> assertFalse(Main.isDemoDataEnabled("no", null)),
                () -> assertFalse(Main.isDemoDataEnabled(" FALSE ", null)));
    }

    @Test
    void demoDataSwitchEnablesForOtherValues() {
        assertAll(
                () -> assertTrue(Main.isDemoDataEnabled("true", null)),
                () -> assertTrue(Main.isDemoDataEnabled("1", null)),
                () -> assertTrue(Main.isDemoDataEnabled("yes", null)),
                () -> assertTrue(Main.isDemoDataEnabled("", null)));
    }

    @Test
    void demoDataSwitchUsesSystemPropertyBeforeEnvironmentValue() {
        assertAll(
                () -> assertFalse(Main.isDemoDataEnabled("false", "true")),
                () -> assertTrue(Main.isDemoDataEnabled("true", "false")),
                () -> assertFalse(Main.isDemoDataEnabled(null, "no")),
                () -> assertTrue(Main.isDemoDataEnabled(null, "yes")));
    }

    @Test
    void mainStartsConsoleWithEndOfInputAndRestoresSystemProperty() {
        String previousValue = System.getProperty("ASSISTANT_DEMO_DATA");
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;

        try {
            System.setProperty("ASSISTANT_DEMO_DATA", "0");
            System.setIn(new ByteArrayInputStream(new byte[0]));
            System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));

            Main.main(new String[0]);
        } finally {
            if (previousValue == null) {
                System.clearProperty("ASSISTANT_DEMO_DATA");
            } else {
                System.setProperty("ASSISTANT_DEMO_DATA", previousValue);
            }
            System.setIn(previousIn);
            System.setOut(previousOut);
        }
    }
}
