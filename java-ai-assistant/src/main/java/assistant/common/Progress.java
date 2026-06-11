package assistant.common;

public record Progress(int value) {
    public Progress {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be less than 0");
        }
        if (value > 100) {
            throw new IllegalArgumentException("value must not be greater than 100");
        }
    }

    public static Progress zero() {
        return new Progress(0);
    }

    public static Progress complete() {
        return new Progress(100);
    }

    public static Progress of(int value) {
        return new Progress(value);
    }

    public boolean isComplete() {
        return value == 100;
    }

    public String toPercentageString() {
        return value + "%";
    }
}
