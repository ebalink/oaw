package es.inteco.accesibilidad;

import ca.utoronto.atrc.tile.accessibilitychecker.EvaluatorUtility;
import es.inteco.common.CheckAccessibility;
import es.inteco.common.IntavConstants;
import es.inteco.common.logging.Logger;
import es.inteco.common.properties.PropertiesManager;
import es.inteco.intav.comun.Incidencia;
import es.inteco.intav.datos.AnalisisDatos;
import es.inteco.intav.datos.IncidenciaDatos;
import es.inteco.intav.persistence.Analysis;
import es.inteco.intav.utils.CacheUtils;
import es.inteco.intav.utils.EvaluatorUtils;
import es.inteco.plugin.Cartucho;
import es.inteco.plugin.dao.DataBaseManager;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementación de un cartucho que analiza las urls, así como el contenido
 * de las páginas y clasificarlas como maliciosas o no.
 */
public class CartuchoAccesibilidad extends Cartucho {

    @Override
    public void analyzer(final Map<String, Object> datos) {
        Logger.putLog("Iniciando evaluación de accesibilidad desde el rastreador de la url: " + datos.get("url"), CartuchoAccesibilidad.class, Logger.LOG_LEVEL_INFO);

        final PropertiesManager pmgr = new PropertiesManager();

        final CheckAccessibility checkAccesibility = new CheckAccessibility();
        checkAccesibility.setEntity((String) datos.get("entity"));
        checkAccesibility.setGuideline(datos.get("guidelineFile").toString().substring(0, datos.get("guidelineFile").toString().lastIndexOf('.')).replace("-nobroken", ""));
        checkAccesibility.setGuidelineFile(datos.get("guidelineFile").toString());
        checkAccesibility.setLevel(pmgr.getValue("crawler.core.properties", "check.accessibility.default.level"));
        checkAccesibility.setUrl((String) datos.get("url"));
        checkAccesibility.setIdRastreo((Long) datos.get("idFulfilledCrawling"));
        checkAccesibility.setIdObservatory((Long) datos.get("idObservatory"));
        checkAccesibility.setContent((String) datos.get("contenido"));
        boolean isLast = (Boolean) datos.get("isLast");

        try {
            EvaluatorUtils.evaluateContent(checkAccesibility, pmgr.getValue("crawler.core.properties", "check.accessibility.default.language"));
        } catch (Exception e) {
            Logger.putLog("Excepcion: ", CartuchoAccesibilidad.class, Logger.LOG_LEVEL_ERROR, e);
        }

        if (isLast) {
            CacheUtils.removeFromCache(IntavConstants.CHECKED_LINKS_CACHE_KEY + checkAccesibility.getIdRastreo());
            Logger.putLog("Realizando tareas post-analisis", CartuchoAccesibilidad.class, Logger.LOG_LEVEL_DEBUG);

            // Calculamos el resultado de la comprobacion titulos diferentes ya que requiere haber realizado el rastreo completo
            if (checkAccesibility.getGuidelineFile().startsWith("observatorio-une-2012")) {
                final long idRastreo = (Long) datos.get("idFulfilledCrawling");
                final List<Long> evaluationIds = AnalisisDatos.getEvaluationIdsFromRastreoRealizado(idRastreo);
                processDiferentTitlesCheck(idRastreo, evaluationIds);
            }

        }
    }

    private void processDiferentTitlesCheck(final long idRastreo, final List<Long> evaluationIds) {
        final Set<String> distribucionTitulos = new HashSet<>();
        try (final Connection connection = DataBaseManager.getConnection()) {
            for (Long evaluationId : evaluationIds) {
                final List<Incidencia> incidencias = IncidenciaDatos.getIncidenciasByAnalisisAndComprobacion(connection, evaluationId, 462);
                for (Incidencia incidencia : incidencias) {
                    distribucionTitulos.add(incidencia.getCodigoFuente());
                }
            }
            // Se verifica que todos los títulos no sean idénticos (para tamaños de muestra >= 10).
            if (evaluationIds.size() < 10 || distribucionTitulos.size() > 1) {
                // Si hay menos de 10 páginas o hay más de 1 título se borran las incidencias
                for (Long evaluationId : evaluationIds) {
                    final Analysis analysis = AnalisisDatos.getAnalisisFromId(connection, evaluationId);
                    final String updatedChecks = analysis.getChecksExecutedStr().replace(",462", "");
                    AnalisisDatos.updateChecksEjecutados(updatedChecks, idRastreo);
                    IncidenciaDatos.deleteIncidenciasByAnalisisAndComprobacion(connection, evaluationId, 462);
                }
            }
        } catch (Exception e) {
            Logger.putLog("Exception al intentar comprobar titulos diferentes: ", CartuchoAccesibilidad.class, Logger.LOG_LEVEL_ERROR, e);
        }
    }

    public void setConfig(final long idRastreo) {
        if (!EvaluatorUtility.isInitialized()) {
            try {
                EvaluatorUtility.initialize();
            } catch (Exception e) {
                Logger.putLog("Exception: ", CartuchoAccesibilidad.class, Logger.LOG_LEVEL_ERROR, e);
            }
        }
    }

}