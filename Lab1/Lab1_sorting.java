import java.io.*;
import java.util.*;
import java.util.concurrent.Phaser;

public class Lab1_sorting {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("--- Лабораторна робота №1 (Варіант 3) ---");
        System.out.print("Введіть розмір масиву (N): ");
        int n = sc.nextInt();
        
        // Генеруємо числа
        int[] originalData = generate(n);
        
        // Створюємо вхідний файл
        String inputFile = "data.txt";
        saveToFile(originalData, inputFile);
        System.out.println("Вхідні дані збережено у " + inputFile);

        // Читаємо масив назад
        int[] dataFromFile = readFromFile(inputFile, n);
        
        int[] dataSerial = dataFromFile.clone();
        int[] dataParallel = dataFromFile.clone();

        // Послідовне сортування (Bubble Sort)
        System.out.println("\nЗапуск послідовного алгоритму...");
        long startS = System.nanoTime(); // юзаємо наносекунди для точності
        bubbleSort(dataSerial);
        long endS = System.nanoTime();
        double timeS = (endS - startS) / 1_000_000_000.0; // переводимо в секунди
        System.out.printf("Послідовно: %.3f сек\n", timeS);

        // Паралельне сортування (Odd-Even Sort)
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("\nЗапуск паралельного (" + threads + " потоків)...");
        long startP = System.nanoTime();
        parallelSort(dataParallel, threads);
        long endP = System.nanoTime();
        double timeP = (endP - startP) / 1_000_000_000.0;
        System.out.printf("Паралельно: %.3f сек\n", timeP);
        
        double speedup = timeS / timeP;
        System.out.printf("\nПрискорення: %.2f разів\n", speedup);

        // Записуємо результати у файл (Пункт 3 методички)
        saveResultsToFile(n, timeS, timeP, speedup, "results.txt");
    }

    // Звичайна бульбашка
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
    }

    // Паралельний Odd-Even Sort на потоках
    public static void parallelSort(int[] arr, int numThreads) {
        int n = arr.length;
        final Phaser phaser = new Phaser(numThreads);
        Thread[] workerThreads = new Thread[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            workerThreads[t] = new Thread(() -> {
                for (int i = 0; i < n; i++) {
                    int start, end;
                    int chunkSize = n / numThreads;
                    
                    if (i % 2 == 0) { 
                        start = threadId * chunkSize;
                        if (start % 2 != 0) start++;
                    } else { 
                        start = threadId * chunkSize;
                        if (start % 2 == 0) start++;
                    }
                    end = (threadId == numThreads - 1) ? n - 1 : (threadId + 1) * chunkSize;

                    for (int j = start; j < end; j += 2) {
                        if (j + 1 < n && arr[j] > arr[j + 1]) {
                            int temp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = temp;
                        }
                    }
                    // Бар'єр, щоб потоки не побігли далі фази
                    phaser.arriveAndAwaitAdvance();
                }
            });
            workerThreads[t].start();
        }

        for (Thread t : workerThreads) {
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    // Рандомайзер
    public static int[] generate(int size) {
        Random r = new Random();
        int[] a = new int[size];
        for (int i = 0; i < size; i++) a[i] = r.nextInt(100000);
        return a;
    }

    // Запис вхідного масиву
    public static void saveToFile(int[] arr, String name) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(name))) {
            for (int x : arr) pw.println(x);
        } catch (IOException e) { System.out.println("Помилка запису масиву"); }
    }

    // Збереження результатів замірів (додав у кінці)
    public static void saveResultsToFile(int n, double ts, double tp, double s, String name) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(name, true))) { // true щоб дописувати в кінець
            pw.println("--- Результати замірів ---");
            pw.println("Розмір масиву N: " + n);
            pw.printf("Час послідовно: %.6f сек\n", ts);
            pw.printf("Час паралельно: %.6f сек\n", tp);
            pw.printf("Прискорення: %.2f\n", s);
            pw.println("--------------------------\n");
            System.out.println("Фінальні результати записані в " + name);
        } catch (IOException e) { System.out.println("Помилка запису результатів"); }
    }

    // Читання файлу
    public static int[] readFromFile(String name, int size) {
        int[] arr = new int[size];
        try (Scanner s = new Scanner(new File(name))) {
            for (int i = 0; i < size && s.hasNextInt(); i++) arr[i] = s.nextInt();
        } catch (Exception e) { System.out.println("Помилка читання файлу"); }
        return arr;
    }
}