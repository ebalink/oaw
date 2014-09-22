package es.inteco.rastreador2.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.events.IndexEvents;
import es.inteco.common.Constants;
import es.inteco.common.ConstantsFont;
import es.inteco.common.logging.Logger;
import es.inteco.common.properties.PropertiesManager;
import es.inteco.plugin.dao.DataBaseManager;
import es.inteco.rastreador2.actionform.observatorio.ObservatorioForm;
import es.inteco.rastreador2.actionform.observatorio.ObservatorioRealizadoForm;
import es.inteco.rastreador2.actionform.semillas.CategoriaForm;
import es.inteco.rastreador2.dao.observatorio.ObservatorioDAO;
import es.inteco.rastreador2.pdf.template.ExportPageEventsObservatoryMP;
import es.inteco.rastreador2.pdf.utils.IndexUtils;
import es.inteco.rastreador2.pdf.utils.PDFUtils;
import es.inteco.rastreador2.utils.CrawlerUtils;
import es.inteco.rastreador2.utils.ResultadosAnonimosObservatorioIntavUtils;
import es.inteco.utils.FileUtils;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;

import static es.inteco.common.Constants.CRAWLER_PROPERTIES;

public class AnonymousResultExportPdfAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession().getAttribute(Constants.ROLE) == null || CrawlerUtils.hasAccess(request, "export.anonymous.observatory")) {
            return listPdf(mapping, request, response);
        } else {
            return mapping.findForward(Constants.NO_PERMISSION);
        }
    }

    private ActionForward listPdf(ActionMapping mapping, HttpServletRequest request, HttpServletResponse response) {
        PropertiesManager pmgr = new PropertiesManager();

        long idExecution = 0;
        if (request.getParameter(Constants.ID) != null) {
            idExecution = Long.parseLong(request.getParameter(Constants.ID));
        }
        long idObservatory = 0;
        if (request.getParameter(Constants.ID_OBSERVATORIO) != null) {
            idObservatory = Long.parseLong(request.getParameter(Constants.ID_OBSERVATORIO));
        }

        String path = pmgr.getValue(CRAWLER_PROPERTIES, "path.inteco.exports.observatory.intav")
                + idObservatory + File.separator + idExecution + File.separator + "anonymous"
                + File.separator;

        String filePath = null;

        Connection c = null;
        try {
            c = DataBaseManager.getConnection();
            ObservatorioForm observatoryForm = ObservatorioDAO.getObservatoryForm(c, idObservatory);
            filePath = path + PDFUtils.formatSeedName(observatoryForm.getNombre()) + ".pdf";
            String graphicPath = path + "temp" + File.separator;

            File checkFile = new File(filePath);

            // Si el pdf no ha sido creado lo creamos
            if (ResultadosAnonimosObservatorioIntavUtils.getGlobalResultData(String.valueOf(idExecution), Constants.COMPLEXITY_SEGMENT_NONE, null).size() != 0) {
                if (request.getParameter(Constants.EXPORT_PDF_REGENERATE) != null || !checkFile.exists()) {
                    ResultadosAnonimosObservatorioIntavUtils.generateGraphics(request, graphicPath, Constants.MINISTERIO_P, true);
                    exportToPdf(idObservatory, idExecution, request, filePath, graphicPath, observatoryForm.getTipo());
                    FileUtils.deleteDir(new File(graphicPath));
                }
            } else {
                ActionErrors errors = new ActionErrors();
                errors.add("errorPDF", new ActionMessage("error.pdf.noResults"));
                saveErrors(request, errors);
                return mapping.findForward(Constants.GET_FULFILLED_OBSERVATORIES);
            }
        } catch (Exception e) {
            Logger.putLog("Error al exportar a pdf", ExportAction.class, Logger.LOG_LEVEL_ERROR, e);
            return mapping.findForward(Constants.ERROR_PAGE);
        } finally {
            DataBaseManager.closeConnection(c);
        }

        try {
            CrawlerUtils.returnFile(filePath, response, "application/pdf", false);
        } catch (Exception e) {
            Logger.putLog("Exception al devolver el PDF", ExportAction.class, Logger.LOG_LEVEL_ERROR, e);
        }

        return null;
    }

    private void exportToPdf(Long idObservatory, Long idExecution, HttpServletRequest request, String generalExpPath, String graphicPath, long observatoryType) {
        Connection conn = null;

        try {
            conn = DataBaseManager.getConnection();
            String entidad = ObservatorioDAO.getObservatoryForm(conn, idObservatory).getNombre();

            File file = new File(generalExpPath);
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                Logger.putLog("No se ha podido crear los directorios en exportToPdf " + generalExpPath, AnonymousResultExportPdfAction.class, Logger.LOG_LEVEL_ERROR);
            }

            FileOutputStream fileOut = new FileOutputStream(file);
            Document document = new Document(PageSize.A4, 50, 50, 120, 72);

            PropertiesManager pmgr = new PropertiesManager();
            SimpleDateFormat df = new SimpleDateFormat(pmgr.getValue(CRAWLER_PROPERTIES, "date.format.simple.pdf"));
            ObservatorioRealizadoForm observatoryForm = ObservatorioDAO.getFulfilledObservatory(conn, idObservatory, idExecution);

            PdfWriter writer = PdfWriter.getInstance(document, fileOut);
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            writer.getExtraCatalog().put(new PdfName("Lang"), new PdfString("es"));
            writer.setPageEvent(new ExportPageEventsObservatoryMP(getResources(request).getMessage("ob.resAnon.intav.report.foot", entidad, df.format(observatoryForm.getFecha())), df.format(observatoryForm.getFecha()), false));

            //Inicializamos el índice
            IndexEvents index = new IndexEvents();
            writer.setPageEvent(index);
            writer.setLinearPageMode();

            document.open();

            PDFUtils.addTitlePage(document, getResources(request).getMessage(getLocale(request), "ob.resAnon.intav.report.title", entidad.toUpperCase()), "", ConstantsFont.documentTitleMPFont, ConstantsFont.documentSubtitleMPFont);
            List<CategoriaForm> categories = ObservatorioDAO.getExecutionObservatoryCategories(conn, idExecution);

            final Font titleFont = ConstantsFont.chapterTitleMPFont;
            titleFont.setStyle(Font.BOLD);
            //Introducimos el contenido
            int countSections = 1;
            int numChapter = 1;
            countSections = AnonymousResultExportPdfSections.createIntroductionChapter(request, index, document, countSections, numChapter, titleFont);
            numChapter = numChapter + 1;
            countSections = AnonymousResultExportPdfSections.createObjetiveChapter(request, index, document, countSections, numChapter, titleFont, observatoryType);
            numChapter = numChapter + 1;
            countSections = AnonymousResultExportPdfSections.createMethodologyChapter(request, index, document, countSections, numChapter, titleFont, null, observatoryType, false);
            numChapter = numChapter + 1;
            countSections = AnonymousResultExportPdfSections.createGlobalResultsChapter(request, index, document, countSections, numChapter, titleFont, graphicPath, String.valueOf(idExecution), idObservatory, categories, observatoryType);

            AnonymousResultExportPdfSections.createIntroductionChapter(request, index, document, countSections, numChapter, titleFont);

            for (CategoriaForm category : categories) {
                numChapter = numChapter + 1;
                countSections = AnonymousResultExportPdfSections.createCategoryResultsChapter(request, index, document, countSections, numChapter, titleFont, graphicPath, String.valueOf(idExecution), conn, idObservatory, category, observatoryType);
            }

            if (ObservatorioDAO.getFulfilledObservatories(conn, idObservatory, Constants.NO_PAGINACION, observatoryForm.getFecha()).size() >= Integer.parseInt(pmgr.getValue("pdf.properties", "pdf.anonymous.results.pdf.min.obser"))) {
                numChapter = numChapter + 1;
                countSections = AnonymousResultExportPdfSections.createEvolutionResultsChapter(request, index, document, countSections, numChapter, titleFont, graphicPath, String.valueOf(idExecution), idObservatory);
            }

            numChapter = numChapter + 1;
            AnonymousResultExportPdfSections.createSummaryChapter(request, index, document, countSections, numChapter, titleFont);

            //Creamos el index
            IndexUtils.createIndex(writer, document, request, index, false, ConstantsFont.chapterTitleMPFont);
            ExportPageEventsObservatoryMP.setLastPage(false);

            if (document.isOpen()) {
                try {
                    // ¿Hace falta este try?. En principio no se define ningún throws Exception para el método close
                    document.close();
                } catch (Exception e) {
                    Logger.putLog("Error al cerrar el pdf", AnonymousResultExportPdfAction.class, Logger.LOG_LEVEL_ERROR, e);
                }
            }
        } catch (Exception e) {
            Logger.putLog("Error al crear la seccion de resultados.", AnonymousResultExportPdfAction.class, Logger.LOG_LEVEL_ERROR, e);
        } finally {
            DataBaseManager.closeConnection(conn);
        }
    }

}