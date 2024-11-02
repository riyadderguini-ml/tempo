package minebayd1.test;

import static minebayd1.test.DataProvider.LG_STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import minebayd1.AdState;
import static minebayd1.AdState.*;
import minebayd1.CategorizedAdList;
import minebayd1.Category;
import minebayd1.ClassifiedAd;
import minebayd1.User;

/**
 * Test class for User.
 *
 * Un utilisateur de l'application MinEbay.
 * 
 * <p>
 * Un User est caractérisé par:
 * <ul>
 * <li>son nom et son mot de passe</li>
 * <li>le montant disponible sur son compte bancaire</li>
 * <li>sa date d'inscription sur MinEbay (= date de création de l'instance de
 * l'User)</li>
 * <li>la liste des annonces qu'il a posté et pour lesquelles il n'a pas trouvé
 * d'acheteur (open ads)</li>
 * <li>la liste des annonces qu'il a posté et pour lesquelles il a vendu l'objet
 * concerné (closed ads)</li>
 * <li>la liste des annonces (d'autres utilisateurs) pour lesquelles il a acheté
 * l'objet concerné (puchases)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Chacune des trois listes d'annonces d'un User est représentée en utilisant
 * une instance de CategorizedAdList.
 * </p>
 * 
 * <p>
 * Les méthodes ne possédant pas de paramètre de type AdState ou Category
 * agissent uniquement sur les listes d'annonces dont l'état et, le cas échéant,
 * la catégorie a été sélectionnée.
 * </p>
 * 
 * Un ensemble complet de méthodes permet d'effectuer des itérations
 * bidirectionnelles sur les listes d'annonces de l'User. Ce sont les méthodes:
 * startIteration(), hasNext(), nextIndex(), next(), hasPrevious(),
 * previousIndex(), previous() et lastIndex(). Un exemple d'utilisation de ces
 * méthodes est sonné ci-après:
 * 
 * <pre>{@code
 * aUser.startIteration(); // Initialisation d'une nouvelle itération
 * // Affichage des annonces du plus récent au plus ancien:
 * while (aUser.hasNext()) {
 * 	System.out.println("Annonce suivante (plus ancienne):" + aUser.next());
 * }
 * // Affichage des annonces du plus ancien au plus récent:
 * while (aUser.hasPrevious()) {
 * 	System.out.println("Annonce précédente (plus récente):" + aUser.previous());
 * }
 * }</pre>
 */
public class TestUser {

	public static Stream<User> UserProvider() {
		// Put here the code to return a Stream of instances of User:
		return Stream.generate(DataProvider::userSupplier).limit(LG_STREAM);
	}

	public static Arguments UserAndIntSupplier() {
		User u = DataProvider.userSupplier();
		int index = DataProvider.randInt(u.size() + 1);
		return Arguments.of(u, index);
	}

	public static Stream<Arguments> UserAndIntProvider() {
		return Stream.generate(TestUser::UserAndIntSupplier).limit(LG_STREAM);
	}

