package net.wukl.cacofony.http2.frame;

import java.util.Collections;
import java.util.Set;

/**
 * A PRIORITY frame.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.3">
 *     RFC 7540 Section 6.3 (PRIORITY)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-5.3">
 *     RFC 7540 Section 5.3 (Stream Priority)</a>
 */
public class PriorityFrame implements Frame {
    /**
     * The stream identifier of the stream the priority is for.
     */
    private final int streamId;

    /**
     * Whether the dependency is exclusive or not.
     */
    private final boolean exclusive;

    /**
     * The identifier of the stream this stream depends on.
     */
    private final int dependencyId;

    /**
     * The weight of the stream the priority is for.
     */
    private final int weight;

    /**
     * Creates a new PRIORITY frame.
     *
     * @param streamId the identifier of the stream the priority is for
     * @param exclusive whether the dependency is exclusive or not
     * @param dependencyId the identifier of the stream the stream depends on
     * @param weight the weight of the stream the priority is for
     */
    public PriorityFrame(
            final int streamId, final boolean exclusive, final int dependencyId, final int weight
    ) {
        this.streamId = streamId;
        this.exclusive = exclusive;
        this.dependencyId = dependencyId;
        this.weight = weight;
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return 5;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return FrameType.PRIORITY;
    }

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    @Override
    public Set<FrameFlag> getFlags() {
        return Collections.emptySet();
    }

    /**
     * Returns the identifier of the stream the frame belongs to.
     *
     * @return the stream identifier
     */
    @Override
    public int getStreamId() {
        return this.streamId;
    }

    /**
     * Checks whether the dependency is exclusive or not.
     *
     * @return {@code true} if the dependency is exclusive, {@code false} otherwise
     */
    public boolean isExclusive() {
        return this.exclusive;
    }

    /**
     * Returns the identifier of the stream the stream depends on.
     *
     * @return the dependency identifier
     */
    public int getDependencyId() {
        return this.dependencyId;
    }

    /**
     * Returns the weight of the priority.
     *
     * @return the weight
     */
    public int getWeight() {
        return this.weight;
    }
}
