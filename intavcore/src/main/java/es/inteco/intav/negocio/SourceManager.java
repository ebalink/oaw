package es.inteco.intav.negocio;

import ca.utoronto.atrc.tile.accessibilitychecker.Check;
import ca.utoronto.atrc.tile.accessibilitychecker.Problem;
import es.inteco.common.logging.Logger;
import es.inteco.intav.utils.EvaluatorUtils;
import org.w3c.dom.Element;

public class SourceManager {


    /**
     * Obtiene el texto a partir de una etiqueta incluyendo, en algunos casos, los textos de etiquetas anidadas de un problema de accesibilidad
     *
     * @param problem el problema de accesibilidad Problem detectado durante el análisis
     * @return la cadena con el texto
     */
    public static String getSourceInfo(Problem problem) {
        final Check check = problem.getCheck();
        final Element elementProblem = (Element) problem.getNode();
        final String nameProblemElement = check.getKeyElement();

        if ("a".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, true);
        } else if ("img".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("area".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("body".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("title".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, true);
        } else if ("input".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("html".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("legend".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, true);
        } else if ("doctype".equals(nameProblemElement)) {
            Element elementRoot = elementProblem.getOwnerDocument().getDocumentElement();
            String hasDoctype = (String) elementRoot.getUserData("doctype");
            if ("false".equals(hasDoctype)) {
                return "-! missing !-";
            } else {
                String stringDoctype = "<!DOCTYPE HTML PUBLIC ";
                String stringPublic = (String) elementRoot.getUserData("doctypePublicId");
                if (stringPublic != null) {
                    stringDoctype += "\"" + stringPublic + "\"";
                }
                String stringSystem = (String) elementRoot.getUserData("doctypeSystemId");
                if (stringSystem != null) {
                    stringDoctype += " \"" + stringSystem + "\"";
                }
                stringDoctype += " >";
                return stringDoctype;
            }
        } else if ("form".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if (("h1".equals(nameProblemElement)) || ("h2".equals(nameProblemElement)) || ("h3".equals(nameProblemElement)) ||
                ("h4".equals(nameProblemElement)) || ("h5".equals(nameProblemElement)) || ("h6".equals(nameProblemElement))) {
            return getHtmlText(elementProblem, true);
        } else if ("select".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("table".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, false);
        } else if ("caption".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, true);
        } else if ("p".equals(nameProblemElement)) {
            return getHtmlText(elementProblem, true);
        } else {
            if ("*".equals(check.getTriggerElement())) {
                return getHtmlText(elementProblem, false);
            } else {
                return getHtmlText(elementProblem, true);
            }
        }
    }

    /**
     * @param elementGiven     el elemento DOM donde se produce un problema de accesibilidad
     * @param bIncludeChildren flag que indica si se deben incluir los textos de las etiquetas hijas
     * @return a string of HTML code for the given element.
     */
    private static String getHtmlText(Element elementGiven, boolean bIncludeChildren) {
        try {
            return EvaluatorUtils.serializeXmlElement(elementGiven, bIncludeChildren);
        } catch (Exception e) {
            Logger.putLog("Exception: ", SourceManager.class, Logger.LOG_LEVEL_ERROR, e);
        }
        return "";
    }
}