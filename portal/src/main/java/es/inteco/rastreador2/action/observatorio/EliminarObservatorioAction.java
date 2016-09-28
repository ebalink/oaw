package es.inteco.rastreador2.action.observatorio;

import es.inteco.common.Constants;
import es.inteco.common.logging.Logger;
import es.inteco.plugin.dao.DataBaseManager;
import es.inteco.rastreador2.actionform.observatorio.ObservatorioForm;
import es.inteco.rastreador2.dao.observatorio.ObservatorioDAO;
import es.inteco.rastreador2.utils.ActionUtils;
import es.inteco.rastreador2.utils.CrawlerUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;

public class EliminarObservatorioAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {

        try {
            if (CrawlerUtils.hasAccess(request, "delete.observatory")) {
                String idObservatorio = request.getParameter(Constants.ID_OBSERVATORIO);
                if (request.getParameter(Constants.ES_PRIMERA) != null) {
                    return confirm(mapping, request);
                } else {
                    if (request.getParameter(Constants.CONFIRMACION).equals(Constants.CONF_SI)) {
                        return deleteObservatory(mapping, request, Long.parseLong(idObservatorio));
                    } else {
                        return mapping.findForward(Constants.VOLVER);
                    }
                }
            } else {
                return mapping.findForward(Constants.NO_PERMISSION);
            }
        } catch (Exception e) {
            CrawlerUtils.warnAdministrators(e, this.getClass());
            return mapping.findForward(Constants.ERROR_PAGE);
        }
    }

    private ActionForward confirm(ActionMapping mapping, HttpServletRequest request) throws Exception {
        final Long idObservatory = Long.valueOf(request.getParameter(Constants.ID_OBSERVATORIO));
        try (Connection c = DataBaseManager.getConnection()) {
            ObservatorioForm observatorioForm = ObservatorioDAO.getObservatoryForm(c, idObservatory);
            request.setAttribute(Constants.OBSERVATORY_FORM, observatorioForm);
        }

        return mapping.findForward(Constants.CONFIRMACION_DELETE);
    }

    private ActionForward deleteObservatory(ActionMapping mapping, HttpServletRequest request, long idObservatorio) {
        try (Connection c = DataBaseManager.getConnection()) {
            try {
                ObservatorioDAO.deteleObservatory(c, idObservatorio);
            } catch (Exception e) {
                Logger.putLog("Exception: ", EliminarObservatorioAction.class, Logger.LOG_LEVEL_ERROR, e);
                throw e;
            }

            ActionUtils.setSuccesActionAttributes(request, "mensaje.exito.observatorio.eliminado", "volver.carga.observatorio");
            return mapping.findForward(Constants.EXITO);
        } catch (Exception e) {
            CrawlerUtils.warnAdministrators(e, this.getClass());
            return mapping.findForward(Constants.ERROR_PAGE);
        }
    }
}