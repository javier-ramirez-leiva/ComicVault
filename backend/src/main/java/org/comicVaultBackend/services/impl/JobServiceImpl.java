package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.repositories.JobRepository;
import org.comicVaultBackend.repositories.JobRepositoryPage;
import org.comicVaultBackend.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobServiceImpl implements JobService {
    // Executor with max 5 concurrent jobs
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private final JobRepository jobRepository;

    private final JobRepositoryPage jobRepositoryPage;

    @Autowired
    private Mapper<JobEntity, JobDTO> jobMapper;

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    public JobServiceImpl(JobRepository jobRepository, JobRepositoryPage jobRepositoryPage) {
        this.jobRepository = jobRepository;
        this.jobRepositoryPage = jobRepositoryPage;
    }

    @Override
    public JobEntity createJob(JobDTO job) {
        JobEntity jobEntity = jobMapper.mapFrom(job);
        jobEntity.setLogsIds(new ArrayList<>());
        save(jobEntity);
        return jobEntity;
    }

    @Override
    public void save(JobEntity jobEntity) {
        jobRepository.save(jobEntity);
    }

    @Override
    public void finishJob(JobEntity jobEntity, JobEntity.Status status) {
        jobEntity.setStatus(status);
        setDuration(jobEntity);
        save(jobEntity);
    }


    @Override
    public void setDuration(JobEntity jobEntity) {
        Date now = new Date(); // now


        // Convert Date -> LocalDateTime
        LocalDateTime start = LocalDateTime.ofInstant(jobEntity.getTimeStamp().toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());

        // Calculate duration
        Duration duration = Duration.between(start, end);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        jobEntity.setDuration(String.format("%d:%d:%d", hours, minutes, seconds));
    }

    @Override
    public List<JobEntity> listJobs(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<JobEntity> jobs = jobRepositoryPage.findAll(pageable);
        return jobs.getContent();
    }

    @Override
    public boolean areThereJobs(JobEntity.Type type, JobEntity.Status status) {
        return !jobRepository.findAllByTypeAndStatus(type, status).isEmpty();
    }

    @Override
    public Optional<JobEntity> getJobById(Long jobId) {
        List<JobEntity> listJobs = jobRepository.findAllByJobId(jobId);
        if (!listJobs.isEmpty()) {
            return Optional.of(listJobs.get(0));
        }
        return Optional.empty();
    }

    @Override
    public void cleanOnGoingJobs() {
        for (JobEntity jobEntity : jobRepository.findAllByStatus(JobEntity.Status.ON_GOING)) {
            jobEntity.setStatus(JobEntity.Status.INTERRUPTED);
            jobRepository.save(jobEntity);
        }
    }
}
