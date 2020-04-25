package signal;

import org.jetbrains.annotations.NotNull;

public enum FunctionCode implements Signal {
    ADD("100000"),
    SUBTRACT("100010"),
    AND("100100"),
    OR("100101"),
    SET_ON_LESS_THAN("101010");

    @NotNull
    private final String raw;

    FunctionCode(@NotNull String raw) {
        this.raw = raw;
    }

    @NotNull
    @Override
    public String getRaw() {
        return raw;
    }
}