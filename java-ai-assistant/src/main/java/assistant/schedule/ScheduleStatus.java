package assistant.schedule;

public enum ScheduleStatus {
    UPCOMING("即将开始"),
    ONGOING("进行中"),
    EXPIRED("已过期");

    private final String displayName;

    ScheduleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isUpcoming() {
        return this == UPCOMING;
    }

    public boolean isOngoing() {
        return this == ONGOING;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }
}
