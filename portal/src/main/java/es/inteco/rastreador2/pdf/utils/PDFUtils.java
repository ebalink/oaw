package es.inteco.rastreador2.pdf.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.events.IndexEvents;
import es.inteco.common.Constants;
import es.inteco.common.ConstantsFont;
import es.inteco.common.logging.Logger;
import es.inteco.common.properties.PropertiesManager;
import es.inteco.intav.utils.StringUtils;
import es.inteco.rastreador2.actionform.observatorio.ModalityComparisonForm;
import es.inteco.rastreador2.pdf.AnonymousResultExportPdfSectionEv;
import es.inteco.rastreador2.pdf.AnonymousResultExportPdfSections;
import es.inteco.rastreador2.utils.CrawlerUtils;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public final class PDFUtils {

    private PDFUtils() {
    }

    public static void addTitlePage(Document document, String titleText, String subtitleText, Font titleFont, Font subtitleFont) throws DocumentException {

        document.add(Chunk.NEWLINE);
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setSpacingBefore(ConstantsFont.SPACE_TITLE_LINE);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        if (!subtitleText.equals("")) {
            Paragraph subtitle = new Paragraph(subtitleText, subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingBefore(ConstantsFont.SPACE_SUBTITLE_LINE);
            document.add(subtitle);
        }
    }

    public static Chapter addChapterTitle(String title, IndexEvents index, int countSections, int numChapter, Font titleFont) {
        return addChapterTitle(title, index, countSections, numChapter, titleFont, true);
    }

    public static Chapter addChapterTitle(String title, IndexEvents index, int countSections, int numChapter, Font titleFont, boolean upperCase) {
        if (upperCase) {
            title = title.toUpperCase();
        }
        Chunk chunk = new Chunk(title);
        chunk.setLocalDestination(Constants.ANCLA_PDF + (countSections));
        Paragraph paragraph = new Paragraph("", titleFont);
        paragraph.add(chunk);
        Chapter chapter = new Chapter(paragraph, numChapter);
        if (index != null) {
            paragraph.add(index.create(" ", countSections + "@&" + title));
        }
        return chapter;
    }

    public static Section addSection(String title, IndexEvents index, Font levelFont, Section section, int countSections, int level) {
        return addSection(title, index, levelFont, section, countSections, level, null);
    }

    public static Section addSection(String title, IndexEvents index, Font levelFont, Section section, int countSections, int level, String anchor) {
        Chunk chunk = new Chunk(title.toUpperCase());
        Paragraph paragraph = new Paragraph("", levelFont);
        Chunk whiteChunk = new Chunk(" ");
        if (countSections != -1) {
            chunk.setLocalDestination(Constants.ANCLA_PDF + (countSections));
        }
        paragraph.add(chunk);
        if (anchor != null) {
            whiteChunk.setLocalDestination(anchor);
            paragraph.add(whiteChunk);
        }
        paragraph.setSpacingBefore(ConstantsFont.SPACE_LINE);
        Section sectionL2 = section.addSection(paragraph);
        if (index != null) {
            paragraph.add(index.create(" ", countSections + "@" + level + "&" + title.toUpperCase()));
        }
        return sectionL2;
    }

    public static void addParagraph(String text, Font font, Section section) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        p.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        section.add(p);
    }

    public static void addParagraph(String text, Font font, Section section, int align) {
        addParagraph(text, font, section, align, true, false);
    }

    public static void addParagraph(String text, Font font, Section section, int align, boolean spaceBefore, boolean spaceAfter) {
        Paragraph p = new Paragraph(text, font);
        if (spaceBefore) {
            p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        }
        if (spaceAfter) {
            p.setSpacingAfter(ConstantsFont.SPACE_LINE);
        }
        p.setAlignment(align);
        section.add(p);
    }

    public static void addParagraphCode(String text, String message, Section section) {
        float[] widths = {100f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        java.util.List<String> boldWords = new ArrayList<String>();
        if (!StringUtils.isEmpty(message)) {
            text = "{0} \n\n" + text.trim();
            boldWords.add(message);
        }

        PdfPCell labelCell = new PdfPCell(PDFUtils.createParagraphWithDiferentFormatWord(text, boldWords, ConstantsFont.strongNoteCellFont, ConstantsFont.noteCellFont, false));
        labelCell.setBackgroundColor(new Color(255, 244, 223));
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(5f);
        table.addCell(labelCell);
        table.setSpacingBefore(ConstantsFont.SPACE_LINE / 3);
        section.add(table);
    }

    public static void addParagraphRationale(java.util.List<String> text, Section section) {
        float[] widths = {95f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(95);

        Paragraph paragraph = new Paragraph();
        boolean isFirst = true;
        for (String phraseText : text) {
            if (isFirst) {
                if (StringUtils.isNotEmpty(phraseText)) {
                    paragraph.add(new Phrase(StringUtils.removeHtmlTags(phraseText) + "\n", ConstantsFont.moreInfoFont));
                }
                isFirst = false;
            } else {
                paragraph.add(new Phrase(StringUtils.removeHtmlTags(phraseText) + "\n", ConstantsFont.moreInfoFont));
            }
        }

        PdfPCell labelCell = new PdfPCell(paragraph);
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPadding(5f);
        table.addCell(labelCell);
        table.setSpacingBefore(ConstantsFont.SPACE_LINE / 2);
        section.add(table);
    }

    public static Paragraph createImageTextParagraph(Image image, String text, Font font) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(image, 0, 0));
        if (text != null) {
            p.add(new Chunk(text, font));
        }
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    public static Paragraph createImageParagraphWithDiferentFont(Image image, String text, Font font, String text2, Font font2, int alignment) {
        Paragraph p = new Paragraph();
        if (image != null) {
            p.add(new Chunk(image, 0, 0));
        }
        p.add(new Chunk(text, font));
        if (!StringUtils.isEmpty(text2)) {
            p.add(new Chunk(text2, font2));
        }
        p.setAlignment(alignment);
        p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        return p;
    }

    public static Paragraph createParagraphAnchor(String text, Map<Integer, SpecialChunk> specialChunkMap, Font font) {
        return createParagraphAnchor(text, specialChunkMap, font, true);
    }

    public static Paragraph createParagraphAnchor(String text, Map<Integer, SpecialChunk> specialChunkMap, Font font, boolean spaceBefore) {
        int init = 0;
        int iAnchor = 1;
        int iFormat = 0;
        Paragraph p = new Paragraph();

        for (Map.Entry<Integer, SpecialChunk> specialChunkEntry : specialChunkMap.entrySet()) {
            Chunk chunk;
            int indexOf = 0;
            if ((text.indexOf("[anchor" + iAnchor + "]") > 0) && (text.indexOf("[anchor" + iAnchor + "]") > init)) {
                indexOf = text.indexOf("[anchor" + iAnchor + "]");
            }
            if ((text.indexOf("{" + iFormat + "}") > 0) && ((indexOf != 0 && text.indexOf("{" + iFormat + "}") < indexOf) || indexOf == 0) && (text.indexOf("{" + iFormat + "}") > init)) {
                indexOf = text.indexOf("{" + iFormat + "}");
            }
            if (indexOf != 0) {
                chunk = new Chunk(text.substring(init, indexOf), font);
                p.add(chunk);
            }

            if (specialChunkEntry.getValue().getAnchor() != null && !StringUtils.isEmpty(specialChunkEntry.getValue().getAnchor())) {
                p = createAnchor(specialChunkEntry.getKey(), specialChunkMap, p);
                init = text.indexOf("[anchor" + iAnchor + "]") + 8 + String.valueOf(iAnchor).length();
                iAnchor++;
            } else {
                p = createSpecialFormatText(specialChunkEntry.getKey(), specialChunkMap, p);
                init = text.indexOf("{" + iFormat + "}") + 2 + String.valueOf(iFormat).length();
                iFormat++;
            }
        }
        if (text.length() > init) {
            Chunk finalChunk = new Chunk(text.substring(init, text.length()), font);
            p.add(finalChunk);
        }

        if (spaceBefore) {
            p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        }
        p.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        return p;
    }

    private static Paragraph createSpecialFormatText(Integer anchor, Map<Integer, SpecialChunk> anchorMap, Paragraph p) {
        Chunk anchorChunk = new Chunk(anchorMap.get(anchor).getText(), anchorMap.get(anchor).getFont());
        p.add(anchorChunk);
        return p;
    }

    private static Paragraph createAnchor(Integer anchor, Map<Integer, SpecialChunk> anchorMap, Paragraph p) {
        Chunk anchorChunk = new Chunk(anchorMap.get(anchor).getText(), anchorMap.get(anchor).getFont());
        if (anchorMap.get(anchor).isExternalLink()) {
            anchorChunk.setAnchor(anchorMap.get(anchor).getAnchor());
        } else {
            if (!anchorMap.get(anchor).isDestination()) {
                anchorChunk.setLocalGoto(anchorMap.get(anchor).getAnchor());
            } else {
                anchorChunk.setLocalDestination(anchorMap.get(anchor).getAnchor());
            }
        }
        p.add(anchorChunk);
        return p;
    }

    public static Paragraph createParagraphWithDiferentFormatWord(String text, java.util.List<String> boldWords, Font fontB, Font font, boolean spaceBefore) {
        return createParagraphWithDiferentFormatWord(text, boldWords, fontB, font, spaceBefore, Paragraph.ALIGN_JUSTIFIED);
    }

    public static Phrase createPhrase(String text, Font font) {
        return new Phrase(text, font);
    }

    public static Phrase createPhraseLink(String text, String link, Font font) {
        return new Phrase(new Chunk(text, font).setAnchor(link));
    }

    public static Paragraph createParagraphWithDiferentFormatWord(String text, java.util.List<String> boldWords, Font fontB, Font font, boolean spaceBefore, int alignment) {
        Paragraph p = new Paragraph();
        p.setAlignment(alignment);
        if (text != null) {
            int count = 0;
            int textLength = text.length();
            try {
                for (int i = 0; i < boldWords.size(); i++) {
                    String normalText = text.substring(count, text.indexOf("{" + i + "}"));
                    count = normalText.length() + count + ("{" + i + "}").length();
                    Phrase phraseN = new Phrase(normalText, font);
                    Phrase phraseB = new Phrase(boldWords.get(i), fontB);
                    p.add(phraseN);
                    p.add(phraseB);
                    textLength = textLength + boldWords.get(i).length() - 3;
                }
                if (textLength > p.getContent().length()) {
                    Phrase phraseN = new Phrase(text.substring(count), font);
                    p.add(phraseN);
                }
                if (spaceBefore) {
                    p.setSpacingBefore(ConstantsFont.SPACE_LINE);
                }
                return p;
            } catch (Exception e) {
                Logger.putLog("Error, faltan parámetros en el texto. ", PDFUtils.class, Logger.LOG_LEVEL_ERROR, e);
            }
        }
        return p;
    }

    public static Paragraph addLinkParagraph(String text, String link, Font font) {
        return new Paragraph(new Chunk(text, font).setAnchor(link));
    }

    public static ListItem addMixFormatListItem(String text, java.util.List<String> words, Font fontB, Font font, boolean spaceAfter) {
        Paragraph p = createParagraphWithDiferentFormatWord(text, words, fontB, font, spaceAfter);
        return new ListItem(p);
    }

    public static void addListItem(String text, List list, Font font, boolean spaceBefore, boolean withSymbol) {
        addListItem(text, list, font, spaceBefore, withSymbol, Element.ALIGN_LEFT);
    }

    public static void addListItem(String text, List list, Font font) {
        addListItem(text, list, font, true, true, Element.ALIGN_LEFT);
    }

    public static void addListItem(String text, List list, Font font, boolean spaceBefore, boolean withSymbol, int align) {
        Paragraph p = new Paragraph(text, font);
        if (spaceBefore) {
            p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        }
        ListItem item = new ListItem(p);
        if (!withSymbol) {
            item.setListSymbol(new Chunk(""));
        }
        item.setAlignment(align);
        list.add(item);
    }

    public static ListItem createListItem(String text, Font font, Chunk symbol, boolean spaceBefore) {
        Paragraph p = new Paragraph(text, font);
        ListItem item = new ListItem(p);
        if (symbol != null) {
            item.setListSymbol(symbol);
        }
        if (spaceBefore) {
            p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        }
        return item;
    }

    /*public static void addListItem(String text, String url, List list, Font font, boolean spaceBefore, boolean withSymbol) {

        Anchor anchor = new Anchor(text);
        anchor.setReference(url);

        Paragraph p = new Paragraph(anchor);
        if (spaceBefore) {
            p.setSpacingBefore(ConstantsFont.SPACE_LINE);
        }
        ListItem item = new ListItem(p);
        if (!withSymbol) {
            item.setListSymbol(new Chunk(""));
        }
        list.add(item);
    }*/

    public static Image createImage(String path, String alt) {
        try {
            Image image = Image.getInstance(path);
            image.setAlt(alt);
            image.setAlignment(Element.ALIGN_CENTER);
            return image;
        } catch (Exception e) {
            Logger.putLog("Imagen no encontrada", AnonymousResultExportPdfSections.class, Logger.LOG_LEVEL_ERROR, e);
        }
        return null;
    }

    public static PdfPCell createTableCell(HttpServletRequest request, String text, Color backgroundColor, Font font, int align, int margin) {
        return createTableCell(CrawlerUtils.getResources(request).getMessage(CrawlerUtils.getLocale(request), text), backgroundColor, font, align, margin);
    }

    public static PdfPCell createTableCell(String text, Color backgroundColor, Font font, int align, int margin) {
        return createTableCell(text, backgroundColor, font, align, margin, 17f);
    }

    public static PdfPCell createTableCell(String text, Color backgroundColor, Font font, int align, int margin, float height) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(text, font));
        labelCell.setBackgroundColor(backgroundColor);
        labelCell.setHorizontalAlignment(align);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPaddingLeft(margin);
        if (height != -1) {
            labelCell.setFixedHeight(height);
        }
        return labelCell;
    }

    public static PdfPCell createListTableCell(List list, Color backgroundColor, int align, int margin) {
        PdfPCell labelCell = new PdfPCell();
        labelCell.addElement(list);
        labelCell.setBackgroundColor(backgroundColor);
        labelCell.setHorizontalAlignment(align);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setPaddingLeft(margin);
        return labelCell;
    }

    public static PdfPCell createColSpanTableCell(HttpServletRequest request, String text, Color backgroundColor, Font font, int colSpan, int align) {
        return createColSpanTableCell(CrawlerUtils.getResources(request).getMessage(CrawlerUtils.getLocale(request), text), backgroundColor, font, colSpan, align);
    }

    public static PdfPCell createColSpanTableCell(String text, Color backgroundColor, Font font, int colSpan, int align) {
        int margin = 0;
        if (align == Element.ALIGN_LEFT) {
            margin = 5;
        }
        PdfPCell labelCell = createTableCell(text, backgroundColor, font, align, margin);
        labelCell.setColspan(colSpan);
        return labelCell;
    }

    /*public static List addResultsList(java.util.List<LabelValueBean> results, String text1) {
        List list = new List();
        boolean first = true;
        for (LabelValueBean label : results) {
            String text = text1 + " {0} : " + label.getValue();
            ArrayList<String> words = new ArrayList<String>();
            words.add(label.getLabel());
            ListItem item = PDFUtils.addMixFormatListItem(text, words, ConstantsFont.paragraphBoldFont, ConstantsFont.paragraphFont, false);
            if (first) {
                item.setSpacingBefore(ConstantsFont.SPACE_LINE);
                first = false;
            }
            list.add(item);
        }
        list.setIndentationLeft(ConstantsFont.IDENTATION_LEFT_SPACE);
        return list;
    }*/

    public static PdfPTable createResultTable(java.util.List<LabelValueBean> results, java.util.List<String> headers) {
        float[] widths = {50f, 50f};
        PdfPTable table = new PdfPTable(widths);

        for (String header : headers) {
            table.addCell(PDFUtils.createTableCell(header, Constants.VERDE_C_MP, ConstantsFont.labelCellFont, Element.ALIGN_CENTER, 0));
        }
        for (LabelValueBean label : results) {
            table.addCell(createTableCell(label.getLabel(), Color.white, ConstantsFont.noteCellFont, Element.ALIGN_LEFT, 5));
            table.addCell(createTableCell(label.getValue(), Color.white, ConstantsFont.noteCellFont, Element.ALIGN_CENTER, 0));
        }

        table.setSpacingBefore(ConstantsFont.SPACE_LINE);
        table.setSpacingAfter(ConstantsFont.SPACE_LINE);
        return table;
    }

