package minebayd1.test;

import static minebayd1.test.DataProvider.LG_STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebayd1.CategorizedAdList;
import minebayd1.Category;
import minebayd1.ClassifiedAd;

/**
 * Test class for CategorizedAdList.
 *
 * Gestion d'une liste de ClassifiedAd triée selon la date de création (de la
 * plus récente à la plus ancienne). Une CategorizedAdList ne peut pas contenir
 * d'éléments dupliqués, plus formelement, pour tout couple d'entiers (i, j)
 * tels que: <br/>
 * i >= 0 && j >= 0 && i != j && i < size() && j < size() on a
 * !get(i).equals(get(j)). Une CategorizedAdList prend en compte les catégories
 * des annonces en permettant:
 * <ul>
 * <li>soit un parcours des annonces de toutes les catégories</li>
 * <li>soit un parcours des seules annonces d'une catégorie sélectionnée</li>
 * </ul>
 * Dans ce but, CategorizedAdList utilise une liste distincte (une ArrayList)
 * pour chaque catégorie. Les méthodes selectCategory, getSelectedCategory et
 * clearCatégory permettent de sélectionner ou déselectionner une catégorie
 * particulière. Lorsqu'une catégorie est sélectionnée, toutes les opérations
 * relatives aux itérations (startIteration, next, previous, ...) agissent
 * uniquement sur les annonces de cette catégorie.
 * 
 * Pour les autres méthodes, celles possédant un paramètre de type Category
 * agissent uniquement sur cette catégorie, les autres méthodes agissant sur
 * toutes les annonces.
 * 
 * Les méthodes modifiant le contenu de cette liste (add et remove), entraine
 * une réinitialisation de l'itération en cours (même effet qu'un appel à la
 * méthode startIteration).
 */
public class TestCategorizedAdList {

	public static Stream<CategorizedAdList> CategorizedAdListProvider() {
		// Put here the code to return a Stream of instances of CategorizedAdList:
		return Stream.generate(DataProvider::CategorizedAdListSupplier).limit(LG_STREAM);
	}

	public static Arguments CatAdListAndCatAdListSupplier() {
		CategorizedAdList list1 = DataProvider.CategorizedAdListSupplier();
		CategorizedAdList list2 = DataProvider.CategorizedAdListSupplier();
		if (DataProvider.randInt(2) == 0) {
			list2 = list1.clone();
		}
		return Arguments.of(list1, list2);
	}

	public static Stream<Arguments> CatAdListAndCatAdListProvider() {
		return Stream.generate(() -> CatAdListAndCatAdListSupplier()).limit(LG_STREAM);

	}

	public static Stream<Arguments> CatAdListAndCatProvider() {
		return Stream.generate(
				() -> Arguments.of(DataProvider.CategorizedAdListSupplier(), DataProvider.enumSupplier(Category.class)))
				.limit(LG_STREAM);
	}

	public static Arguments CatAdListAndCatAndIntSupplier() {
		CategorizedAdList list = DataProvider.CategorizedAdListSupplier();
		Category cat = DataProvider.enumSupplier(Category.class);
		int index = DataProvider.randInt(list.size(cat) + 1);
		return Arguments.of(list, cat, index);
	}

	public static Stream<Arguments> CatAdListAndIntProvider() {
		return CategorizedAdListProvider().map(list -> Arguments.of(list, DataProvider.randInt(list.size() + 1)));
	}

	public static Stream<Arguments> CatAdListAndCatAndIntProvider() {
		return Stream.generate(() -> CatAdListAndCatAndIntSupplier()).limit(LG_STREAM);
	}

	public static Arguments CatAdListAndAdSupplier() {
		CategorizedAdList list = DataProvider.CategorizedAdListSupplier();
		ClassifiedAd ad = DataProvider.adSupplier();
		list.remove(ad);
		return Arguments.of(list, ad);
	}

