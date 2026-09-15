package parstools.zubr.lr;

/** Shift target or global reduction number (0 is reserved for the augmented rule). */
public record Action(ActionKind kind, int number) {
    public static Action shift(int state) { return new Action(ActionKind.SHIFT, state); }
    public static Action reduce(int rule) { return new Action(ActionKind.REDUCTION, rule); }
    public static Action accept() { return new Action(ActionKind.ACCEPT, 0); }

    @Override
    public String toString() {
        return switch (kind) {
            case SHIFT -> "s" + number;
            case REDUCTION -> "r" + number;
            case ACCEPT -> "acc";
            case ERROR -> "—";
        };
    }
}
