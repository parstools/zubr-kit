package parstools.zubr.lr;

enum ActionKind {
    SHIFT,
    REDUCTION,
    ACCEPT,
    ERROR,
}

public class Action {
    ActionKind kind;
    int number;
}