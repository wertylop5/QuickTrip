package io.github.kkysen.quicktrip.web;

import io.github.kkysen.quicktrip.io.MyFiles;
import io.github.kkysen.quicktrip.util.regex.RegexUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// TODO add Selenium support for rendered documents

/**
 * utilities for reading the html from a website
 * 
 * @author Khyber Sen
 */
public class Internet {
    
    /**
     * Jsoup user agent string for Chrome
     */
    private static final String JSOUP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/53.0.2785.143 Safari/537.36";
    
    /**
     * Jsoup Google referrer
     */
    private static final String JSOUP_REFERRER = "http://www.google.com";
    
    /**
     * HtmlUnit Chrome browser version for WebClient
     */
    private static final BrowserVersion HTML_UNIT_BROWSER_VERSION = BrowserVersion.CHROME;
    
    /**
     * helpful for catching lots of exceptions in streams, but probably not a
     * good idea to use
     */
    private static boolean suppressedExceptions;
    
    public static boolean hasSuppressedExceptions() {
        return suppressedExceptions;
    }
    
    public static void suppressExceptions() {
        suppressedExceptions = true;
    }
    
    public static void unsuppressExceptions() {
        suppressedExceptions = false;
    }
    
    /**
     * @param url a URL
     * @return an InputStreamReader of the HTTP response to the url
     * @throws IOException an IOException
     */
    public static InputStreamReader getInputStreamReader(final String url) throws IOException {
        return new InputStreamReader(new URL(url).openStream());
    }
    
    /**
     * @param url a URL
     * @return a BufferedReader of the HTTP response to the url
     * @throws IOException an IOException
     */
    public static BufferedReader getBufferedReader(final String url) throws IOException {
        return new BufferedReader(getInputStreamReader(url));
    }
    
    /**
     * @param url a URL
     * @return an InputStreamReader of the HTTP response to the url, which has
     *         an Accept header of application/json so that the response is JSON
     *         formatted
     * @throws IOException an IOException
     */
    public static InputStreamReader getJsonInputStreamReader(final String url) throws IOException {
        final HttpURLConnection urlCon = (HttpURLConnection) new URL(url).openConnection();
        urlCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlCon.setRequestProperty("Accept", "application/json");
        urlCon.connect();
        //urlCon.setRequestProperty("Connection", "keep-alive");
        //urlCon.setRequestProperty("Upgrade-Insecure-Requests", "1");
        /*System.out.println("headers");
        for (int x = 0; x < urlCon.getHeaderFields().size();x++) {
          System.out.println(urlCon.getHeaderFieldKey(x) + ": " + urlCon.getHeaderField(x));
        }*/
        return new InputStreamReader(urlCon.getInputStream());
    }
    
    /**
     * @param url a URL
     * @return a StringBuilder of HTTP response
     * @throws IOException an IOException
     */
    public static StringBuilder getStringBuilder(final String url) throws IOException {
        final BufferedReader reader = getBufferedReader(url);
        final StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb;
    }
    
    /**
     * @param url a URL
     * @return a String of HTTP response
     * @throws IOException an IOException
     */
    public static String getString(final String url) throws IOException {
        return getStringBuilder(url).toString();
    }
    
    /**
     * returns a Jsoup Document
     * 
     * @param url URL of the website
     * @return a Jsoup Document of the website
     * @throws IOException an IOException
     */
    public static Document getDocument(final String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(JSOUP_USER_AGENT)
                .referrer(JSOUP_REFERRER)
                .get();
    }
    
    public static Stream<Document> getDocuments(final Stream<String> urls) {
        return urls.map(url -> {
            try {
                return getDocument(url);
            } catch (final IOException e) {
                if (!suppressedExceptions) {
                    throw new RuntimeException(e);
                }
                //e.printStackTrace();
                return Jsoup.parse(""); // blank doc
            }
        });
    }
    
    public static Stream<Document> getDocuments(final String... urls) {
        return getDocuments(Stream.of(urls));
    }
    
    public static Stream<Document> getDocuments(final Collection<String> urls) {
        return getDocuments(urls.parallelStream());
    }
    
