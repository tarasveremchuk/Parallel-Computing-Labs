import java.io.*;
import java.util.*;
import java.util.concurrent.Phaser;

public class Lab1_sorting {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("--- Лабораторна робота №1 (Варіант 3) ---");
        System.out.print("Введіть розмір масиву (N): ");
        int n = sc.nextInt();
        
        int[] originalData = generate(n);
        saveToFile(originalData, "data.txt");

        // 1. Послідовне сортування (Еталон)
        int[] dataSerial = originalData.clone();
        System.out.println("\nЗапуск послідовного алгоритму (Bubble Sort)...");
        long startS = System.nanoTime();
        bubbleSort(dataSerial);
        long endS = System.nanoTime();
        double timeSerial = (endS - startS) / 1_000_000_000.0;
        System.out.printf("Час послідовно: %.4f сек\n", timeSerial);

        
        int cores = Runtime.getRuntime().availableProcessors();
        int maxThreads = cores * 2;
        System.out.println("\nВиявлено ядер: " + cores);
        System.out.println("Тестування паралельного алгоритму (1 - " + maxThreads + " потоків):");
        
        System.out.println("\nПотоки\t| Час (сек)\t| Прискорення");
        System.out.println("---------------------------------------------");

        StringBuilder report = new StringBuilder();
        report.append(String.format("Результати для N=%d на Apple M1\n", n));
        report.append(String.format("Послідовно: %.6f сек\n\n", timeSerial));
        report.append("Потоки\t| Час (сек)\t| Прискорення\n");

        for (int threads = 1; threads <= maxThreads; threads++) {
            int[] dataParallel = originalData.clone();
            
            long startP = System.nanoTime();
            parallelSort(dataParallel, threads);
            long endP = System.nanoTime();
            
            double timeP = (endP - startP) / 1_000_000_000.0;
            double speedup = timeSerial / timeP;

            String row = String.format("%d\t| %.6f\t| %.2fx", threads, timeP, speedup);
            System.out.println(row);
            report.append(row).append("\n");
        }

        saveFinalReport(report.toString(), "results_table.txt");
    }

    // Паралельний Odd-Even Sort (Метод обміну)
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
                    phaser.arriveAndAwaitAdvance();
                }
            });
            workerThreads[t].start();
        }

        for (Thread t : workerThreads) {
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

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

    public static int[] generate(int size) {
        Random r = new Random();
        int[] a = new int[size];
        for (int i = 0; i < size; i++) a[i] = r.nextInt(100000);
        return a;
    }

    public static void saveToFile(int[] arr, String name) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(name))) {
            for (int x : arr) pw.println(x);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void saveFinalReport(String content, String name) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(name))) {
            pw.println(content);
            System.out.println("\nДетальний звіт збережено у " + name);
        } catch (IOException e) { e.printStackTrace(); }
    }
}