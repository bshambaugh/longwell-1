// (c) 2004 Andreas Harth

package edu.mit.simile.scutter;

import java.util.LinkedList;

/**
 * Queue implementation.
 * from http://www.developer.com/java/data/article.php/10932_3296821_2
 *
 * $Id$
 */
public class Queue  {
    private LinkedList items;
    
    public Queue() {
        items = new LinkedList();
    }
    
    public Object enqueue(Object element) {
        items.add(element);
        return element;
    }
    
    public Object dequeue() {
        if (items.size() == 0)
            throw new EmptyQueueException() ;
        return items.removeFirst();		
    }
    
    public boolean isEmpty() {
        if (items.size() == 0)
            return true;
        else
            return false;
    }
}
