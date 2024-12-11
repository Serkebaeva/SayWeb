package com.sayweb.number;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SayNumber {
  public static final long MIN_NUMBER = 0; // The below range starting from 0
  public static final long MAX_NUMBER = Long.MAX_VALUE; // The upper range numbers

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

  // Cache with a maximum size of 1000
  private static final int CACHE_MAX_SIZE = 1000;

  private static final Map<Long, String> NUMBER_TO_WORDS_CACHE =
      Collections.synchronizedMap(
          new LinkedHashMap<Long, String>(CACHE_MAX_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
              return size()
                  > CACHE_MAX_SIZE; // Remove the eldest entry if the cache exceeds the maximum size
            }
          });

  // Private constructor to prevent instantiation
  private SayNumber() {}

  public static String convertToWords(Long num) {

    if (num == null) {
      throw new IllegalArgumentException("Number cannot be null.");
    }

    if (num == MIN_NUMBER) {
      return "zero";
    }

    if (num < MIN_NUMBER || num > MAX_NUMBER) {
      throw new IllegalArgumentException(
          "Number out of range. Please enter a number between "
              + MIN_NUMBER
              + " and "
              + MAX_NUMBER
              + ".");
    }

    // Check if the number is already cached
    synchronized (NUMBER_TO_WORDS_CACHE) {
      if (NUMBER_TO_WORDS_CACHE.containsKey(num)) {
        return NUMBER_TO_WORDS_CACHE.get(num); // Return the cashed result
      }
    }

    String result = "";

    if (num < 10) {
      result = ONES[num.intValue()];
    } else if (num < 20) {
      result = TEENS[num.intValue() - 10];
    } else if (num < 100) {
      result = TENS[num.intValue() / 10] + (num % 10 == 0 ? "" : " " + ONES[num.intValue() % 10]);
    } else if (num < 1000) {
      result =
          ONES[num.intValue() / 100]
              + " hundred"
              + (num % 100 == 0 ? "" : " " + convertToWords(num % 100));
    } else if (num < 1000000) {
      result =
          convertToWords(num / 1000)
              + " thousand"
              + (num % 1000 == 0 ? "" : " " + convertToWords(num % 1000));
    } else if (num < 1000000000) {
      result =
          convertToWords(num / 1000000)
              + " million"
              + (num % 1000000 == 0 ? "" : " " + convertToWords(num % 1000000));
    } else {
      result =
          convertToWords(num / 1000000000)
              + " billion"
              + (num % 1000000000 == 0 ? "" : " " + convertToWords(num % 1000000000));
    }

    // Cashe the result
    synchronized (NUMBER_TO_WORDS_CACHE) {
      NUMBER_TO_WORDS_CACHE.put(num, result);
    }
    return result;
  }

  // Generate the filename
  public static String getFileNameForNumber(Long num) {
    String words = convertToWords(num);
    return words.hashCode() + ".wav"; // Generate the filename based on hash
  }

  // Method to retrieve a cached value
  public static String getCachedValue(Long num) {
    return NUMBER_TO_WORDS_CACHE.get(num); // Return the cached value
  }

  // Method to check if value is cached
  public static boolean isCached(Long num) {
    return NUMBER_TO_WORDS_CACHE.containsKey(num); // Return true if value is cached
  }

  public static synchronized Set<Long> getAllCachedNumbers() {
    return new HashSet<>(NUMBER_TO_WORDS_CACHE.keySet());
  }
}
