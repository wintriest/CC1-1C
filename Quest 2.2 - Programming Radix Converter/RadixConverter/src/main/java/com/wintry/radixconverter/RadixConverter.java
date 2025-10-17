/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.wintry.radixconverter;

/**
 *
 * @author Wintry
 */
import java.util.Scanner;
import java.util.ArrayList;

public class RadixConverter {
    private static final String DIGITS = "0123456789ABCDEF";
    private static final int FRACTION_PRECISION = 10;

    // ========================= MAIN =========================
    public static void main(String[] args) {
        ArrayList<String[]> results = new ArrayList<>(); // store results as String arrays now
        try (Scanner scanner = new Scanner(System.in)) {

            int[] allowedBases = {2, 8, 10, 16};

            while (true) {
                System.out.print("Enter origin base (2, 8, 10, 16): ");
                String baseInput = scanner.nextLine().trim();
                if (baseInput.equalsIgnoreCase("STOP")) break;

                int originBase;
                try {
                    originBase = Integer.parseInt(baseInput);

                    boolean isValidBase = false;
                    for (int base : allowedBases) {
                        if (originBase == base) {
                            isValidBase = true;
                            break;
                        }
                    }

                    if (!isValidBase) {
                        System.out.println(">> Invalid base. Only bases 2, 8, 10, and 16 are allowed.\n");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(">> Invalid input. Enter 2, 8, 10, or 16.\n");
                    continue;
                }

                System.out.print("Enter number in base " + originBase + ": ");
                String numberInput = scanner.nextLine().trim();
                if (numberInput.equalsIgnoreCase("STOP")) break;

                double decimalValue;
                try {
                    if (originBase == 10 || !numberInput.matches("[0-9A-Fa-f]+(\\.[0-9A-Fa-f]+)?")) {
                        decimalValue = evaluateExpression(numberInput);
                    } else {
                        decimalValue = toDecimal(numberInput, originBase);
                    }
                } catch (Exception e) {
                    System.out.println(">> Invalid input: " + e.getMessage() + "\n");
                    continue;
                }

                String output1 = "", output2 = "", output3 = "";
                switch (originBase) {
                    case 2 -> {
                        output1 = formatOutput(decimalValue, 8, FRACTION_PRECISION);
                        output2 = formatOutput(decimalValue, 10, FRACTION_PRECISION);
                        output3 = formatOutput(decimalValue, 16, FRACTION_PRECISION);
                    }
                    case 8 -> {
                        output1 = formatOutput(decimalValue, 2, FRACTION_PRECISION);
                        output2 = formatOutput(decimalValue, 10, FRACTION_PRECISION);
                        output3 = formatOutput(decimalValue, 16, FRACTION_PRECISION);
                    }
                    case 10 -> {
                        output1 = formatOutput(decimalValue, 2, FRACTION_PRECISION);
                        output2 = formatOutput(decimalValue, 8, FRACTION_PRECISION);
                        output3 = formatOutput(decimalValue, 16, FRACTION_PRECISION);
                    }
                    case 16 -> {
                        output1 = formatOutput(decimalValue, 2, FRACTION_PRECISION);
                        output2 = formatOutput(decimalValue, 8, FRACTION_PRECISION);
                        output3 = formatOutput(decimalValue, 10, FRACTION_PRECISION);
                    }
                }

                // Store as an array for easier dynamic formatting
                results.add(new String[]{
                    formatOutput(decimalValue, originBase, FRACTION_PRECISION),
                        output1,
                        output2,
                        output3
                    });

                System.out.println(">> Conversion stored. Enter next conversion or type 'STOP' to display results.\n");
            }

            // Display all results only after STOP is entered
            if (!results.isEmpty()) {
                displayResults(results);
            } else {
                System.out.println("No conversions to display.");
            }
        }
    }

    // ========================= DISPLAY RESULTS =========================
    private static void displayResults(ArrayList<String[]> results) {
        // Calculate max width per column dynamically
        int inputWidth = "Input".length();
        int out1Width = "Output 1".length();
        int out2Width = "Output 2".length();
        int out3Width = "Output 3".length();

        for (String[] row : results) {
            if (row[0].length() > inputWidth) inputWidth = row[0].length();
            if (row[1].length() > out1Width) out1Width = row[1].length();
            if (row[2].length() > out2Width) out2Width = row[2].length();
            if (row[3].length() > out3Width) out3Width = row[3].length();
        }

        // Add some padding for readability
        inputWidth += 2;
        out1Width += 2;
        out2Width += 2;
        out3Width += 2;

        String formatString = String.format("%%-%ds %%-%ds %%-%ds %%-%ds", inputWidth, out1Width, out2Width, out3Width);

        // Print header
        System.out.println("\n" + String.format(formatString, "Input", "Output 1", "Output 2", "Output 3"));

        // Print all results
        for (String[] row : results) {
            System.out.println(String.format(formatString, row[0], row[1], row[2], row[3]));
        }
        System.out.println();
    }

    // ========================= DECIMAL ↔ RADIX =========================
    public static String fromDecimal(double decimal, int base, int precision) {
        if (decimal == 0) return "0";
        boolean negative = decimal < 0;
        decimal = Math.abs(decimal);

        long wholePart = (long) decimal;
        double fracPart = decimal - wholePart;

        StringBuilder sb = new StringBuilder();

        if (wholePart == 0) sb.append('0');
        else {
            StringBuilder wholeStr = new StringBuilder();
            while (wholePart > 0) {
                wholeStr.append(DIGITS.charAt((int) (wholePart % base)));
                wholePart /= base;
            }
            sb.append(wholeStr.reverse());
        }

        if (fracPart > 0) {
            sb.append('.');
            int count = 0;
            while (fracPart > 0 && count < precision) {
                fracPart *= base;
                int digit = (int) fracPart;
                sb.append(DIGITS.charAt(digit));
                fracPart -= digit;
                count++;
            }
        }

        return negative ? "-" + sb.toString() : sb.toString();
    }

    public static double toDecimal(String number, int base) {
        number = number.toUpperCase();
        String[] parts = number.split("\\.");
        double result = 0;

        for (char c : parts[0].toCharArray()) {
            int digit = DIGITS.indexOf(c);
            if (digit == -1 || digit >= base) throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
            result = result * base + digit;
        }

        if (parts.length > 1) {
            double frac = 0;
            double power = base;
            for (char c : parts[1].toCharArray()) {
                int digit = DIGITS.indexOf(c);
                if (digit == -1 || digit >= base) throw new NumberFormatException("Invalid digit '" + c + "' for base " + base);
                frac += digit / power;
                power *= base;
            }
            result += frac;
        }

        return result;
    }
    
    public static String toSubscript(int base) {
        String baseStr = String.valueOf(base);
        StringBuilder subscript = new StringBuilder();
        for (char c : baseStr.toCharArray()) {
            switch (c) {
                case '0' -> subscript.append('₀');
                case '1' -> subscript.append('₁');
                case '2' -> subscript.append('₂');
                case '3' -> subscript.append('₃');
                case '4' -> subscript.append('₄');
                case '5' -> subscript.append('₅');
                case '6' -> subscript.append('₆');
                case '7' -> subscript.append('₇');
                case '8' -> subscript.append('₈');
                case '9' -> subscript.append('₉');
            }
        }
        return subscript.toString();
    }
    
    public static String formatOutput(double decimalValue, int base, int precision) {
        return "(" + fromDecimal(decimalValue, base, precision).toUpperCase() + ")" + toSubscript(base);
    }

    // ========================= EXPRESSION EVALUATION =========================
    public static double evaluateExpression(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() { ch = (++pos < expr.length()) ? expr.charAt(pos) : -1; }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) { x = parseExpression(); eat(')'); }
                else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'e' || ch == 'E') {
                        if (ch == 'e' || ch == 'E') { nextChar(); if (ch == '+' || ch == '-') nextChar(); }
                        else nextChar();
                    }
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                }
                else if (Character.isLetter(ch) || ch == 'π') {
                    while (Character.isLetter(ch) || ch == 'π') nextChar();
                    String func = expr.substring(startPos, this.pos);
                    switch (func.toLowerCase()) {
                        case "pi", "π" -> x = Math.PI;
                        case "e" -> x = Math.E;
                        case "sqrt" -> x = Math.sqrt(parseFactor());
                        case "sin"  -> x = Math.sin(parseFactor());
                        case "cos"  -> x = Math.cos(parseFactor());
                        case "tan"  -> x = Math.tan(parseFactor());
                        case "log"  -> x = Math.log10(parseFactor());
                        case "ln"   -> x = Math.log(parseFactor());
                        case "abs"  -> x = Math.abs(parseFactor());
                        default -> throw new RuntimeException("Unknown function: " + func);
                    }
                } else throw new RuntimeException("Unexpected: " + (char)ch);

                if (eat('^')) x = Math.pow(x, parseFactor());

                if (eat('!')) {
                    if (x < 0) throw new IllegalArgumentException("Factorial of negative number not defined.");
                    long f = 1;
                    for (int i = 2; i <= (int)x; i++) f *= i;
                    x = f;
                }

                return x;
            }
        }.parse();
    }
}
