package nu.marginalia.converting.processor.logic;

import gnu.trove.list.array.TLongArrayList;
import nu.marginalia.model.crawl.UrlIndexingState;
import nu.marginalia.converting.model.ProcessedDocument;
import nu.marginalia.lsh.EasyLSH;

/** Deduplicates documents based on their LSH
 *
 * @see EasyLSH
 */
public class LshDocumentDeduplicator implements AutoCloseable {

    private final TLongArrayList hashCodes = new TLongArrayList(1000);
    private static final int DISTANCE_THRESHOLD = 2;

    public void markIfDuplicate(ProcessedDocument document) {
        if (!document.isProcessedFully()) {
            return;
        }

        if (document.words.size() < 100) {
            return;
        }

        long hashCode = document.details.hashCode;

        for (int i = 0; i < hashCodes.size(); i++) {
            if (EasyLSH.hammingDistance(hashCode, hashCodes.get(i)) < DISTANCE_THRESHOLD) {
                document.state = UrlIndexingState.DISQUALIFIED;
                document.stateReason = "Duplicate";
                return;
            }
        }

        hashCodes.add(hashCode);
    }

    @Override
    public void close() throws Exception {
        hashCodes.clear(1);
    }
}
