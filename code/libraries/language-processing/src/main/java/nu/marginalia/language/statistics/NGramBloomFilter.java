package nu.marginalia.language.statistics;

import ca.rmen.porterstemmer.PorterStemmer;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import nu.marginalia.LanguageModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class NGramBloomFilter {
    private final DenseBitMap bitMap;
    private static final PorterStemmer ps = new PorterStemmer();
    private static final HashFunction hasher = Hashing.murmur3_128(0);

    private static final Logger logger = LoggerFactory.getLogger(NGramBloomFilter.class);

    @Inject
    public NGramBloomFilter(LanguageModels lm) throws IOException {
        this(loadSafely(lm.ngramBloomFilter));
    }

    private static DenseBitMap loadSafely(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            return DenseBitMap.loadFromFile(path);
        }
        else {
            logger.warn("NGrams file missing " + path);
            return new DenseBitMap(1);
        }
    }

    public NGramBloomFilter(DenseBitMap bitMap) {
        this.bitMap = bitMap;
    }

    public boolean isKnownNGram(String word) {
        long bit = bitForWord(word, bitMap.cardinality);

        return bitMap.get(bit);
    }

//    public static void main(String... args) throws IOException {
//        var filter = convertFromDictionaryFile(new File(args[0]));
//        filter.bitMap.writeToFile(Path.of(args[1]));
//    }

    public static NGramBloomFilter load(Path file) throws IOException {
        return new NGramBloomFilter(DenseBitMap.loadFromFile(file));
    }

//    public static NGramBloomFilter convertFromDictionaryFile(File file) throws IOException {
//        DenseBitMap bitMap = new DenseBitMap(1024*1024*1024L);
//        AtomicInteger popCount = new AtomicInteger();
//        try (var f = new KeywordLexiconJournalFile(file)) {
//            f.loadFile(data -> {
//                long bit = bitForWord(new String(data), bitMap.cardinality);
//                if (!bitMap.set(bit))
//                    popCount.incrementAndGet();
//            });
//        }
//
//        System.out.println("popcount = " + popCount.get());
//        return new NGramBloomFilter(bitMap);
//    }

    private static final Pattern underscore = Pattern.compile("_");

    private static long bitForWord(String s, long n) {
        String[] parts = underscore.split(s);
        long hc = 0;
        for (String part : parts) {
            hc = hc * 31 + hasher.hashString(ps.stemWord(part), StandardCharsets.UTF_8).padToLong();
        }
        return (hc & 0x7FFF_FFFF_FFFF_FFFFL) % n;
    }

}
