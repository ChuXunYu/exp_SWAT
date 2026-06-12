package assistant.app;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() {
        throw new UnsupportedOperationException("utility class");
    }

    public static void main(String[] args) {
        ApplicationServices services = new ApplicationFactory().create();
        if (isDemoDataEnabled()) {
            new DemoDataFactory().load(services);
        }
        new ConsoleApplication(
                        services,
                        new InputStreamReader(System.in, StandardCharsets.UTF_8),
                        new OutputStreamWriter(System.out, StandardCharsets.UTF_8))
                .run();
    }

    private static boolean isDemoDataEnabled() {
        return isDemoDataEnabled(
                System.getProperty("ASSISTANT_DEMO_DATA"),
                System.getenv("ASSISTANT_DEMO_DATA"));
    }

    static boolean isDemoDataEnabled(String systemPropertyValue, String environmentValue) {
        String value = systemPropertyValue == null ? environmentValue : systemPropertyValue;
        if (value == null) {
            return true;
        }
        String normalized = value.strip().toLowerCase();
        return !normalized.equals("false") && !normalized.equals("0") && !normalized.equals("no");
    }
}
