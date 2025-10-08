package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/")
public class JobController {
    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private JobService jobService;

    @Autowired
    private Mapper<JobEntity, JobDTO> jobMapper;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/jobs")
    public List<JobDTO> jobs(@RequestParam(name = "page", required = false, defaultValue = "1") int page) {

        List<JobEntity> jobs = jobService.listJobs(page - 1);
        List<JobDTO> listJobs = jobs.stream()
                .map(jobMapper::mapTo)
                .collect(Collectors.toList());
        //Collections.reverse(listLogs);
        return listJobs;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(path = "/job/search")
    public JobDTO getJobById(@RequestParam(name = "jobId", required = false) Long jobId) throws EntityNotFoundException {

        Optional<JobEntity> jobEntity;
        jobEntity = jobService.getJobById(jobId);

        if (jobEntity.isEmpty()) {
            throw new EntityNotFoundException(jobId.toString(), EntityNotFoundException.Entity.JOB);
        }

        return jobMapper.mapTo(jobEntity.get());
    }

}
