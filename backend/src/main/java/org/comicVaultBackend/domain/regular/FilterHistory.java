package org.comicVaultBackend.domain.regular;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comicVaultBackend.domain.entities.HistoryEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterHistory {
    private String comicTitle;
    private String dateStart;
    private String dateEnd;
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
        return true;
    }
}
