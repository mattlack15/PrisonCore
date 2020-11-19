package com.soraxus.prisons.util.string;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Similarity {
    private static final StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());

    /**
     * Get how similar two strings are
     *
     * @param str1 First string
     * @param str2 Second string
     * @return Percentage similarity
     */
    public static int percentSimilarity(String str1, String str2) {
        return (int) (service.score(str1, str2) * 100);
    }

    /**
     * Get the {@code count} most similar
     *
     * @param strs
     * @param str
     * @param count
     * @return
     */
    public static List<String> getMostSimilar(List<String> strs, String str, int count) {
        return strs.stream()
                .sorted(Comparator.comparingInt(s -> percentSimilarity(s, str)))
                .limit(count)
                .collect(Collectors.toList());
    }
}
