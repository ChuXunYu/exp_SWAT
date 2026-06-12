package assistant.ai;

public enum SuggestionDraftStatus {
    CONFIRMABLE,
    CANCELLED,
    IMPORTED;

    public boolean isConfirmable() {
        return this == CONFIRMABLE;
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == IMPORTED;
    }
}
