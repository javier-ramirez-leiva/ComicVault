package org.comicVaultBackend.config;

import org.apache.coyote.BadRequestException;
import org.comicVaultBackend.domain.dto.ResponseErrorDTO;
import org.comicVaultBackend.exceptions.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {
    private void printException(Exception ex) {
        System.out.println(ex.getMessage());
        System.out.println(Arrays.toString(ex.getStackTrace()));
    }

    //FORBIDDEN
    @ExceptionHandler(ForbiddenAuthException.class)
    public ResponseEntity<ResponseErrorDTO> handleForbiddenAuthException(ForbiddenAuthException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.FORBIDDEN;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "AUTH_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "RESOURCE_NOT_FOUND", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "RESOURCE_ENTITY_NOT_FOUND", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EntityWriteException.class)
    public ResponseEntity<ResponseErrorDTO> handleEntityWriteException(EntityWriteException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.CONFLICT;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "RESOURCE_ENTITY_NOT_FOUND", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseErrorDTO> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "USER_DETAILS_NOT_FOUND", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //BAD_REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "INVALID_ARGUMENT", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseErrorDTO> handleBadRequestException(BadRequestException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "BAD_AUTH_REQUEST", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ResponseErrorDTO> handleIllegalAccessException(IllegalAccessException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "ILLEGAL_ENTITY_ACCESS", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ConfigurationArgumentException.class)
    public ResponseEntity<ResponseErrorDTO> handleConfigurationException(ConfigurationArgumentException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "INVALID_CONFIGURATION_ARGUMENT", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //BAD_GATEWAY
    @ExceptionHandler(ComicScrapperGatewayException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicScrapperGatewayException(ComicScrapperGatewayException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "SCRAPER_GATEWAY_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ComicScrapperGatewayPageException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicScrapperGatewayPageException(ComicScrapperGatewayPageException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "SCRAPER_PAGE_GATEWAY_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(SlackNotifyException.class)
    public ResponseEntity<ResponseErrorDTO> handleSlackNotifyException(SlackNotifyException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "SLACK_GATEWAY_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //UNPROCESSABLE_ENTITY
    @ExceptionHandler(ComicScrapperParsingException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicScrapperParsingException(ComicScrapperParsingException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "SCRAPER_PARSING_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //CONFLICT
    @ExceptionHandler(EntityAlreadyExistringException.class)
    public ResponseEntity<ResponseErrorDTO> hnadleEntityAlreadyExistringException(EntityAlreadyExistringException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.CONFLICT;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "RESOURCE_ALREADY_EXISTING_FOUND", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AtLeastOneAdminException.class)
    public ResponseEntity<ResponseErrorDTO> handleAtLeastOneAdminException(AtLeastOneAdminException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.CONFLICT;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "ONE_ADMIN", status.value());
        return ResponseEntity.status(status).body(error);
    }

    //INTERNAL_SERVER_ERROR
    @ExceptionHandler(ConfigurationValueException.class)
    public ResponseEntity<ResponseErrorDTO> handleConfigurationValueException(ConfigurationValueException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "INVALID_CONFIGURATION_VALUE", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ComicScrapperUntreatedException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicScrapperUntreatedException(ComicScrapperUntreatedException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "SCRAPER_UNKNOWN_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EmptySeriesException.class)
    public ResponseEntity<ResponseErrorDTO> handleEmptySeriesException(EmptySeriesException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "EMPTY_SERIES", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(FileManagerException.class)
    public ResponseEntity<ResponseErrorDTO> handleFileManagerException(FileManagerException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "FILES_INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DownloadException.class)
    public ResponseEntity<ResponseErrorDTO> handleDownloadException(DownloadException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "DOWNLOAD_INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DownloadToDeviceException.class)
    public ResponseEntity<ResponseErrorDTO> handleDownloadToDeviceException(DownloadToDeviceException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "DOWNLOAD_DEVICE_INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ComicMetaDataException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicMetaDataException(ComicMetaDataException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "METADATA_INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ComicFileException.class)
    public ResponseEntity<ResponseErrorDTO> handleComicFileException(ComicFileException ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "COMIC_FILE_INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler({IOException.class, Exception.class})
    public ResponseEntity<ResponseErrorDTO> handleInternalServerException(Exception ex) {
        printException(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseErrorDTO error = new ResponseErrorDTO(ex.getMessage(), "INTERNAL_ERROR", status.value());
        return ResponseEntity.status(status).body(error);
    }
}
