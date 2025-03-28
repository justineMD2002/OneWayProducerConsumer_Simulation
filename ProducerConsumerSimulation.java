import java.util.Scanner;

class SharedMemory {
    final int buffer_size = 5;
    char[] buffer = new char[buffer_size];
    int in = 0, out = 0;
    boolean isEmpty = true, isFull = false;

    public void printBuffer() {
        System.out.print("Buffer: [");
        for (int i = 0; i < buffer_size; i++) {
            if (i == in && i == out) {
                System.out.print("(I/O)" + buffer[i] + " ");
            } else if (i == in) {
                System.out.print("(I)" + buffer[i] + " ");
            } else if (i == out) {
                System.out.print("(O)" + buffer[i] + " ");
            } else {
                System.out.print(buffer[i] + " ");
            }
        }
        System.out.println("]");
    }
}

public class ProducerConsumerSimulation {
    public static void main(String[] args) {
        SharedMemory sharedMemory = new SharedMemory();
        Scanner scanner = new Scanner(System.in);

        Thread producer = new Thread(() -> {
            System.out.print("Enter a message: ");
            String message = scanner.nextLine();
            int index = 0;

            while (index < message.length()) {
                synchronized (sharedMemory) {
                    while (sharedMemory.isFull) {
                        try {
                            sharedMemory.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sharedMemory.buffer[sharedMemory.in] = message.charAt(index++);
                    sharedMemory.in = (sharedMemory.in + 1) % sharedMemory.buffer_size;
                    sharedMemory.isEmpty = false;
                    if (sharedMemory.in == sharedMemory.out) {
                        sharedMemory.isFull = true;
                    }
                    sharedMemory.printBuffer();
                    sharedMemory.notify();
                }
                try {
                    Thread.sleep(500); // Simulate processing delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread consumer = new Thread(() -> {
            while (true) {
                synchronized (sharedMemory) {
                    while (sharedMemory.isEmpty) {
                        try {
                            sharedMemory.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(sharedMemory.buffer[sharedMemory.out]);
                    sharedMemory.buffer[sharedMemory.out] = '-'; // Visualize consumed character
                    sharedMemory.out = (sharedMemory.out + 1) % sharedMemory.buffer_size;
                    sharedMemory.isFull = false;
                    if (sharedMemory.in == sharedMemory.out) {
                        sharedMemory.isEmpty = true;
                    }
                    sharedMemory.printBuffer();
                    sharedMemory.notify();
                }
                try {
                    Thread.sleep(1000); // Simulate processing delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        producer.start();
        consumer.start();
    }
}
