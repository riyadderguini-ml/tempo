/**
 * @author Riyad Derguini 12312754
 * Je déclare qu'il s'agit de mon propre travail.
 */
package minebayd1;

import java.lang.classfile.ClassBuilder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Iterator;

import static minebayd1.Category.*;

/**
 * Gestion d'une liste de ClassifiedAd triée selon la date de création (de la
 * plus récente à la plus ancienne).
 * 
 * Une CategorizedAdList ne peut pas contenir
 * d'éléments dupliqués, plus formelement, pour tout couple d'entiers (i, j)
 * tels que: <br/>
 * i >= 0 && j >= 0 && i != j && i < size() && j < size() on a
 * !get(i).equals(get(j)).
 * 
 * 
 * Une CategorizedAdList prend en compte les catégories des annonces en
 * permettant:
 * <ul>
 * <li>soit un parcours des annonces de toutes les catégories</li>
 * <li>soit un parcours des seules annonces d'une catégorie sélectionnée</li>
 * </ul>
 * 
 * 
 * Dans ce but, CategorizedAdList utilise une liste distincte (une ArrayList)
 * pour chaque catégorie.
 * 
 * 
 * Les méthodes selectCategory, getSelectedCategory et
 * clearCatégory permettent de sélectionner ou déselectionner une catégorie
 * particulière.
 * 
 * 
 * Lorsqu'une catégorie est sélectionnée, toutes les opérations
 * relatives aux itérations (startIteration, next, previous, ...) agissent
 * uniquement sur les annonces de cette catégorie.
 * 
 * 
 * Pour les autres méthodes, celles possédant un paramètre de type Category
 * agissent uniquement sur cette catégorie, les autres méthodes agissant sur
 * toutes les annonces.
 * 
 * 
 * Les méthodes modifiant le contenu de cette liste (add et remove), entraine
 * une réinitialisation de l'itération en cours (même effet qu'un appel à la
 * méthode startIteration).
 * 
 * @invariant \var Optional&lt;Category&gt; optCat = getSelectedCategory();
 * @invariant getSelectedCategory() != null;
 * @invariant (\forall int i, j; i >= 0 && i < j && j < size();
 *            get(i).isAfter(get(j)));
 * @invariant (\forall Category cat;true; <br/>
 *            (\forall int i, j; i >= 0 && i < j && j < size(cat); <br/>
 *            get(cat, i).isAfter(get(cat, j))));
 * @invariant size() == (\sum Category cat;true;size(cat));
 * @invariant nextIndex() >= 0 && previousIndex() >= -1;
 * @invariant nextIndex() <= size();
 * @invariant previousIndex() < size();
 * @invariant optCat.isPresent() ==> nextIndex() <= size(optCat.get());
 * @invariant optCat.isPresent() ==> previousIndex() < size(optCat.get());
 * @invariant nextIndex() == previousIndex() + 1;
 * @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex();
 * @invariant !hasPrevious() <==> previousIndex() == -1;
 * @invariant optCat.isEmpty() ==> (!hasNext() <==> nextIndex() == size());
 * @invariant optCat.isPresent() ==> <br/>
 *            (!hasNext() <==> nextIndex() == size(optCat.get()));
 * @invariant !contains(null);
 * @invariant (\forall int i, j; i >= 0 && i < j && j < size(); <br/>
 *            (!get(i).equals(get(j)));
 * 
 * @author Marc Champesme
 * @since 27/09/2024
 * @version 20/10/2024
 */
public class CategorizedAdList implements Cloneable {

	private ArrayList<ClassifiedAd>[] tabCatAd; // tableau d'instance de ArrayList<ClassifiedAd>.
	private Optional<Category> SelectedCategory; // pourra etre accedder par la methode getSelectedCategory().
	private ClassifiedAd AdPointedTo;
	private int IndxAdPointedTo;

