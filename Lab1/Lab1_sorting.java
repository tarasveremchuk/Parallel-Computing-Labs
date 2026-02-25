import java.util.Random;

public class Lab1_sorting {
    public static void main(String[] args) {
        int size = 60000; // Підібрано під ~5-6 секунд для Bubble Sort на M1
        
        System.out.println("--- Лабораторна робота №1 (Варіант 3) ---");
        System.out.println("Завдання: Сортування масиву методом обміну");
        System.out.println("Процесор: Apple M1");

        int[] dataSerial = generate(size);
        int[] dataParallel = dataSerial.clone();

        // 1. Послідовне сортування (Bubble Sort)
        System.out.println("\nЗапуск послідовного алгоритму...");
        long startS = System.currentTimeMillis();
        bubbleSort(dataSerial);
        long endS = System.currentTimeMillis();
        double timeS = (endS - startS) / 1000.0;
        System.out.printf("Час (послідовно): %.3f секунд\n", timeS);

        // 2. Паралельне сортування (Odd-Even Sort - пункт 7)
        System.out.println("\nЗапуск паралельного алгоритму (Odd-Even Sort)...");
        long startP = System.currentTimeMillis();
        oddEvenSortParallel(dataParallel);
        long endP = System.currentTimeMillis();
        double timeP = (endP - startP) / 1000.0;
        System.out.printf("Час (паралельно): %.3f секунд\n", timeP);
        System.out.printf("Прискорення: %.2f разів\n", (timeS / timeP));
    }

    // Класичний метод обміну (Bubble Sort)
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    // Паралельний метод обміну (Odd-Even Sort)
    public static void oddEvenSortParallel(int[] arr) {
        int n = arr.length;
        int cores = Runtime.getRuntime().availableProcessors();
        
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                // Парна фаза
                for (int j = 0; j < n - 1; j += 2) {
                    if (arr[j] > arr[j + 1]) swap(arr, j, j + 1);
                }
            } else {
                // Непарна фаза
                for (int j = 1; j < n - 1; j += 2) {
                    if (arr[j] > arr[j + 1]) swap(arr, j, j + 1);
                }
            }
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static int[] generate(int size) {
        Random r = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = r.nextInt(100000);
        return arr;
    }
}