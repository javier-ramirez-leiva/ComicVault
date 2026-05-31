package org.comicVaultBackend.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.comicVaultBackend.domain.dto.ComicDTO;
import org.comicVaultBackend.domain.entities.ComicEntity;
import org.comicVaultBackend.domain.entities.ProgressEntity;
import org.comicVaultBackend.domain.entities.SeriesEntity;
import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.FilterComics;
import org.comicVaultBackend.exceptions.EntityWriteException;
import org.comicVaultBackend.repositories.ComicRepository;
import org.comicVaultBackend.services.ComicService;
import org.comicVaultBackend.services.ConfigurationService;
import org.comicVaultBackend.services.ProgressService;
import org.comicVaultBackend.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class ComicServiceImpl implements ComicService {
    private final ComicRepository comicRepository;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigurationService configurationService;

    private static final Logger logger = LoggerFactory.getLogger(ComicServiceImpl.class);

    public ComicServiceImpl(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }

    @Override
    public List<ComicEntity> listAll() {
        return StreamSupport
                .stream(
                        comicRepository.findAllByOrderByCreatedAtDesc().spliterator(),
                        false)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComicEntity> listAllWithFilter(FilterComics filterComics) {
        return StreamSupport
                .stream(
                        comicRepository.findAllByOrderByCreatedAtDesc().spliterator(),
                        false)
                .filter(comicEntity -> filterComic(comicEntity, filterComics))
                .sorted(buildComparator(filterComics))
                .collect(Collectors.toList());
    }

    @Override
    public List<ComicEntity> listOnGoing() {
        return StreamSupport.stream(comicRepository.findAll().spliterator(), false)
                .filter(comic -> comic.getPageStatus() > 0 && !comic.getReadStatus())
                .sorted(Comparator.comparing(ComicEntity::getProgressDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public ComicEntity createComic(ComicEntity comic) {
        Optional<ComicEntity> optionalComicEntity = getComicByID(comic.getId());
        logger.info("Comic created '" + comic.getId() + "': " + comic.getTitle());
        return optionalComicEntity.orElseGet(() -> comicRepository.save(comic));
    }

    @Override
    public Optional<ComicEntity> getComicByID(String comicID) {
        return comicRepository.findById(comicID);
    }

    @Override
    public Optional<ComicEntity> getcomicbyidGc(String comicidGc) {
        List<ComicEntity> listComics = comicRepository.findAllByIdGc(comicidGc);
        if (!listComics.isEmpty()) {
            return Optional.of(listComics.get(0));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteComicById(String comicID) {
        logger.info("Comic deleted '" + comicID);
        comicRepository.deleteById(comicID);
    }


    @Override
    public void updateNonNullProperties(ComicDTO comicDto, ComicEntity comicEntity) throws IllegalAccessException, EntityWriteException {
        Field[] fields = comicDto.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true); // Allow access to private fields
            Object value = field.get(comicDto);
            if (value != null) {
                if (field.getName().equals("readStatus")) {
                    boolean booleanValue = (Boolean) value;
                    setReadStatus(comicEntity, booleanValue);
                } else if (field.getName().equals("pageStatus")) {
                    int intValue = (int) value;
                    setPageStatus(comicEntity, intValue);
                } else if (field.getName().equals("issue")) {
                    int intValue = (int) value;
                    setIssueSafe(comicEntity, intValue);
                } else if (field.getName().equals("link")) {
                    String link = (String) value;
                    setLink(comicEntity, link);
                } else {
                    Field targetField = getField(comicEntity.getClass(), field.getName());
                    if (targetField != null) {
                        targetField.setAccessible(true);
                        targetField.set(comicEntity, value);
                    }
                }
            }
        }
        save(comicEntity);
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

    @Override
    public void save(ComicEntity comicEntity) {
        logger.info("Comic updated '" + comicEntity.getId() + "': " + comicEntity.getTitle());
        comicRepository.save(comicEntity);
    }

    @Override
    public void setPageStatus(ComicEntity comicEntity, int pageStatus) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<ProgressEntity> progressEntity = comicEntity.getProgress().stream()
                .filter(progress -> progress.getUser().getUsername().equals(username))
                .findFirst();
        if (progressEntity.isPresent()) {
            progressEntity.get().setPageStatus(pageStatus);
            progressService.save(progressEntity.get());
        } else {
            Optional<UserEntity> user = userService.findByUsername(username);
            if (user.isPresent()) {
                ProgressEntity newProgressEntity = ProgressEntity.builder().comic(comicEntity).pageStatus(pageStatus).readStatus(false).user(user.get()).build();
                progressService.save(newProgressEntity);
            }
        }
    }

    @Override
    public void setReadStatus(ComicEntity comicEntity, Boolean readStatus) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<ProgressEntity> progressEntity = comicEntity.getProgress().stream()
                .filter(progress -> progress.getUser().getUsername().equals(username))
                .findFirst();
        if (progressEntity.isPresent()) {
            progressEntity.get().setReadStatus(readStatus);
            progressService.save(progressEntity.get());
        } else {
            Optional<UserEntity> user = userService.findByUsername(username);
            if (user.isPresent()) {
                ProgressEntity newProgressEntity = ProgressEntity.builder().comic(comicEntity).pageStatus(0).readStatus(readStatus).user(user.get()).build();
                progressService.save(newProgressEntity);
            }
        }
    }

    @Override
    public int fixDoublePageQuery(ComicEntity comicEntity, int page) {
        int fixedDoublePage = -1;

        if (page == 0) {
            return page;
        }

        //1. Try to find the next closest doublePage, on-wards or back-wards
        List<Integer> doublePages = comicEntity.getDoublePages();
        if (doublePages.get(doublePages.size() - 1) > page) {
            //On-wards
            int closestPage = -1;
            for (int doublePage : doublePages) {
                if (doublePage > page) {
                    closestPage = doublePage;
                    break;
                }
            }
            if (closestPage >= 0) {
                // If there is a mismatch in eventy, page to return is -1
                boolean isDoubleEven = closestPage % 2 == 0;
                boolean isPageEven = page % 2 == 0;
                if (isDoubleEven == isPageEven) {
                    fixedDoublePage = page;
                } else {
                    fixedDoublePage = page - 1;
                }
            }
        } else {
            //Back-wards
            int closestPage = doublePages.get(doublePages.size() - 1);
            // If there is a match in eventy, page to return is -1
            boolean isDoubleEven = closestPage % 2 == 0;
            boolean isPageEven = page % 2 == 0;
            if (isDoubleEven == isPageEven) {
                fixedDoublePage = page - 1;
            } else {
                fixedDoublePage = page;
            }
        }

        return fixedDoublePage;
    }


    //Safely set issue number making sure it's available on the series
    private void setIssueSafe(ComicEntity comic, int issue) throws EntityWriteException {
        if (comic.getIssue() == issue) {
            return;
        }

        SeriesEntity seriesEntity = comic.getSeries();
        List<Integer> seriesIssues = seriesEntity.getComics().stream().map(ComicEntity::getIssue)
                .collect(Collectors.toList());

        if (seriesIssues.contains(issue)) {
            throw new EntityWriteException("Issue already set on another entity");
        }
        comic.setIssue(issue);

    }

    //
    private void setLink(ComicEntity comic, String link) {
        String newLink = link.replace(configurationService.getConfiguration().getGetComicsConfiguration().getBaseUrl(), "");
        comic.setLink(newLink);
    }

    private boolean filterComic(ComicEntity comic, FilterComics filter) {
        if (StringUtils.isNotBlank(filter.getComicTitle()) && !comic.getTitle().toLowerCase().contains(filter.getComicTitle().toLowerCase())) {
            return false;
        }

        try {
            int comicYear = Integer.parseInt(comic.getYear());
            if (comicYear > filter.getYearEnd() || comicYear < filter.getYearStart()) {
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Not a valid year integer for \"" + comic.getId() + "\": " + comic.getYear());
        }

        try {
            double comicSize = Double.parseDouble(comic.getSize().replace(" MB", ""));
            if (comicSize > filter.getSizeEnd() || comicSize < filter.getSizeStart()) {
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Not a valid size double for \"" + comic.getId() + "\": " + comic.getSize());
        }

        if (filter.getRemoveCategories().contains(comic.getCategory())) {
            return false;
        }

        for (String user : filter.getNotNotStartedUsers()) {
            if (comic.getNotStartedForUser(user)) {
                return false;
            }
        }

        for (String user : filter.getNotOnGoingUsers()) {
            if (comic.getOnGoingStatusForUser(user)) {
                return false;
            }
        }

        for (String user : filter.getNotReadUsers()) {
            if (comic.getReadStatusForUser(user)) {
                return false;
            }
        }

        return true;
    }

    private Comparator<ComicEntity> buildComparator(FilterComics filter) {
        Comparator<ComicEntity> comparator;
        switch (filter.getSortAttribute()) {
            case TITLE:
                comparator = Comparator.comparing(
                        ComicEntity::getTitle,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );
                break;
            case YEAR:
                comparator = Comparator.comparing(
                        ComicEntity::getYear,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );
                break;
            case SIZE:
                comparator = Comparator.comparing(
                        comic -> {
                            try {
                                if (comic.getSize() == null) return 0.0;
                                return Double.parseDouble(
                                        comic.getSize().replace(" MB", "").trim()
                                );
                            } catch (Exception e) {
                                return 0.0;
                            }
                        },
                        Comparator.nullsLast(Double::compareTo)
                );
                break;
            case CATEGORY:
                comparator = Comparator.comparing(
                        ComicEntity::getCategory,
                        String.CASE_INSENSITIVE_ORDER

                );
                break;
            case LATEST:
            default:
                comparator = Comparator.comparing(
                        ComicEntity::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
        }
        return filter.isSortDescendingDirection()
                ? comparator.reversed()
                : comparator;
    }
}
