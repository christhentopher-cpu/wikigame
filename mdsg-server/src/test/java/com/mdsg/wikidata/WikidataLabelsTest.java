package com.mdsg.wikidata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class WikidataLabelsTest {

	@Test
	void rejectsQidUsedAsLabel() {
		assertThat(WikidataLabels.isDisplayable("Q708059", "Q708059")).isFalse();
		assertThat(WikidataLabels.isDisplayable("Q708059", "q708059")).isFalse();
	}

	@Test
	void acceptsHumanReadableLabel() {
		assertThat(WikidataLabels.isDisplayable("Q708059", "Chris Ellis")).isTrue();
	}

	@Test
	void rejectsBlankAndGenericQidPattern() {
		assertThat(WikidataLabels.isDisplayable("Q42", "")).isFalse();
		assertThat(WikidataLabels.isDisplayable("Q42", "   ")).isFalse();
		assertThat(WikidataLabels.isDisplayable("Q42", "Q999")).isFalse();
	}

	@Test
	void pickBestPrefersEnglishThenFallsBack() {
		Map<String, String> labels = Map.of(
				"de", "Chris Ellis",
				"fr", "Chris Ellis");

		assertThat(WikidataLabels.pickBest(labels, "Q708059")).isEqualTo("Chris Ellis");
	}

	@Test
	void pickBestUsesEnglishWhenPresent() {
		Map<String, String> labels = Map.of(
				"en", "The Matrix",
				"de", "Matrix");

		assertThat(WikidataLabels.pickBest(labels, "Q83495")).isEqualTo("The Matrix");
	}

}
