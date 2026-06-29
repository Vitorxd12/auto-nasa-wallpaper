package com.example.nasa_apod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

@Service
public class ApodService implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(ApodService.class.getName());

    @Value("${nasa.api.key:DEMO_KEY}")
    private String apiKey;

    @Value("${nasa.download.dir:nasa-images}")
    private String downloadDir;

    private final RestTemplate restTemplate;

    public ApodService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void run(String... args) {
        fetchApodAndSave();
    }

    public void fetchApodAndSave() {
        logger.info("Fetching APOD from NASA API...");
        try {
            String urlStr = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;
            ApodResponse response = restTemplate.getForObject(urlStr, ApodResponse.class);

            if (response != null && "image".equals(response.getMedia_type())) {
                String imageUrl = response.getHdurl() != null ? response.getHdurl() : response.getUrl();
                logger.info("APOD is an image. URL: " + imageUrl);
                
                Path dir = Paths.get(downloadDir);
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }

                // Make sure the image has the correct extension based on URL or default to jpg
                String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
                if (extension.contains("?")) {
                    extension = extension.substring(0, extension.indexOf("?"));
                }
                if (extension.length() > 5 || !extension.startsWith(".")) {
                    extension = ".jpg";
                }

                String fileName = response.getDate() + extension;
                Path filePath = dir.resolve(fileName);

                try (InputStream in = new URL(imageUrl).openStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Image saved to: " + filePath.toAbsolutePath());
                }

                String wallpaperFileName = response.getDate() + "_wallpaper.jpg";
                Path wallpaperPath = dir.resolve(wallpaperFileName);

                try {
                    logger.info("Processing wallpaper using ImageMagick...");
                    ProcessBuilder pb = new ProcessBuilder(
                            "convert",
                            filePath.toAbsolutePath().toString(),
                            "-gravity", "North",
                            "-crop", "16:9",
                            "+repage",
                            "-gravity", "NorthWest",
                            "-fill", "rgba(255,255,255,0.4)",
                            "-pointsize", "64",
                            "-annotate", "+75+75", response.getTitle(),
                            wallpaperPath.toAbsolutePath().toString()
                    );
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        logger.info("Wallpaper created successfully at: " + wallpaperPath.toAbsolutePath());
                        setUbuntuWallpaper(wallpaperPath.toAbsolutePath().toString());
                    } else {
                        logger.warning("ImageMagick convert failed with exit code " + exitCode);
                        String output = new String(process.getInputStream().readAllBytes());
                        logger.warning("ImageMagick output: " + output);
                    }
                } catch (Exception e) {
                    logger.severe("Failed to run ImageMagick: " + e.getMessage());
                }

                String textFileName = response.getDate() + ".txt";
                Path textFilePath = dir.resolve(textFileName);
                String textContent = "Title: " + response.getTitle() + "\n" +
                                     "Date: " + response.getDate() + "\n\n" +
                                     response.getExplanation();
                Files.writeString(textFilePath, textContent);
                logger.info("Text saved to: " + textFilePath.toAbsolutePath());

            } else {
                logger.info("Today's APOD is not an image (maybe a video). Skipping download.");
            }
        } catch (RestClientException e) {
            logger.warning("NASA API is currently unavailable or returned an error: " + e.getMessage());
            logger.warning("Please try running the command again later.");
        } catch (Exception e) {
            logger.severe("Unexpected error fetching APOD: " + e.getMessage());
        }
    }

    private void setUbuntuWallpaper(String absoluteFilePath) {
        try {
            logger.info("Setting Ubuntu wallpaper...");
            String fileUri = "file://" + absoluteFilePath;
            
            // Set for light theme
            ProcessBuilder pbLight = new ProcessBuilder("gsettings", "set", "org.gnome.desktop.background", "picture-uri", fileUri);
            pbLight.start().waitFor();
            
            // Set for dark theme
            ProcessBuilder pbDark = new ProcessBuilder("gsettings", "set", "org.gnome.desktop.background", "picture-uri-dark", fileUri);
            pbDark.start().waitFor();

            logger.info("Ubuntu wallpaper updated successfully.");
        } catch (Exception e) {
            logger.warning("Could not set Ubuntu wallpaper: " + e.getMessage());
        }
    }
}