	public static Stream<Arguments> UserAndStateProvider() {
		return Stream
				.generate(() -> Arguments.of(DataProvider.userSupplier(), DataProvider.enumSupplier(AdState.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> UserAndCatProvider() {
		return Stream
				.generate(() -> Arguments.of(DataProvider.userSupplier(), DataProvider.enumSupplier(Category.class)))
				.limit(LG_STREAM);
	}

	public static Stream<Arguments> UserAndStateAndOptCatProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.userSupplier(), DataProvider.enumSupplier(AdState.class),
				Optional.of(DataProvider.enumSupplier(Category.class)))).limit(LG_STREAM);
	}

	public static Arguments UserAndStateAndOptCatAndIntSupplier() {
		User u = DataProvider.userSupplier();
		AdState state = DataProvider.enumSupplier(AdState.class);
		Optional<Category> optCat = Optional.of(DataProvider.enumSupplier(Category.class));
		int index = DataProvider.randInt(u.size(state, optCat) + 1);
		return Arguments.of(u, state, optCat, index);
	}

	public static Stream<Arguments> UserAndStateAndOptCatAndIntProvider() {
		return Stream.generate(TestUser::UserAndStateAndOptCatAndIntSupplier).limit(LG_STREAM);
	}

	public static Stream<Arguments> twoStringProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.stringSupplier(), DataProvider.stringSupplier()))
				.limit(LG_STREAM);
	}

	public static Arguments toBuySupplier() {
		User u1 = DataProvider.userSupplier();
		User u2 = DataProvider.userSupplier();
		ClassifiedAd ad = null;
		int maxTries = 10;
		while (maxTries > 0 && (u1.equals(u2) || u2.size(OPEN, Optional.empty()) == 0)) {
			u2 = DataProvider.userSupplier();
			maxTries--;
		}
		if (maxTries == 0) {
			ad = DataProvider.adSupplier();
		} else {
			ad = u2.get(OPEN, Optional.empty(), DataProvider.randInt(u2.size(OPEN, Optional.empty())));
		}
		return Arguments.of(u1, u2, ad);
	}

	public static Stream<Arguments> toBuyProvider() {
		return Stream.generate(TestUser::toBuySupplier).limit(LG_STREAM);
	}

	public static Stream<Arguments> UserAndCatAndNameAndPrice() {
		return Stream.generate(() -> Arguments.of(DataProvider.userSupplier(),
				DataProvider.enumSupplier(Category.class), "À vendre", DataProvider.randInt(50))).limit(LG_STREAM);
	}

	public static Arguments UserAndStateAndObjSupplier() {
		User u = DataProvider.userSupplier();
		AdState state = DataProvider.enumSupplier(AdState.class);
		Object obj = DataProvider.adSupplier();
		if (DataProvider.randBool(2) && u.size(state, Optional.empty()) > 0) {
			obj = u.get(state, Optional.empty(), DataProvider.randInt(u.size(state, Optional.empty())));
		}
		return Arguments.of(u, state, obj);
	}

	public static Stream<Arguments> UserAndStateAndObjProvider() {
		return Stream.generate(TestUser::UserAndStateAndObjSupplier).limit(LG_STREAM);
	}

	private static AdState[] tabState = AdState.values();
	private static Category[] tabCat = Category.values();

	// State of a User
	private String name;
	private String password;
	private Instant registrationtionDate;
	private AdState selectedState;
	private Optional<Category> selectedCategory;
	private CategorizedAdList openAds;
	private CategorizedAdList closedAds;
	private CategorizedAdList purchases;
	private int bankAccount;
	private int previousIndex;
	private int nextIndex;
	private int lastIndex;

	private void saveState(User self) {
		// Put here the code to save the state of self:
		name = self.getName();
		password = self.getPassword();
		registrationtionDate = self.getRegistrationDate();
		selectedState = self.getSelectedAdState();
		selectedCategory = self.getSelectedCategory();
		bankAccount = self.getAvailableCash();
		previousIndex = self.previousIndex();
		nextIndex = self.nextIndex();
		lastIndex = self.lastIndex();
		openAds = new CategorizedAdList();
		for (int i = 0; i < self.size(OPEN, Optional.empty()); i++) {
			openAds.add(self.get(OPEN, Optional.empty(), i));
		}
		closedAds = new CategorizedAdList();
		for (int i = 0; i < self.size(CLOSED, Optional.empty()); i++) {
			closedAds.add(self.get(CLOSED, Optional.empty(), i));
		}
		purchases = new CategorizedAdList();
		for (int i = 0; i < self.size(PURCHASE, Optional.empty()); i++) {
			purchases.add(self.get(PURCHASE, Optional.empty(), i));
		}
	}

	private void assertPurity(User self) {
		// Put here the code to check purity for self:
		assertEquals(name, self.getName());
		assertEquals(password, self.getPassword());
		assertEquals(registrationtionDate, self.getRegistrationDate());
		assertEquals(selectedState, self.getSelectedAdState());
		assertEquals(selectedCategory, self.getSelectedCategory());
		assertEquals(bankAccount, self.getAvailableCash());
		assertEquals(previousIndex, self.previousIndex());
		assertEquals(nextIndex, self.nextIndex());
		assertEquals(lastIndex, self.lastIndex());
		for (int i = 0; i < self.size(OPEN, Optional.empty()); i++) {
			assertEquals(openAds.get(i), self.get(OPEN, Optional.empty(), i));
		}
		for (int i = 0; i < self.size(CLOSED, Optional.empty()); i++) {
			assertEquals(closedAds.get(i), self.get(CLOSED, Optional.empty(), i));
		}
		for (int i = 0; i < self.size(PURCHASE, Optional.empty()); i++) {
			assertEquals(purchases.get(i), self.get(PURCHASE, Optional.empty(), i));
		}
	}

	public void assertInvariant(User self) {
		// Put here the code to check the invariant:
		// @invariant getName() != null && !getName().isBlank();
		assertNotNull(self.getName());
		assertFalse(self.getName().isBlank());
		// @invariant getPassword() != null && !getPassword().isBlank();
		assertNotNull(self.getPassword());
		assertFalse(self.getPassword().isBlank());
		// @invariant getRegistrationDate() != null;
		assertNotNull(self.getRegistrationDate());
		// @invariant (\forall AdState state; true; !contains(state, null));
		for (AdState state : tabState) {
			assertFalse(self.containsInState(state, null));
		}
		// @invariant (\forall int i, j; i >= 0 && i < j && j < size();
		// get(i).isAfter(get(j)));
		for (int i = 1; i < self.size(); i++) {
			assertTrue(self.get(i - 1).isAfter(self.get(i)));
		}
		// @invariant (\forall AdState state, Category cat;true; (\forall int i, j; i >=
		// 0 && i < j && j < size(state, Optional<Category>.of(cat)); get(state,
		// Optional<Category>.of(cat), i).isAfter(get(state, Optional<Category>.of(cat),
		// j))));
		for (AdState state : tabState) {
			for (Category cat : tabCat) {
				for (int i = 1; i < self.size(state, Optional.of(cat)); i++) {
					assertTrue(self.get(state, Optional.of(cat), i - 1).isAfter(self.get(state, Optional.of(cat), i)));
				}
			}
		}
		// @invariant previousIndex() >= -1 && previousIndex() < size();
		assertTrue(self.previousIndex() >= -1);
		assertTrue(self.previousIndex() < self.size());
		// @invariant nextIndex() >= 0 && nextIndex() <= size();
		assertTrue(self.nextIndex() >= 0);
		assertTrue(self.nextIndex() <= self.size());
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant lastIndex() >= -1 && lastIndex() < size();
		assertTrue(self.lastIndex() >= -1);
		assertTrue(self.lastIndex() < self.size());
		// @invariant lastIndex() == previousIndex() || lastIndex() == nextIndex();
		assertTrue(self.lastIndex() == self.previousIndex() || self.lastIndex() == self.nextIndex());
	}

	private void assertInitIterState(User u) {
		// @ensures !hasPrevious();
		assertFalse(u.hasPrevious());
		// @ensures previousIndex() == -1;
		assertEquals(-1, u.previousIndex());
		// @ensures nextIndex() == 0;
		assertEquals(0, u.nextIndex());
		// @ensures lastIndex() == -1;
		assertEquals(-1, u.lastIndex());
	}

	/**
	 * Test method for constructor User
	 *
	 * Initialise une nouvelle instance ayant les nom et mot de passe spécifiés. La
	 * date d'inscription du nouvel utilisateur est la date au moment de l'exécution
	 * de ce constructeur. Une nouvelle itération est initialisée sur les annonces
	 * OPEN de ce User.
	 */
	@ParameterizedTest
	@MethodSource("twoStringProvider")
	public void testUser(String userName, String password) {

		// Pré-conditions:
		// @requires userName != null && !userName.isBlank();
		assumeTrue(userName != null && !userName.isBlank());
		// @requires password != null && !password.isBlank();
		assumeTrue(password != null && !password.isBlank());

		// Oldies:
		// old in:@ensures \old(Instant.now()).isBefore(getRegistrationDate());
		Instant oldNow = Instant.now();

		// Exécution:
		User result = new User(userName, password);

		// Post-conditions:
		// @ensures getName().equals(userName);
		assertEquals(userName, result.getName());
		// @ensures getPassword().equals(password);
		assertEquals(password, result.getPassword());
		// @ensures getRegistrationDate() != null;
		assertNotNull(result.getRegistrationDate());
		// @ensures \old(Instant.now()).isBefore(getRegistrationDate());
		assertTrue(oldNow.isBefore(result.getRegistrationDate()));
		// @ensures getRegistrationDate().isBefore(Instant.now());
		assertTrue(result.getRegistrationDate().isBefore(Instant.now()));
		// @ensures getAvailableCash() = DEFAULT_CASH_AMMOUNT;
		assertEquals(User.DEFAULT_CASH_AMMOUNT, result.getAvailableCash());
		// @ensures getSelectedAdState().equals(OPEN);
		assertEquals(OPEN, result.getSelectedAdState());
		// @ensures getSelectedCategory().isEmpty();
		assertTrue(result.getSelectedCategory().isEmpty());
		// @ensures !hasPrevious();
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(result);
		// @ensures !hasNext();
		assertFalse(result.hasNext());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getSelectedAdState
	 *
	 * Renvoie l'état actuellement sélectionné pour les annonces.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetSelectedAdState(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		AdState result = self.getSelectedAdState();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method selectAdState
	 *
	 * Sélectionne l'état spécifié. Une nouvelle itération est initialisée pour les
	 * annonces dans cet état et le cas échéant la catégorie sélectionnée.
	 */
	@ParameterizedTest
	@MethodSource("UserAndStateProvider")
	public void testselectAdState(User self, AdState state) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		assumeTrue(state != null);

		// Oldies:
		// old in:@ensures getSelectedCategory().equals(\old(getSelectedCategory()));
		Optional<Category> oldOptCat = self.getSelectedCategory();

		// Exécution:
		self.selectAdState(state);

		// Post-conditions:
		// @ensures getSelectedAdState().equals(state);
		assertEquals(state, self.getSelectedAdState());
		// @ensures getSelectedCategory().equals(\old(getSelectedCategory()));
		assertEquals(oldOptCat, self.getSelectedCategory());
		// @ensures !hasPrevious();
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method selectCategory
	 *
	 * Sélectionne la catégorie spécifiée. Une nouvelle itération est initialisée
	 * pour les annonces de cette catégorie et dans l'état spécifié.
	 */
	@ParameterizedTest
	@MethodSource("UserAndCatProvider")
	public void testselectCategory(User self, Category cat) {
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
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getSelectedCategory
	 *
	 * Renvoie un {@code Optional<Category>} représentant la catégorie actuellement
	 * sélectionnée. Si aucun catégorie n'est sélectionnée, renvoie un
	 * Optional.empty().
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetSelectedCategory(User self) {
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
	@MethodSource("UserProvider")
	public void testclearSelectedCategory(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:
		// old in:@ensures getSelectedAdState().equals(\old(getSelectedAdState()));
		AdState oldState = self.getSelectedAdState();

		// Exécution:
		self.clearSelectedCategory();

		// Post-conditions:
		// @ensures getSelectedCategory().isEmpty();
		assertTrue(self.getSelectedCategory().isEmpty());
		// @ensures getSelectedAdState().equals(\old(getSelectedAdState()));
		assertEquals(oldState, self.getSelectedAdState());
		// @ensures !hasPrevious();
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getName
	 *
	 * Renvoie le nom de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetName(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getName();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPassword
	 *
	 * Renvoie le mot de passe de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetPassword(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getPassword();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getRegistrationDate
	 *
	 * Renvoie l'Instant représentant la date d'inscription de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetRegistrationDate(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getRegistrationDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getAvailableCash
	 *
	 * Renvoie la somme d'argent disponible pour cet utilisateur pour acheter des
	 * objets.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testgetAvailableCash(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getAvailableCash();

		// Post-conditions:
		// @ensures \result >= 0;
		assertTrue(result >= 0);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method buy
	 *
	 * Achète à l'utilisateur spécifié l'objet présenté dans l'annonce spécifiée.
	 * Pour le vendeur l'annonce passe de la liste des annonces OPEN à la liste des
	 * annonces CLOSED et il reçoit le montant du prix de l'objet présenté dans
	 * l'annonce. Pour ce User, l'annonce est ajoutée à la liste des annonces
	 * PURCHASE et sa somme d'argent disponible est diminuée du prix de l'objet.
	 * Pour l'acheteur et le vendeur les itérations en cours sont réinitialisées
	 * comme par un appel à startIteration.
	 */
	@ParameterizedTest
	@MethodSource("toBuyProvider")
	public void testbuy(User self, User vendor, ClassifiedAd ad) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires vendor != null;
		assumeTrue(vendor != null);
		// @requires !this.equals(vendor);
		assumeTrue(!self.equals(vendor));
		// @requires ad != null;
		assumeTrue(ad != null);
		// @requires vendor.containsInState(OPEN, ad);
		assumeTrue(vendor.containsInState(OPEN, ad));
		// @requires getAvailableCash() >= ad.getPrice();
		assumeTrue(self.getAvailableCash() >= ad.getPrice());

		// Oldies:
		// old in:@ensures size(PURCHASE, Optional.of(ad.getCategory())) ==
		// \old(size(PURCHASE, Optional.of(ad.getCategory()))) + 1;
		int oldSizeBuyerPurchase = self.size(PURCHASE, Optional.of(ad.getCategory()));
		// old in:@ensures vendor.size(OPEN, Optional.of(ad.getCategory())) ==
		// \old(vendor.size(OPEN, Optional.of(ad.getCategory()))) - 1;
		int oldSizeVendorOpen = vendor.size(OPEN, Optional.of(ad.getCategory()));
		// old in:@ensures vendor.size(CLOSED, Optional.of(ad.getCategory())) ==
		// \old(vendor.size(CLOSED, Optional.of(ad.getCategory()))) + 1;
		int oldSizeVendorClosed = vendor.size(CLOSED, Optional.of(ad.getCategory()));
		// old in:@ensures getAvailableCash() == \old(getAvailableCash()) -
		// ad.getPrice();
		int oldBuyerCash = self.getAvailableCash();
		// old in:@ensures vendor.getAvailableCash() == \old(vendor.getAvailableCash())
		int oldVendorCash = vendor.getAvailableCash();
		// +
		ad.getPrice();

		// Exécution:
		self.buy(vendor, ad);

		// Post-conditions:
		// @ensures containsInState(PURCHASE, ad);
		assertTrue(self.containsInState(PURCHASE, ad));
		// @ensures size(PURCHASE, Optional.of(ad.getCategory())) == \old(size(PURCHASE,
		// Optional.of(ad.getCategory()))) + 1;
		assertEquals(oldSizeBuyerPurchase + 1, self.size(PURCHASE, Optional.of(ad.getCategory())));
		// @ensures !vendor.containsInState(OPEN, ad);
		assertFalse(vendor.containsInState(OPEN, ad));
		// @ensures vendor.size(OPEN, Optional.of(ad.getCategory())) ==
		// \old(vendor.size(OPEN, Optional.of(ad.getCategory()))) - 1;
		assertEquals(oldSizeVendorOpen - 1, vendor.size(OPEN, Optional.of(ad.getCategory())));
		// @ensures vendor.containsInState(CLOSED, ad);
		assertTrue(vendor.containsInState(CLOSED, ad));
		// @ensures vendor.size(CLOSED, Optional.of(ad.getCategory())) ==
		// \old(vendor.size(CLOSED, Optional.of(ad.getCategory()))) + 1;
		assertEquals(oldSizeVendorClosed + 1, vendor.size(CLOSED, Optional.of(ad.getCategory())));
		// @ensures getAvailableCash() == \old(getAvailableCash()) - ad.getPrice();
		assertEquals(oldBuyerCash - ad.getPrice(), self.getAvailableCash());
		// @ensures vendor.getAvailableCash() == \old(vendor.getAvailableCash()) +
		// ad.getPrice();
		assertEquals(oldVendorCash + ad.getPrice(), vendor.getAvailableCash());
		// @ensures !hasPrevious();
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(self);
		// @ensures vendor.!hasPrevious();
		// @ensures vendor.previousIndex() == -1;
		// @ensures vendor.nextIndex() == 0;
		// @ensures vendor.lastIndex() == -1;
		assertInitIterState(vendor);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 * Crée et renvoie une nouvelle instance de ClassifiedAd dont les
	 * caractéristiques sont les arguments spécifiés et l'ajoute à la liste des
	 * annonces ouvertes (open ads) de cet utilisateur. La date de cette annonce est
	 * la date d'exécution de cette méthode. Si l'état sélectionné des annonces est
	 * OPEN, l'itération en cours est réinitialisée, comme par un appel à
	 * startIteration.
	 */
	@ParameterizedTest
	@MethodSource("UserAndCatAndNameAndPrice")
	public void testadd(User self, Category cat, String msg, int price) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires cat != null;
		assumeTrue(cat != null);
		// @requires msg != null;
		assumeTrue(msg != null);
		// @requires price > 0;
		assumeTrue(price > 0);

		// Oldies:
		// old in:@ensures size(OPEN, Optional.of(cat)) == \old(size(OPEN,
		// Optional.of(cat))) + 1;
		int oldSize = self.size(OPEN, Optional.of(cat));
		Instant oldDate = Instant.now();

		// Exécution:
		ClassifiedAd result = self.add(cat, msg, price);

		// Post-conditions:
		// @ensures get(OPEN, Optional.of(cat), 0).equals(\result);
		assertEquals(self.get(OPEN, Optional.of(cat), 0), result);
		assertEquals(self.get(OPEN, Optional.empty(), 0), result);
		// @ensures \result.getCategory().equals(cat);
		assertEquals(cat, result.getCategory());
		// @ensures \result.getDescription().equals(msg);
		assertEquals(msg, result.getDescription());
		// @ensures \result.getPrice() == price;
		assertEquals(price, result.getPrice());
		// @ensures size(OPEN, Optional.of(cat)) == \old(size(OPEN, Optional.of(cat))) +
		// 1;
		assertEquals(oldSize + 1, self.size(OPEN, Optional.of(cat)));
		// @ensures oldDate.isBefore(\result.getDate());
		assertTrue(oldDate.isBefore(result.getDate()));
		// @ensures \result.getDate().isBefore(Instant.now());
		assertTrue(result.getDate().isBefore(Instant.now()));
		// @ensures getSelectedAdState().equals(OPEN) ==> !hasPrevious();
		// @ensures getSelectedAdState().equals(OPEN) ==> previousIndex() == -1;
		// @ensures getSelectedAdState().equals(OPEN) ==> nextIndex() == 0;
		// @ensures getSelectedAdState().equals(OPEN) ==> lastIndex() == -1;
		if (self.getSelectedAdState().equals(OPEN)) {
			assertInitIterState(self);
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'annonces de cet utilisateur dans l'état sélectionné
	 * (open, closed ou purchase). Si une catégorie est sélectionnée seules les
	 * annonces de cette catégorie sont comptées.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testsize(User self) {
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
		// @ensures \result == size(getSelectedAdState(), getSelectedCategory());
		assertEquals(self.size(self.getSelectedAdState(), self.getSelectedCategory()), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method size
	 *
	 * Renvoie le nombre d'annonces de cet utilisateur dans l'état spécifié et
	 * appartenant à la catégorie spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("UserAndStateAndOptCatProvider")
	public void testsize(User self, AdState state, Optional<Category> cat) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		assumeTrue(state != null);
		// @requires cat != null;
		assumeTrue(cat != null);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.size(state, cat);

		// Post-conditions:
		// @ensures \result >= 0;
		assertTrue(result >= 0);
		// @ensures getSelectedAdState().equals(state) &&
		// getSelectedCategory().equals(cat) ==> \result == size();
		if (self.getSelectedAdState().equals(state) && self.getSelectedCategory().equals(cat)) {
			assertEquals(self.size(), result);
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie la ième plus récente annonce de ce User dans l'état et la catégorie
	 * sélectionnée.
	 * 
	 * Cette opération ne concerne les annonces dans l'état sélectionné (open,
	 * closed ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette opération ne concerne que les
	 * annonces de cette catégorie, pour les annonces dans l'état sélectionné.
	 */
	@ParameterizedTest
	@MethodSource("UserAndIntProvider")
	public void testget(User self, int i) {
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
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(get(getSelectedAdState(), getSelectedCategory(), i);
		assertEquals(self.get(self.getSelectedAdState(), self.getSelectedCategory(), i), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method get
	 *
	 * Renvoie le ième élément de la liste des annonces étant dans l'état spécifié
	 * et appartenant à la catégorie spécifiée.
	 */
	@ParameterizedTest
	@MethodSource("UserAndStateAndOptCatAndIntProvider")
	public void testget(User self, AdState state, Optional<Category> cat, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		assumeTrue(state != null);
		// @requires cat != null;
		assumeTrue(cat != null);
		// @requires i >= 0 && i < size(state, cat);
		assumeTrue(i >= 0 && i < self.size(state, cat));

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ClassifiedAd result = self.get(state, cat, i);

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures containsInState(state, \result);
		assertTrue(self.containsInState(state, result));
		// @ensures cat.isPresent() ==> \result.getCategory().equals(cat.get());
		if (cat.isPresent()) {
			assertEquals(cat.get(), result.getCategory());
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method containsInState
	 *
	 * Renvoie true si ce User possède parmi les annonces dans l'état spécifié,
	 * l'objet spécifié.
	 */
	@ParameterizedTest
	@MethodSource("UserAndStateAndObjProvider")
	public void testcontainsInState(User self, AdState state, Object obj) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires state != null;
		assumeTrue(state != null);

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.containsInState(state, obj);

		// Post-conditions:
		// @ensures !(obj instanceof ClassifiedAd) ==> !\result;
		if (!(obj instanceof ClassifiedAd)) {
			assertFalse(result);
		}
		// @ensures \result <==> (\exists int i; i >= 0 && i < size(state,
		// Optional.empty()); get(state, Optional.empty(), i).equals(obj));
		boolean found = false;
		for (int i = 0; i < self.size(state, Optional.empty()) && !found; i++) {
			found = self.get(state, Optional.empty(), i).equals(obj);
		}
		assertEquals(found, result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method startIteration
	 *
	 * Initialise ce User pour le démarrage d'une nouvelle itération sur les
	 * annonces de ce User. Cette itération s'effectue à partir des annonces les
	 * plus récentes, de sorte que chaque appel à next() renvoie une annonce plus
	 * ancienne.
	 * 
	 * Cette itération ne concerne que les annonces dans l'état sélectionné (open,
	 * closed ou purchase).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void teststartIteration(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		self.startIteration();

		// Post-conditions:
		// @ensures !hasPrevious();
		// @ensures previousIndex() == -1;
		// @ensures nextIndex() == 0;
		// @ensures lastIndex() == -1;
		assertInitIterState(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasNext
	 *
	 * Renvoie true si ce User possède une annonce plus ancienne pour l'itération en
	 * cours.
	 * 
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testhasNext(User self) {
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
		// @ensures \result <==> nextIndex() < size();
		assertEquals(self.nextIndex() < self.size(), result);

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
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testnext(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		assumeTrue(self.hasNext());

		// Oldies:
		// old in:@ensures \result.equals(\old(get(nextIndex())));
		int oldNextIndex = self.nextIndex();
		// old in:@ensures nextIndex() == \old(nextIndex()) + 1;
		// old in:@ensures previousIndex() == \old(previousIndex()) + 1;
		int oldPreviousIndex = self.previousIndex();
		// old in:@ensures previousIndex() == \old(nextIndex());
		// old in:@ensures lastIndex() == \old(nextIndex());

		// Exécution:
		ClassifiedAd result = self.next();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(\old(get(nextIndex())));
		assertEquals(self.get(oldNextIndex), result);
		// @ensures \result.equals(get(lastIndex()));
		assertEquals(self.get(self.lastIndex()), result);
		// @ensures getSelectedCategory().isPresent() ==>
		// \result.getCategory().equals(getSelectedCategory().get());
		if (self.getSelectedCategory().isPresent()) {
			assertEquals(self.getSelectedCategory().get(), result.getCategory());
		}
		// @ensures containsInState(getSelectedAdState(), \result);
		assertTrue(self.containsInState(self.getSelectedAdState(), result));
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
	 * next(). Si l'itération est arrivée à la fin la valeur renvoyée est size().
	 * 
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testnextIndex(User self) {
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
		// @ensures \result == size() <==> !hasNext();
		assertEquals(!self.hasNext(), result == self.size());
		// @ensures hasNext() <==> \result >= 0 && \result < size();
		assertEquals(self.hasNext(), result >= 0 && result < self.size());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasPrevious
	 *
	 * Renvoie true si ce User possède une annonce plus récente pour l'itération en
	 * cours.
	 * 
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testhasPrevious(User self) {
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
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testprevious(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		assumeTrue(self.hasPrevious());

		// Oldies:
		// old in:@ensures \result.equals(\old(get(previousIndex())));
		int oldPreviousIndex = self.previousIndex();
		// old in:@ensures nextIndex() == \old(nextIndex()) - 1;
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex()) - 1;
		// old in:@ensures nextIndex() == \old(previousIndex());
		// old in:@ensures lastIndex() == \old(previousIndex());

		// Exécution:
		ClassifiedAd result = self.previous();

		// Post-conditions:
		// @ensures \result.equals(\old(get(previousIndex())));
		assertEquals(self.get(oldPreviousIndex), result);
		// @ensures \result.equals(get(lastIndex()));
		assertEquals(self.get(self.lastIndex()), result);
		// @ensures getSelectedCategory().isPresent() ==>
		// \result.getCategory().equals(getSelectedCategory().get());
		if (self.getSelectedCategory().isPresent()) {
			assertEquals(self.getSelectedCategory().get(), result.getCategory());
		}
		// @ensures containsInState(getSelectedAdState(), \result);
		assertTrue(self.containsInState(self.getSelectedAdState(), result));
		// @ensures nextIndex() == \old(nextIndex()) - 1;
		assertEquals(oldNextIndex - 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex()) - 1;
		assertEquals(oldPreviousIndex - 1, self.previousIndex());
		// @ensures nextIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.nextIndex());
		// @ensures lastIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.lastIndex());
		// @ensures lastIndex() == nextIndex();
		assertEquals(self.lastIndex(), self.nextIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previousIndex
	 *
	 * Renvoie l'index de l'annonce qui sera renvoyée par le prochain appel à
	 * previous(). Si l'itération est arrivée au début la valeur renvoyée est -1.
	 * 
	 * Cette itération ne concerne que le type d'annonces sélectionné (open ads,
	 * closed ads ou purchases).
	 * 
	 * Si une catégorie est sélectionnée, cette itération ne concerne que les
	 * annonces de cette catégorie.
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testpreviousIndex(User self) {
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
		// @ensures \result == -1 <==> !hasPrevious();
		assertEquals(!self.hasPrevious(), result == -1);
		// @ensures hasPrevious() <==> \result >= 0 && \result < size();
		assertEquals(self.hasPrevious(), result >= 0 && result < self.size());

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
	@MethodSource("UserProvider")
	public void testlastIndex(User self) {
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
	 * Test method for method toString
	 *
	 * Renvoie une chaîne de caractères contenant le nom de ce User ainsi que le
	 * nombre d'annonces de cet utilisateur dans les toirs états possibles (OPEN,
	 * CLOSED, PURCHASE).
	 */
	@ParameterizedTest
	@MethodSource("UserProvider")
	public void testtoString(User self) {
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
		// @ensures \result.contains(getName());
		assertTrue(result.contains(self.getName()));
		// @ensures \result.contains("" + size(OPEN, Optional.empty()));
		assertTrue(result.contains("" + self.size(OPEN, Optional.empty())));
		// @ensures \result.contains("" + size(CLOSED, Optional.empty()));
		assertTrue(result.contains("" + self.size(CLOSED, Optional.empty())));
		// @ensures \result.contains("" + size(PURCHASE, Optional.empty()));
		assertTrue(result.contains("" + self.size(PURCHASE, Optional.empty())));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for User
