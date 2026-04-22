package dsa.heap;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MaxHeap<T extends Comparable<T>> {
    private List<T> heap;
    
    public MaxHeap() {
        this.heap = new ArrayList<>();
    }
    
    public MaxHeap(List<T> items) {
        this.heap = new ArrayList<>(items);
        buildHeap();
    }
    
    // Core heap operations
    public void insert(T item) {
        heap.add(item);
        heapifyUp(heap.size() - 1);
    }
    
    public T extractMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        
        T max = heap.get(0);
        T lastItem = heap.remove(heap.size() - 1);
        
        if (!isEmpty()) {
            heap.set(0, lastItem);
            heapifyDown(0);
        }
        
        return max;
    }
    
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return heap.get(0);
    }
    
    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    public int size() {
        return heap.size();
    }
    
    // Helper methods
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = getParentIndex(index);
            if (heap.get(index).compareTo(heap.get(parentIndex)) <= 0) {
                break;
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }
    
    private void heapifyDown(int index) {
        while (true) {
            int leftChildIndex = getLeftChildIndex(index);
            int rightChildIndex = getRightChildIndex(index);
            int largestIndex = index;
            
            if (leftChildIndex < heap.size() && 
                heap.get(leftChildIndex).compareTo(heap.get(largestIndex)) > 0) {
                largestIndex = leftChildIndex;
            }
            
            if (rightChildIndex < heap.size() && 
                heap.get(rightChildIndex).compareTo(heap.get(largestIndex)) > 0) {
                largestIndex = rightChildIndex;
            }
            
            if (largestIndex == index) {
                break;
            }
            
            swap(index, largestIndex);
            index = largestIndex;
        }
    }
    
    private void buildHeap() {
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }
    
    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    private int getParentIndex(int i) { return (i - 1) / 2; }
    private int getLeftChildIndex(int i) { return 2 * i + 1; }
    private int getRightChildIndex(int i) { return 2 * i + 2; }
    
    // Utility methods
    public void clear() {
        heap.clear();
    }
    
    public boolean contains(T item) {
        return heap.contains(item);
    }
    
    public List<T> getAll() {
        return new ArrayList<>(heap);
    }
    
    // For debugging
    public void printHeap() {
        System.out.println("Heap: " + heap);
    }
    
    @Override
    public String toString() {
        return "MaxHeap{" + heap + "}";
    }
}
