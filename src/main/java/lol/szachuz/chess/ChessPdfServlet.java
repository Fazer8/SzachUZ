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
import java.util.TimeZone;

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

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 22, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(bf, 12, Font.BOLD, Color.BLACK);
            Font tableHeaderFont = new Font(bf, 10, Font.BOLD, Color.BLACK);
            Font normalFont = new Font(bf, 10, Font.NORMAL, Color.BLACK);
            Font smallFont = new Font(bf, 9, Font.ITALIC, Color.GRAY);

            Font captureFont = new Font(bf, 10, Font.BOLD, Color.RED);
            Font castlingFont = new Font(bf, 10, Font.BOLD, Color.BLUE);
            Font promoFont = new Font(bf, 10, Font.BOLD, new Color(0, 128, 0)); // Ciemny zielony

            String logoPath = getServletContext().getRealPath("/assets/logo_tmp.png");
            try {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(150, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {}
            document.add(Chunk.NEWLINE);

            Paragraph title = new Paragraph("Raport z meczu", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
            Paragraph datePara = new Paragraph("Data wygenerowania: " + sdf.format(new Date()), smallFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            document.add(datePara);
            document.add(Chunk.NEWLINE);

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

            float[] widths = {0.6f, 1f,1f,1.2f, 1f,1f,1.2f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            PdfPCell cellNr = new PdfPCell(new Phrase("#", tableHeaderFont));
            cellNr.setRowspan(2);
            cellNr.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNr.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellNr.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cellNr);

            PdfPCell cellWhite = new PdfPCell(new Phrase("Białe", tableHeaderFont));
            cellWhite.setColspan(3);
            cellWhite.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellWhite.setBackgroundColor(new Color(240, 240, 240));
            table.addCell(cellWhite);

            PdfPCell cellBlack = new PdfPCell(new Phrase("Czarne", tableHeaderFont));
            cellBlack.setColspan(3);
            cellBlack.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBlack.setBackgroundColor(new Color(220, 220, 220));
            table.addCell(cellBlack);

            addHeaderCell(table, "Fig.", tableHeaderFont);
            addHeaderCell(table, "Z pola", tableHeaderFont);
            addHeaderCell(table, "Na pole", tableHeaderFont);
            addHeaderCell(table, "Fig.", tableHeaderFont);
            addHeaderCell(table, "Z pola", tableHeaderFont);
            addHeaderCell(table, "Na pole", tableHeaderFont);

            List<Match.MoveLog> history = match.getMoveHistoryDetails();
            String piecesPath = getServletContext().getRealPath("/assets/pieces/");

            int moveNum = 1;
            for (int i = 0; i < history.size(); i += 2) {
                addCell(table, moveNum + ".", normalFont);

                addMoveDetails(table, history.get(i), "w", piecesPath, normalFont, captureFont, castlingFont, promoFont);

                if (i + 1 < history.size()) {
                    addMoveDetails(table, history.get(i + 1), "b", piecesPath, normalFont, captureFont, castlingFont, promoFont);
                } else {
                    table.addCell(""); table.addCell(""); table.addCell("");
                }
                moveNum++;
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    private void addMoveDetails(PdfPTable table, Match.MoveLog move, String colorPrefix, String basePath,
                                Font normalF, Font captureF, Font castleF, Font promoF) {

        addPieceCell(table, move.pieceCode, colorPrefix, basePath);

        addCell(table, move.from, normalF);

        if (move.isCastling) {
            addCell(table, "Roszada", castleF);
        }
        else if (move.isPromotion) {
            addCell(table, move.to + "=D", promoF);
        }
        else if (move.isCapture) {
            addCell(table, move.to, captureF);
        }
        else {
            addCell(table, move.to, normalF);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(245, 245, 245));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addPieceCell(PdfPTable table, String pieceCode, String colorPrefix, String basePath) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        String iconFilename = colorPrefix + pieceCode + ".png";
        try {
            Image img = Image.getInstance(basePath + java.io.File.separator + iconFilename);
            img.scaleToFit(18, 18);
            cell.addElement(img);
        } catch (Exception e) {
            cell.setPhrase(new Phrase(pieceCode));
        }
        table.addCell(cell);
    }

    private String resolveName(long playerId) {
        String username = leaderboardRepository.findUsernameByUserId((int) playerId);
        return username != null ? username : "Gracz " + playerId;
    }
}