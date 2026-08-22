package org.comicVaultBackend.domain.regular;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.Date;

@Getter
@Setter
public class JobFilter {

    public enum JobSortAttribute {
        LATEST,
        STATUS,
        TYPE,
        DURATION,
        USERNAME
    }

    @Getter
    @Setter
    public static class Types {
        boolean download;
        boolean downloadList;
        boolean delete;
        boolean scanLib;
        boolean cleanLib;
        boolean deleteLib;
    }

    @Getter
    @Setter
    public static class Status {
        boolean onGoing;
        boolean completed;
        boolean error;
        boolean interrupted;
    }


    JobSortAttribute jobSortAttribute;
    boolean sortDescendingDirection;
    @Nullable
    String username;
    Types types;
    Status status;
    @Nullable
    Date dateStart;
    @Nullable
    Date dateEnd;
}
