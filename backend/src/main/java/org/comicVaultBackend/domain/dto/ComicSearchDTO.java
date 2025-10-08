package org.comicVaultBackend.domain.dto;


import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ComicSearchDTO implements Cloneable {
    protected String idGc;
    protected String idGcIssue;
    protected String title;
    protected String link;
    protected String image;
    protected String category;
    protected String year;
    protected String size;
    protected String downloadingStatus;
    @Setter
    protected String series;
    protected Long totalBytes;
    protected Long currentBytes;
    protected int totalComics;
    protected int currentComic;

    public void setCategory(String category) {
        this.category = getClosestCategory(category);
    }

    public void setDownloadingStatus(String downloadingStatus) {
        String lower = downloadingStatus.toLowerCase();
        if (lower.equals("downloaded") || lower.equals("downloading") || lower.equals("not-downloaded")) {
            this.downloadingStatus = lower;
        } else {
            throw new IllegalArgumentException("Invalid status: " + downloadingStatus);
        }
    }


    public void setLink(String link) {
        this.link = link;
        String[] segments = this.link.split("/");
        if (segments.length < 2) {
            throw new IllegalArgumentException("URL does not contain enough segments");
        }
        this.idGc = segments[segments.length - 1];
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String ToJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static public String getClosestCategory(String string) {
        String lower = string.toLowerCase();
        if (lower.contains("marvel")) {
            return "marvel";
        } else if (lower.contains("dc")) {
            return "dc";
        } else {
            return "other-comics";
        }
    }
}
