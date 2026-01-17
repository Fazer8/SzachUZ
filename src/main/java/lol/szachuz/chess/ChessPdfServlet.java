package lol.szachuz.chess;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone; // Ważny import

@WebServlet("/game/pdf")
public class ChessPdfServlet extends HttpServlet {

    private final LeaderboardRepository leaderboardRepository = new LeaderboardRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String gameId = req.getParameter("gameId");

        MatchService service = MatchService.getInstance();
        Match match = service.loadMatchByMatchId(gameId);

        if (match == null) {
            resp.sendError(404, "Mecz nie istnieje lub wygasł.");
            return;
        }

        String whiteName = resolveName(match.getWhite().getId());
        String blackName = resolveName(match.getBlack().getId());

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=szachuz_" + gameId + ".pdf");

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();

            // Fonty
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 22, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(bf, 12, Font.BOLD, Color.BLACK);
            Font normalFont = new Font(bf, 12, Font.NORMAL, Color.BLACK);
            Font smallFont = new Font(bf, 10, Font.ITALIC, Color.GRAY);

            // Logo
            String logoPath = getServletContext().getRealPath("/assets/logo_tmp.png");
            try {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(150, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("Brak logo: " + e.getMessage());
            }

            document.add(Chunk.NEWLINE);

            // Tytuł
            Paragraph title = new Paragraph("Raport z meczu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // DATA - FIX STREFY CZASOWEJ
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw")); // Ustawiamy czas polski

            // Jeśli chcesz datę rozpoczęcia meczu, musiałbyś użyć np. match.getStartDate() zamiast new Date()
            // Tutaj zostawiam datę wygenerowania PDF, ale z poprawną godziną:
            Paragraph datePara = new Paragraph("Data wygenerowania: " + sdf.format(new Date()), smallFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
            document.add(Chunk.NEWLINE);

            // Gracze
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            PdfPCell whiteInfo = new PdfPCell();
            whiteInfo.setBorder(Rectangle.NO_BORDER);
            whiteInfo.addElement(new Paragraph("BIAŁE:", headerFont));
            whiteInfo.addElement(new Paragraph(whiteName, normalFont));
            infoTable.addCell(whiteInfo);

            PdfPCell blackInfo = new PdfPCell();
            blackInfo.setBorder(Rectangle.NO_BORDER);
            blackInfo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pLabel = new Paragraph("CZARNE:", headerFont);
            pLabel.setAlignment(Element.ALIGN_RIGHT);
            blackInfo.addElement(pLabel);
            Paragraph pName = new Paragraph(blackName, normalFont);
            pName.setAlignment(Element.ALIGN_RIGHT);
            blackInfo.addElement(pName);
            infoTable.addCell(blackInfo);

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Historia Ruchów:", headerFont));
            document.add(Chunk.NEWLINE);

            // Tabela ruchów
            PdfPTable table = new PdfPTable(3);
            table.setWidths(new float[]{1, 4, 4});
            table.setWidthPercentage(100);

            addHeaderCell(table, "#", headerFont);
            addHeaderCell(table, "Białe", headerFont);
            addHeaderCell(table, "Czarne", headerFont);

            List<String> history = match.getMoveHistorySan();
            String piecesPath = getServletContext().getRealPath("/assets/pieces/");

            int moveNum = 1;
            for (int i = 0; i < history.size(); i += 2) {
                addCell(table, moveNum + ".", normalFont);
                addMoveCell(table, history.get(i), "w", piecesPath, normalFont);
                if (i + 1 < history.size()) {
                    addMoveCell(table, history.get(i + 1), "b", piecesPath, normalFont);
                } else {
                    addCell(table, "", normalFont);
                }
                moveNum++;
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    private void addMoveCell(PdfPTable table, String san, String colorPrefix, String basePath, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String pieceCode = getPieceCodeFromSan(san);
        String iconFilename = colorPrefix + pieceCode + ".png";

        Phrase content = new Phrase();
        try {
            Image img = Image.getInstance(basePath + java.io.File.separator + iconFilename);
            img.scaleToFit(14, 14); // Nieco mniejsze w PDF żeby nie rozwalały wierszy
            content.add(new Chunk(img, 0, -2));
            content.add(new Chunk("  "));
        } catch (Exception e) {}

        content.add(new Chunk(san, font));
        cell.addElement(content);
        table.addCell(cell);
    }

    private String getPieceCodeFromSan(String san) {
        if (san.startsWith("O-O")) return "K";
        char firstChar = san.charAt(0);
        return Character.isUpperCase(firstChar) ? String.valueOf(firstChar) : "P";
    }

    private String resolveName(long playerId) {
        String username = leaderboardRepository.findUsernameByUserId((int) playerId);
        return username != null ? username : "Gracz " + playerId;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}