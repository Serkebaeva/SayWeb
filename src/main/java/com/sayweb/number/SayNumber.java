package com.sayweb.number;

import org.springframework.stereotype.Service;

@Service
public class SayNumber {

  private static final String[] ones = {
    "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"
  };

  private static final String[] teens = {
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
  private static final String[] tens = {
    "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
  };

  public static String convertToWords(Long num) {
    if (num == null) {
      throw new IllegalArgumentException("Number cannot be null.");
    }
    if (num < 1 || num > 999999999999L) {
      throw new IllegalArgumentException(
          "Number out of range. Please enter a number between 1 and 999,999,999,999.");
    }

    if (num < 10) {
      return ones[num.intValue()];
    } else if (num < 20) {
      return teens[num.intValue() - 10];
    } else if (num < 100) {
      return tens[num.intValue() / 10] + (num % 10 == 0 ? "" : " " + ones[num.intValue() % 10]);
    } else if (num < 1000) {
      return ones[num.intValue() / 100]
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
