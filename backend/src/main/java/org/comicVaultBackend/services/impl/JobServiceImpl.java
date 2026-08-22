package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.dto.JobDTO;
import org.comicVaultBackend.domain.entities.JobEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.repositories.JobRepository;
import org.comicVaultBackend.repositories.JobRepositoryPage;
import org.comicVaultBackend.services.JobService;
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

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRepositoryPage jobRepositoryPage;

    @Autowired
    private Mapper<JobEntity, JobDTO> jobMapper;


    @Override
    @CVWriteTransaction
    public JobEntity createJob(JobDTO job) {
        JobEntity jobEntity = jobMapper.mapFrom(job);
        jobEntity.setLogsIds(new ArrayList<>());
        save(jobEntity);
        return jobEntity;
    }

    @Override
    @CVWriteTransaction
    public void save(JobEntity jobEntity) {
        jobRepository.save(jobEntity);
    }

    @Override
    @CVWriteTransaction
    public void finishJob(JobEntity jobEntity, JobEntity.Status status) {
        jobEntity.setStatus(status);
        setDuration(jobEntity);
        save(jobEntity);
    }


    @Override
    @CVWriteTransaction
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
    @CVReadTransaction
    public List<JobEntity> listJobs(int page) {
        Pageable pageable = PageRequest.of(page, 25, Sort.by(Sort.Direction.DESC, "timeStamp"));
        Page<JobEntity> jobs = jobRepositoryPage.findAll(pageable);
        return jobs.getContent();
    }

    @Override
    @CVReadTransaction
    public boolean areThereJobs(JobEntity.Type type, JobEntity.Status status) {
        return !jobRepository.findAllByTypeAndStatus(type, status).isEmpty();
    }

    @Override
    @CVReadTransaction
    public Optional<JobEntity> getJobById(Long jobId) {
        List<JobEntity> listJobs = jobRepository.findAllByJobId(jobId);
        if (!listJobs.isEmpty()) {
            return Optional.of(listJobs.get(0));
        }
        return Optional.empty();
    }

    @Override
    @CVWriteTransaction
    public void cleanOnGoingJobs() {
        for (JobEntity jobEntity : jobRepository.findAllByStatus(JobEntity.Status.ON_GOING)) {
            jobEntity.setStatus(JobEntity.Status.INTERRUPTED);
            jobRepository.save(jobEntity);
        }
    }
}
