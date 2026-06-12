package assistant.ai;

public enum AiRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String wireValue;

    AiRole(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
