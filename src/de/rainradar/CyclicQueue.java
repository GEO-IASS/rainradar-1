package de.rainradar;

import java.util.Iterator;
import java.util.Objects;

/**
 * A simple cyclic queue implementation.
 * 
 * @author maximilianstrauch
 * @param <E> Type of the queue elements.
 */
public class CyclicQueue<E> implements Iterable<E> {
   
    private int size;
    
    private QueueElement<E> front;
    
    public CyclicQueue(int size) {
        setSize(size);
        this.front = null;
    }
    
    public final void setSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Queue must have a length of "
                    + "at least 1 element!");
        }
        
        this.size = size - 1;
    }
    
    public void add(E element) {
        QueueElement<E> wrapper = new QueueElement<>();
        wrapper.value = element;
        wrapper.previous = front;
        if (front != null) {
            front.next = wrapper;
        }
        front = wrapper;

        QueueElement<E> next = front;
        for (int i = 0; i < size; i++) {
            if (next.previous == null) {
                break;
            }

            next = next.previous;
        }
        next.previous = null;
        
    }
    
    public E front() {
        QueueElement<E> frontWrapper = front;
        front = frontWrapper.previous;
        return frontWrapper.value;
    }
    
    public boolean contains(E element) {
        for (E internalElement : this) {
            if (Objects.equals(internalElement, element)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isEmpty() {
        return front == null;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private QueueElement<E> ptr = front;
            {
                while (ptr != null && ptr.previous != null) {
                    ptr = ptr.previous;
                }
            }
            
            @Override
            public boolean hasNext() {
                return ptr != null;
            }

            @Override
            public E next() {
                E value = ptr.value;
                ptr = ptr.next;
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private class QueueElement<E> {
        
        private E value;
        private QueueElement<E> previous, next;
        
    }
    
}
