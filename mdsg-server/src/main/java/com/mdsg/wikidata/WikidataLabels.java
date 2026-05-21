package com.mdsg.wikidata;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wikidata's {@code wikibase:label} service returns the raw QID when no English label exists.
 * These helpers pick a human-readable label and reject QID-shaped placeholders.
 */
public final class WikidataLabels {

	static final List<String> PREFERRED_LANGUAGES = List.of(
			"en", "mul", "de", "fr", "es", "it", "nl", "pt", "sv", "pl", "ja", "zh");

	private WikidataLabels() {
	}

	public static boolean isDisplayable(String qid, String label) {
		if (label == null) {
			return false;
		}
		String trimmed = label.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		if (trimmed.equalsIgnoreCase(qid)) {
			return false;
		}
		return !trimmed.toUpperCase(Locale.ROOT).matches("^Q\\d+$");
	}

	public static String pickBest(Map<String, String> labelsByLanguage, String qid) {
		if (labelsByLanguage == null || labelsByLanguage.isEmpty()) {
			return null;
		}
		for (String language : PREFERRED_LANGUAGES) {
			String candidate = labelsByLanguage.get(language);
			if (isDisplayable(qid, candidate)) {
				return candidate.trim();
			}
		}
		for (String candidate : labelsByLanguage.values()) {
			if (isDisplayable(qid, candidate)) {
				return candidate.trim();
			}
		}
		return null;
	}

}
