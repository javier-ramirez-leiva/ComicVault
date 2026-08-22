package org.comicVaultBackend.domain.regular;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.Date;

@Getter
@Setter
public class LogFilter {
    public enum LogSortAttribute {
        LATEST,
        SEVERITY,
        USERNAME
    }

    @Getter
    @Setter
    public static class Severities {
        boolean info;
        boolean error;
        boolean warning;
    }

    LogSortAttribute logSortAttribute;
    boolean sortDescendingDirection;
    @Nullable
    String username;
    Severities severities;
    @Nullable
    Date dateStart;
    @Nullable
    Date dateEnd;
    @Nullable
    String messageContains;
    @Nullable
    String detailsContains;
}
