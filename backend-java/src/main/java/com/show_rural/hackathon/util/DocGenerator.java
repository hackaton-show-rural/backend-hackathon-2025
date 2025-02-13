package com.show_rural.hackathon.util;

import java.util.Random;


public class DocGenerator {
    private static final Random random = new Random();

    public static String generateDoc() {
        int[] numbers = new int[14];

        for (int i = 0; i < 8; i++) {
            numbers[i] = random.nextInt(10);
        }

        numbers[8] = 0;
        numbers[9] = 0;
        numbers[10] = 0;
        numbers[11] = 1;
        numbers[12] = calculateDigit(numbers, 12);
        numbers[13] = calculateDigit(numbers, 13);

        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            doc.append(numbers[i]);
            if (i == 1 || i == 4) {
                doc.append('.');
            } else if (i == 7) {
                doc.append('/');
            } else if (i == 11) {
                doc.append('-');
            }
        }

        return doc.toString();
    }

    private static int calculateDigit(int[] numbers, int position) {
        int[] multipliers = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;

        for (int i = 0; i < position; i++) {
            sum += numbers[i] * multipliers[multipliers.length - position + i];
        }

        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }
}