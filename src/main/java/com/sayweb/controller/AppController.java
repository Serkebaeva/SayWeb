package com.sayweb.controller;

import com.sayweb.number.SayNumber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class AppController {
  private static final long MIN_NUMBER = 0; // The below range starting from 0
  private static final long MAX_NUMBER = Long.MAX_VALUE; // The upper range numbers
  private static final String AUDIO_DIR =
      "/app/static/audio"; // An audio directory path to Docker container's static directory

  @GetMapping("/convert-to-words-and-audio")
  public ResponseEntity<Map<String, String>> convertToWordsAndAudio(
      @RequestParam("number") String numberInput) {
    Map<String, String> response = new HashMap<>();

    try {
      // Check if the input is a valid number
      Long number = null;
      try {
        number = Long.parseLong(numberInput);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Error: Invalid number format." + "\nPlease Enter only numbers!");
      }

      // Validate the range of the number
      if (number < MIN_NUMBER || number > MAX_NUMBER) {
        throw new IllegalArgumentException(
            "Error: Please enter a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ".");
      }

      // Convert a number to words
      String words = SayNumber.convertToWords(number);
      response.put("words", words);

      // Generate a predictable filename
      String fileName = SayNumber.getFileNameForNumber(number);
      response.put("audioFileUrl", "/audio/" + fileName);

      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      response.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      e.printStackTrace();
      response.put("error", "An unexpected error occurred.");
      return ResponseEntity.status(500).body(response);
    }
  }

  @GetMapping("/audio/{fileName}")
  public ResponseEntity<InputStreamResource> getAudioFile(@PathVariable String fileName) {
    try {
      // Ensure the directory exists
      File audioDir = new File(AUDIO_DIR);
      if (!audioDir.exists()) {
        audioDir.mkdirs();
      }

      // Check and cleanup old files
      cleanupOldFiles(audioDir);

      // Generate a file path
      File audioFile = new File(audioDir, fileName);
      if (!audioFile.exists()) {
        // Extract a number from file name if possible
        Long number = extractNumberFromFileName(fileName);

        // Convert the number to words
        String words = SayNumber.convertToWords(number);

        // Generate an audiofile
        String[] command = {"espeak-ng", "-w", audioFile.getAbsolutePath(), words};
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor(); // Wait for the audio file to be created
      }

      // Verify the file exists after attempting to create it
      if (!audioFile.exists()) {
        throw new IOException("Failed to generate the audio file.");
      }

      // Serve the file
      InputStreamResource resource = new InputStreamResource(new FileInputStream(audioFile));
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "inline; filename=" + audioFile.getName());

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(audioFile.length())
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(resource);
    } catch (IOException | InterruptedException e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  private void cleanupOldFiles(File audioDir) {
    File[] files = audioDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile() && isOlderThanOneDay(file)) {
          file.delete();
        }
      }
    }
  }

  private boolean isOlderThanOneDay(File file) {
    long oneDayMillis = 24 * 60 * 60 * 1000; // 1 Day in milliseconds
    return (new Date().getTime() - file.lastModified()) > oneDayMillis;
  }

  private Long extractNumberFromFileName(String fileName) {
    try {
      // Convert hashCode back to the number, by reversing the process.
      // We don't really reverse hashCode (it’s not a reversible operation), so for now, we'll
      // assume that the number generating this filename is unique and identifiable.
      String hashStr = fileName.replace(".wav", "");
      int hashCode = Integer.parseInt(hashStr);

      // Search for corresponding number based on hashCode (an imperfect workaround)
      for (Map.Entry<Long, String> entry : SayNumber.NUMBER_TO_WORDS_CACHE.entrySet()) {
        if (entry.getValue().hashCode() == hashCode) {
          return entry.getKey(); // Return the number corresponding to the hash code
        }
      }
      throw new IllegalArgumentException(
          "Invalid file name format or hash code mismatch: " + fileName);

    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid file name format: " + fileName);
    }
  }
}
