package net.cmpsb.cacofony.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * An unbounded priority queue with explicit priorities.
 *
 * @param <T> the type of object the queue will contain
 *
 * @author Luc Everse
 */
public class ExplicitPriorityQueue<T> extends AbstractCollection<T> {
    /**
     * The backing queue for this queue.
     */
    private final PriorityQueue<Container<T>> queue;

    /**
     * Creates a new explicit priority queue with the default initial capacity that orders
     * its elements according to their natural ordering.
     */
    public ExplicitPriorityQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public Iterator<T> iterator() {
        return new UnpackingIterator<>(this.queue.iterator());
    }

    /**
     * Returns the number of items in the collection.
     *
     * @return the number of items in the collection
     */
    @Override
    public int size() {
        return this.queue.size();
    }

    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions.
     *
     * @param priority the element's priority
     * @param t        the element to add
     * @return {@code true} if the element was added to this queue, else
     * {@code false}
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    public boolean offer(final double priority, final T t) {
        return this.queue.offer(new Container<T>(priority, t));
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    public T poll() {
        return this.queue.poll().getItem();
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    public T peek() {
        return this.queue.peek().getItem();
    }

    /**
     * A container class that associates items with their explicit priorities for insertion into
     * the real queue.
     *
     * @param <E> the type of the item
     */
    private class Container<E> implements Comparable<Container<E>> {
        /**
         * The object the container holds.
         */
        private final E item;

        /**
         * The item's priority.
         */
        private final double priority;

        /**
         * Creates a new container object.
         *
         * @param priority the item's explicit priority
         * @param item     the contained item
         */
        Container(final double priority, final E item) {
            this.item = item;
            this.priority = priority;
        }

        /**
         * Returns the contained item.
         *
         * @return the contained item
         */
        public E getItem() {
            return this.item;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param other the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(final Container<E> other) {
            return (int) (this.priority - other.priority);
        }
    }

    /**
     * An iterator that unpacks elements from an iterator returning containers.
     *
     * @param <E> the type the container will contain
     */
    private final class UnpackingIterator<E> implements Iterator<E> {
        /**
         * The source iterator this iterator is unpacking.
         */
        private final Iterator<Container<E>> source;

        /**
         * Creates a new unpacking iterator.
         *
         * @param source the source iterator to unpack
         */
        UnpackingIterator(final Iterator<Container<E>> source) {
            this.source = source;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return this.source.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         */
        @Override
        public E next() {
            return this.source.next().getItem();
        }
    }
}
