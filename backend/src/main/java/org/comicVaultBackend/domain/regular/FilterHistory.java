package org.comicVaultBackend.domain.regular;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.entities.HistoryEntity;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterHistory {
    private String comicTitle;
    private Date dateStart;
    private Date dateEnd;
    private boolean readStatusOnGoing;
    private boolean readStatusRead;
    private boolean inLibraryYes;
    private boolean inLibraryNo;

    public boolean filterHistory(HistoryEntity historyEntity) {
        if (!historyEntity.getComicTitle().toLowerCase().contains(this.comicTitle.toLowerCase())) {
            return false;
        }
        if (historyEntity.getReadStatus() && !readStatusRead) {
            return false;
        }
        if (!historyEntity.getReadStatus() && !readStatusOnGoing) {
            return false;
        }
        if (historyEntity.getAlive() && !inLibraryYes) {
            return false;
        }
        if (!historyEntity.getAlive() && !inLibraryNo) {
            return false;
        }
        if (dateStart != null && historyEntity.getProgress_date().before(dateStart)) {
            return false;
        }

        if (dateEnd != null && historyEntity.getProgress_date().after(dateEnd)) {
            return false;
        }
        return true;
    }
}
