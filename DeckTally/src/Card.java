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
     * Creates a new card with a name and an energy cost.
     */
    public Card(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    /**
     * Return the card's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the card's energy cost.
     */
    public int getCost() {
        return cost;
    }
}
