package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.ExceptionDTO;
import org.comicVaultBackend.domain.entities.ExceptionEntity;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/")
public class ExceptionController {
    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private Mapper<ExceptionEntity, ExceptionDTO> exceptionMapper;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/exceptions")
    public List<ExceptionDTO> exceptions(@RequestParam(name = "page", required = false, defaultValue = "1") int page) {

        List<ExceptionEntity> exceptions = exceptionService.listExceptions(page - 1);
        return exceptions.stream()
                .map(exceptionMapper::mapTo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(path = "/exception/search")
    public ExceptionDTO getJobById(@RequestParam(name = "exceptionId", required = false) Long exceptionId) throws EntityNotFoundException {

        Optional<ExceptionEntity> exceptionEntity;
        exceptionEntity = exceptionService.getByEntityId(exceptionId);

        if (exceptionEntity.isEmpty()) {
            throw new EntityNotFoundException(exceptionId.toString(), EntityNotFoundException.Entity.EXCEPTION);
        }

        return exceptionMapper.mapTo(exceptionEntity.get());
    }


    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/exceptions/deleteAll")
    public void deleteAll() {
        exceptionService.deleteAll();
    }
}
