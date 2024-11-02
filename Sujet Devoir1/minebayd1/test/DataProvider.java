/**
 * 
 */
package minebayd1.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


import minebayd1.AdState;
import minebayd1.CategorizedAdList;
import minebayd1.Category;
import minebayd1.ClassifiedAd;
import minebayd1.User;
import static minebayd1.Category.*;
import static minebayd1.AdState.*;

/**
 * 
 */
public class DataProvider {
	static final int LG_STREAM = 100;
	private static List<String> goodUserNames = Arrays.asList("Marcel", "Adam", "Sonia", "Idir", "Mohamed", "Marc",
			"Ali", "Ziad", "Lyes", "Ayman", "Mounir", "Pierre", "Chanez", "Lamia", "Yanis", "Faycal", "Boris", "Imam",
			"Naila", "Zahra", "Rosa", "Lisa", "Sheraz", "Nima", "Aliou", "Issa", "Mamadou", "Ismael");
	private static List<String> badNames = Arrays.asList("", " ", "  ", "\n \t", null);
	private static List<User> allUsers;
	private static List<ClassifiedAd> allAds;
	private static Random randGen = new Random();

	static {
		// System.out.println("Début init User");
		initUsers(goodUserNames);
		// System.out.println("Fin init User");
	}

	private static void initUsers(List<String> userNames) {
		allUsers = new ArrayList<User>(userNames.size());
		for (String name : userNames) {
			allUsers.add(new User(name, "pass" + name));
		}
		addAdsToUsers(allUsers);
		addPurchaseToUsers(allUsers);
		someIter(allUsers);
	}

	private static void addAdsToUsers(List<User> userList) {
		int nbMsg = 1500;
		allAds = new ArrayList<ClassifiedAd>(nbMsg);
		while (nbMsg > 0) {
			User u = getRandomElt(userList);
			allAds.add(
					u.add(enumSupplier(Category.class), "Annonce n°" + nbMsg + " from " + u.getName(), randInt(100) + 1));
			nbMsg--;
		}
	}

	private static void addPurchaseToUsers(List<User> userList) {
		int nbPurchase = 400;
		while (nbPurchase > 0) {
			User buyer = getRandomElt(userList);
			User vendor = getRandomElt(userList);
			if (buyer != vendor && buyer.getAvailableCash() > 100 && vendor.size() > 0) {
				ClassifiedAd ad = vendor.get(randInt(vendor.size()));
				buyer.buy(vendor, ad);
				nbPurchase -= 3;
			} else {
				nbPurchase--;
			}
		}
	}

	private static void someIter(List<User> userList) {
		int nbUsers = userList.size();
		while (nbUsers > 0) {
			User user = getRandomElt(userList);
			user.selectAdState(enumSupplier(AdState.class));

			user.selectCategory(enumSupplier(Category.class));
			int nbSteps = randInt(5);
			while (user.hasNext() && nbSteps > 0) {
				user.next();
				nbSteps--;
			}
			nbUsers--;
		}
	}

	public static List<User> allUser() {
		return Collections.unmodifiableList(allUsers);
	}

	public static ClassifiedAd adSupplier() {
		return getRandomElt(allAds);
	}

	public static CategorizedAdList CategorizedAdListSupplier() {
		CategorizedAdList result = new CategorizedAdList();
		int count = randInt(5);
		int index = randInt(allAds.size());
		while (count > 0 && index < allAds.size()) {
			result.add(allAds.get(index));
			index += randInt(5) + 1;
			count--;
		}
		if (randBool(3)) {
			result.selectCategory(enumSupplier(Category.class));
		}
		int nbSteps = randInt(5);
		while (result.hasNext() && nbSteps > 0) {
			result.next();
			nbSteps--;
		}
		return result;
	}

	public static <T extends Enum<T>> T enumSupplier(Class<T> enumClass) {
		T[] values = enumClass.getEnumConstants();
		return values[randInt(values.length)];
	}

	public static User userSupplier() {
		return getRandomElt(allUsers);
	}



	public static String stringSupplier() {
		if (randBool(50)) {
			return getRandomElt(badNames);
		}
		return getRandomElt(goodUserNames);
	}

	/**
	 * Renvoie un élément tiré aléatoirement parmi les éléments de la collection
	 * spécifiée.
	 *
	 * @requires c != null;
	 * @requires !c.isEmpty();
	 * @ensures c.contains(\result);
	 *
	 * @param <T> Type des éléments de la collection spécifiée
	 * @param c   collection dans laquelle est choisi l'élément retourné
	 *
	 * @return un élément tiré aléatoirement parmi les éléments de la collection
	 *         spécifiée
	 * 
	 * @throws NullPointerException     si l'argument spécifié est null
	 * @throws IllegalArgumentException si l'argument spécifié est vide
	 */
	public static <T> T getRandomElt(Collection<T> c) {
		int index = randInt(c.size());
		if (c instanceof List<?>) {
			return ((List<T>) c).get(index);
		}
		int i = 0;
		for (T elt : c) {
			if (i == index) {
				return elt;
			}
			i++;
		}
		throw new NoSuchElementException(); // Ne peut pas arriver
	}

	/**
	 * Renvoie un int obtenue par un générateur pseudo-aléatoire.
	 *
	 * @param max la valeur maximale du nombre aléatoire attendu
	 *
	 * @return un entier >= 0 et < max
	 *
	 * @throws IllegalArgumentException si max <= 0
	 *
	 * @requires max > 0;
	 * @ensures \result >= 0;
	 * @ensures \result < max;
	 */
	public static int randInt(int max) {
		return randGen.nextInt(max);
	}

	/**
	 * Renvoie une valeur booléenne obtenue par un générateur pseudo-aléatoire. La
	 * valeur renvoyée a une probabilité d'être true similaire à la probabilité que
	 * randInt(max) renvoie la valeur 0.
	 *
	 * @return une valeur booléenne aléatoire
	 * 
	 * @throws IllegalArgumentException si max <= 0
	 * 
	 * @requires max > 0;
	 */
	public static boolean randBool(int max) {
		return randGen.nextInt(max) == 0;
	}
}
