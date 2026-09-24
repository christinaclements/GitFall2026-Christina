import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reads a Slay the Spire deck from a text file and creates a report with
 * the deck's total energy cost and a histogram of card costs.
 */

public class DeckTally {

    // Lowest and highest energy cost a valid card can have
    private static final int MIN_COST = 0;
    private static final int MAX_COST = 6;

    /**
     *   1. Ask the user for the deck file name.
     *   2. Read the file into a list of valid cards and a list of invalid lines.
     *   3. Print the results so they can be checked
     */
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        String fileName = promptForFileName(keyboard);

        ArrayList<Card> validCards = new ArrayList<Card>();
        ArrayList<String> invalidCards = new ArrayList<String>();
        readDeck(fileName, validCards, invalidCards);

        // test reading the file
        System.out.println("Valid cards: " + validCards.size());
        for (int i = 0; i < validCards.size(); i++) {
            Card card = validCards.get(i);
            System.out.println("  " + card.getName() + " - " + card.getCost() + " energy");
        }

        System.out.println("Invalid cards: " + invalidCards.size());
        for (int i = 0; i < invalidCards.size(); i++) {
            System.out.println("  " + invalidCards.get(i));
        }
        keyboard.close();
    }

    /**
     *   1. Prompt the user for a file name of a valid deck file.
     *   2. Check whether that file exists.
     *   3. If it does not, show an error and ask again.
     *   4. Return the file name once a valid file is entered.
     */
    public static String promptForFileName(Scanner keyboard) {
        String fileName = "";
        boolean fileFound = false;

        while (!fileFound) {
            System.out.print("Enter the name of the deck file: ");
            fileName = keyboard.nextLine().trim();
            File file = new File(fileName);
            if (file.isFile()) {
                fileFound = true;
            } else {
                System.out.println("File not found: " + fileName + ". Please try again.");
            }
        }
        return fileName;
    }

    /**
     * Reads every line of the deck file and sorts each card as valid or invalid.
     *   1. Open the file.
     *   2. Read one line at a time.
     *   3. Skip blank lines.
     *   4. Try to turn each line into a Card.
     *   5. Add the card to validCards, or add the line to invalidCards if it is not valid.
     *   6. Close the file.
     */
    public static void readDeck(String fileName, ArrayList<Card> validCards, ArrayList<String> invalidCards) {
        try {
            Scanner fileReader = new Scanner(new File(fileName));
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                // Blank lines are skipped
                if (line.trim().isEmpty()) {
                    continue;
                }
                Card card = parseCard(line);
                if (card != null) {
                    validCards.add(card);
                } else {
                    invalidCards.add(line);
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + fileName);
        }
    }

    /**
     * Turns each line of the file into a Card, if the format is valid.
     *   1. Find the last colon in the line. If there is none, the line is invalid.
     *   2. Split the line into the name (before the colon) and the cost (after it).
     *   3. If the name is empty or only spaces/tabs, the line is invalid.
     *   4. If the cost is not a whole number, the line is invalid.
     *   5. If the cost is not between 0 and 6, the line is invalid.
     *   6. Otherwise, return a new Card with the name and cost.
     */
    public static Card parseCard(String line) {
        int colonIndex = line.lastIndexOf(':');
        //if there is no colon, invalid
        if (colonIndex == -1) {
            return null;
        }
        String name = line.substring(0, colonIndex).trim();
        String costText = line.substring(colonIndex + 1).trim();
        // trim() to remove spaces and tabs
        if (name.isEmpty()) {
            return null;
        }
        int cost;
        try {
            cost = Integer.parseInt(costText); //convert text to int
        } catch (NumberFormatException e) {
            return null;
        }
        if (cost < MIN_COST || cost > MAX_COST) {
            return null;
        }
        return new Card(name, cost);
    }
}
