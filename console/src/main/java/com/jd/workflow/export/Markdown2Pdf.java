package com.jd.workflow.export;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.anchorlink.internal.AnchorLinkNodeRenderer;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.*;
import java.util.*;

public class Markdown2Pdf {

    static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)
            , TocExtension.create()).toMutable()
            .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
            .toImmutable();

    static String getResourceFileContent(String resourcePath) {
        StringWriter writer = new StringWriter();
        getResourceFileContent(writer, resourcePath);
        return writer.toString();
    }

    private static void getResourceFileContent(StringWriter writer, String resourcePath) {
        InputStream inputStream = Markdown2Pdf.class.getResourceAsStream(resourcePath);
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String pegdown = IOUtils.toString(new FileInputStream("D:\\tmp\\docConfig\\a.md"));
        System.out.println("pegdown\n");
        System.out.println(pegdown);

        Parser PARSER = Parser.builder(OPTIONS).build();
        HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

        Node document = PARSER.parse(pegdown);
        String html = RENDERER.render(document);

        // add embedded fonts for non-latin character set rendering
        // change file:///usr/local/fonts/ to your system's path for font installation Unix path or
        // on windows the path should start with `file:/X:/...` where `X:/...` is the drive
        // letter followed by the full installation path.
        //
        // Google Noto fonts can be downloaded from https://www.google.com/get/noto/
        // `arialuni.ttf` from https://www.wfonts.com/font/arial-unicode-ms
        String nonLatinFonts = "" +
                "<style>\n" +
                "@font-face {\n" +
                "  font-family: 'noto-cjk';\n" +
                "  src: url('file:///usr/local/fonts/arialuni.ttf');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-serif';\n" +
                "  src: url('file:///usr/local/fonts/NotoSerif-Regular.ttf');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-sans';\n" +
                "  src: url('file:///usr/local/fonts/NotoSans-Regular.ttf');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "@font-face {\n" +
                "  font-family: 'noto-mono';\n" +
                "  src: url('file:///usr/local/fonts/NotoMono-Regular.ttf');\n" +
                "  font-weight: normal;\n" +
                "  font-style: normal;\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "    font-family: 'noto-sans', 'noto-cjk', sans-serif;\n" +
                "    overflow: hidden;\n" +
                "    word-wrap: break-word;\n" +
                "    font-size: 14px;\n" +
                "}\n" +
                "\n" +
                "var,\n" +
                "code,\n" +
                "kbd,\n" +
                "pre {\n" +
                "    font: 0.9em 'noto-mono', Consolas, \"Liberation Mono\", Menlo, Courier, monospace;\n" +
                "}\n" +
                "</style>\n" +
                "";

        html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n" +
                "" +  // add your stylesheets, scripts styles etc.
                // uncomment line below for adding style for custom embedded fonts
                // nonLatinFonts +
                "</head><body>" + html + "\n" +
                "</body></html>";

        PdfConverterExtension.exportToPdf("D:\\tmp\\docConfig\\a.pdf", html, "", OPTIONS);


    }
}
