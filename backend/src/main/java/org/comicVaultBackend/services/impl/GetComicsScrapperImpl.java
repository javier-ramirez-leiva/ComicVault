package org.comicVaultBackend.services.impl;


import org.apache.commons.text.similarity.LevenshteinDistance;
import org.comicVaultBackend.domain.dto.*;
import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.exceptions.*;
import org.comicVaultBackend.services.ComicTitleParserService;
import org.comicVaultBackend.services.GetComicsScrapperService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
public class GetComicsScrapperImpl implements GetComicsScrapperService {

    private static final List<String> _articlesToAvoid = List.of("article1", "article2");
    private static final List<String> _categoriesToAvoid = List.of("News", "Sponsored");
    private static final Map<String, ComicSearchDTO> _listCacheComicSearches = new HashMap<String, ComicSearchDTO>();

    @Autowired
    private ComicTitleParserService comicTitleParserService;

    private String baseURL = "";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    @Override
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public List<ComicSearchDTO> getComics(String url, int page) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperGatewayPageException {
        List<ComicSearchDTO> listComics = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            try {
                Element postListPosts = doc.selectFirst(".post-list-posts");

                Elements posts = postListPosts.select("article");

                for (Element post : posts) {
                    try {
                        if (_articlesToAvoid.contains(post.id())) {
                            continue;
                        }

                        // Category
                        Element categoryTag = post.selectFirst(".category-fade");
                        String category = categoryTag.text().strip();
                        if (_categoriesToAvoid.contains(category)) {
                            continue;
                        }

                        // Title
                        Element titleTag = post.selectFirst(".post-title");
                        String title = titleTag.text().strip();

                        List<String> filteredKeywords = List.of("Weekly Pack", "GetComics");

                        if (filteredKeywords.stream().anyMatch(title::contains)) {
                            continue;
                        }

                        // Link
                        String link = titleTag.selectFirst("a").attr("href");

                        // Image
                        Element imgTag = post.selectFirst(".post-header-image img");
                        String imageUrl = imgTag.attr("src");

                        // Year and size
                        Element infoParagraph = post.selectFirst("p[style='text-align: center;']");
                        String year = "";
                        String size = "";

                        if (infoParagraph != null) {
                            String infoText = infoParagraph.text();

                            if (infoText.contains("Year :")) {
                                String[] yearSplit = infoText.split("Year : ");
                                if (yearSplit.length == 2) {
                                    year = yearSplit[1].split(" \\| ")[0].strip();
                                }
                            }

                            if (infoText.contains("Size :")) {
                                String[] sizeSplit = infoText.split("Size :");
                                if (sizeSplit.length == 2) {
                                    size = sizeSplit[1].strip();
                                }
                            }
                        }

                        ComicTitle comicTitle = comicTitleParserService.parseTitle(title);

                        ComicSearchDTO comic = ComicSearchDTO.builder()
                                .title(title)
                                .image(imageUrl)
                                .year(year)
                                .size(size)
                                .downloadingStatus("not-downloaded")
                                .series(comicTitle.getSeries())
                                .build();
                        comic.setLink(link);
                        comic.setCategory(category);

                        if (!_listCacheComicSearches.containsValue(comic.getIdGc())) {
                            _listCacheComicSearches.put(comic.getIdGc(), comic);
                        }

                        listComics.add(comic);
                    } catch (Exception e) {
                        //Do nothing, a comic is missing so what?
                    }

                }
            } catch (Exception e) {
                logger.error("Error scraping URL: " + url + ": " + e.getMessage());
                throw new ComicScrapperParsingException("Error scraping URL: " + url);
            }


        } catch (IOException e) {
            String message = "Error connecting to URL: " + url + ": " + e.getMessage();
            logger.error(message);
            if (page > 1) {
                throw new ComicScrapperGatewayPageException(message);
            } else {
                throw new ComicScrapperGatewayException(message);
            }
        }

