/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.wintry.binaryconverter;

/**
 *
 * @author Wintry
 */
import java.util.Scanner;
import java.util.ArrayList;

public class BinaryConverter {
    private static final int DECIMAL_PLACES_LIMIT = 10;
    
    public static String toBinary(double decimal) {
        if (decimal == 0) return "0";

        boolean isNegative = decimal < 0;
        decimal = Math.abs(decimal);
        
        // Separates Whole Numbers from its Decimals.
        long whole = (long) decimal;
        double frac = decimal - whole;

        StringBuilder binary = new StringBuilder();

        if (whole == 0) {
            binary.append("0");
        } else {
            while (whole > 0) {
                binary.append(whole % 2);
                whole /= 2;
            }
            binary.reverse();
        }

        if (frac > 0) {
            binary.append(".");
            for (int i = 0; i < DECIMAL_PLACES_LIMIT; i++) {
                frac *= 2;
                if (frac >= 1) {
                    binary.append("1");
                    frac -= 1;
                } else {
                    binary.append("0");
                }
            }
        }
        return isNegative ? "-" + binary.toString() : binary.toString();
    }

    // Fraction and Radical Support,
    public static double evaluate(String input) {
        input = input.trim();
        
        try {
            return evalExpression(input); // new expression evaluator
        } catch (Exception ignored) {
            // if it fails, fall back to your old handling
        }

        if (input.contains("û")) {
            throw new IllegalArgumentException("Use 'sqrt' instead.");
        }

        // Square Root Handling,
        if (input.startsWith("sqrt") && !input.contains(" ")) {
            return parseTerm(input);
        }

        // Mixed Fractions Handling,
        if (input.matches("^-?\\d+\\s+\\d+/\\d+$")) {
            String[] parts = input.split("\\s+");
            double whole = Double.parseDouble(parts[0]);
            String[] frac = parts[1].split("/");
            double numerator = parseTerm(frac[0].trim());
            double denominator = parseTerm(frac[1].trim());
            if (denominator == 0) throw new IllegalArgumentException("Denominator cannot be zero.");
            double sign = whole >= 0 ? 1 : -1;
            return whole + sign * (numerator / denominator);
        }

        // Fraction Handling,
        if (input.contains("/")) {
            if (input.chars().filter(ch -> ch == '/').count() > 1) {
                throw new IllegalArgumentException("Too many '/' characters.");
            }
            String[] parts = input.split("/", 2);
            double numerator = parseTerm(parts[0].trim());
            double denominator = parseTerm(parts[1].trim());
            if (denominator == 0) throw new IllegalArgumentException("Denominator cannot be zero.");
            return numerator / denominator;
        }
        return parseTerm(input);
    }

    // User Input Parsing,
    private static double parseTerm(String term) {
        term = term.trim();
        
        // Parentheses Handling
        if (term.startsWith("(") && term.endsWith(")")) {
            return evaluate(term.substring(1, term.length() - 1));
        }
        // Random number [0,1)
        if (term.equalsIgnoreCase("rand")) {
            return Math.random();
        }

        // Constants Handling
        if (term.equalsIgnoreCase("pi") || term.equals("π")) {
            return Math.PI;
        }
        if (term.equalsIgnoreCase("e")) {
            return Math.E;
        }
        
        // Trigonometric Functions
        if (term.startsWith("sin")) {
            return Math.sin(parseTerm(term.substring(3)));
        }
        if (term.startsWith("cos")) {
            return Math.cos(parseTerm(term.substring(3)));
        }
        if (term.startsWith("tan")) {
            return Math.tan(parseTerm(term.substring(3)));
        }
        
        // Logarithmic Functions
        if (term.startsWith("ln")) {
            return Math.log(parseTerm(term.substring(2))); // natural log
        }
        if (term.startsWith("log")) {
            return Math.log10(parseTerm(term.substring(3))); // base-10 log
        }
        
        // Absolute Value
        if (term.startsWith("abs")) {
            return Math.abs(parseTerm(term.substring(3)));
        }

        // Alternative Square Root,
        if (term.startsWith("√")) {
            return Math.sqrt(parseTerm(term.substring(1)));
        }
        if (term.startsWith("sqrt")) {
            return Math.sqrt(parseTerm(term.substring(4))); // remove "sqrt"
        }
        
        if (term.contains("^")) {
            String[] parts = term.split("\\^", 2);
            double base = parseTerm(parts[0].trim());
            double exponent = parseTerm(parts[1].trim());
            return Math.pow(base, exponent);
        }

        // Negative Handling,
        if (term.startsWith("-")) {
            return -parseTerm(term.substring(1));
        }

        // Square Root Handling,
        if (term.startsWith("√")) {
            return Math.sqrt(parseTerm(term.substring(1)));
        }
        
        // Factorial Handling
        if (term.endsWith("!")) {
            int n = (int) parseTerm(term.substring(0, term.length() - 1));
            if (n < 0) throw new IllegalArgumentException("Factorial of negative number not defined.");
            long fact = 1;
            for (int i = 2; i <= n; i++) fact *= i;
            return fact;
        }

        // Regular Number,
        return Double.parseDouble(term);
    }
    
