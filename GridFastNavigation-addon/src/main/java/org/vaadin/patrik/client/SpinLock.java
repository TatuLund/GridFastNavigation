package org.vaadin.patrik.client;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;

/**
 * Perform asynchronous spinlocking through RequestAnimationFrame
 */
public class SpinLock {

    /**
     * LockFunction interface: single function, should return 'true' as long
     * as lock is to be held, and 'false' when it should be released
     */
    public interface LockFunction {
        boolean execute();
    }
    
    /**
     * Callback function gets called when the lock is released.
     */
    public interface Callback {
        void complete();
    }
    
    public static void lock(final LockFunction fn, final Callback cb) {
        final AnimationCallback a = new AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                if(fn.execute()) {
                    AnimationScheduler.get().requestAnimationFrame(this);
                } else {
                    cb.complete();
                }
            }
        };
        AnimationScheduler.get().requestAnimationFrame(a);
        
    }
    
}
