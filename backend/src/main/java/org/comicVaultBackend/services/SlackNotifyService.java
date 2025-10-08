package org.comicVaultBackend.services;

import org.comicVaultBackend.domain.dto.ComicSearchDetailsDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.exceptions.SlackNotifyException;

import java.nio.file.Path;

public interface SlackNotifyService {

    void sendNotification(String template, Path notificationFile, String slackWebHook, String comicVaultBaseUrl, ComicSearchDetailsDTO comicSearchDetailsDto, ComicEntity comicEntity) throws SlackNotifyException;
}
