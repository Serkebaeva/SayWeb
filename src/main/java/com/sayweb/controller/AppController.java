package com.sayweb.controller;

import com.sayweb.number.SayNumber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

  // Update audio directory path to the Docker container's static directory
  private static final String AUDIO_DIR = "/app/static/audio";

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

      // Ensure the audio directory exists inside the container
      File audioDir = new File(AUDIO_DIR);
      if (!audioDir.exists()) {
        audioDir.mkdirs(); // Ensure the directory exists
      }

      // Generate temporary audio file in the static/audio directory
      File tempFile = new File(audioDir, "audio_" + System.currentTimeMillis() + ".wav");
      String[] command = {"espeak-ng", "-w", tempFile.getAbsolutePath(), words};
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor(); // Wait for the audio file to be created

      // Return the audio file URL to the client
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
      // Use the dedicated audio directory in the container
      File audioFile = new File(AUDIO_DIR, fileName);
      if (!audioFile.exists()) {
        return ResponseEntity.notFound().build();
      }

      // Serve the file
      InputStreamResource resource = new InputStreamResource(new FileInputStream(audioFile));
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "inline; filename=" + audioFile.getName());

      ResponseEntity<InputStreamResource> response =
          ResponseEntity.ok()
              .headers(headers)
              .contentLength(audioFile.length())
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .body(resource);

      // Schedule the file for deletion after a longer delay
      new Thread(
              () -> {
                try {
                  // Wait for 5 minutes to ensure the download completes
                  Thread.sleep(300000); // 300 seconds delay (5 minutes)
                  audioFile.delete();
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              })
          .start();

      return response;

    } catch (IOException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