        return listComics;
    }

    @Override
    public ComicSearchDetailsLinksDTO getComicDetails(String urlString) throws ComicScrapperParsingException, ComicScrapperGatewayException {
        try {
            Document doc = Jsoup.connect(urlString).get();

            try {
                URL url = new URL(urlString);
                String path = url.getPath();

                Elements list_aio_pulse = doc.select(".aio-pulse");

                if (!list_aio_pulse.isEmpty()) {
                    List<DownloadLinkDTO> downLoadLinkDTOS = new ArrayList<DownloadLinkDTO>();

                    for (Element aio_pulse : list_aio_pulse) {
                        Element a = aio_pulse.selectFirst("a");
                        String link = a.attr("href");
                        String platform = a.text();
                        if (!platform.equals("Read Online")) {
                            downLoadLinkDTOS.add(DownloadLinkDTO.builder().link(link).platform(platform).build());
                        }
                    }

                    Element post_content = doc.selectFirst(".post-contents");
                    Element paragraph = post_content.select("p").get(0);

                    String description = paragraph.text();

                    String title = "";
                    String size = "";
                    String year = "";

                    //Improve this parsing
                    for (int i = 1; i < post_content.select("p").size(); ++i) {
                        try {
                            Element otherInfo = post_content.select("p").get(i);
                            Elements infos = otherInfo.select("strong");
                            title = infos.get(0).text();
                            for (Element strong : infos) {
                                String header = strong.text().trim();
                                if (header.equalsIgnoreCase("Year :")) {
                                    String parentText = strong.parent().ownText();
                                    year = parentText.replaceAll("\\D+", "");
                                }
                                if (header.equalsIgnoreCase("Size :")) {
                                    size = strong.parent().ownText().trim();
                                }
                            }
                            break;
                        } catch (Exception e) {
                            if (i >= post_content.select("p").size() - 1) {
                                throw e;
                            }
                        }
                    }
                    List<TagDTO> tags = scrapeTags(doc);
                    TagDTO mainTag = getMainTag(tags, title);


                    DownloadIssueDTO downloadIssueDTO = DownloadIssueDTO.builder().links(downLoadLinkDTOS).description(description).build();

                    return ComicSearchDetailsLinksDTO.builder().
                            description(description).
                            downloadIssues(List.of(downloadIssueDTO)).
                            title(title).
                            year(year).
                            size(size).
                            category("").
                            tags(tags).
                            mainTag(mainTag).
                            build();
                } else {
                    Elements postContents = doc.select(".post-contents");

                    String description = postContents.get(0).selectFirst("p").text();

                    Element ul = postContents.get(0).selectFirst("ul");

                    //One of the titles
                    String title = null;

                    List<DownloadIssueDTO> downloadIssueDTOS = new ArrayList<DownloadIssueDTO>();
                    for (Element li : ul.select("li")) {
                        String descriptionIssue = li.text().split(":", 2)[0];
                        String issueTitle = _generateTitleIssue(descriptionIssue);
                        if (title == null) {
                            title = issueTitle;
                        }
                        String idGcIssue = _generateidGcIssue(issueTitle);
                        //Try all the strings we may find
                        for (Element strong : li.select("strong")) {
                            Elements as = strong.select("a");
                            if (!as.isEmpty()) {
                                List<DownloadLinkDTO> downLoadLinkDTOS = new ArrayList<DownloadLinkDTO>();
                                for (Element a : strong.select("a")) {
                                    String link = a.attr("href");
                                    String platform = a.text();
                                    downLoadLinkDTOS.add(DownloadLinkDTO.builder().link(link).platform(platform).build());
                                }
                                downloadIssueDTOS.add(DownloadIssueDTO.builder().links(downLoadLinkDTOS).description(descriptionIssue).idGcIssue(idGcIssue).title(issueTitle).build());
                                break;
                            }

                        }
                    }

                    List<TagDTO> tags = scrapeTags(doc);
                    TagDTO mainTag = getMainTag(tags, title);

                    return ComicSearchDetailsLinksDTO.builder().description(description).downloadIssues(downloadIssueDTOS).tags(tags).mainTag(mainTag).build();

                }
            } catch (Exception e) {
                logger.error("Error scraping URL: " + urlString + ": " + e.getMessage());
                throw new ComicScrapperParsingException("Error scraping URL: " + urlString);
            }
        } catch (IOException e) {
            logger.error("Error connecting to URL: " + urlString + ": " + e.getMessage());
            throw new ComicScrapperGatewayException("Error connecting to URL: " + urlString);
        }
    }

    @Override
    public ComicSearchDTO getCachedComicSearchByIdgc(String idgc) throws EntityNotFoundException {
        if (_listCacheComicSearches.containsKey(idgc)) {
            return _listCacheComicSearches.get(idgc);
        } else {
            throw new EntityNotFoundException(idgc, EntityNotFoundException.Entity.COMIC_SEARCH);
        }
    }

    private List<TagDTO> scrapeTags(Document doc) {
        List<TagDTO> tags = new ArrayList<>();
        Elements post_tags = doc.select(".post-tags");

        if (post_tags.size() == 1) {
            Elements tags_container = post_tags.get(0).select(".tags");
            if (tags_container.size() == 1) {
                Elements tags_element = tags_container.get(0).select("[rel=tag]");
                for (Element tag_element : tags_element) {
                    String name = tag_element.text();
                    if (name.contains("0-Day")) {
                        continue;
                    }
                    String link = tag_element.attr("href");
                    /*Get only the tag*/
                    link = link.replaceFirst(baseURL + "/tag/", "");
                    if (link.endsWith("/")) {
                        link = link.substring(0, link.length() - 1);
                    }
                    tags.add(TagDTO.builder().name(name).link(link).build());
                }
            }
        }
        return tags;
    }

    private TagDTO getMainTag(List<TagDTO> tags, String comicTitle) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        LevenshteinDistance distance = new LevenshteinDistance();

        return tags.stream()
                .min(Comparator.comparingInt(s -> distance.apply(comicTitle, s.getName()))
                )
                .orElse(null);
    }

    @Override
    public ComicSearchDetailsLinksDTO getComicDetailsFromIDgc(String idGc) throws ComicScrapperParsingException, ComicScrapperGatewayException, ComicScrapperUntreatedException {
        //If the comic has been already scrapped, priorize cached information
        if (_listCacheComicSearches.containsKey(idGc)) {
            ComicSearchDTO comicSearchDto = _listCacheComicSearches.get(idGc);
            ComicSearchDetailsLinksDTO comicSearchDetailsLinksDTO = getComicDetails(comicSearchDto.getLink());
            return ComicSearchDetailsLinksDTO.builder().
                    idGc(comicSearchDto.getIdGc()).
                    idGcIssue("").
                    title(comicSearchDto.getTitle()).
                    year(comicSearchDto.getYear()).
                    link(comicSearchDto.getLink()).
                    image(comicSearchDto.getImage()).
                    category(comicSearchDto.getCategory()).
                    size(comicSearchDto.getSize()).
                    series(comicSearchDto.getSeries().isBlank() ? "" : comicSearchDto.getSeries()).
                    downloadingStatus(comicSearchDto.getDownloadingStatus().isBlank() ? "not-downloaded" : comicSearchDto.getDownloadingStatus()).
                    currentBytes(comicSearchDto.getCurrentBytes() == null ? 0 : comicSearchDto.getCurrentBytes()).
                    totalBytes(comicSearchDto.getTotalBytes() == null ? 0 : comicSearchDto.getTotalBytes()).
                    description(comicSearchDetailsLinksDTO.getDescription()).
                    downloadIssues(comicSearchDetailsLinksDTO.getDownloadIssues()).
                    tags(comicSearchDetailsLinksDTO.getTags()).
                    mainTag(comicSearchDetailsLinksDTO.getMainTag()).
                    build();

        } else {
            throw new ComicScrapperUntreatedException("Error id has not been previously threaded: " + idGc);
        }

    }


    private static String _generateTitleIssue(String description) {
        // Remove everything inside parentheses
        return description.replaceAll("\\(.*?\\)", "");
    }

    private static String _generateidGcIssue(String title) {
        return title.replaceAll("[^a-zA-Z0-9]", "");
    }
}
