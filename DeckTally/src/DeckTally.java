import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Reads a Slay the Spire deck from a text file and creates a report with
 * the deck's total energy cost and a histogram of card costs.
 */
public class DeckTally {

    // Lowest and highest energy cost a valid card can have
    private static final int MIN_COST = 0;
    private static final int MAX_COST = 6;

    // Smallest and largest possible 9-digit deck ids
    private static final int MIN_DECK_ID = 100000000;
    private static final int MAX_DECK_ID = 999999999;

    // Limits that make a report VOID
    private static final int MAX_INVALID_CARDS = 10;
    private static final int MAX_CARDS = 1000;

    // Layout of the report page, in PDF points (72 points = 1 inch)
    private static final int LEFT_MARGIN = 72;
    private static final int BAR_START_X = 150;
    private static final int MAX_BAR_WIDTH = 300;
    private static final int BAR_HEIGHT = 14;
    private static final int ROW_SPACING = 22;
    private static final int LINE_SPACING = 16;

    /**
     * Overview: Runs the program.
     * Input: args - command line arguments (not used)
     * Output: None
     * Steps:
     *   1. Ask the user for the deck file name.
     *   2. Read the file into a list of valid cards and a list of invalid lines.
     *   3. Generate a deck ID.
     *   4. If the deck is void, create a VOID report.
     *   5. Otherwise, total the costs, build the histogram, and create the regular report.
     *   6. Tell the user the name of the report file, or show an error if it could not be saved.
     */
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        String fileName = promptForFileName(keyboard);

        ArrayList<Card> validCards = new ArrayList<Card>();
        ArrayList<String> invalidCards = new ArrayList<String>();
        readDeck(fileName, validCards, invalidCards);

        int deckId = generateDeckId();

        try {
            String reportName;
            if (isVoidDeck(validCards, invalidCards)) {
                reportName = getVoidReportFileName(deckId);
                writeVoidReport(reportName);
            } else {
                int totalCost = calculateTotalCost(validCards);
                int[] histogram = buildHistogram(validCards);
                reportName = getReportFileName(deckId);
                writeReport(reportName, deckId, totalCost, histogram, invalidCards);
            }
            System.out.println("Report saved as " + reportName);
        } catch (IOException e) {
            System.out.println("Could not save the report: " + e.getMessage());
        }