    private static WebClient getSilencedWebClient() {
        
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
        Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        
        final WebClient webClient = new WebClient(HTML_UNIT_BROWSER_VERSION);
        
        final WebClientOptions webClientOptions = webClient.getOptions();
        webClientOptions.setCssEnabled(false);
        webClientOptions.setThrowExceptionOnFailingStatusCode(false);
        webClientOptions.setThrowExceptionOnScriptError(false);
        
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        
        webClient.setIncorrectnessListener((arg0, arg1) -> {});
        
        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
            
            @Override
            public void loadScriptError(final InteractivePage arg0, final URL arg1,
                    final Exception arg2) {}
            
            @Override
            public void malformedScriptURL(final InteractivePage arg0, final String arg1,
                    final MalformedURLException arg2) {}
            
            @Override
            public void scriptException(final InteractivePage arg0,
                    final ScriptException arg1) {}
            
            @Override
            public void timeoutError(final InteractivePage arg0, final long arg1,
                    final long arg2) {}
            
        });
        
        webClient.setHTMLParserListener(new HTMLParserListener() {
            
            @Override
            public void error(final String arg0, final URL arg1, final String arg2, final int arg3,
                    final int arg4, final String arg5) {}
            
            @Override
            public void warning(final String arg0, final URL arg1, final String arg2,
                    final int arg3,
                    final int arg4, final String arg5) {}
            
        });
        
        return webClient;
        
    }
    
    /**
     * @param url a URL
     * @return a Jsoup Document of the website after the JavaScript has been run
     *         (post-DOM modifications) using HtmlUnit
     * @throws IOException an IOException
     */
    public static Document getRenderedDocument(final String url) throws IOException {
        final WebClient webClient = getSilencedWebClient();
        final HtmlPage page = webClient.getPage(url);
        final File tempFile = new File("tempHtmlPage.html");
        page.save(tempFile);
        final Document doc = Jsoup.parse(tempFile, "UTF-8");
        webClient.close();
        tempFile.delete();
        return doc;
    }
    
    public static Stream<Document> getRenderedDocuments(final Stream<String> urls) {
        return urls.map(url -> {
            try {
                return getRenderedDocument(url);
            } catch (FailingHttpStatusCodeException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public static Stream<Document> getRenderedDocuments(final String... urls) {
        return getRenderedDocuments(Stream.of(urls));
    }
    
    public static Stream<Document> getRenderedDocuments(final Collection<String> urls) {
        return getRenderedDocuments(urls.parallelStream());
    }
    
    public static Stream<String> getLinkedUrls(final Document doc) {
        return RegexUtils.findUrls(doc.html()).parallelStream();
    }
    
    public static Stream<String> getLinkedUrls(final Document doc,
            final Predicate<String> urlFilter) {
        return getLinkedUrls(doc).filter(urlFilter);
    }
    
    public static Stream<Document> getLinkedDocuments(final Document doc) {
        return getDocuments(getLinkedUrls(doc));
    }
    
    public static Stream<Document> getLinkedDocuments(final Document doc,
            final Predicate<String> urlFilter) {
        return getDocuments(getLinkedUrls(doc, urlFilter));
    }
    
    public static Stream<Document> getLinkedRenderedDocuments(final Document doc) {
        return getRenderedDocuments(getLinkedUrls(doc));
    }
    
    public static Stream<Document> getLinkedRenderedDocuments(final Document doc,
            final Predicate<String> urlFilter) {
        return getRenderedDocuments(getLinkedUrls(doc, urlFilter));
    }
    
    private static Stream<Document> getLinkedDocumentsUsingFunction(final Document doc, int depth,
            final Function<Document, Stream<Document>> mapper) {
        if (--depth < 0) {
            throw new IllegalArgumentException("cannot be 0 or negative");
        }
        Stream<Document> docs = getLinkedDocuments(doc);
        for (int i = 0; i < depth; i++) {
            docs = docs.flatMap(mapper);
        }
        return docs;
    }
    
    public static Stream<Document> getLinkedDocuments(final Document doc, final int depth) {
        return getLinkedDocumentsUsingFunction(doc, depth, Internet::getLinkedDocuments);
    }
    
    public static Stream<Document> getLinkedDocuments(final Document doc,
            final Predicate<String> urlFilter, final int depth) {
        return getLinkedDocumentsUsingFunction(doc, depth,
                doc0 -> getLinkedDocuments(doc0, urlFilter));
    }
    
    public static Stream<Document> getLinkedRenderedDocuments(final Document doc, final int depth) {
        return getLinkedDocumentsUsingFunction(doc, depth, Internet::getLinkedRenderedDocuments);
    }
    
    public static Stream<Document> getLinkedRenderedDocuments(final Document doc,
            final Predicate<String> urlFilter, final int depth) {
        return getLinkedDocumentsUsingFunction(doc, depth,
                doc0 -> getLinkedRenderedDocuments(doc0, urlFilter));
    }
    
    public static void main(final String[] args) throws Exception {
        final String urlBeginning = "https://www.hotels.com/search/listings.json?"
                + "sort-order=DISTANCE_FROM_LANDMARK&"
                + "q-destination=296+6th+St,+Brooklyn,+NY+11215,+USA&"
                + "q-room-1-children=0&"
                + "q-room-0-adults=2&"
                + "q-room-1-adults=2&"
                + "q-rooms=2&"
                + "q-room-0-children=0&"
                + "pn=";
        final String urlEnd = "&callback=dio.pages.sha.searchResultsCallback";
        IntStream.rangeClosed(0, 20).parallel().forEach(i -> {
            final String url = urlBeginning + i + urlEnd;
            String json;
            try {
                json = getString(url);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            json = json.substring(json.indexOf('{'), json.lastIndexOf('}') + 1);
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
            final JsonArray results = jsonObj.get("data").getAsJsonObject().get("body")
                    .getAsJsonObject()
                    .get("searchResults").getAsJsonObject().get("results").getAsJsonArray();
            final String formattedJson = gson.toJson(results);
            try {
                MyFiles.write(Paths.get("hotels.com test pg=" + i + ".json"), formattedJson);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
}
