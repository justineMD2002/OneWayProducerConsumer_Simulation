import java.util.Scanner;

class SharedMemory {
    final int buffer_size;
    char[] buffer;
    int in, out;
    boolean isEmpty, isFull;

    public SharedMemory() {
        this.buffer_size = 5;
        this.buffer = new char[buffer_size];
        this.in = 0;
        this.out = 0;
        this.isEmpty = true;
        this.isFull = false;
    }

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

    static SharedMemory sharedMemory = new SharedMemory();

    static void produce(String message, int length) {
        int index = 0;
        while (index < length) {
            synchronized (sharedMemory) {
                while (sharedMemory.isFull) {
                    try {
                        Thread.sleep(1000); 
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
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static String consume(int len) {
        StringBuilder consumed = new StringBuilder();

        for (int i = 0; i < len; i++) {
            synchronized (sharedMemory) {
                while (sharedMemory.isEmpty) {
                    try {
                        Thread.sleep(1000); 
                        sharedMemory.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                char ch = sharedMemory.buffer[sharedMemory.out];
                consumed.append(ch);
                System.out.println("Consumed: " + ch);

                sharedMemory.buffer[sharedMemory.out] = '-';
                sharedMemory.out = (sharedMemory.out + 1) % sharedMemory.buffer_size;

                sharedMemory.isFull = false;
                if (sharedMemory.in == sharedMemory.out) {
                    sharedMemory.isEmpty = true;
                }

                sharedMemory.printBuffer();
                sharedMemory.notify();
            }

            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return consumed.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Thread producer = new Thread(() -> {
            System.out.print("Enter a message (max 20 chars): ");
            String message = scanner.nextLine();
            if (message.length() > 20) {
                message = message.substring(0, 20);
            }
            produce(message, message.length());
        });

        Thread consumer = new Thread(() -> {
            String result = consume(20);
            System.out.println("Final consumed message: " + result);
        });

        producer.start();
        consumer.start();
    }
}
