package minebayd1.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static minebayd1.test.DataProvider.LG_STREAM;
import static minebayd1.test.DataProvider.adSupplier;
import static minebayd1.test.DataProvider.stringSupplier;
import static minebayd1.test.DataProvider.randInt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebayd1.Category;
import minebayd1.ClassifiedAd;
import minebayd1.User;

/**
 * Test class for Post.
 *
 * Un post d'un utilisateur du réseau social Microdon, caractérisé par son
 * texte, sa date de création et l'ensemble des utilisateurs l'ayant "liké".
 * 
 * La seule caractéristque modifiable de cette classe est l'ensemble des
 * utilisteurs ayant "liké" ce Post.
 */
public class TestClassifiedAd {

	public static Stream<ClassifiedAd> adProvider() {
		return Stream.generate(() -> adSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> CatAndNameAndPrice() {
		return Stream.generate(
				() -> Arguments.of(DataProvider.enumSupplier(Category.class), "À vendre", DataProvider.randInt(50) + 1))
				.limit(LG_STREAM);
	}

	public static Stream<String> stringProvider() {
		return Stream.generate(() -> stringSupplier()).limit(LG_STREAM);
	}

	public static Stream<Arguments> adAndadProvider() {
		return Stream.generate(() -> Arguments.of(adSupplier(), adSupplier())).limit(LG_STREAM);
	}

	private Instant dateCreation;
	private Category cat;
	private String text;
	private int price;

	private void saveState(ClassifiedAd self) {
		// Put here the code to save the state of self:
		this.dateCreation = self.getDate();
		this.text = self.getDescription();
		this.cat = self.getCategory();
		this.price = self.getPrice();
	}

	private void assertPurity(ClassifiedAd self) {
		// Put here the code to check purity for self:
		assertEquals(dateCreation, self.getDate());
		assertEquals(text, self.getDescription());
		assertEquals(cat, self.getCategory());
		assertEquals(price, self.getPrice());
	}

	public void assertInvariant(ClassifiedAd self) {
		// Put here the code to check the invariant:
		// @invariant getDescription() != null;
		assertNotNull(self.getDescription());
		// @invariant !getDescription().isBlank();
		assertFalse(self.getDescription().isBlank());
		// @invariant getCategory() != null;
		assertNotNull(self.getCategory());
		// @invariant getPrice() > 0;
		assertTrue(self.getPrice() > 0);
		// @invariant getDate() != null;
		assertNotNull(self.getDate());
	}

	/**
	 * Test method for constructor ClassifiedAd
	 *
	 * Initialise une nouvelle annonce. La date de cette nouvelle annonce est la
	 * date courante au moment de l'exécution de ce constructeur.
	 */
	@ParameterizedTest
	@MethodSource("CatAndNameAndPrice")
	public void testClassifiedAd(Category cat, String desc, int price) {

		// Pré-conditions:
		// @requires cat != null;
		assumeTrue(cat != null);
		// @requires desc != null;
		assumeTrue(desc != null);
		// @requires !desc.isBlank();
		assumeTrue(!desc.isBlank());
		// @requires price > 0;
		assumeTrue(price > 0);

		// Oldies:
		// old in:@ensures \old(Instant.now()).isBefore(getDate());
		Instant oldNow = Instant.now();

		// Exécution:
		ClassifiedAd result = new ClassifiedAd(cat, desc, price);

		// Post-conditions:
		// @ensures getCategory().equals(cat);
		assertEquals(cat, result.getCategory());
		// @ensures getDescription().equals(desc);
		assertEquals(desc, result.getDescription());
		// @ensures getPrice() == price;
		assertEquals(price, result.getPrice());
		// @ensures getDate() != null;
		assertNotNull(result.getDate());
		// @ensures \old(Instant.now()).isBefore(getDate());
		assertTrue(oldNow.isBefore(result.getDate()));
		// @ensures getDate().isBefore(Instant.now());
		assertTrue(result.getDate().isBefore(Instant.now()));

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getDate
	 *
	 * Renvoie la date de création de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetDate(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getDescription
	 *
	 * Renvoie la description de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetDescription(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getDescription();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method isBefore
	 *
	 * Teste si cette annonce a été publiée avant l'annonce spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testisBefore(ClassifiedAd self, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ad != null;
		assumeTrue(ad != null);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isBefore(ad);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isBefore(ad.getDate());
		assertEquals(self.getDate().isBefore(ad.getDate()), result);
		// @ensures !(\result && this.isAfter(ad));
		assertFalse(result && self.isAfter(ad));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method isAfter
	 *
	 * Teste si cette annonce a été publié après l'annonce spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testisAfter(ClassifiedAd self, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires ad != null;
		assumeTrue(ad != null);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isAfter(ad);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isAfter(ad.getDate());
		assertEquals(self.getDate().isAfter(ad.getDate()), result);
		// @ensures !(\result && this.isBefore(ad));
		assertFalse(result && self.isBefore(ad));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method equals
	 *
	 * Renvoie true si l'objet spécifié est une ClassifiedAd ayant les mêmes
	 * caractéristiques que cette ClassifiedAd.
	 */
	/**
	 * @param self
	 * @param obj
	 */
	@ParameterizedTest
	@MethodSource("adAndadProvider")
	public void testequals(ClassifiedAd self, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.equals(obj);

		// Post-conditions:
		// @ensures !(obj instanceof ClassifiedAd) ==> !\result;
		if (!(obj instanceof ClassifiedAd)) {
			assertFalse(result);
		} else {
			ClassifiedAd ad = (ClassifiedAd) obj;
			// @ensures \result <==> (obj instanceof ClassifiedAd) &&
			// getDate().equals(((ClassifiedAd) obj).getDate()) &&
			// getDescription().equals(((ClassifiedAd) obj).getDescription()) &&
			// getCategory().equals(((ClassifiedAd) obj).getCategory()) &&
			// getPrice() == ((ClassifiedAd) obj).getPrice());
			boolean isEquals = self.getDate().equals(ad);
			isEquals = isEquals && self.getDescription().equals(ad.getDescription());
			isEquals = isEquals && self.getCategory().equals(ad.getCategory());
			isEquals = isEquals && self.getPrice() == ad.getPrice();
			assertEquals(isEquals, result);
		}
		// @ensures \result ==> hashCode() == obj.hashCode());
		if (result) {
			assertEquals(self.hashCode(), obj.hashCode());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hashCode
	 *
	 * Renvoie un code de hashage pour cette ClassifiedAd.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testhashCode(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.hashCode();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method toString
	 *
	 * Renvoie une chaîne de caractères contenant les caractéristiques de cette
	 * ClassifiedAd.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testtoString(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.toString();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.contains(getCategory().toString());
		assertTrue(result.contains(self.getCategory().toString()));
		// @ensures \result.contains(getDate().toString());
		assertTrue(result.contains(self.getDate().toString()));
		// @ensures \result.contains(getDescription().toString());
		assertTrue(result.contains(self.getDescription().toString()));
		// @ensures \result.contains("" + getPrice());
		assertTrue(result.contains("" + self.getPrice()));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getCategory
	 *
	 * Renvoie la catégorie de cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetCategory(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Category result = self.getCategory();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPrice
	 *
	 * Renvoie le prix de l'objet mis en vente par cette annonce.
	 */
	@ParameterizedTest
	@MethodSource("adProvider")
	public void testgetPrice(ClassifiedAd self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getPrice();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for ClassifiedAd