        keyboard.close();
    }

    /**
     * Overview: Asks the user for the deck file name until they enter a file that exists.
     * Input: keyboard - Scanner used to read what the user types
     * Output: The name of a file that exists
     * Steps:
     *   1. Ask the user to type a file name.
     *   2. Check whether that file exists.
     *   3. If it does not, show an error and ask again.
     *   4. Return the file name once a real file is entered.
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
     * Overview: Reads every line of the deck file and sorts each card as valid or invalid.
     * Input: fileName - the deck file to read;
     *        validCards - list that valid cards are added to;
     *        invalidCards - list that the text of invalid lines is added to
     * Output: None (fills the two lists that are passed in)
     * Steps:
     *   1. Open the file.
     *   2. Read it one line at a time.
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
     * Overview: Turns one line of the file into a Card, if the line is valid.
     * Input: line - one line from the deck file, in the format name:cost
     * Output: A Card if the line is valid, or null if it is invalid
     * Steps:
     *   1. Find the last colon in the line. If there is none, the line is invalid.
     *   2. Split the line into the name (before the colon) and the cost (after it).
     *   3. If the name is empty or only spaces/tabs, the line is invalid.
     *   4. If the cost is not a whole number, the line is invalid.
     *   5. If the cost is not between 0 and 6, the line is invalid.
     *   6. Otherwise, return a new Card with the name and cost.
     */
    public static Card parseCard(String line) {
        int colonIndex = line.lastIndexOf(':');
        // If there is no colon, the line is invalid
        if (colonIndex == -1) {
            return null;
        }
        String name = line.substring(0, colonIndex).trim();
        String costText = line.substring(colonIndex + 1).trim();

        // trim() removes spaces and tabs
        if (name.isEmpty()) {
            return null;
        }
        int cost;
        try {
            cost = Integer.parseInt(costText);
        } catch (NumberFormatException e) {
            return null;
        }
        if (cost < MIN_COST || cost > MAX_COST) {
            return null;
        }
        return new Card(name, cost);
    }

    /**
     * Overview: Creates a random 9-digit id for the deck that is not already
     *           used by a report in the current folder.
     * Input: None
     * Output: A 9-digit deck id (100000000 to 999999999)
     * Steps:
     *   1. Pick a random number from 100000000 to 999999999, so it always has 9 digits.
     *   2. Check whether a report (regular or VOID) with that id already exists.
     *   3. If one does, pick a new number and check again.
     *   4. Return the id once it is not in use.
     */
    public static int generateDeckId() {
        Random random = new Random();
        int deckId = 0;
        boolean idIsUnique = false;
        while (!idIsUnique) {
            deckId = MIN_DECK_ID + random.nextInt(MAX_DECK_ID - MIN_DECK_ID + 1);
            File regularReport = new File(getReportFileName(deckId));
            File voidReport = new File(getVoidReportFileName(deckId));
            if (!regularReport.exists() && !voidReport.exists()) {
                idIsUnique = true;
            }
        }
        return deckId;
    }

    /**
     * Overview: Adds up the energy cost of every valid card in the deck.
     * Input: validCards - the list of valid cards
     * Output: The total energy cost of the deck
     * Steps:
     *   1. Start the total at 0.
     *   2. Go through each card and add its cost to the total.
     *   3. Return the total.
     */
    public static int calculateTotalCost(ArrayList<Card> validCards) {
        int totalCost = 0;
        for (int i = 0; i < validCards.size(); i++) {
            totalCost = totalCost + validCards.get(i).getCost();
        }
        return totalCost;
    }

    /**
     * Overview: Counts how many valid cards there are at each energy cost.
     * Input: validCards - the list of valid cards
     * Output: An array of 7 counts, where index 0 is the number of 0-cost cards,
     *         index 1 is the number of 1-cost cards, and so on up to 6
     * Steps:
     *   1. Create an array with one slot for each cost from 0 to 6, all starting at 0.
     *   2. Go through each card and add 1 to the slot that matches its cost.
     *   3. Return the array.
     */
    public static int[] buildHistogram(ArrayList<Card> validCards) {
        int[] histogram = new int[MAX_COST + 1];
        for (int i = 0; i < validCards.size(); i++) {
            int cost = validCards.get(i).getCost();
            histogram[cost] = histogram[cost] + 1;
        }
        return histogram;
    }

    /**
     * Overview: Decides whether the deck should get a VOID report.
     * Input: validCards - the list of valid cards;
     *        invalidCards - the list of invalid lines
     * Output: true if the report should be VOID, false otherwise
     * Steps:
     *   1. Count the total number of cards (valid plus invalid).
     *   2. If there are more than 10 invalid cards, the deck is void.
     *   3. If there are more than 1000 cards in total, the deck is void.
     *   4. Otherwise, the deck is not void.
     */
    public static boolean isVoidDeck(ArrayList<Card> validCards, ArrayList<String> invalidCards) {
        int totalCards = validCards.size() + invalidCards.size();
        if (invalidCards.size() > MAX_INVALID_CARDS) {
            return true;
        } else if (totalCards > MAX_CARDS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Overview: Builds the file name for a regular report.
     * Input: deckId - the deck's 9-digit id
     * Output: The file name, for example SpireDeck_252644859.pdf
     * Steps:
     *   1. Join "SpireDeck_", the id, and ".pdf".
     */
    public static String getReportFileName(int deckId) {
        return "SpireDeck_" + deckId + ".pdf";
    }

    /**
     * Overview: Builds the file name for a VOID report.
     * Input: deckId - the deck's 9-digit id
     * Output: The file name, for example SpireDeck_252644859(VOID).pdf
     * Steps:
     *   1. Join "SpireDeck_", the id, and "(VOID).pdf".
     */
    public static String getVoidReportFileName(int deckId) {
        return "SpireDeck_" + deckId + "(VOID).pdf";
    }

    /**
     * Total Disclaimer: I used AI to help me create this method for the report
     * builder. I gave it a mock example of what I wanted it to look like
     * so that it could help with the specific formatting. I had no idea where to start on my own
     *
     * Overview: Creates the regular PDF report for a deck.
     * Input: reportName - the file name to save the report as;
     *        deckId - the deck's 9-digit id;
     *        totalCost - the total energy cost of the valid cards;
     *        histogram - the number of cards at each cost from 0 to 6;
     *        invalidCards - the list of invalid lines from the file
     * Output: None (creates the PDF file)
     * Steps:
     *   1. Add the title, deck id, and total cost.
     *   2. Find the largest histogram count so the bars can be scaled to fit the page.
     *   3. For each cost from 0 to 6, add a label, a bar sized by its count, and the count.
     *   4. Add the list of invalid cards, or "None" if there are none.
     *   5. Save the PDF.
     * Throws: IOException if the file cannot be written
     */
    public static void writeReport(String reportName, int deckId, int totalCost, int[] histogram,
                                   ArrayList<String> invalidCards) throws IOException {
        SimplePdf pdf = new SimplePdf();
        int y = 720;
        pdf.addText("Slay the Spire Deck Report", LEFT_MARGIN, y, 20, true);
        y = y - 30;
        pdf.addText("Deck ID: " + deckId, LEFT_MARGIN, y, 12, false);
        y = y - 20;
        pdf.addText("Total cost: " + totalCost + " energy", LEFT_MARGIN, y, 12, false);

        // Histogram
        y = y - 35;
        pdf.addText("Energy Cost Histogram", LEFT_MARGIN, y, 14, true);

        int largestCount = 0;
        for (int cost = MIN_COST; cost <= MAX_COST; cost++) {
            if (histogram[cost] > largestCount) {
                largestCount = histogram[cost];
            }
        }

        for (int cost = MIN_COST; cost <= MAX_COST; cost++) {
            y = y - ROW_SPACING;
            pdf.addText(cost + " energy", LEFT_MARGIN, y, 12, false);

            // The largest count gets the full bar width; the others are scaled to match
            int barWidth = 0;
            if (largestCount > 0) {
                barWidth = histogram[cost] * MAX_BAR_WIDTH / largestCount;
            }
            if (barWidth > 0) {
                pdf.addRectangle(BAR_START_X, y - 3, barWidth, BAR_HEIGHT);
            }

            String countLabel;
            if (histogram[cost] == 1) {
                countLabel = "1 card";
            } else {
                countLabel = histogram[cost] + " cards";
            }
            pdf.addText(countLabel, BAR_START_X + barWidth + 8, y, 12, false);
        }

        // Invalid cards
        y = y - 40;
        pdf.addText("Invalid Cards (" + invalidCards.size() + ")", LEFT_MARGIN, y, 14, true);
        if (invalidCards.size() == 0) {
            y = y - LINE_SPACING - 4;
            pdf.addText("None", LEFT_MARGIN, y, 12, false);
        } else {
            for (int i = 0; i < invalidCards.size(); i++) {
                y = y - LINE_SPACING;
                // Quotes show where each line starts and ends, including blank names
                pdf.addText("\"" + invalidCards.get(i) + "\"", LEFT_MARGIN, y, 12, false);
            }
        }

        pdf.save(reportName);
    }

    /**
     * Overview: Creates the VOID PDF report, which shows VOID instead of the deck information.
     * Input: reportName - the file name to save the report as
     * Output: None (creates the PDF file)
     * Steps:
     *   1. Add the title.
     *   2. Add the word VOID in large text.
     *   3. Save the PDF.
     * Throws: IOException if the file cannot be written
     */
    public static void writeVoidReport(String reportName) throws IOException {
        SimplePdf pdf = new SimplePdf();
        pdf.addText("Slay the Spire Deck Report", LEFT_MARGIN, 720, 20, true);
        pdf.addText("VOID", LEFT_MARGIN, 650, 48, true);
        pdf.save(reportName);
    }
}
