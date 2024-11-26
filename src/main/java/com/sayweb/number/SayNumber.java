package com.sayweb.number;

public class SayNumber {
  private static final long MIN_NUMBER = 0; // below range starting from 0
  private static final long MAX_NUMBER = Long.MAX_VALUE; // upper range

  private static final String[] ONES = {
    "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
  };

  private static final String[] TEENS = {
    "ten",
    "eleven",
    "twelve",
    "thirteen",
    "fourteen",
    "fifteen",
    "sixteen",
    "seventeen",
    "eighteen",
    "nineteen"
  };

  private static final String[] TENS = {
    "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
  };

  // Private constructor to prevent instantiation
  private SayNumber() {}

  public static String convertToWords(Long num) {
    if (num == null) {
      throw new IllegalArgumentException("Number cannot be null.");
    }
    if (num < MIN_NUMBER || num > MAX_NUMBER) {
      throw new IllegalArgumentException(
          "Number out of range. Please enter a number between "
              + MIN_NUMBER
              + " and "
              + MAX_NUMBER
              + ".");
    }

    if (num < 10) {
      return ONES[num.intValue()];
    } else if (num < 20) {
      return TEENS[num.intValue() - 10];
    } else if (num < 100) {
      return TENS[num.intValue() / 10] + (num % 10 == 0 ? "" : " " + ONES[num.intValue() % 10]);
    } else if (num < 1000) {
      return ONES[num.intValue() / 100]
          + " hundred"
          + (num % 100 == 0 ? "" : " " + convertToWords(num % 100));
    } else if (num < 1000000) {
      return convertToWords(num / 1000)
          + " thousand"
          + (num % 1000 == 0 ? "" : " " + convertToWords(num % 1000));
    } else if (num < 1000000000) {
      return convertToWords(num / 1000000)
          + " million"
          + (num % 1000000 == 0 ? "" : " " + convertToWords(num % 1000000));
    } else {
      return convertToWords(num / 1000000000)
          + " billion"
          + (num % 1000000000 == 0 ? "" : " " + convertToWords(num % 1000000000));
    }
  }
}
