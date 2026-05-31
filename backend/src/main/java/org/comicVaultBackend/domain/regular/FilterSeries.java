package org.comicVaultBackend.domain.regular;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterSeries {

    public enum SortAttribute {
        LATEST,
        TITLE,
        YEAR,
        CATEGORY,
        ISSUES
    }

    private String comicTitle;
    private List<String> removeCategories;
    private int yearStart;
    private int yearEnd;
    private int issuesStart;
    private long issuesEnd;

    private SortAttribute sortAttribute;
    private boolean sortDescendingDirection;
}
