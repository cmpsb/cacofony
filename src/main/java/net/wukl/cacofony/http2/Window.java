package net.wukl.cacofony.http2;

import java.util.concurrent.Semaphore;

/**
 * A flow-control window.
 */
public class Window {
    /**
     * The local window.
     */
    private final Semaphore local;

    /**
     * The remote window.
     */
    private final Semaphore remote;

    /**
     * Creates a new, empty flow-control window.
     */
    public Window() {
        this(0, 0);
    }

    /**
     * Creates a new flow-control window with initial values.
     *
     * @param local the initial size of the local window
     * @param remote the initial size of the remote window
     */
    public Window(final int local, final int remote) {
        this.local = new Semaphore(local);
        this.remote = new Semaphore(remote);
    }

    /**
     * Shrinks the local window after the client consumed part of it.
     *
     * @param permits the amount to shrink the window with
     *
     * @return whether shrinking succeeded or not
     */
    public boolean shrinkLocal(final int permits) {
        return this.local.tryAcquire(permits);
    }

    /**
     * Tries to shrink the remote window.
     *
     * If the window is too small to fit the requested size, it is halved until a fitting amount is
     * found. If the window is closed, {@code 0} is returned.
     *
     * @param permits the initial amount to shrink the window with
     *
     * @return the actual amount the window was shrunk with
     */
    public int acquireRemote(final int permits) {
        for (int p = permits; p > 0; p /= 2) {
            if (this.remote.tryAcquire(p)) {
                return p;
            }
        }

        return 0;
    }

    /**
     * Grows the local window.
     *
     * @param permits the amount to grow the window with
     */
    public void topOffLocal(final int permits) {
        this.local.release(permits);
    }

    /**
     * Grows the remote window.
     *
     * @param permits the amount the remote allows to let the window grow
     */
    public void topOffRemote(final int permits) {
        this.remote.release(permits);
    }
}
