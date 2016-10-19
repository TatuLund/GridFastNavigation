package org.vaadin.patrik.client;

import java.util.HashSet;
import java.util.Set;

public class RPCLock {

    private Set<Integer> locks;
    private int seq;
    
    public RPCLock() {
        locks = new HashSet<Integer>();
        seq = 1;
    }
    
    int requestLock() {
        locks.add(seq);
        return seq++;
    }
    
    void releaseLock(int id) {
        locks.remove(id);
    }
    
    void clearLocks() {
        locks.clear();
    }
    
    boolean isLocked() {
        return !locks.isEmpty();
    }
    
}
