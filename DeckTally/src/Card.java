/**
 * Represents one valid card from a Slay the Spire deck file.
 * Each card has a name and an energy cost from 0 to 6.
 */
public class Card {

    // The name of the card, with extra spaces removed
    private String name;

    // The energy cost of the card (0 to 6)
    private int cost;

    /**
     * Overview: Creates a new card with a name and an energy cost.
     * Input: name - the card's name; cost - the card's energy cost (0 to 6)
     * Output: A new Card object
     * Steps:
     *   1. Store the name.
     *   2. Store the cost.
     */
    public Card(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    /**
     * Overview: Gets the card's name.
     * Input: None
     * Output: The card's name
     * Steps:
     *   1. Return the stored name.
     */
    public String getName() {
        return name;
    }

    /**
     * Overview: Gets the card's energy cost.
     * Input: None
     * Output: The card's energy cost (0 to 6)
     * Steps:
     *   1. Return the stored cost.
     */
    public int getCost() {
        return cost;
    }
}
