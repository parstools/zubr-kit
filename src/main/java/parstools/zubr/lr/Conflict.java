package parstools.zubr.lr;

import java.util.List;
import java.util.Set;

/** One conflicting ACTION cell; the terminal word includes EOF as -1. */
public record Conflict(int state, List<Integer> lookahead, Set<Action> actions) {
    public Conflict {
        lookahead = List.copyOf(lookahead);
        actions = Set.copyOf(actions);
    }

    public boolean shiftReduce() {
        return actions.stream().anyMatch(a -> a.kind() == ActionKind.SHIFT)
                && actions.stream().anyMatch(a -> a.kind() == ActionKind.REDUCTION);
    }

    public boolean reduceReduce() {
        return actions.stream().filter(a -> a.kind() == ActionKind.REDUCTION).count() > 1;
    }
}