	/**
	 * Initialise une nouvelle instance ne contenant aucune annonce.
	 * 
	 * @ensures size() == 0;
	 * @ensures getSelectedCategory() != null;
	 * @ensures getSelectedCategory().isEmpty();
	 * @ensures !hasPrevious();
	 * @ensures !hasNext();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	public CategorizedAdList() {
		this.tabCatAd = new ArrayList[5];

		for (int i = 0; i < this.tabCatAd.length; i++) {
			this.tabCatAd[i] = new ArrayList<ClassifiedAd>();
		}

		this.SelectedCategory = Optional.empty();

	}

	/**
	 * Sélectionne la catégorie sur laquelle sera effectuée la prochaine itération.
	 * Effectue les initialisations nécessaire pour démarrer une nouvelle itération
	 * de la même manière qu'un appel à startIteration().
	 * 
	 * @param cat la catégorie d'annonce à sélectionner
	 * 
	 * @requires cat != null;
	 * @ensures getSelectedCategory().isPresent();
	 * @ensures getSelectedCategory().get().equals(cat);
	 * @ensures !hasPrevious();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	public void selectCategory(Category cat) {
		this.SelectedCategory = Optional.ofNullable(cat);
		this.startIteration();
	}

	/**
	 * Renvoie un Optional pour la catégorie sélectionnée. Si une catégorie a été
	 * sélectionnée \result.isPresent() est true; sinon \result.isEmpty() est true.
	 * 
	 * @return un Optional pour la catégorie sélectionnée
	 * 
	 * @ensures \result != null;
	 * 
	 * @pure
	 */
	public Optional<Category> getSelectedCategory() {
		return this.SelectedCategory;
	}

