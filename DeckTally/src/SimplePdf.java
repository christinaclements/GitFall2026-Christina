import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Total Disclaimer: I used AI to help me create this class as well as the report builder
 * in the DeckTally. I gave it a mock example of what I wanted it to look like
 * so that it could help with the specific formatting. I had no idea where to start on my own
 *
 * Builds a simple one-page PDF file with text and filled rectangles.
 * It writes the PDF format directly, so no outside library is needed.
 */
public class SimplePdf {

    // Size of a US Letter page in PDF points (72 points = 1 inch)
    public static final int PAGE_WIDTH = 612;
    public static final int PAGE_HEIGHT = 792;

    // The drawing commands for the page (text and rectangles)
    private StringBuilder pageContent;

    /**
     * Overview: Creates an empty PDF page.
     * Input: None
     * Output: A new SimplePdf object
     * Steps:
     *   1. Start with no drawing commands on the page.
     */
    public SimplePdf() {
        pageContent = new StringBuilder();
    }

    /**
     * Overview: Adds a line of text to the page.
     * Input: text - the text to show;
     *        x - distance from the left edge of the page, in points;
     *        y - distance from the bottom edge of the page, in points;
     *        fontSize - size of the text;
     *        bold - true for bold text, false for regular text
     * Output: None (the text is added to the page)
     * Steps:
     *   1. Pick the regular font (F1) or the bold font (F2).
     *   2. Add a PDF text command with the font, size, position, and escaped text.
     */
    public void addText(String text, int x, int y, int fontSize, boolean bold) {
        String font;
        if (bold) {
            font = "/F2";
        } else {
            font = "/F1";
        }

        // BT/ET begin and end text, Tf sets the font, Td moves to x y, Tj draws the text
        pageContent.append("BT " + font + " " + fontSize + " Tf " + x + " " + y + " Td (" + escapeText(text) + ") Tj ET\n");
    }

    /**
     * Overview: Adds a filled blue rectangle to the page (used for histogram bars).
     * Input: x - left edge of the rectangle, in points;
     *        y - bottom edge of the rectangle, in points;
     *        width - width of the rectangle, in points;
     *        height - height of the rectangle, in points
     * Output: None (the rectangle is added to the page)
     * Steps:
     *   1. Save the current colour settings (q).
     *   2. Set the fill colour to blue (rg).
     *   3. Draw and fill the rectangle (re f).
     *   4. Restore the colour settings so later text stays black (Q).
     */
    public void addRectangle(int x, int y, int width, int height) {
        pageContent.append("q 0.20 0.40 0.75 rg " + x + " " + y + " " + width + " " + height + " re f Q\n");
    }

    /**
     * Overview: Makes text safe to put inside a PDF text command.
     * Input: text - the original text
     * Output: The text with special characters escaped or replaced
     * Steps:
     *   1. Go through the text one character at a time.
     *   2. Put a backslash before \, (, and ) because PDF uses them in its syntax.
     *   3. Replace tabs with spaces.
     *   4. Replace characters the PDF font cannot show with ?.
     *   5. Return the new text.
     */
    private String escapeText(String text) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\\' || c == '(' || c == ')') {
                escaped.append('\\');
                escaped.append(c);
            } else if (c == '\t') {
                escaped.append(' ');
            } else if (c < 32 || c > 255) {
                escaped.append('?');
            } else {
                escaped.append(c);
            }
        }

        return escaped.toString();
    }

    /**
     * Overview: Writes the page to a PDF file.
     * Input: fileName - the name of the PDF file to create
     * Output: None (creates the file)
     * Steps:
     *   1. Build the six PDF objects: catalog, page list, page, regular font,
     *      bold font, and the page content.
     *   2. Write the PDF header, then each object, recording where each one starts.
     *   3. Write the cross-reference table that lists those starting positions.
     *   4. Write the trailer that tells PDF readers where everything is.
     *   5. Save everything to the file.
     * Throws: IOException if the file cannot be written
     */
    public void save(String fileName) throws IOException {
        String content = pageContent.toString();

        String[] objects = new String[6];
        objects[0] = "<< /Type /Catalog /Pages 2 0 R >>";
        objects[1] = "<< /Type /Pages /Kids [3 0 R] /Count 1 >>";
        objects[2] = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "]"
                + " /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>";
        objects[3] = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>";
        objects[4] = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>";
        objects[5] = "<< /Length " + content.length() + " >>\nstream\n" + content + "endstream";

        // Every character is one byte in this file, so the text length is the byte position
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        int[] objectPositions = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            objectPositions[i] = pdf.length();
            pdf.append((i + 1) + " 0 obj\n" + objects[i] + "\nendobj\n");
        }

        // The cross-reference table lists where each object starts in the file
        int xrefPosition = pdf.length();
        pdf.append("xref\n");
        pdf.append("0 " + (objects.length + 1) + "\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 0; i < objects.length; i++) {
            pdf.append(String.format("%010d 00000 n \n", objectPositions[i]));
        }

        pdf.append("trailer\n<< /Size " + (objects.length + 1) + " /Root 1 0 R >>\n");
        pdf.append("startxref\n" + xrefPosition + "\n%%EOF\n");

        FileOutputStream output = new FileOutputStream(fileName);
        output.write(pdf.toString().getBytes(StandardCharsets.ISO_8859_1));
        output.close();
    }
}
