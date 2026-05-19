package _test;

import nbbrd.design.Demo;
import nbbrd.heylogs.Heylogs;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeLinkParser;
import nbbrd.heylogs.spi.ForgeLinkType;
import nbbrd.heylogs.spi.MessageFetcher;
import nbbrd.io.http.HttpClient;

import java.io.IOException;
import java.net.URL;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;

public class MessageFetcherDemo {

    @Demo
    public static void main(String[] args) {
        Heylogs heylogs = Heylogs.ofServiceLoader();
        HttpClient client = heylogs.getHttpFactory().getClient();
        System.out.println(test(client, heylogs, urlOf("https://github.com/nbbrd/heylogs/issues/511")));
        System.out.println(test(client, heylogs, urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/issues/1")));
        System.out.println(test(client, heylogs, urlOf("https://codeberg.org/forgejo/forgejo/issues/1")));
    }

    private static String test(HttpClient client, Heylogs heylogs, URL issue) {
        System.out.println("Testing " + issue);

        Forge forge = heylogs.getForges()
                .stream()
                .filter(item -> item.isKnownHost(issue))
                .findFirst()
                .orElse(null);
        if (forge == null) return "No forge found for URL: " + issue;

        ForgeLinkParser linkParser = forge.getLinkParser(ForgeLinkType.ISSUE);
        if (linkParser == null) return "Forge does not support fetching messages";

        MessageFetcher messageFetcher = forge.getMessageFetcher(ForgeLinkType.ISSUE);
        if (messageFetcher == null) return "Forge does not support fetching messages";

        try {
            return messageFetcher.fetchMessage(client, linkParser.parseForgeLink(issue));
        } catch (IOException ex) {
            return "Failed to fetch message: " + ex.getMessage();
        }
    }
}