	public static Stream<Arguments> CatAdListAndAdProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.CategorizedAdListSupplier(), DataProvider.adSupplier()))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> CatAdListAndNewAdProvider() {
		return Stream.generate(() -> CatAdListAndAdSupplier()).limit(LG_STREAM);
	}

	public static Collection<Object> fakeProvider() {
		return List.of(new Object(), new Object(), new Object());
	}

	// Sauvegarde de l'état d'une instance
	private List<ClassifiedAd> backupList;
	private int[] sizes;
	private int previousIndex;
	private int nextIndex;
	private int lastIndex;
	private Optional<Category> selectedCat;

	private void saveState(CategorizedAdList self) {
		// Put here the code to save the state of self:
		Category[] tabCat = Category.values();
		sizes = new int[tabCat.length];
		for (Category cat : tabCat) {
			sizes[cat.ordinal()] = self.size(cat);
		}
		backupList = new ArrayList<ClassifiedAd>();
		for (int i = 0; i < self.size(); i++) {
			backupList.add(self.get(i));
		}
		this.previousIndex = self.previousIndex();
		this.nextIndex = self.nextIndex();
		this.lastIndex = self.lastIndex();
		this.selectedCat = self.getSelectedCategory();

	}

	private void assertPurity(CategorizedAdList self) {
		// Put here the code to check purity for self:
		Category[] tabCat = Category.values();
		for (Category cat : tabCat) {
			assertEquals(sizes[cat.ordinal()], self.size(cat));
		}
		for (int i = 0; i < self.size(); i++) {
			assertEquals(this.backupList.get(i), self.get(i));
		}
		assertEquals(this.previousIndex, self.previousIndex());
		assertEquals(this.nextIndex, self.nextIndex());
		assertEquals(this.lastIndex, self.lastIndex());
		assertEquals(this.selectedCat, self.getSelectedCategory());
	}

	public void assertInvariant(CategorizedAdList self) {
		// Put here the code to check the invariant:
		// @invariant \var Optional<Category> optCat = getSelectedCategory();
		Optional<Category> optCat = self.getSelectedCategory();
		// @invariant getSelectedCategory() != null;
		assertNotNull(optCat);
		// @invariant (\forall int i, j; i >= 0 && i < j && j < size();
		// get(i).isAfter(get(j)));
		for (int i = 1; i < self.size(); i++) {
			assertTrue(self.get(i - 1).isAfter(self.get(i)), "i=" + i);
		}
		// @invariant (\forall Category cat;true;
		// (\forall int i, j; i >= 0 && i < j && j < size(cat);
		// get(cat, i).isAfter(get(cat, j))));
		for (Category cat : Category.values()) {
			for (int i = 1; i < self.size(cat); i++) {
				assertTrue(self.get(cat, i - 1).isAfter(self.get(cat, i)));
			}
		}
		// @invariant size() == (\sum Category cat;true;size(cat));
		int totalSize = 0;
		for (Category cat : Category.values()) {
			totalSize += self.size(cat);
		}
		assertEquals(totalSize, self.size());
		// @invariant nextIndex() >= 0 && previousIndex() >= -1;
		assertTrue(self.nextIndex() >= 0);
		assertTrue(self.previousIndex() >= -1);
		// @invariant nextIndex() <= size();
		assertTrue(self.nextIndex() <= self.size());
		// @invariant previousIndex() < size();
		assertTrue(self.previousIndex() < self.size());
		// @invariant optCat.isPresent() ==> nextIndex() <= size(optCat.get());
		// @invariant optCat.isPresent() ==> previousIndex() < size(optCat.get());
		if (optCat.isPresent()) {
			assertTrue(self.nextIndex() <= self.size(optCat.get()));
			assertTrue(self.previousIndex() < self.size(optCat.get()));
		}
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex();
		assertTrue(self.lastIndex() == self.nextIndex() || self.lastIndex() == self.previousIndex());
		// @invariant !hasPrevious() <==> previousIndex() == -1;
		assertEquals(!self.hasPrevious(), self.previousIndex() == -1);
		// @invariant optCat.isEmpty() ==> (!hasNext() <==> nextIndex() == size());
		if (optCat.isEmpty()) {
			assertEquals(!self.hasNext(), self.nextIndex() == self.size());
		} else {
			// @invariant optCat.isPresent() ==> (!hasNext() <==> nextIndex() ==
			// size(optCat.get()));
			assertEquals(!self.hasNext(), self.nextIndex() == self.size(optCat.get()));
		}
		// @invariant !contains(null);
		assertFalse(self.contains(null));
		// @invariant (\forall int i, j; i >= 0 && i < j && j < size();
		// !get(i).equals(get(j)));
		for (int i = 1; i < self.size(); i++) {
			assertFalse(self.get(i - 1).equals(self.get(i)));
		}
	}

	/**
	 * Test method for constructor CategorizedAdList
	 *
	 * Initialise une nouvelle instance ne contenant aucune annonce.
	 */
	@ParameterizedTest
	@MethodSource("fakeProvider")
	public void testCategorizedAdList(Object fakeArg) {

		// Pré-conditions:

		// Oldies:

		// Exécution:
		CategorizedAdList result = new CategorizedAdList();

		// Post-conditions:
		// @ensures size() == 0;
		assertTrue(result.size() == 0);
		// @ensures getSelectedCategory() != null;
		assertNotNull(result.getSelectedCategory());
		// @ensures getSelectedCategory().isEmpty();
		assertTrue(result.getSelectedCategory().isEmpty());
		// @ensures !hasPrevious();
		assertFalse(result.hasPrevious());
		// @ensures !hasNext();
		assertFalse(result.hasNext());
		// @ensures previousIndex() == -1;
		assertTrue(result.previousIndex() == -1);
		// @ensures nextIndex() == 0;
		assertTrue(result.nextIndex() == 0);
		// @ensures lastIndex() == -1;
		assertTrue(result.lastIndex() == -1);

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method selectCategory
	 *
	 * Sélectionne la catégorie sur laquelle sera effectuée la prochaine itération.
	 * Effectue les initialisations nécessaire pour démarrer une nouvelle itération
	 * de la même manière qu'un appel à startIteration().
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndCatProvider")
	public void testselectCategory(CategorizedAdList self, Category cat) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		assumeTrue(cat != null);

		// Oldies:

		// Exécution:
		self.selectCategory(cat);

		// Post-conditions:
		// @ensures getSelectedCategory().isPresent();
		assertTrue(self.getSelectedCategory().isPresent());
		// @ensures getSelectedCategory().get().equals(cat);
		assertEquals(cat, self.getSelectedCategory().get());
		// @ensures !hasPrevious();
		assertFalse(self.hasPrevious());
		// @ensures previousIndex() == -1;
		assertEquals(-1, self.previousIndex());
		// @ensures nextIndex() == 0;
		assertEquals(0, self.nextIndex());
		// @ensures lastIndex() == -1;
		assertEquals(-1, self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getSelectedCategory
	 *
	 * Renvoie un Optional pour la catégorie sélectionnée. Si une catégorie a été
	 * sélectionnée \result.isPresent() est true; sinon \result.isEmpty() est true.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testgetSelectedCategory(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Optional<Category> result = self.getSelectedCategory();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clearSelectedCategory
	 *
	 * Déselectionne la catégorie sélectionnée et effectue les initialisations
	 * nécessaire pour démarrer une nouvelle itération (cf.startIteration()). Si
	 * aucune catégorie n'était sélectionnée l'appel à cette méthode est équivalent
	 * à un appel à startIteration.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testclearSelectedCategory(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		self.clearSelectedCategory();

		// Post-conditions:
		// @ensures getSelectedCategory().isEmpty();
		assertTrue(self.getSelectedCategory().isEmpty());
		// @ensures !hasPrevious();
		assertFalse(self.hasPrevious());
		// @ensures previousIndex() == -1;
		assertEquals(-1, self.previousIndex());
		// @ensures nextIndex() == 0;
		assertEquals(0, self.nextIndex());
		// @ensures lastIndex() == -1;
		assertEquals(-1, self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method startIteration
	 *
	 * Initialise cette liste pour le démarrage d'une nouvelle itération sur les
	 * annonces de cette liste. Cette itération s'effectue à partir de l'annonce la
	 * plus récente, de sorte que chaque appel à next() renvoie une annonce plus
	 * ancienne.
	 * 
	 * Si une catégorie est sélectionnée, l'itération ne concernera que les annonces
	 * de cette catégorie; sinon l'itération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void teststartIteration(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		self.startIteration();

		// Post-conditions:
		// @ensures !hasPrevious();
		assertFalse(self.hasPrevious());
		// @ensures previousIndex() == -1;
		assertEquals(-1, self.previousIndex());
		// @ensures nextIndex() == 0;
		assertEquals(0, self.nextIndex());
		// @ensures lastIndex() == -1;
		assertEquals(-1, self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasNext
	 *
	 * Renvoie true si cette liste possède une annonce plus ancienne pour
	 * l'itération en cours.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testhasNext(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasNext();

		// Post-conditions:
		// @ensures \var Optional<Category> optCat = getSelectedCategory();
		Optional<Category> optCat = self.getSelectedCategory();
		if (optCat.isEmpty()) {
			// @ensures optCat.isEmpty() ==> (\result <==> nextIndex() < size());
			assertEquals(self.nextIndex() < self.size(), result);
			// @ensures optCat.isEmpty() ==> (!\result <==> nextIndex() == size());
			assertEquals(self.nextIndex() == self.size(), !result);
		} else {
			// @ensures optCat.isPresent() ==> (\result <==> nextIndex() <
			// size(optCat.get()));
			assertEquals(self.nextIndex() < self.size(optCat.get()), result);
			// @ensures optCat.isPresent() ==> (!\result <==> nextIndex() ==
			// size(optCat.get()));
			assertEquals(self.nextIndex() == self.size(optCat.get()), !result);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method next
	 *
	 * Renvoie l'annonce suivante (plus ancienne) dans l'itération en cours et
	 * avance d'un élément dans l'itération.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testnext(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		assumeTrue(self.hasNext());

		// Oldies:
		Optional<Category> optCat = self.getSelectedCategory();
		ClassifiedAd oldNext = null;
		if (optCat.isEmpty()) {
			// old in:@ensures optCat.isEmpty() ==> \result.equals(\old(get(nextIndex())));
			oldNext = self.get(self.nextIndex());
		} else {
			// old in:@ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
			// nextIndex())));
			oldNext = self.get(optCat.get(), self.nextIndex());
		}
		// old in:@ensures nextIndex() == \old(nextIndex()) + 1;
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex()) + 1;
		int oldPreviousIndex = self.previousIndex();
		// old in:@ensures previousIndex() == \old(nextIndex());
		// old in:@ensures lastIndex() == \old(nextIndex());

		// Exécution:
		ClassifiedAd result = self.next();

		// Post-conditions:
		// @ensures \var Optional<Category> optCat = getSelectedCategory();
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures optCat.isEmpty() ==> \result.equals(\old(get(nextIndex())));
		// @ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
		// nextIndex())));
		assertEquals(oldNext, result);
		// @ensures optCat.isPresent() ==> \result.getCategory().equals(optCat.get());
		if (optCat.isPresent()) {
			assertEquals(optCat.get(), result.getCategory());
		}
		// @ensures nextIndex() == \old(nextIndex()) + 1;
		assertEquals(oldNextIndex + 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex()) + 1;
		assertEquals(oldPreviousIndex + 1, self.previousIndex());
		// @ensures previousIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.previousIndex());
		// @ensures lastIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.lastIndex());
		// @ensures lastIndex() == previousIndex();
		assertEquals(self.lastIndex(), self.previousIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method nextIndex
	 *
	 * Renvoie l'index de l'annonce qui sera renvoyée par le prochain appel à
	 * next(). Si l'itération est arrivée à la fin la valeur renvoyée est size() (ou
	 * size(getSelectedCategory().get()) si une catégorie est sélectionnée).
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testnextIndex(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.nextIndex();

		// Post-conditions:
		// @ensures \var Optional<Category> optCat = getSelectedCategory();
		Optional<Category> optCat = self.getSelectedCategory();
		if (optCat.isEmpty()) {
			// @ensures optCat.isEmpty() ==> (\result == size() <==> !hasNext());
			assertEquals(!self.hasNext(), (result == self.size()));
			// @ensures optCat.isEmpty() ==> (hasNext() <==> \result >= 0 && \result <
			// size());
			assertEquals(self.hasNext(), (result >= 0 && result < self.size()));
		} else {
			// @ensures optCat.isPresent() ==> (\result == size(optCat.get()) <==>
			// !hasNext());
			assertEquals(!self.hasNext(), (result == self.size(optCat.get())));
			// @ensures optCat.isPresent() ==> (hasNext() <==> \result >= 0 && \result <
			// size(optCat.get()));
			assertEquals(self.hasNext(), (result >= 0 && result < self.size(optCat.get())));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasPrevious
	 *
	 * Renvoie true si cette liste possède une annonce plus récente pour l'itération
	 * en cours.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testhasPrevious(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasPrevious();

		// Post-conditions:
		// @ensures \result <==> previousIndex() >= 0;
		assertEquals(self.previousIndex() >= 0, result);
		// @ensures !\result <==> previousIndex() == -1;
		assertEquals(self.previousIndex() == -1, !result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previous
	 *
	 * Renvoie l'annonce précedente (plus récente) dans l'itération en cours et
	 * recule d'un élément dans l'itération.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testprevious(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		assumeTrue(self.hasPrevious());

		// Oldies:
		Optional<Category> optCat = self.getSelectedCategory();
		// old in:@ensures optCat.isEmpty() ==>
		// \result.equals(\old(get(previousIndex())));
		ClassifiedAd oldPrevious = self.get(self.previousIndex());
		// old in:@ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
		// previousIndex())));
		if (optCat.isPresent()) {
			oldPrevious = self.get(optCat.get(), self.previousIndex());
		}
		// old in:@ensures nextIndex() == \old(nextIndex()) - 1;
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex()) - 1;
		// old in:@ensures nextIndex() == \old(previousIndex());
		// old in:@ensures lastIndex() == \old(previousIndex());
		int oldPreviousIndex = self.previousIndex();

		// Exécution:
		ClassifiedAd result = self.previous();

		// Post-conditions:
		// @ensures \var Optional<Category> optCat = getSelectedCategory();
		// @ensures optCat.isEmpty() ==> \result.equals(\old(get(previousIndex())));
		// @ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
		// previousIndex())));
		assertEquals(oldPrevious, result);
		if (optCat.isPresent()) {
			// @ensures optCat.isPresent() ==> \result.getCategory().equals(optCat.get());
			assertEquals(optCat.get(), result.getCategory());
		}
		// @ensures nextIndex() == \old(nextIndex()) - 1;
		assertEquals(oldNextIndex - 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex()) - 1;
		assertEquals(oldPreviousIndex - 1, self.previousIndex());
		// @ensures nextIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.nextIndex());
		// @ensures lastIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.lastIndex());
		// @ensures lastIndex() == nextIndex();
		assertTrue(self.lastIndex() == self.nextIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previousIndex
	 *
	 * Renvoie l'index de l'annonce qui sera renvoyée par le prochain appel à
	 * previous(). Si l'itération est arrivée au début la valeur renvoyée est -1.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testpreviousIndex(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.previousIndex();

		// Post-conditions:
		// @ensures \var Optional<Category> optCat = getSelectedCategory();
		Optional<Category> optCat = self.getSelectedCategory();
		// @ensures \result == -1 <==> !hasPrevious();
		assertEquals(!self.hasPrevious(), result == -1);
		if (optCat.isEmpty()) {
			// @ensures optCat.isEmpty() ==> (hasPrevious() <==> \result >= 0 && \result <
			// size());
			assertEquals(self.hasPrevious(), (result >= 0 && result < self.size()));
		} else {
			// @ensures optCat.isPresent() ==> (hasPrevious() <==> \result >= 0 && \result <
			// size(optCat.get()));
			assertEquals(self.hasPrevious(), (result >= 0 && result < self.size(optCat.get())));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method lastIndex
	 *
	 * Renvoie l'index de l'annonce qui a été renvoyée par le dernier appel à
	 * previous() ou next(). Si next() ou previous() n'ont pas été appelé depuis le
	 * dernier appel à startIteration() la valeur renvoyée est -1.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testlastIndex(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.lastIndex();

		// Post-conditions:
		// @ensures \result == nextIndex() || \result == previousIndex();
		assertTrue(result == self.nextIndex() || result == self.previousIndex());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie l'élément d'index spécifié dans la liste des annonces de la catégorie
	 * spécifiée. À la différence de la méthode get(int i), cette implémentation
	 * effectue un accès direct au ième élément et peut donc être utilisée sans
	 * pénalité pour effectuer une itération sur les éléments de la catégorie
	 * spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndCatAndIntProvider")
	public void testget(CategorizedAdList self, Category cat, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		assumeTrue(cat != null);
		// @requires i >= 0 && i < size(cat);
		assumeTrue(i >= 0 && i < self.size(cat));

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(cat, i);

		// Post-conditions:
		// @ensures contains(\result);
		assertTrue(self.contains(result));
		// @ensures \result.getCategory().equals(cat);
		assertEquals(cat, result.getCategory());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie l'élément d'index spécifié dans la liste des annonces. Cette
	 * implémentation n'est pas adaptée à une utilisation pour effectuer des
	 * itérations sur l'ensemble de cette liste. Pour effectuer une itération, il
	 * est fortement recommandé d'utiliser les méthodes prévues à cet effet
	 * (startIteration, hasNext, next, hasPrevious, previous, ...).
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndIntProvider")
	public void testget(CategorizedAdList self, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires i >= 0 && i < size();
		assumeTrue(i >= 0 && i < self.size());

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(i);

		// Post-conditions:
		// @ensures contains(\result);
		assertTrue(self.contains(result));
		// @ensures (\exists int i; i >= 0 && i < size(\result.getCategory());
		// \result.equals(get(\result.getCategory(), i)));
		boolean found = false;
		for (int index = 0; index < self.size(result.getCategory()) && !found; index++) {
			found = result.equals(self.get(result.getCategory(), index));
		}
		assertTrue(found);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Ajoute l'élement spécifié dans cette liste. L'élement est inséré dans la
	 * liste de manière à ce que la liste reste triée. L'itération en cours est
	 * réinitialisée.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndNewAdProvider")
	public void testadd(CategorizedAdList self, ClassifiedAd elt) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires elt != null;
		assumeTrue(elt != null);
		// @requires !contains(elt);
		assumeFalse(self.contains(elt));

		// Oldies:
		// old in:@ensures size() == \old(size()) + 1;
		int oldSize = self.size();
		// old in:@ensures size(elt.getCategory()) == \old(size(elt.getCategory())) + 1;
		// old in:@ensures (\forall Category cat; !cat.equals(elt.getCategory());
		// size(cat) == \old(size(cat)));
		int oldCatSize = self.size(elt.getCategory());
		Category[] allCat = Category.values();
		int[] tabOldCatSize = new int[allCat.length];
		for (int i = 0; i < allCat.length; i++) {
			tabOldCatSize[i] = self.size(allCat[i]);
		}

		// Exécution:
		self.add(elt);

		// Post-conditions:
		// @ensures contains(elt);
		assertTrue(self.contains(elt));
		// @ensures size() == \old(size()) + 1;
		assertEquals(oldSize + 1, self.size());
		// @ensures size(elt.getCategory()) == \old(size(elt.getCategory())) + 1;
		assertEquals(oldCatSize + 1, self.size(elt.getCategory()));
		// @ensures (\forall Category cat; !cat.equals(elt.getCategory()); size(cat) ==
		// \old(size(cat)));
		for (int i = 0; i < allCat.length; i++) {
			if (i != elt.getCategory().ordinal()) {
				assertEquals(tabOldCatSize[i], self.size(allCat[i]));
			}
		}
		// @ensures !hasPrevious();
		assertFalse(self.hasPrevious());
		// @ensures previousIndex() == -1;
		assertEquals(-1, self.previousIndex());
		// @ensures nextIndex() == 0;
		assertEquals(0, self.nextIndex());
		// @ensures lastIndex() == -1;
		assertEquals(-1, self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method remove
	 *
	 * Retire une occurence de l'élement spécifié de cette liste s'il y était
	 * présent. Renvoie true si l'élement a effectivement été retiré de cette liste.
	 * Si l'élément spécifié est effectivement retiré, l'itération en cours est
	 * réinitialisée.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndAdProvider")
	public void testremove(CategorizedAdList self, Object o) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:
		// old in:@ensures \result <==> \old(contains(o));
		boolean oldContains = self.contains(o);
		// old in:@ensures \result <==> (size() == \old(size()) - 1);
		// old in:@ensures !\result <==> (size() == \old(size());
		int oldSize = self.size();
		// old in:@ensures \result <==> (o instanceof ClassifiedAd) &&
		// (size(((ClassifiedAd)
		// o).getCategory()) == \old(size(((ClassifiedAd) o).getCategory())) - 1);
		// old in:@ensures (o instanceof ClassifiedAd) ==> (\forall Category
		// cat; !cat.equals(((ClassifiedAd) o).getCategory()); size(cat) ==
		// \old(size(cat)));
		int oldCatSize = -1;
		Category[] allCat = null;
		int[] tabOldCatSize = null;
		if (o instanceof ClassifiedAd) {
			oldCatSize = self.size(((ClassifiedAd) o).getCategory());
			allCat = Category.values();
			tabOldCatSize = new int[allCat.length];
			for (int i = 0; i < allCat.length; i++) {
				tabOldCatSize[i] = self.size(allCat[i]);
			}
		}
		// old in:@ensures !\result ==> (hasPrevious() == \old(hasPrevious()));
		boolean oldHasPrevious = self.hasPrevious();
		// old in:@ensures !\result ==> (previousIndex() == \old(previousIndex()));
		int oldPreviousIndex = self.previousIndex();
		// old in:@ensures !\result ==> (nextIndex() == \old(nextIndex()));
		int oldNextIndex = self.nextIndex();
		// old in:@ensures !\result ==> (lastIndex() == \old(lastIndex()));
		int oldLastIndex = self.lastIndex();

		// Exécution:
		boolean result = self.remove(o);

		// Post-conditions:
		// @ensures !contains(o);
		assertFalse(self.contains(o));
		// @ensures \result <==> \old(contains(o));
		assertEquals(oldContains, result);
		// @ensures \result <==> (size() == \old(size()) - 1);
		assertEquals(self.size() == oldSize - 1, result);
		// @ensures !\result <==> (size() == \old(size());
		assertEquals(self.size() == oldSize, !result);
		if (o instanceof ClassifiedAd) {
			ClassifiedAd ad = (ClassifiedAd) o;
			// @ensures \result <==> (o instanceof ClassifiedAd) && (size(((ClassifiedAd)
			// o).getCategory()) == \old(size(((ClassifiedAd) o).getCategory())) - 1);
			assertEquals(self.size(ad.getCategory()) == oldCatSize - 1, result);
			// @ensures (o instanceof ClassifiedAd) ==> (\forall Category cat;
			// !cat.equals(((ClassifiedAd) o).getCategory()); size(cat) == \old(size(cat)));
			for (int i = 0; i < allCat.length; i++) {
				if (i != ad.getCategory().ordinal()) {
					assertEquals(tabOldCatSize[i], self.size(allCat[i]));
				}
			}
		} else {
			assertFalse(result);
		}
		if (result) {
			// @ensures \result ==> !hasPrevious();
			assertFalse(self.hasPrevious());
			// @ensures \result ==> previousIndex() == -1;
			assertEquals(-1, self.previousIndex());
			// @ensures \result ==> nextIndex() == 0;
			assertEquals(0, self.nextIndex());
			// @ensures \result ==> lastIndex() == -1;
			assertEquals(-1, self.lastIndex());
		} else {
			// @ensures !\result ==> (hasPrevious() == \old(hasPrevious()));
			assertEquals(oldHasPrevious, self.hasPrevious());
			// @ensures !\result ==> (previousIndex() == \old(previousIndex()));
			assertEquals(oldPreviousIndex, self.previousIndex());
			// @ensures !\result ==> (nextIndex() == \old(nextIndex()));
			assertEquals(oldNextIndex, self.nextIndex());
			// @ensures !\result ==> (lastIndex() == \old(lastIndex()));
			assertEquals(oldLastIndex, self.lastIndex());
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method contains
	 *
	 * Renvoie true si et seulement si l'argument spécifié est présent dans cette
	 * liste.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndAdProvider")
	public void testcontains(CategorizedAdList self, Object o) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.contains(o);

		// Post-conditions:
		// @ensures \result <==> (\exists int i; i >= 0 && i < size();
		// get(i).equals(o));
		boolean iExists = false;
		for (int i = 0; i < self.size() && !iExists; i++) {
			iExists = self.get(i).equals(o);
		}
		assertEquals(iExists, result);
		// @ensures \result <==> (o instanceof ClassifiedAd)
		// && (\exists int i; i >= 0 && i < size((ClassifiedAd) o).getCategory());
		// get(((ClassifiedAd) o).getCategory(), i).equals(o));
		if (o instanceof ClassifiedAd) {
			ClassifiedAd ad = (ClassifiedAd) o;
			iExists = false;
			for (int i = 0; i < self.size(ad.getCategory()) && !iExists; i++) {
				iExists = self.get(ad.getCategory(), i).equals(o);
			}
			assertEquals(iExists, result);
		} else {
			assertFalse(result);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre total d'éléments de cette liste.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testsize(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size();

		// Post-conditions:
		// @ensures \result >= 0;
		assertTrue(result >= 0);
		// @ensures \result == (\sum Category cat;true;size(cat));
		Category[] allCat = Category.values();
		int totalSize = 0;
		for (int i = 0; i < allCat.length; i++) {
			totalSize += self.size(allCat[i]);
		}
		assertEquals(totalSize, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'annonces de cette liste appartenant à la catégorie
	 * spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndCatProvider")
	public void testsize(CategorizedAdList self, Category cat) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		assumeTrue(cat != null);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size(cat);

		// Post-conditions:
		// @ensures \result >= 0 && \result <= size();
		assertTrue(result >= 0 && result <= self.size());
		// @ensures \result == (\sum int i; i >= 0 && i < size() &&
		// get(i).getCategory().equals(cat); 1);
		int sum = 0;
		for (int i = 0; i < self.size(); i++) {
			if (self.get(i).getCategory().equals(cat)) {
				sum++;
			}
		}
		assertEquals(sum, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method equals
	 *
	 * Renvoie true si et seulement si l'argument spécifié est une CategorizedAdList
	 * contenant les mêmes éléments dans le même ordre que cette CategorizedAdList,
	 * et que la CategorizedAdList spécifiée est dans le même état que cette liste
	 * concernant l'itération en cours et la catégorie sélectionnée.
	 */
	@ParameterizedTest
	@MethodSource("CatAdListAndCatAdListProvider")
	public void testequals(CategorizedAdList self, Object obj) {
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
		// @ensures !(obj instanceof CategorizedAdList) ==> !\result;
		if (!(obj instanceof CategorizedAdList)) {
			assertFalse(result);
		}
		if (result) {
			CategorizedAdList list = (CategorizedAdList) obj;
			// @ensures \result ==> ((CategorizedAdList) obj).size() == size();
			assertEquals(list.size(), self.size());
			// @ensures \result ==> ((CategorizedAdList) obj).previousIndex() ==
			// previousIndex();
			assertEquals(list.previousIndex(), self.previousIndex());
			// @ensures \result ==> ((CategorizedAdList) obj).nextIndex() == nextIndex();
			assertEquals(list.nextIndex(), self.nextIndex());
			// @ensures \result ==> ((CategorizedAdList) obj).lastIndex() == lastIndex();
			assertEquals(list.lastIndex(), self.lastIndex());
			// @ensures \result ==> ((CategorizedAdList)
			// obj).getSelectedCategory().equals(getSelectedCategory());
			assertEquals(list.getSelectedCategory(), self.getSelectedCategory());
			// @ensures \result ==> (\forall int i; i >= 0 && i < size();
			// ((CategorizedAdList) obj).get(i).equals(get(i)));
			for (int i = 0; i < self.size(); i++) {
				assertEquals(list.get(i), self.get(i));
			}
			// @ensures \result ==> ((CategorizedAdList) obj).hashCode() == hashCode();
			assertEquals(list.hashCode(), self.hashCode());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method clone
	 *
	 * Renvoie un clone de cette liste.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testclone(CategorizedAdList self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		CategorizedAdList result = self.clone();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result != this;
		assertNotSame(result, self);
		// @ensures \result.equals(this);
		assertEquals(result, self);
		// @ensures \result.getClasse() == this.getClass();
		assertEquals(self.getClass(), result.getClass());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hashCode
	 *
	 * Renvoie un code de hashage pour cette liste.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testhashCode(CategorizedAdList self) {
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
	 * Renvoie une représentation de cette liste sous forme d'une chaîne de
	 * caractères.
	 */
	@ParameterizedTest
	@MethodSource("CategorizedAdListProvider")
	public void testtoString(CategorizedAdList self) {
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
		// @ensures \result.contains("" + lastIndex());
		assertTrue(result.contains("" + self.lastIndex()));
		// @ensures \result.contains("" + nextIndex());
		assertTrue(result.contains("" + self.nextIndex()));
		// @ensures \result.contains("" + previousIndex());
		assertTrue(result.contains("" + self.previousIndex()));
		// @ensures \result.contains(getSelectedCategory().toString());
		assertTrue(result.contains(self.getSelectedCategory().toString()));
		// @ensures (\forall ClassifiedAd ad; contains(ad);
		// \result.contains(ad.toString()));
		for (int i = 0; i < self.size(); i++) {
			assertTrue(result.contains(self.get(i).toString()));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for CategorizedAdList
