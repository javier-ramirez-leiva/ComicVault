package org.comicVaultBackend.services.impl;


import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.dto.SeriesDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.repositories.SeriesRepository;
import org.comicVaultBackend.services.SeriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SeriesServiceImpl implements SeriesService {

    private final SeriesRepository seriesRepository;

    private static final Logger logger = LoggerFactory.getLogger(SeriesServiceImpl.class);
    @Autowired
    private Mapper<ComicEntity, ComicDTO> comicMapper;

    public SeriesServiceImpl(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @Override
    public List<SeriesEntity> listAll() {
        return new ArrayList<>(seriesRepository.findAllByOrderByModifiedAtDesc());
    }

    @Override
    public List<SeriesEntity> listOnGoingSeries() {
        return StreamSupport.stream(seriesRepository.findAll().spliterator(), false)
                .filter(series -> series.getComics().stream().anyMatch(comic -> !comic.getReadStatus()))
                .filter(series -> series.getLastProgressDate() != null)
                .sorted((series1, series2) -> series2.getLastProgressDate().compareTo(series1.getLastProgressDate()))
                .collect(Collectors.toList());
    }


    @Override
    public SeriesEntity createSeries(SeriesEntity series) {
        logger.info("Series created '" + series.getSeriesID() + "': " + series.getTitle());
        return seriesRepository.save(series);
    }

    @Override
    public Optional<SeriesEntity> getSeriesByID(String seriesID) {
        return seriesRepository.findById(seriesID);
    }

    @Override
    @Transactional
    public void deleteSeriesById(String seriesID) {
        logger.info("Series deleted '" + seriesID);
        seriesRepository.deleteById(seriesID);
    }

    @Override
    public void save(SeriesEntity seriesEntity) {
        logger.info("Series updated '" + seriesEntity.getSeriesID() + "': " + seriesEntity.getTitle());
        seriesRepository.save(seriesEntity);
    }

    @Override
    public ComicEntity getCurrentComicForSeries(SeriesEntity seriesEntity) {
        for (ComicEntity comicEntity : seriesEntity.getComics()) {
            ComicDTO comicDto = comicMapper.mapTo(comicEntity);
            if (!comicDto.getReadStatus()) {
                return comicEntity;
            }
        }
        return null;
    }

    @Override
    public void updateNonNullProperties(SeriesDTO seriesDTO, SeriesEntity seriesEntity) throws IllegalAccessException {
        Field[] fields = seriesDTO.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // Allow access to private fields
            Object value = field.get(seriesDTO);
            if (value != null) {
                Field targetField = getField(seriesEntity.getClass(), field.getName());
                if (targetField != null) {
                    targetField.setAccessible(true);
                    targetField.set(seriesEntity, value);
                }
            }
        }
        seriesRepository.save(seriesEntity);
    }

    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }


}
