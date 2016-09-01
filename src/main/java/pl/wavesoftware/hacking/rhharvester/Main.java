package pl.wavesoftware.hacking.rhharvester;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import pl.wavesoftware.eid.exceptions.Eid;
import pl.wavesoftware.eid.exceptions.EidIllegalStateException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main {
    private static final String RH_DOMAIN = "https://access.redhat.com";
    private static final String SOFTWARE_DETAIL =
            RH_DOMAIN + "/jbossnetwork/restricted/softwareDetail.html";

    public static void main(String[] args) throws IOException {

        String fmt = "%s?softwareId=45371";
        String address = String.format(fmt, SOFTWARE_DETAIL);
        try (WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45)) {
            HtmlPage page1 = webClient.getPage(address);
            Optional<HtmlForm> optionalForm = page1.getForms()
                    .stream()
                    .filter(htmlForm -> "kc-form-login".equals(htmlForm.getId()))
                    .findFirst();
            HtmlForm form = optionalForm.orElseThrow(() -> illegalState("20160901:112316"));

            final HtmlTextInput usernameField = form.getInputByName("username");
            final HtmlPasswordInput passwordField = form.getInputByName("password");
            final HtmlButton submit = form.getElementsByAttribute("button", "type", "submit")
                    .stream()
                    .map(HtmlButton.class::cast)
                    .findAny()
                    .orElseThrow(() -> illegalState("20160901:113016"));

            usernameField.setValueAttribute("*****");
            passwordField.setValueAttribute("*****");

            HtmlPage page2 = submit.click();
            HtmlAnchor downloadAnchor = page2.getElementsByTagName("a")
                    .stream()
                    .filter(Main::isDownloadAnchor)
                    .map(HtmlAnchor.class::cast)
                    .findFirst()
                    .orElseThrow(() -> illegalState("20160901:114420"));

            webClient.setConfirmHandler((page, message) -> true);
            UnexpectedPage unexpectedPage = downloadAnchor.click();
            Path saved = saveFile(unexpectedPage, Paths.get("target"));
            System.out.println("File saved: " + saved);
        }
    }

    private static Path saveFile(UnexpectedPage unexpectedPage, Path directory) {
        try (InputStream stream = unexpectedPage.getInputStream()) {
            WebResponse response = unexpectedPage.getWebResponse();
            String filename = getFilename(response);
            Path target = directory.resolve(filename);
            Files.copy(stream, target);
            return target;
        } catch (IOException e) {
            throw new EidIllegalStateException("20160901:115919", e);
        }
    }

    private static String getFilename(WebResponse response) {
        String value = Optional.ofNullable(
                response.getResponseHeaderValue("Content-Disposition")
        ).orElseThrow(() -> illegalState("20160901:120147"));
        String regex = "attachment;\\s*filename=\"?(.+?)\"?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.group(1);
    }

    private static boolean isDownloadAnchor(DomElement domElement) {
        return domElement.getTextContent().trim().equals("Download");
    }

    private static RuntimeException illegalState(String eid) {
        return new EidIllegalStateException(new Eid(eid));
    }
}

