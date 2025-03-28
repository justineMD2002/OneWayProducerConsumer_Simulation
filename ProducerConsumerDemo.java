import java.util.Scanner;

class SharedMemory {
    private final int buffer_size = 5;
    private final char[] buffer = new char[buffer_size];
    private int in = 0;
    private int out = 0;
    private boolean isEmpty = true;
    private boolean isFull = false;
    
    public synchronized void produce(char ch) {
        while (isFull) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        buffer[in] = ch;
        in = (in + 1) % buffer_size;
        isEmpty = false;
        isFull = in == out;
        notify(); 
    }
    
    public synchronized char consume() {
        while (isEmpty) {
            try {
                wait(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        char ch = buffer[out];
        out = (out + 1) % buffer_size;
        isFull = false;
        isEmpty = in == out;
        notify(); 
        return ch;
    }
}

class Producer extends Thread {
    private final SharedMemory sharedMemory;
    
    public Producer(SharedMemory sharedMemory) {
        this.sharedMemory = sharedMemory;
    }
    
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a message: ");
        String message = scanner.nextLine();
        scanner.close();
        
        try {
            for (char ch : message.toCharArray()) {
                sharedMemory.produce(ch);
                Thread.sleep(500); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer extends Thread {
    private final SharedMemory sharedMemory;
    
    public Consumer(SharedMemory sharedMemory) {
        this.sharedMemory = sharedMemory;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                char ch = sharedMemory.consume();
                System.out.print(ch);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ProducerConsumerDemo {
    public static void main(String[] args) {
        SharedMemory sharedMemory = new SharedMemory();
        Producer producer = new Producer(sharedMemory);
        Consumer consumer = new Consumer(sharedMemory);
        
        producer.start();
        consumer.start();
    }
}
