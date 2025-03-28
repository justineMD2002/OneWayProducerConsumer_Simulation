import java.util.Scanner;

public class ProducerConsumerDemo {
    public static void main(String[] args) {
        Buffer sharedBuffer = new Buffer();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Producer-Consumer Simulation");
        System.out.print("Enter a message: ");
        String inputMessage = scanner.nextLine();

        ProducerThread producer = new ProducerThread(sharedBuffer, inputMessage);
        ConsumerThread consumer = new ConsumerThread(sharedBuffer);

        System.out.println("Initializing threads...");
        consumer.start();
        producer.start();

        try {
            producer.join();
            consumer.join();
            System.out.println("Process completed successfully.");
        } catch (InterruptedException e) {
            System.err.println("Thread interruption occurred.");
        }

        scanner.close();
    }
}

class Buffer {
    private final int capacity = 5;
    private final char[] storage = new char[capacity];
    private int writeIndex = 0;
    private int readIndex = 0;
    private boolean completed = false;

    public synchronized boolean isBufferFull() {
        return (writeIndex + 1) % capacity == readIndex;
    }

    public synchronized boolean isBufferEmpty() {
        return writeIndex == readIndex;
    }

    public synchronized void insert(char ch) {
        storage[writeIndex] = ch;
        writeIndex = (writeIndex + 1) % capacity;
    }

    public synchronized char retrieve() {
        char ch = storage[readIndex];
        readIndex = (readIndex + 1) % capacity;
        return ch;
    }

    public void markComplete() {
        completed = true;
    }

    public boolean isProcessingDone() {
        return completed && isBufferEmpty();
    }
}

class ProducerThread extends Thread {
    private final Buffer buffer;
    private final String message;

    public ProducerThread(Buffer buffer, String message) {
        this.buffer = buffer;
        this.message = message;
    }

    @Override
    public void run() {
        for (char ch : message.toCharArray()) {
            while (buffer.isBufferFull()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Producer interrupted.");
                }
            }
            buffer.insert(ch);
            System.out.println("Produced: " + ch);
        }
        buffer.markComplete();
        System.out.println("Producer has finished processing.");
    }
}

class ConsumerThread extends Thread {
    private final Buffer buffer;

    public ConsumerThread(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (true) {
            while (buffer.isBufferEmpty()) {
                if (buffer.isProcessingDone()) {
                    System.out.println("\nConsumer has finished processing.");
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Consumer interrupted.");
                }
            }
            char ch = buffer.retrieve();
            System.out.print(ch);
            System.out.flush();
            System.out.println("\nConsumed: " + ch);
        }
    }
}
