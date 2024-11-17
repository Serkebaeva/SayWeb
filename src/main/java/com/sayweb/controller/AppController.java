package com.sayweb.controller;

import com.sayweb.number.SayNumber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
      if (number < 1 || number > 999999999999L) {
        throw new IllegalArgumentException(
            "Error: Please enter a number between 1 and 999,999,999,999.");
      }

      // Convert number to words
      String words = SayNumber.convertToWords(number);
      response.put("words", words);

      // Generate temporary audio file
      File tempFile = Files.createTempFile("audio_", ".wav").toFile();
      String[] command = {
        "C:/Program Files/eSpeak NG/espeak-ng.exe", "-w", tempFile.getAbsolutePath(), words
      };
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor(); // Wait for the audio file to be created

      // Save temp file path for deletion
      response.put("audioFileUrl", "/audio/" + tempFile.getName());
      return ResponseEntity.ok(response);

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      response.put("error", "An error occurred while generating the audio.");
      return ResponseEntity.status(500).body(response);
    } catch (IllegalArgumentException e) {
      response.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/audio/{fileName}")
  public ResponseEntity<InputStreamResource> getAudioFile(@PathVariable String fileName) {
    try {
      // Locate the temp file
      File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
      if (!tempFile.exists()) {
        return ResponseEntity.notFound().build();
      }

      // Serve the file
      InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "inline; filename=" + tempFile.getName());

      ResponseEntity<InputStreamResource> response =
          ResponseEntity.ok()
              .headers(headers)
              .contentLength(tempFile.length())
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .body(resource);

      // Delete the temp file after serving
      tempFile.delete();

      return response;

    } catch (IOException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
