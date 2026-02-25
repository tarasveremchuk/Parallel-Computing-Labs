import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Lab1_sorting {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String fileName = "data.txt";
        
        while (true) {
            System.out.println("\n--- МЕНЮ (Лабораторна №1) ---");
            System.out.println("1. Згенерувати новий масив та зберегти у файл");
            System.out.println("2. Зчитати масив з файлу та виконати сортування");
            System.out.println("0. Вихід");
            System.out.print("Виберіть дію: ");
            
            int choice = scanner.nextInt();

            if (choice == 0) {
                System.out.println("Програму завершено.");
                break; 
            }

            if (choice == 1) {
                System.out.print("Введіть розмір масиву (для ~5 сек спробуйте 60000): ");
                int size = scanner.nextInt();
                int[] arr = generate(size);
                saveToFile(arr, fileName);
                System.out.println("Готово! Масив збережено у файл: " + fileName);
            } 
            else if (choice == 2) {
                int[] arr = readFromFile(fileName);
                if (arr == null) continue;

                System.out.println("Масив зчитано. Розмір: " + arr.length);
                int[] arrParallel = arr.clone();

                // 1. Послідовне сортування
                long startTime = System.currentTimeMillis();
                bubbleSort(arr);
                long endTime = System.currentTimeMillis();
                double sequentialTime = (endTime - startTime) / 1000.0;
                System.out.println("Час виконання (послідовно): " + sequentialTime + " с.");

                // 2. Паралельне сортування
                startTime = System.currentTimeMillis();
                oddEvenSort(arrParallel);
                endTime = System.currentTimeMillis();
                double parallelTime = (endTime - startTime) / 1000.0;
                System.out.println("Час виконання (паралельно): " + parallelTime + " с.");
                
                System.out.printf("Прискорення: %.2f x\n", (sequentialTime / parallelTime));
            } else {
                System.out.println("Невірний вибір. Спробуйте ще раз.");
            }
        }
    }

    // --- АЛГОРИТМИ ---

    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) swap(arr, j, j + 1);
            }
        }
    }

    public static void oddEvenSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < n - 1; j += 2) {
                    if (arr[j] > arr[j + 1]) swap(arr, j, j + 1);
                }
            } else {
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

    // --- РОБОТА З ДАНИМИ ---

    public static int[] generate(int size) {
        Random r = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = r.nextInt(100000);
        return arr;
    }

    public static void saveToFile(int[] arr, String fileName) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
            out.println(arr.length);
            for (int val : arr) out.print(val + " ");
        } catch (IOException e) {
            System.out.println("Помилка запису: " + e.getMessage());
        }
    }

    public static int[] readFromFile(String fileName) {
        try (Scanner sc = new Scanner(new File(fileName))) {
            if (!sc.hasNextInt()) return null;
            int n = sc.nextInt();
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                if (sc.hasNextInt()) arr[i] = sc.nextInt();
            }
            return arr;
        } catch (FileNotFoundException e) {
            System.out.println("Помилка: Файл '" + fileName + "' не знайдено. Спочатку згенеруйте дані (пункт 1).");
            return null;
        }
    }
}