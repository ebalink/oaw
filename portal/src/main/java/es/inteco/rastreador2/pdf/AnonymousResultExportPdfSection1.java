package es.inteco.rastreador2.pdf;

import com.lowagie.text.Chapter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import es.inteco.common.ConstantsFont;
import es.inteco.rastreador2.pdf.utils.PDFUtils;
import es.inteco.rastreador2.utils.CrawlerUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

public final class AnonymousResultExportPdfSection1 {

    private AnonymousResultExportPdfSection1() {
    }

    protected static void createChapter1(HttpServletRequest request, Chapter chapter) {
        ArrayList<String> boldWords = new ArrayList<String>();
        boldWords.add(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p1.bold"));
        chapter.add(PDFUtils.createParagraphWithDiferentFormatWord(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p1"), boldWords, ConstantsFont.paragraphBoldFont, ConstantsFont.PARAGRAPH, true));
        boldWords = new ArrayList<String>();
        boldWords.add(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p2.bold"));
        Paragraph p = PDFUtils.createParagraphWithDiferentFormatWord(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p2"), boldWords, ConstantsFont.paragraphBoldFont, ConstantsFont.PARAGRAPH, true);
        Phrase ph = new Phrase(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p2.m1"), ConstantsFont.paragraphUnderlinedFont);
        p.add(ph);
        chapter.add(p);
        boldWords = new ArrayList<String>();
        boldWords.add(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p3.bold"));
        chapter.add(PDFUtils.createParagraphWithDiferentFormatWord(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p3"), boldWords, ConstantsFont.paragraphBoldFont, ConstantsFont.PARAGRAPH, true));
        PDFUtils.addParagraph(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p4"), ConstantsFont.PARAGRAPH, chapter);
        PDFUtils.addParagraph(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.1.p5"), ConstantsFont.PARAGRAPH, chapter);
    }

    protected static void createSection11(HttpServletRequest request, Section section) {
        ArrayList<String> boldWords = new ArrayList<String>();
        boldWords.add(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.11.p4.bold"));
        section.add(PDFUtils.createParagraphWithDiferentFormatWord(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.11.p4"), boldWords, ConstantsFont.paragraphBoldFont, ConstantsFont.PARAGRAPH, true));
        PDFUtils.addParagraph(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.11.p5"), ConstantsFont.PARAGRAPH, section);
        boldWords = new ArrayList<String>();
        boldWords.add(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.11.p1.bold"));
        section.add(PDFUtils.createParagraphWithDiferentFormatWord(CrawlerUtils.getResources(request).getMessage("ob.resAnon.intav.report.11.p1"), boldWords, ConstantsFont.paragraphBoldFont, ConstantsFont.PARAGRAPH, true));
    }

}