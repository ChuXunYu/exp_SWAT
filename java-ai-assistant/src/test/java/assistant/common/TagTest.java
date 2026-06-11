package assistant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagTest {
    @Test
    void createsTagWithNormalizedLowercaseValue() {
        Tag tag = new Tag("Java");

        assertEquals("java", tag.value());
        assertEquals("java", tag.displayName());
    }

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        Tag tag = new Tag("  java  ");

        assertEquals("java", tag.value());
    }

    @Test
    void trimsLeadingAndTrailingUnicodeWhitespace() {
        Tag tag = new Tag("\u2003java\u2003");

        assertEquals("java", tag.value());
    }

    @Test
    void normalizesUppercaseAndMixedCaseForEquality() {
        Tag uppercase = new Tag("JAVA");
        Tag mixedCase = new Tag("Java");
        Tag lowercase = new Tag("java");

        assertEquals(lowercase, uppercase);
        assertEquals(lowercase, mixedCase);
        assertEquals(lowercase.hashCode(), uppercase.hashCode());
        assertEquals(lowercase.hashCode(), mixedCase.hashCode());
    }

    @Test
    void lowercasesWithRootLocaleWhenDefaultLocaleIsTurkish() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertEquals("ai", Tag.of("AI").value());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void preservesInternalWhitespace() {
        Tag tag = new Tag(" Review Notes ");

        assertEquals("review notes", tag.value());
    }

    @Test
    void factoryCreatesNormalizedTag() {
        Tag tag = Tag.of(" Study ");

        assertEquals("study", tag.value());
    }

    @Test
    void rejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
        assertThrows(NullPointerException.class, () -> Tag.of(null));
    }

    @Test
    void rejectsEmptyValue() {
        assertThrows(IllegalArgumentException.class, () -> new Tag(""));
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new Tag("   "));
    }

    @Test
    void rejectsUnicodeBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new Tag("\u2003\u2003"));
    }

    @Test
    void factoryRejectsEmptyAndBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> Tag.of(""));
        assertThrows(IllegalArgumentException.class, () -> Tag.of("   "));
    }

    @Test
    void normalizedTagsCanBeUsedAsSetKeys() {
        Set<Tag> tags = new HashSet<>();

        tags.add(Tag.of("Java"));
        tags.add(Tag.of(" java "));

        assertEquals(1, tags.size());
    }

    @Test
    void normalizedTagsCanBeUsedAsMapKeysForDistribution() {
        Map<Tag, Integer> countsByTag = new HashMap<>();
        countsByTag.put(Tag.of("AI"), 2);

        assertEquals(2, countsByTag.get(Tag.of(" ai ")));
    }

    @Test
    void toStringUsesRecordComponentNameAndNormalizedValue() {
        assertEquals("Tag[value=java]", new Tag("Java").toString());
    }
}