	/**
	 * Déselectionne la catégorie sélectionnée et effectue les initialisations
	 * nécessaire pour démarrer une nouvelle itération (cf.startIteration()). Si
	 * aucune catégorie n'était sélectionnée l'appel à cette méthode est équivalent
	 * à un appel à startIteration.
	 * 
	 * @ensures getSelectedCategory().isEmpty();
	 * @ensures !hasPrevious();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	public void clearSelectedCategory() {
		this.SelectedCategory = Optional.empty(); // Optional.empty() retourne une instance vide d'un Optional object.
		this.startIteration();
	}

	/**
	 * Initialise cette liste pour le démarrage d'une nouvelle itération sur les
	 * annonces de cette liste. Cette itération s'effectue à partir de l'annonce la
	 * plus récente, de sorte que chaque appel à next() renvoie une annonce plus
	 * ancienne.
	 * 
	 * Si une catégorie est sélectionnée, l'itération ne concernera que les annonces
	 * de cette catégorie; sinon l'itération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @ensures !hasPrevious();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	public void startIteration() {
		// if no category was selected i just need to point to the newest ad.
		if (!(this.getSelectedCategory().isPresent())) {
			this.AdPointedTo = this.tabCatAd[0].get(0);
			for (int i = 0; i < this.tabCatAd.length; i++) {
				for (int j = 0; j < this.tabCatAd[i].size(); j++) {
					if (this.AdPointedTo.isAfter(tabCatAd[i].get(j))) {
						this.AdPointedTo = this.tabCatAd[i].get(j);
					}
				}
			}
		}

		else {
			// if a category is selected i need to point to the newest Ad of that specific,
			// category

			int indx = 0;

			while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
				indx = indx + 1;
			}

			this.AdPointedTo = this.tabCatAd[indx].get(0);
			for (int j = 0; j < this.tabCatAd[indx].size(); j++) {
				if (this.AdPointedTo.isAfter(tabCatAd[indx].get(j))) {
					this.AdPointedTo = this.tabCatAd[indx].get(j);
				}
			}

		}

		this.IndxAdPointedTo = -1;

	}

	/**
	 * Renvoie true si cette liste possède une annonce plus ancienne pour
	 * l'itération en cours.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return true si cette liste possède une annonce plus ancienne pour
	 *         l'itération en cours
	 * 
	 * @ensures \var Optional&lt;Category&gt; optCat = getSelectedCategory();
	 * @ensures optCat.isEmpty() ==> (\result <==> nextIndex() < size());
	 * @ensures optCat.isEmpty() ==> (!\result <==> nextIndex() == size());
	 * @ensures optCat.isPresent() ==> (\result <==> nextIndex() <
	 *          size(optCat.get()));
	 * @ensures optCat.isPresent() ==> (!\result <==> nextIndex() ==
	 *          size(optCat.get()));
	 * 
	 * @pure
	 */
	public boolean hasNext() {

		if (size() == 0) {
			return false;
		}

		if (!(this.getSelectedCategory().isPresent())) {

			for (int i = 0; i < this.tabCatAd.length; i++) {
				for (int j = 0; j < this.tabCatAd[i].size(); j++) {
					if (this.AdPointedTo.isBefore(tabCatAd[i].get(j))) {
						return true;
					}
				}
			}
		}

		else {
			// if a category is selected.

			int indx = 0;

			while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
				indx = indx + 1;
			}

			for (int j = 0; j < this.tabCatAd[indx].size(); j++) {
				if (this.AdPointedTo.isBefore(tabCatAd[indx].get(j))) {
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Renvoie l'annonce suivante (plus ancienne) dans l'itération en cours et
	 * avance d'un élément dans l'itération.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return l'annonce suivante (plus ancienne) dans l'itération en cours
	 * 
	 * @requires hasNext();
	 * @ensures \var Optional&lt;Category&gt; optCat = getSelectedCategory();
	 * @ensures \result != null;
	 * @ensures optCat.isEmpty() ==> \result.equals(\old(get(nextIndex())));
	 * @ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
	 *          nextIndex())));
	 * @ensures optCat.isPresent() ==> \result.getCategory().equals(optCat.get());
	 * @ensures nextIndex() == \old(nextIndex()) + 1;
	 * @ensures previousIndex() == \old(previousIndex()) + 1;
	 * @ensures previousIndex() == \old(nextIndex());
	 * @ensures lastIndex() == \old(nextIndex());
	 * @ensures lastIndex() == previousIndex();
	 */
	public ClassifiedAd next() {

		// for clearence :

		// isBefore(ad) : Teste si cette annonce a été publiée avant l'annonce
		// spécifiée.

		// isAfter(ad) : Teste si cette annonce a été publié après l'annonce spécifiée.
		ClassifiedAd nextJustOlderAd = null;

		// in the case where no cat is selected.
		if (!(this.getSelectedCategory().isPresent())) {

			for (int i = 0; i < this.tabCatAd.length; i++) {
				for (int j = 0; j < this.tabCatAd[i].size(); j++) {
					// Find an Ad older than the AdPointedto.
					if (this.AdPointedTo.isAfter(tabCatAd[i].get(j))) {
						// Update nextJustOlderAd if we haven't set it yet, or if we found a newer
						// "Older" Ad.
						if (nextJustOlderAd == null || tabCatAd[i].get(j).isAfter(nextJustOlderAd)) {
							nextJustOlderAd = tabCatAd[i].get(j);
						}

					}
				}
			}

			this.IndxAdPointedTo += 1;
			return nextJustOlderAd;

		}

		// if a category is selected.
		else {

			int indx = 0;

			while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
				indx = indx + 1;
			}

			for (int j = 0; j < this.tabCatAd[indx].size(); j++) {
				// Find an Ad older than the AdPointedto.
				if (this.AdPointedTo.isAfter(tabCatAd[indx].get(j))) {
					// Update nextJustOlderAd if we haven't set it yet, or if we found a newer
					// "Older" Ad.
					if (nextJustOlderAd == null || tabCatAd[indx].get(j).isAfter(nextJustOlderAd)) {
						nextJustOlderAd = tabCatAd[indx].get(j);
					}

				}
			}

			this.IndxAdPointedTo += 1;
			return nextJustOlderAd;

		}

	}

	/**
	 * Renvoie l'index de l'annonce qui sera renvoyée par le prochain appel à
	 * next(). Si l'itération est arrivée à la fin la valeur renvoyée est size() (ou
	 * size(getSelectedCategory().get()) si une catégorie est sélectionnée).
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return l'index de l'annonce qui sera renvoyé par le prochain appel à next();
	 *         ou size() s'il n'y a pas d'élément suivant
	 * 
	 * @ensures \var Optional&lt;Category&gt; optCat = getSelectedCategory();
	 * @ensures optCat.isEmpty() ==> (\result == size() <==> !hasNext());
	 * @ensures optCat.isPresent() ==> (\result == size(optCat.get()) <==>
	 *          !hasNext());
	 * @ensures optCat.isEmpty() ==> (hasNext() <==> \result >= 0 && \result <
	 *          size());
	 * @ensures optCat.isPresent() ==> (hasNext() <==> \result >= 0 && \result <
	 *          size(optCat.get()));
	 * 
	 * @pure
	 */
	public int nextIndex() {
		if (this.getSelectedCategory().isEmpty()) {
			if (!(hasNext())) {
				return size();
			}
		}

		if (this.getSelectedCategory().isPresent()) {
			if (!(hasNext())) {
				return size(this.SelectedCategory
						.get());
			}
		}

		return ++this.IndxAdPointedTo;
	}

	/**
	 * Renvoie true si cette liste possède une annonce plus récente pour l'itération
	 * en cours.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return true si cette liste possède une annonce plus récente pour l'itération
	 *         en cours
	 * 
	 * @ensures \result <==> previousIndex() >= 0;
	 * @ensures !\result <==> previousIndex() == -1;
	 * 
	 * @pure
	 */
	public boolean hasPrevious() {

		if (size() == 0) {
			return false;
		}

		if (!(this.getSelectedCategory().isPresent())) {

			for (int i = 0; i < this.tabCatAd.length; i++) {
				for (int j = 0; j < this.tabCatAd[i].size(); j++) {
					if (this.AdPointedTo.isAfter(tabCatAd[i].get(j))) {
						return true;
					}
				}
			}
		}

		else {
			// if a category is selected.

			int indx = 0;

			while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
				indx = indx + 1;
			}

			for (int j = 0; j < this.tabCatAd[indx].size(); j++) {
				if (this.AdPointedTo.isAfter(tabCatAd[indx].get(j))) {
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Renvoie l'annonce précedente (plus récente) dans l'itération en cours et
	 * recule d'un élément dans l'itération.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return l'annonce précedente (plus récente) dans l'itération en cours
	 * 
	 * 
	 * @requires hasPrevious();
	 * @ensures \var Optional&lt;Category&gt; optCat = getSelectedCategory();
	 * @ensures optCat.isEmpty() ==> \result.equals(\old(get(previousIndex())));
	 * @ensures optCat.isPresent() ==> \result.equals(\old(get(optCat.get(),
	 *          previousIndex())));
	 * @ensures optCat.isPresent() ==> \result.getCategory().equals(optCat.get());
	 * @ensures nextIndex() == \old(nextIndex()) - 1;
	 * @ensures previousIndex() == \old(previousIndex()) - 1;
	 * @ensures nextIndex() == \old(previousIndex());
	 * @ensures lastIndex() == \old(previousIndex());
	 * @ensures lastIndex() == nextIndex();
	 * 
	 */
	public ClassifiedAd previous() {
		// for clearence :

		// isBefore(ad) : Teste si cette annonce a été publiée avant l'annonce
		// spécifiée.

		// isAfter(ad) : Teste si cette annonce a été publié après l'annonce spécifiée.
		ClassifiedAd nextJustNewerAd = null;

		// in the case where no cat is selected.
		if (!(this.getSelectedCategory().isPresent())) {

			for (int i = 0; i < this.tabCatAd.length; i++) {
				for (int j = 0; j < this.tabCatAd[i].size(); j++) {
					// Find an Ad newer than the AdPointedto.
					if (this.AdPointedTo.isBefore(tabCatAd[i].get(j))) {

						if (nextJustNewerAd == null || tabCatAd[i].get(j).isBefore(nextJustNewerAd)) {
							nextJustNewerAd = tabCatAd[i].get(j);
						}

					}
				}
			}

			this.IndxAdPointedTo -= 1;
			return nextJustNewerAd;

		}

		// if a category is selected.
		else {

			int indx = 0;

			while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
				indx = indx + 1;
			}

			for (int j = 0; j < this.tabCatAd[indx].size(); j++) {

				if (this.AdPointedTo.isBefore(tabCatAd[indx].get(j))) {

					if (nextJustNewerAd == null || tabCatAd[indx].get(j).isBefore(nextJustNewerAd)) {
						nextJustNewerAd = tabCatAd[indx].get(j);
					}

				}
			}

			this.IndxAdPointedTo -= 1;
			return nextJustNewerAd;

		}
	}

	/**
	 * Renvoie l'index de l'annonce qui sera renvoyée par le prochain appel à
	 * previous(). Si l'itération est arrivée au début la valeur renvoyée est -1.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return l'index de l'annonce qui sera renvoyé par le prochain appel à
	 *         previous(); ou -1
	 * 
	 * @ensures \var Optional&lt;Category&gt; optCat = getSelectedCategory();
	 * @ensures \result == -1 <==> !hasPrevious();
	 * @ensures optCat.isEmpty() ==> (hasPrevious() <==> \result >= 0 && \result <
	 *          size());
	 * @ensures optCat.isPresent() ==> (hasPrevious() <==> \result >= 0 && \result <
	 *          size(optCat.get()));
	 * 
	 * @pure
	 */
	public int previousIndex() {
		if (!(hasPrevious())) {
			return -1;
		}

		return --this.IndxAdPointedTo;
	}

	/**
	 * Renvoie l'index de l'annonce qui a été renvoyée par le dernier appel à
	 * previous() ou next(). Si next() ou previous() n'ont pas été appelé depuis le
	 * dernier appel à startIteration() la valeur renvoyée est -1.
	 * 
	 * Si une catégorie est sélectionnée, l'opération ne concernera que les annonces
	 * de cette catégorie; sinon l'opération concerne toutes les annonces, quelque
	 * soit leur catégorie.
	 * 
	 * @return l'index de l'annonce qui a été renvoyé par le dernier appel à
	 *         previous() ou next(); ou -1
	 * 
	 * @ensures \result == nextIndex() || \result == previousIndex();
	 * 
	 * @pure
	 */
	public int lastIndex() {
		return this.IndxAdPointedTo;
	}

	/**
	 * Renvoie l'élément d'index spécifié dans la liste des annonces de la catégorie
	 * spécifiée. À la différence de la méthode get(int i), cette implémentation
	 * effectue un accès direct au ième élément et peut donc être utilisée sans
	 * pénalité pour effectuer une itération sur les éléments de la catégorie
	 * spécifiée.
	 * 
	 * @param cat catégorie dans la quelle l'élement est recherché
	 * @param i   rang de l'élément cherché dans la liste des éléments de la
	 *            catégorie spécifiée
	 * @return l'élément d'index spécifié dans la liste des annonces de la catégorie
	 *         spécifiée
	 * 
	 * @requires cat != null;
	 * @requires i >= 0 && i < size(cat);
	 * @ensures contains(\result);
	 * @ensures \result.getCategory().equals(cat);
	 * 
	 * @pure
	 */
	public ClassifiedAd get(Category cat, int i) {

		// selectCategory(cat);

		int indx = 0;

		while (!(this.tabCatAd[indx].get(0).getCategory().equals(this.getSelectedCategory().get()))) {
			indx = indx + 1;
		}

		return this.tabCatAd[0].get(i);
	}

	/**
	 * Renvoie l'élément d'index spécifié dans la liste des annonces. Cette
	 * implémentation n'est pas adaptée à une utilisation pour effectuer des
	 * itérations sur l'ensemble de cette liste. Pour effectuer une itération, il
	 * est fortement recommandé d'utiliser les méthodes prévues à cet effet
	 * (startIteration, hasNext, next, hasPrevious, previous, ...).
	 * 
	 * @param i rang de l'élément cherché dans la liste des éléments
	 * @return l'élément d'index spécifié dans la liste des annonces
	 * 
	 * @requires i >= 0 && i < size();
	 * @ensures contains(\result);
	 * @ensures (\exists int i; i >= 0 && i < size(\result.getCategory());
	 *          \result.equals(get(\result.getCategory(), i)));
	 * 
	 * @pure
	 */
	public ClassifiedAd get(int i) {

		startIteration();

		while (hasNext() && this.IndxAdPointedTo != i) {
			next();
		}

		return this.AdPointedTo;
	}

	/**
	 * Ajoute l'élement spécifié dans cette liste. L'élement est inséré dans la
	 * liste de manière à ce que la liste reste triée. L'itération en cours est
	 * réinitialisée.
	 * 
	 * @param elt l'élement à ajouter
	 * 
	 * @requires elt != null;
	 * @requires !contains(elt);
	 * @ensures contains(elt);
	 * @ensures size() == \old(size()) + 1;
	 * @ensures size(elt.getCategory()) == \old(size(elt.getCategory())) + 1;
	 * @ensures (\forall Category cat; !cat.equals(elt.getCategory()); size(cat) ==
	 *          \old(size(cat)));
	 * @ensures !hasPrevious();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	public void add(ClassifiedAd elt) {

		int indx = 0;

		while (!(this.tabCatAd[indx].get(0).getCategory().equals(elt.getCategory()))) {
			indx = indx + 1;
		}

		this.tabCatAd[indx].add(elt);

		startIteration();
	}

	/**
	 * Retire une occurence de l'élement spécifié de cette liste s'il y était
	 * présent. Renvoie true si l'élement a effectivement été retiré de cette liste.
	 * Si l'élément spécifié est effectivement retiré, l'itération en cours est
	 * réinitialisée.
	 * 
	 * @param o l'élement à retirer de cette liste
	 * 
	 * @return true si l'élement a effectivement été retiré de cette liste; false
	 *         sinon
	 * 
	 * @ensures !contains(o);
	 * @ensures \result <==> \old(contains(o));
	 * @ensures \result <==> (size() == \old(size()) - 1);
	 * @ensures !\result <==> (size() == \old(size());
	 * @ensures \result <==> (o instanceof ClassifiedAd) <br/>
	 *          && (size(((ClassifiedAd) o).getCategory()) <br/>
	 *          == \old(size(((ClassifiedAd) o).getCategory())) - 1);
	 * @ensures (o instanceof ClassifiedAd) ==> (\forall Category cat;<br/>
	 *          !cat.equals(((ClassifiedAd) o).getCategory()); <br/>
	 *          size(cat) == \old(size(cat)));
	 * @ensures \result ==> !hasPrevious();
	 * @ensures \result ==> previousIndex() == -1;
	 * @ensures \result ==> nextIndex() == 0;
	 * @ensures \result ==> lastIndex() == -1;
	 * @ensures !\result ==> (hasPrevious() == \old(hasPrevious()));
	 * @ensures !\result ==> (previousIndex() == \old(previousIndex()));
	 * @ensures !\result ==> (nextIndex() == \old(nextIndex()));
	 * @ensures !\result ==> (lastIndex() == \old(lastIndex()));
	 * 
	 */
	public boolean remove(Object o) {

		int indx = 0;

		ClassifiedAd ad = (ClassifiedAd) o;

		while (!(this.tabCatAd[indx].get(0).getCategory().equals(ad.getCategory()))) {
			indx = indx + 1;
		}

		if (this.tabCatAd[indx].contains(o)) {
			this.tabCatAd[indx].remove(o);
			return true;
		}

		return false;
	}

	/**
	 * Renvoie true si et seulement si l'argument spécifié est présent dans cette
	 * liste.
	 * 
	 * @param o l'objet dont on veut tester la présence dans cette liste
	 * 
	 * @return true si l'argument spécifié est présent dans cette liste; false sinon
	 * 
	 * @ensures \result <==> (\exists int i; i >= 0 && i < size();
	 *          get(i).equals(o));
	 * @ensures \result <==> (o instanceof ClassifiedAd) <br/>
	 *          && (\exists int i; i >= 0 && i < size((ClassifiedAd)
	 *          o).getCategory()); <br/>
	 *          get(((ClassifiedAd) o).getCategory(), i).equals(o));
	 * 
	 * @pure
	 */
	public boolean contains(Object o) {

		int indx = 0;

		ClassifiedAd ad = (ClassifiedAd) o;

		while (!(this.tabCatAd[indx].get(0).getCategory().equals(ad.getCategory()))) {
			indx = indx + 1;
		}

		if (this.tabCatAd[indx].contains(o)) {

			return true;
		}

		return false;
	}

	/**
	 * Renvoie le nombre total d'éléments de cette liste.
	 * 
	 * @return le nombre total d'éléments de cette liste
	 * 
	 * @ensures \result >= 0;
	 * @ensures \result == (\sum Category cat;true;size(cat));
	 * 
	 * @pure
	 */
	public int size() {

		int cmp = 0;

		for (int i = 0; i < this.tabCatAd.length; ++i) {
			cmp = this.tabCatAd[i].size() + cmp;
		}

		return cmp;
	}

	/**
	 * Renvoie le nombre d'annonces de cette liste appartenant à la catégorie
	 * spécifiée.
	 * 
	 * @param cat catégorie dont on souhaite connaître le nombre d'éléments
	 * 
	 * @return le nombre d'annonces de cette liste appartenant à la catégorie
	 *         spécifiée
	 * 
	 * @requires cat != null;
	 * @ensures \result >= 0 && \result <= size();
	 * @ensures \result == (\sum int i; i >= 0 && i < size() &&
	 *          get(i).getCategory().equals(cat); 1);
	 * 
	 * @pure
	 */
	public int size(Category cat) {
	

		int indx = 0;

		while (!(this.tabCatAd[indx].get(0).getCategory().equals(cat))) {
			indx = indx + 1;
		}

		return this.tabCatAd[indx].size();
	}

	/**
	 * Renvoie true si et seulement si l'argument spécifié est une CategorizedAdList
	 * contenant les mêmes éléments dans le même ordre que cette CategorizedAdList,
	 * et que la CategorizedAdList spécifiée est dans le même état que cette liste
	 * concernant l'itération en cours et la catégorie sélectionnée.
	 * 
	 * @param obj l'objet à comparer à cette liste
	 * 
	 * @return true si l'argument spécifié contient les mêmes éléments et est dans
	 *         le même état que cette liste
	 * 
	 * @ensures !(obj instanceof CategorizedAdList) ==> !\result;
	 * @ensures \result ==> ((CategorizedAdList) obj).size() == size();
	 * @ensures \result ==> ((CategorizedAdList) obj).previousIndex() ==
	 *          previousIndex();
	 * @ensures \result ==> ((CategorizedAdList) obj).nextIndex() == nextIndex();
	 * @ensures \result ==> ((CategorizedAdList) obj).lastIndex() == lastIndex();
	 * @ensures \result ==> ((CategorizedAdList)
	 *          obj).getSelectedCategory().equals(getSelectedCategory());
	 * @ensures \result ==> (\forall int i; i >= 0 && i < size();
	 *          ((CategorizedAdList) obj).get(i).equals(get(i)));
	 * @ensures \result ==> ((CategorizedAdList) obj).hashCode() == hashCode();
	 * 
	 * @pure
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof CategorizedAdList)) {
			return false;
		}

		CategorizedAdList x = (CategorizedAdList) obj;

		CategorizedAdList A = this.clone();
		CategorizedAdList B = x.clone();

		A.startIteration();
		B.startIteration();

		while (A.hasNext() && B.hasNext()) {
			A.next();
			B.next();

			if (!(A.AdPointedTo.equals(B.AdPointedTo))) {
				return false;
			}
		}

		return this.size() == x.size() && this.previousIndex() == x.previousIndex() && this.nextIndex() == x.nextIndex()
				&& this.lastIndex() == x.lastIndex() && this.getSelectedCategory().equals(x.getSelectedCategory())
				&& this.hashCode() == x.hashCode();
	}

	/**
	 * Renvoie un clone de cette liste.
	 * 
	 * @return un clone de cette liste
	 * 
	 * @ensures \result != null;
	 * @ensures \result != this;
	 * @ensures \result.equals(this);
	 * @ensures \result.getClasse() == this.getClass();
	 * 
	 * @pure
	 */
	@Override
	public CategorizedAdList clone() {
		CategorizedAdList clone = new CategorizedAdList();

		for (int i = 0; i < this.tabCatAd.length; i++) {
			clone.tabCatAd[i] = (ArrayList<ClassifiedAd>) this.tabCatAd[i].clone();
		}

		return clone;
	}

	/**
	 * Renvoie un code de hashage pour cette liste.
	 * 
	 * @return un code de hashage pour cette liste
	 * 
	 * @pure
	 */
	@Override
	public int hashCode() {
		int hashint = 0;

		for (int i = 0; i < this.tabCatAd.length; i++) {
			hashint = this.tabCatAd[i].hashCode() + hashint;
		}

		return hashint;
	}

	/**
	 * Renvoie une représentation de cette liste sous forme d'une chaîne de
	 * caractères.
	 * 
	 * @return une représentation de cette liste sous forme d'une chaîne de
	 *         caractères
	 * 
	 * @ensures \result != null;
	 * @ensures \result.contains("" + lastIndex());
	 * @ensures \result.contains("" + nextIndex());
	 * @ensures \result.contains("" + previousIndex());
	 * @ensures \result.contains(getSelectedCategory().toString());
	 * @ensures (\forall ClassifiedAd ad; contains(ad);
	 *          \result.contains(ad.toString()));
	 * 
	 * @pure
	 */
	@Override
	public String toString() {
		return "" + this.lastIndex() + "" + nextIndex() + "" + previousIndex() + this.getSelectedCategory().toString();
	}
}
