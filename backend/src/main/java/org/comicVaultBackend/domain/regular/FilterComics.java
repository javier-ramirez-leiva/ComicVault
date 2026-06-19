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
public class FilterComics {

    public enum SortAttribute {
        LATEST,
        TITLE,
        YEAR,
        CATEGORY,
        SIZE
    }

    private String comicTitle;
    private List<String> notNotStartedUsers;
    private List<String> notOnGoingUsers;
    private List<String> notReadUsers;
    private List<String> removeCategories;
    private int yearStart;
    private int yearEnd;
    private long sizeStart;
    private long sizeEnd;

    private SortAttribute sortAttribute;
    private boolean sortDescendingDirection;
}
