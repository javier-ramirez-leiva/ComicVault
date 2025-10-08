package org.comicVaultBackend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.comicVaultBackend.domain.dto.ComicSearchDetailsDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.exceptions.SlackNotifyException;
import org.comicVaultBackend.services.SlackNotifyService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;


@Service
public class SlackNotifyServiceImpl implements SlackNotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotifyService.class);


    @Override
    public void sendNotification(String template, Path notificationFile, String slackWebHook, String comicVaultBaseUrl, ComicSearchDetailsDTO comicSearchDetailsDto, ComicEntity comicEntity) throws SlackNotifyException {


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode templateJSON;
        try {
            JsonNode rootNode = objectMapper.readTree(notificationFile.toFile());
            templateJSON = rootNode.get(template);
        } catch (IOException e) {
            String errorMessage = String.format("Error reading template from %s: %s", notificationFile.toString(), e.getMessage());
            logger.error(errorMessage);
            throw new SlackNotifyException(errorMessage, e);
        }
        String payloadStr = templateJSON.toString();

        HttpRequest.BodyPublisher bodyPublisher;

        JSONObject payload = new JSONObject(payloadStr);
        payloadStr = payloadStr.replace("$comicVaultBaseUrl", comicVaultBaseUrl);

        if (comicSearchDetailsDto != null) {
            payloadStr = payloadStr.replace("$comic.title", comicSearchDetailsDto.getTitle());
            payloadStr = payloadStr.replace("$comic.image", comicSearchDetailsDto.getImage());
            payloadStr = payloadStr.replace("$comic.year", comicSearchDetailsDto.getYear());
            payloadStr = payloadStr.replace("$comic.size", comicSearchDetailsDto.getSize());
            payloadStr = payloadStr.replace("$comic.link", comicSearchDetailsDto.getLink());
            payloadStr = payloadStr.replace("$comic.idGc", comicSearchDetailsDto.getIdGc());
            payloadStr = payloadStr.replace("$comic.description", comicSearchDetailsDto.getDescription());
            payloadStr = payloadStr.replace("$comic.url", comicSearchDetailsDto.getLink());
        }
        if (comicEntity != null) {
            payloadStr = payloadStr.replace("$comic.id", comicEntity.getId());
            //Override title with the one of the DB just in case
            payloadStr = payloadStr.replace("$comic.title", comicEntity.getTitle());
        }


        HttpRequest request;

        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(slackWebHook))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadStr))
                    .build();
        } catch (URISyntaxException e) {
            String errorMessage = String.format("Error creating HttpRequest: %s", e.getMessage());
            logger.error(errorMessage);
            throw new SlackNotifyException(errorMessage, e);
        }

        try {
            // Send the request and get the response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle the response
            if (response.statusCode() == 200) {
                logger.info("Slack notification sent successfully.");
            } else {
                String errorMessage = String.format("Failed to send Slack notification. Status code: %s", response.statusCode());
                logger.error(errorMessage);
                throw new SlackNotifyException(errorMessage);
            }
        } catch (IOException | InterruptedException e) {
            String errorMessage = String.format("Failed to send Slack notification: %s", e.getMessage());
            logger.error(errorMessage);
            throw new SlackNotifyException(errorMessage, e);
        }
    }
}
