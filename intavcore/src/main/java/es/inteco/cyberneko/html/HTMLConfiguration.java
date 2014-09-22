package es.inteco.cyberneko.html;

public class HTMLConfiguration extends org.cyberneko.html.HTMLConfiguration {
    public HTMLConfiguration() {
        super();

        // No queremos incluir el balanceado de etiquetas
        setFeature(BALANCE_TAGS, false);

        // No se alteran las mayúsculas ni minúsculas de los nombres de elementos y atributos
        setProperty(NAMES_ELEMS, "match");
        setProperty(NAMES_ATTRS, "no-change");
    }

    public HTMLConfiguration(boolean balanceTags) {
        super();

        // No queremos incluir el balanceado de etiquetas
        setFeature(BALANCE_TAGS, balanceTags);

        // No se alteran las mayúsculas ni minúsculas de los nombres de elementos y atributos
        setProperty(NAMES_ELEMS, "match");
        setProperty(NAMES_ATTRS, "no-change");
    }
}