/*    public static PdfPTable createResultTable(java.util.List<LabelValueBean> results, java.util.List<String> headers) {
        return createResultTable(results, headers, Element.ALIGN_CENTER);
    }//*/

    public static void createTitleTable(String text, Section section, float scaleX) throws BadElementException, IOException {
        PropertiesManager pmgr = new PropertiesManager();
        Image img = Image.getInstance(pmgr.getValue("pdf.properties", "path.images") + pmgr.getValue("pdf.properties", "name.table.line.roja.image"));
        img.setAlt("");
        img.scaleAbsolute(scaleX, img.getHeight() / 2);
        img.setAlignment(Element.ALIGN_CENTER);

        section.add(img);
        PDFUtils.addParagraph(text, ConstantsFont.paragraphTitleTableFont, section, Element.ALIGN_CENTER, false, false);
        section.add(img);
    }

    public static PdfPTable createTableMod(HttpServletRequest request, java.util.List<ModalityComparisonForm> result) {
        float[] widths = {50f, 25f, 25f};
        PdfPTable table = new PdfPTable(widths);
        table.addCell(PDFUtils.createTableCell(CrawlerUtils.getResources(request).getMessage("resultados.anonimos.puntuacion.verificacion"), Constants.VERDE_C_MP, ConstantsFont.labelCellFont, Element.ALIGN_CENTER, 0));
        table.addCell(PDFUtils.createTableCell(CrawlerUtils.getResources(request).getMessage("resultados.anonimos.porc.pasa"), Constants.VERDE_C_MP, ConstantsFont.labelCellFont, Element.ALIGN_CENTER, 0));
        table.addCell(PDFUtils.createTableCell(CrawlerUtils.getResources(request).getMessage("resultados.anonimos.porc.falla"), Constants.VERDE_C_MP, ConstantsFont.labelCellFont, Element.ALIGN_CENTER, 0));

        for (ModalityComparisonForm form : result) {
            table.addCell(PDFUtils.createTableCell(CrawlerUtils.getResources(request).getMessage(form.getVerification()), Color.white, ConstantsFont.noteCellFont, Element.ALIGN_LEFT, 5));
            table.addCell(PDFUtils.createTableCell(form.getGreenPercentage(), Color.white, ConstantsFont.noteCellFont, Element.ALIGN_CENTER, 0));
            table.addCell(PDFUtils.createTableCell(form.getRedPercentage(), Color.white, ConstantsFont.noteCellFont, Element.ALIGN_CENTER, 0));
        }

        table.setSpacingBefore(ConstantsFont.SPACE_LINE);
        return table;
    }

    public static String replaceAccent(String phrase) {
        phrase = phrase.replace('á', 'a');
        phrase = phrase.replace('à', 'a');
        phrase = phrase.replace('â', 'a');
        phrase = phrase.replace('ã', 'a');
        phrase = phrase.replace('ä', 'a');
        phrase = phrase.replace('å', 'a');
        phrase = phrase.replace('é', 'e');
        phrase = phrase.replace('è', 'e');
        phrase = phrase.replace('ê', 'e');
        phrase = phrase.replace('ë', 'e');
        phrase = phrase.replace('í', 'i');
        phrase = phrase.replace('ì', 'i');
        phrase = phrase.replace('ï', 'i');
        phrase = phrase.replace('î', 'i');
        phrase = phrase.replace('ó', 'o');
        phrase = phrase.replace('ò', 'o');
        phrase = phrase.replace('ô', 'o');
        phrase = phrase.replace('ö', 'o');
        phrase = phrase.replace('õ', 'o');
        phrase = phrase.replace('ú', 'u');
        phrase = phrase.replace('ù', 'u');
        phrase = phrase.replace('ü', 'u');
        phrase = phrase.replace('û', 'u');
        phrase = phrase.replace('ý', 'y');
        phrase = phrase.replace('ÿ', 'y');
        phrase = phrase.replace('ñ', 'n');
        phrase = phrase.replace('ç', 'c');
        phrase = phrase.replaceAll("ª", "a.");
        phrase = phrase.replaceAll("º", "o.");
        return phrase;
    }


    public static String formatSeedName(String seedName) {
        if (seedName != null) {
            seedName = replaceAccent(seedName.trim()).replace(" ", "_").toLowerCase();
        }
        return seedName;
    }

    public static void addImageToSection(final Section section, final String imagePath, final String imageAlt, final float scale) {
        try {
            final Image graphic = createImage(imagePath, imageAlt);
            graphic.scalePercent(scale);
            section.add(graphic);
        } catch (Exception e) {
            Logger.putLog("Error al crear imagen en la exportación anónima de resultados", AnonymousResultExportPdfSectionEv.class, Logger.LOG_LEVEL_ERROR, e);
        }
    }
}