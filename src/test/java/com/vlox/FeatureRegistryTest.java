package com.vlox;

import com.vlox.feature.Feature;
import com.vlox.feature.Features;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the settings registry itself; no game launch required. */
class FeatureRegistryTest {
	@Test
	void registryHasAtLeastSeventyFiveFeatures() {
		assertTrue(Features.ALL.size() >= 75, "expected 75+ features, found " + Features.ALL.size());
	}

	@Test
	void idsAreUnique() {
		Set<String> seen = new HashSet<>();
		for (Feature feature : Features.ALL) {
			assertTrue(seen.add(feature.id), "duplicate feature id: " + feature.id);
		}
		assertEquals(Features.ALL.size(), seen.size());
	}

	@Test
	void defaultsAreInsideTheirRange() {
		for (Feature feature : Features.ALL) {
			assertTrue(feature.def >= feature.min, feature.id + " default below minimum");
			assertTrue(feature.def <= feature.max, feature.id + " default above maximum");
		}
	}

	@Test
	void choicesAreDeclared() {
		for (Feature feature : Features.ALL) {
			if (feature.type == Feature.Type.CHOICE) {
				assertTrue(feature.choices != null && feature.choices.length > 1, feature.id + " needs choices");
			}
			assertFalse(feature.label.isBlank(), feature.id + " needs a label");
			assertFalse(feature.description.isBlank(), feature.id + " needs a description");
		}
	}

	@Test
	void everyCategoryHasFeatures() {
		for (com.vlox.feature.Category category : com.vlox.feature.Category.values()) {
			long count = Features.ALL.stream().filter(f -> f.category == category).count();
			assertTrue(count > 0, "category " + category + " has no features");
		}
	}
}