    // Add this helper to evaluate full expressions with operators
    private static double evalExpression(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | number | func factor | factor `^` factor | ( expression )
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else if (eat('%')) x %= parseFactor(); // modulo
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;

                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers (dec, hex, sci)
                    if (ch == '0' && (pos + 1 < expr.length()) && (expr.charAt(pos + 1) == 'x' || expr.charAt(pos + 1) == 'X')) {
                        // Hexadecimal number
                        nextChar(); // consume '0'
                        nextChar(); // consume 'x' or 'X'
                        startPos = this.pos;
                        while ((ch >= '0' && ch <= '9') ||
                               (ch >= 'a' && ch <= 'f') ||
                               (ch >= 'A' && ch <= 'F')) {
                            nextChar();
                        }
                        String hexStr = expr.substring(startPos, this.pos);
                        x = Long.parseLong(hexStr, 16);
                    } else {
                        // Decimal or scientific notation
                        while ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'e' || ch == 'E') {
                            if (ch == 'e' || ch == 'E') {
                                nextChar(); 
                                if (ch == '+' || ch == '-') nextChar();
                            } else {
                                nextChar();
                            }
                        }
                        String numberPart = expr.substring(startPos, this.pos);
                        x = Double.parseDouble(numberPart);
                    }
                } else if (Character.isLetter(ch)) { // functions & constants
                    while (Character.isLetter(ch)) nextChar();
                    String func = expr.substring(startPos, this.pos);
                    switch (func) {
                        case "pi", "π" -> x = Math.PI;
                        case "e" -> x = Math.E;
                        default -> {
                            x = parseFactor();
                            switch (func) {
                                case "sqrt" -> x = Math.sqrt(x);
                                case "sin"  -> x = Math.sin(x);
                                case "cos"  -> x = Math.cos(x);
                                case "tan"  -> x = Math.tan(x);
                                case "log"  -> x = Math.log10(x);
                                case "ln"   -> x = Math.log(x);
                                case "abs"  -> x = Math.abs(x);
                                case "asin" -> x = Math.asin(x);
                                case "acos" -> x = Math.acos(x);
                                case "atan" -> x = Math.atan(x);
                                case "sinh" -> x = Math.sinh(x);
                                case "cosh" -> x = Math.cosh(x);
                                case "tanh" -> x = Math.tanh(x);
                                case "cbrt" -> x = Math.cbrt(x);
                                case "exp"  -> x = Math.exp(x);
                                default -> throw new RuntimeException("Unknown function: " + func);
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                // ✅ Implicit multiplication globally (numbers, constants, functions, parentheses)
                while ((ch >= '0' && ch <= '9') || ch == '.' || ch == '(' || Character.isLetter(ch)) {
                    x *= parseFactor();
                }

                return x;
            }
        }.parse();
    }

    // ========================= DISPLAY RESULTS =========================
    private static void displayResults(ArrayList<String[]> results) {
        // Calculate max width per column dynamically
        int inputWidth = "Input".length();
        int outputWidth = "Output".length();

        for (String[] row : results) {
            if (row[0].length() > inputWidth) inputWidth = row[0].length();
            if (row[1].length() > outputWidth) outputWidth = row[1].length();
        }

        // Add some padding for readability
        inputWidth += 2;
        outputWidth += 2;

        String formatString = String.format("%%-%ds %%-%ds", inputWidth, outputWidth);

        // Print header
        System.out.println("\n" + String.format(formatString, "Input", "Output"));

        // Print all results
        for (String[] row : results) {
            System.out.println(String.format(formatString, row[0], row[1]));
        }
        System.out.println();
    }

    public static void main(String[] args) {
        ArrayList<String[]> results = new ArrayList<>(); // store results as String arrays
        try (Scanner userInput = new Scanner(System.in)) {

            while (true) {
                System.out.print("Decimal: ");
                String input = userInput.nextLine().trim();

                if (input.equalsIgnoreCase("STOP")) {
                    break;
                }

                try {
                    double decimal = evaluate(input);
                    String binary = toBinary(decimal);

                    // Store as an array for easier dynamic formatting
                    results.add(new String[]{input, binary});
                    
                    System.out.println(">> Conversion stored. Enter next number or type 'STOP' to display results.\n");
                    
                } catch (Exception e) {
                    System.out.println(">> Invalid input: " + e.getMessage() + "\n");
                }
            }

            // Display all results only after STOP is entered
            if (!results.isEmpty()) {
                displayResults(results);
            } else {
                System.out.println("No conversions to display.");
            }
        }
    }
}
