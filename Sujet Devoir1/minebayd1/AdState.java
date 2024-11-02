/**
 * @author Riyad Derguini 12312754
 * Je déclare qu'il s'agit de mon propre travail
 */
package minebayd1;

/**
 * Les différents états possibles d'une annonce dans MinEbay.
 * 
 * @author Marc Champesme
 * @since 27/09/2024
 * @version 20/10/2024
 */
public enum AdState {
	/**
	 * État d'une annonce qui est encore disponible à l'achat.
	 */
	OPEN,
	/**
	 * Ètat d'une annonce dont l'utilisateur a vendu l'objet.
	 */
	CLOSED,
	/**
	 * Ètat d'une annonce dont l'utilisateur a acheté l'objet.
	 */
	PURCHASE
}
