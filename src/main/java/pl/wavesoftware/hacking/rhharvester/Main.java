package pl.wavesoftware.hacking.rhharvester;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

public final class Main {
    private static final String RH_DOMAIN = "https://access.redhat.com";
    private static final String SOFTWARE_DETAIL =
            RH_DOMAIN + "/jbossnetwork/restricted/softwareDetail.html";
    private static final String CHROME_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";

    public static void main(String[] args) throws IOException {

        String fmt = "%s?softwareId=45681";
        String address = String.format(fmt, SOFTWARE_DETAIL);
        Response response = Jsoup.connect(address)
                .userAgent(CHROME_USER_AGENT)
                .execute();

        Document doc = response.parse();
        Element samlForm = doc.select("form").iterator().next();
        String samlUrl = samlForm.attr("action");
        Elements inputs = samlForm.select("input");
        Map<String, String> cookies = response.cookies();

        Connection connection = Jsoup.connect(samlUrl)
                .userAgent(CHROME_USER_AGENT)
                .cookies(cookies)
                .method(Method.POST);
        inputs.forEach(element -> connection.data(element.attr("name"), element.val()));

        response = connection.execute();
        doc = response.parse();
        Element form = doc.select("form").iterator().next();

        String ssoLoginUrl = form.attr("action");
        cookies = response.cookies();

        response = Jsoup.connect(ssoLoginUrl)
                .userAgent(CHROME_USER_AGENT)
                .cookies(cookies)
                .method(Method.POST)
                .data("username", "****")
                .data("password", "****")
                .execute();
        doc = response.parse();
        System.out.print(doc);
    }
}

