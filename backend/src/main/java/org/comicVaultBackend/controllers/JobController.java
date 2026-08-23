package org.comicVaultBackend.controllers;

import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.domain.regular.JobFilter;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/jobs")
    public List<JobDTO> jobs(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestBody(required = false) JobFilter filter) {

        List<JobEntity> jobs = jobService.filterJobs(page - 1, filter);
        List<JobDTO> listJobs = jobs.stream()
                .map(jobMapper::mapTo)
                .collect(Collectors.toList());
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
