package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.domain.regular.ComicTitle;
import org.comicVaultBackend.exceptions.ComicTitleParserServiceException;
import org.comicVaultBackend.services.ComicTitleParserService;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ComicTitleParserServiceImpl implements ComicTitleParserService {

    @Override
    public ComicTitle parseTitle(String title) {
        String series;
        int issueNumber = 0;
        int volumeNumber = 0;
        String fileName;

        title = title.replace("(TPB)", "").trim();
        String yearRegex = "\\s*\\(\\d{4}\\)\\s*";

        title = title.replaceAll(yearRegex, "").trim();


        // Search for the pattern in the title -> Series #Issue
        String patternSeries = "(.+?)\\s*#(\\d+)";
        // Search for the pattern in the title -> Series .Vol
        String patternVolume = "(.+?)\\s+Vol\\.\\s+(\\d+)";

        Pattern seriesPattern = Pattern.compile(patternSeries);
        Pattern volumePattern = Pattern.compile(patternVolume);
        Matcher matchSeries = seriesPattern.matcher(title);
        Matcher matchVolume = volumePattern.matcher(title);

        if (matchSeries.find()) {
            // Extract the true title (main title) and issue number if present
            series = matchSeries.group(1).trim();
            issueNumber = Integer.parseInt(matchSeries.group(2));
            fileName = _sanitizeFilename(String.format("%s #%d", series, issueNumber));
        } else if (matchVolume.find()) {
            series = matchVolume.group(1).trim();
            volumeNumber = Integer.parseInt(matchVolume.group(2));
            fileName = _sanitizeFilename(String.format("%s Vol.%d", series, volumeNumber));
        } else {
            series = title.replace(".cbr", "").replace(".cbz", "").trim();
            issueNumber = 1;
            fileName = _sanitizeFilename(String.format("%s #%d", series, issueNumber));
        }

        return ComicTitle.builder().series(series).issueNumber(issueNumber).volumeNumber(volumeNumber).fileName(fileName).build();
    }

    @Override
    public ComicTitle parseIssueTitle(String title) {
        //Dumb parsing, take the latest word considered to be an only number, that is the issue, the rest is the series
        int issueWordIndex = findLastNumberWord(title);
        String series;
        int issueNumber;
        if (issueWordIndex > -1) {
            String[] words = title.split("\\s+");
            issueNumber = Integer.parseInt(words[issueWordIndex]);
            series = String.join(" ", java.util.Arrays.copyOfRange(words, 0, issueNumber));
        } else {
            series = title;
            issueNumber = 1;
        }
        String fileName = _sanitizeFilename(String.format("%s #%d", series, issueNumber));

        return ComicTitle.builder().series(series).issueNumber(issueNumber).fileName(fileName).build();
    }

    private static int findLastNumberWord(String phrase) {
        String[] words = phrase.split("\\s+"); // Split on whitespace
        for (int i = words.length - 1; i >= 0; i--) {
            if (words[i].matches("\\d+")) { // Check if the word contains only digits
                return i;
            }
        }
        return -1;
    }

    @Override
    public String GetYearDetailsFromStandardTitle(String title) throws ComicTitleParserServiceException {
        // Regular expression pattern to match a year in the form of (2020)
        String yearPattern = "\\((\\d{4})\\)";

        // Compile the pattern
        Pattern pattern = Pattern.compile(yearPattern);

        Matcher matcher = pattern.matcher(title);

        // Check for matches and print them
        if (matcher.find()) {
            return matcher.group(1);

        } else {
            throw new ComicTitleParserServiceException("Standard title could not be parsed from: " + title);
        }

    }

    private String _sanitizeFilename(String fileName) {
        String forbiddenChars = "[/:*?\"<>|]";
        Pattern pattern = Pattern.compile(forbiddenChars);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.replaceAll("_");

    }
}
