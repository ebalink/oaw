package es.inteco.utils;

import es.inteco.common.logging.Logger;
import es.inteco.common.properties.PropertiesManager;
import es.inteco.crawler.common.Constants;
import es.inteco.crawler.ignored.links.IgnoredLink;
import es.inteco.crawler.utils.StringUtils;
import es.inteco.plugin.dao.DataBaseManager;
import es.inteco.plugin.dao.RastreoDAO;
import org.mozilla.universalchardet.UniversalDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrawlerUtils {

    private CrawlerUtils() {
    }

    public static boolean domainMatchs(List<String> domainList, String domain) {
        boolean hasMatched = false;
        for (String domainRegExp : domainList) {
            Pattern pattern = Pattern.compile(domainRegExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

            Matcher matcher = pattern.matcher(domain);
            if (matcher.find()) {
                hasMatched = true;
                break;
            }
        }

        return hasMatched;
    }

    public static boolean isSwitchLanguageLink(Element link, List<IgnoredLink> ignoredLinks) {
        if (ignoredLinks != null) {
            for (IgnoredLink ignoredLink : ignoredLinks) {
                if (matchsText(link, ignoredLink) || matchsImage(link, ignoredLink) || (link.getNodeName().equalsIgnoreCase("AREA") && matchsAlt(link, ignoredLink))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchsText(Element link, IgnoredLink ignoredLink) {
        return matchs(removeInlineTags(link.getTextContent()).trim(), ignoredLink.getText())
                || (CrawlerDOMUtils.hasAttribute(link, "title") && matchs(CrawlerDOMUtils.getAttribute(link, "title").trim(), ignoredLink.getTitle()));
    }

    private static boolean matchsAlt(Element link, IgnoredLink ignoredLink) {
        return CrawlerDOMUtils.hasAttribute(link, "alt") && matchs(CrawlerDOMUtils.getAttribute(link, "alt").trim(), ignoredLink.getText());
    }

    private static String removeInlineTags(String content) {
        PropertiesManager pmgr = new PropertiesManager();
        List<String> inlineTags = Arrays.asList(pmgr.getValue("crawler.core.properties", "inline.tags").split(";"));

        for (String tag : inlineTags) {
            content = Pattern.compile(String.format("</?%s +[^>]*>|</?%s>", tag, tag), Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(content).replaceAll(" ");
        }

        return content;
    }

    private static boolean matchsImage(Element link, IgnoredLink ignoredLink) {
        List<Element> images = CrawlerDOMUtils.getElementsByTagName(link, "frame");

        if (images.size() == 1) {
            for (Element image : images) {
                if (matchs(image.getAttribute("alt"), ignoredLink.getText())
                        || (StringUtils.isEmpty(image.getAttribute("alt").trim()) && matchs(image.getAttribute("title"), ignoredLink.getText()))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean matchs(String text, String regExp) {
        Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    public static String removeHtmlComments(String textContent) {
        return textContent.replaceAll("(?s)<!--.*?-->", "");
    }

    public static URL getAbsoluteUrl(Document document, String rootUrl, String urlLink) throws Exception {
        String base = CrawlerDOMUtils.getBaseUrl(document);
        return StringUtils.isEmpty(base) ? new URL(new URL(rootUrl), urlLink) : new URL(new URL(base), urlLink);
    }

    public static List<String> addDomainsToList(String seedsList, boolean getOnlyDomain, int type) {
        if (StringUtils.isNotEmpty(seedsList)) {
            List<String> domains = new ArrayList<String>();

            String[] seeds = seedsList.split(";");
            for (int i = 0; i < seeds.length; i++) {
                if (type == Constants.ID_LISTA_SEMILLA && !seeds[i].startsWith("http://") && !seeds[i].startsWith("https://")) {
                    seeds[i] = "http://" + seeds[i];
                }
                if (getOnlyDomain) {
                    domains.add(convertDomains(seeds[i]));
                } else {
                    domains.add(seeds[i]);
                }
            }

            return domains;
        } else {
            return null;
        }
    }

    public static String convertDomains(String domain) {
        String convertedDomain = "";

        try {
            URL domainUrl = new URL(domain);

            convertedDomain = domainUrl.getHost();
        } catch (Exception e) {
            Logger.putLog("Error al obtener el dominio base de la URL", CrawlerUtils.class, Logger.LOG_LEVEL_ERROR);
        }

        return convertedDomain;
    }

    public static String getHash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(string.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception e) {
            Logger.putLog("Error al obtener la codificación MD5 de una cadena de texto", CrawlerUtils.class, Logger.LOG_LEVEL_ERROR);
            return null;
        }
    }

    public static boolean hasToBeFilteredUri(HttpServletRequest request) {
        PropertiesManager pmgr = new PropertiesManager();
        List<String> notFilteredUris = Arrays.asList(pmgr.getValue("crawler.properties", "not.filtered.uris").split(";"));

        if (request.getParameter("key") != null
                && request.getParameter("key").equals(pmgr.getValue("crawler.core.properties", "not.filtered.uris.security.key"))
                && containsUriFragment(notFilteredUris, request.getRequestURI())) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean containsUriFragment(List<String> uriFragments, String uri) {
        for (String uriFragment : uriFragments) {
            if (uri.contains(uriFragment)) {
                return true;
            }
        }
        return false;
    }

    public static String encodeUrl(String url) {
        return url.replaceAll(" ", "%20").replaceAll("Á", "%E1").replaceAll("É", "%C9").replaceAll("Í", "%CD")
                .replaceAll("Ó", "%D3").replaceAll("Ú", "%DA").replaceAll("á", "%E1").replaceAll("é", "%E9")
                .replaceAll("í", "%ED").replaceAll("ó", "%F3").replaceAll("ú", "%FA")
                .replaceAll("Ñ", "%D1").replaceAll("ñ", "%F1").replaceAll("&amp;", "&");
    }

    public static List<String> getDomainsList(Long idCrawling, int type, boolean getOnlyDomain) {
        Connection conn = null;
        try {
            conn = DataBaseManager.getConnection();
            return CrawlerUtils.addDomainsToList(RastreoDAO.getList(conn, idCrawling, type), getOnlyDomain, type);
        } catch (Exception e) {
            // TODO: ¿Sustituir por lista vacia Collections.emptyList() ?
            return null;
        } finally {
            DataBaseManager.closeConnection(conn);
        }
    }

    public static String getCharset(HttpURLConnection connection, InputStream markableInputStream) throws Exception {
        String charset = Constants.DEFAULT_CHARSET;
        boolean found = false;

        // Buscamos primero en las cabeceras de la respuesta
        try {
            String header = connection.getHeaderField("Content-type");
            String charsetValue = header.substring(header.indexOf("charset"));
            charsetValue = charsetValue.substring(charsetValue.indexOf('=') + 1);
            if (StringUtils.isNotEmpty(charsetValue)) {
                charset = charsetValue;
                found = true;
            }
        } catch (Exception e) {
            // found = false;
        }


        // Si no lo hemos encontrado en las cabeceras, intentaremos buscarlo en la etiqueta <meta> correspondiente
        /*if(!found) {
            String regexp = "<meta.*charset=(.*?)\"";
			Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				
			Matcher matcher = pattern.matcher(StringUtils.getContentAsString(markableInputStream));
			if(matcher.find()) {
				charset = matcher.group(1);
				found = true;
			}
			
			// Reseteamos el InputStream para poder leerlo de nuevo más tarde
			markableInputStream.reset();
		}*/

        if (!found || !isValidCharset(charset)) {
            charset = getCharsetWithUniversalDetector(markableInputStream);
            markableInputStream.reset();
        }

        if (found && !isValidCharset(charset)) {
            charset = Constants.DEFAULT_CHARSET;
        }

        return charset;
    }

    private static String getCharsetWithUniversalDetector(InputStream markableInputStream) {
        try {
            UniversalDetector detector = new UniversalDetector(null);
            byte[] buf = new byte[4096];

            int nread;
            while ((nread = markableInputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            return detector.getDetectedCharset();
        } catch (Exception e) {
            Logger.putLog("Error al detectar la codificación con Universal Detector", CrawlerUtils.class, Logger.LOG_LEVEL_INFO);
            return null;
        }
    }

    private static boolean isValidCharset(String charset) {
        try {
            byte[] test = new byte[10];
            new String(test, charset);
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public static String getTextContent(HttpURLConnection connection, InputStream markableInputStream) throws Exception {
        String textContent = FileUtils.getContentAsString(markableInputStream, getCharset(connection, markableInputStream));

        textContent = removeHtmlComments(textContent);

        // Añadimos el código de los FRAMES
        String framesSource = CrawlerDOMUtils.getFramesSource(connection.getURL().toString(), textContent);
        if (StringUtils.isNotEmpty(framesSource)) {
            textContent = CrawlerDOMUtils.appendFramesSource(textContent, framesSource);
        }

        // Añadimos el código de los IFRAMES
        try {
            textContent = CrawlerDOMUtils.appendIframesSource(connection.getURL().toString(), textContent);
        } catch (Exception e) {
            Logger.putLog("Error al añadir el código fuente de los iframes", CrawlerUtils.class, Logger.LOG_LEVEL_INFO);
        }

        return textContent;
    }

    public static InputStream getMarkableInputStream(HttpURLConnection connection) throws Exception {
        InputStream content = connection.getInputStream();

        BufferedInputStream stream = new BufferedInputStream(content);

        // mark InputStream so we can restart it for validator
        if (stream.markSupported()) {
            stream.mark(Integer.MAX_VALUE);
        }
        return stream;
    }

    public static HttpURLConnection getConnection(String url, String refererUrl, boolean followRedirects) throws Exception {
        PropertiesManager pmgr = new PropertiesManager();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(followRedirects);
        connection.setConnectTimeout(Integer.parseInt(pmgr.getValue("crawler.core.properties", "crawler.timeout")));
        connection.setReadTimeout(Integer.parseInt(pmgr.getValue("crawler.core.properties", "crawler.timeout")));
        connection.addRequestProperty("Accept", pmgr.getValue("crawler.core.properties", "method.accept.header"));
        connection.addRequestProperty("Accept-Language", pmgr.getValue("crawler.core.properties", "method.accept.language.header"));
        connection.addRequestProperty("User-Agent", pmgr.getValue("crawler.core.properties", "method.user.agent.header"));
        if (refererUrl != null) {
            connection.addRequestProperty("Referer", refererUrl);
        }
        return connection;
    }


    public static HttpURLConnection followRedirection(HttpURLConnection connection, String cookie, URL url, String redirectTo) throws Exception {
        URL metaRedirection = new URL(url, redirectTo);
        Logger.putLog("Siguiendo la redirección de " + connection.getURL() + " a " + metaRedirection, CrawlerUtils.class, Logger.LOG_LEVEL_INFO);
        connection = getConnection(metaRedirection.toString(), url.toString(), false);
        connection.setRequestProperty("Cookie", cookie);
        return connection;
    }

    public static String getCookie(HttpURLConnection connection) {
        // Cogemos la lista de cookies, teniendo en cuenta que el parametro set-cookie no es sensible a mayusculas o minusculas
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        List<String> headers = new ArrayList<String>();
        if (headerFields != null && !headerFields.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                if ("SET-COOKIE".equalsIgnoreCase(entry.getKey())) {
                    headers.addAll(entry.getValue());
                }
            }
        }

        final StringBuilder headerText = new StringBuilder();
        for (String header : headers) {
            if (header.contains(";")) {
                if (!header.substring(0, header.indexOf(';')).toLowerCase().endsWith("deleted")) {
                    headerText.append(header.substring(0, header.indexOf(';'))).append("; ");
                }
            } else {
                headerText.append(header).append("; ");
            }
        }

        return headerText.toString();
    }

    public static boolean isOpenDNSResponse(HttpURLConnection connection) {
        return connection.getHeaderField("Server") != null && connection.getHeaderField("Server").toLowerCase().contains("opendns");
    }

    public static boolean isRss(String content) {
        return !content.toLowerCase().contains("</html>") && content.toLowerCase().contains("</rss>");
    }
}