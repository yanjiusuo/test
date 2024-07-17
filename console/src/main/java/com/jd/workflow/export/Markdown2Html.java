package com.jd.workflow.export;

import com.vladsch.flexmark.ext.wikilink.WikiImage;
import com.vladsch.flexmark.ext.wikilink.WikiLink;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.*;
import com.vladsch.flexmark.parser.Parser;

import java.util.*;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.anchorlink.internal.AnchorLinkNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import lombok.Data;

public class Markdown2Html {
    static final DataKey<List<MdAchor>> ARCHORS = new DataKey<List<MdAchor>>("archors", new ArrayList<>());

    @Data
    public static class MdAchor{
        String id;
        String title;
        List<MdAchor> children = new ArrayList<>();
    }
    private static DataHolder newOptions(List<MdAchor> archors){
         DataHolder options = PegdownOptionsAdapter.flexmarkOptions(
                Extensions.ALL & ~(Extensions.HARDWRAPS)
                , HeadingExtension.create()).toMutable()
                .set(HtmlRenderer.INDENT_SIZE, 2)
                 .set(ARCHORS,archors);

         return options;
    }
    static class HeadingExtension implements HtmlRendererExtension {
        @Override
        public void rendererOptions( MutableDataHolder options) {
            // add any configuration settings to options you want to apply to everything, here
        }

        @Override
        public void extend( HtmlRenderer.Builder htmlRendererBuilder,  String rendererType) {
            htmlRendererBuilder.nodeRendererFactory(new HeadingNodeRenderer.Factory());
        }

        static HeadingExtension create() {
            return new HeadingExtension();
        }
    }

    static class HeadingNodeRenderer implements NodeRenderer {
        DataHolder options;
        public HeadingNodeRenderer(DataHolder options) {
            this.options = options;
        }

        @Override
        public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            return new HashSet<>(Arrays.asList(
                    new NodeRenderingHandler<>(AnchorLink.class, this::render),
                    new NodeRenderingHandler<>(Heading.class, this::render)
            ));
        }

        void render(AnchorLink node, NodeRendererContext context, HtmlWriter html) {
            Node parent = node.getParent();

            if (parent instanceof Heading && ((Heading) parent).getLevel() == 1) {
                // render without anchor link
                context.renderChildren(node);
            } else {
                context.delegateRender();
            }
        }

        static boolean haveExtension(int extensions, int flags) {
            return (extensions & flags) != 0;
        }

        static boolean haveAllExtensions(int extensions, int flags) {
            return (extensions & flags) == flags;
        }

        void render(Heading node, NodeRendererContext context, HtmlWriter html) {

            if (node.getLevel() == 1 || node.getLevel() == 2) {

                // render without anchor link
                int extensions = ParserEmulationProfile.PEGDOWN_EXTENSIONS.get(context.getOptions());
                if (context.getHtmlOptions().renderHeaderId || haveExtension(extensions, Extensions.ANCHORLINKS) || haveAllExtensions(extensions, Extensions.EXTANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP)) {
                    String id = UUID.randomUUID().toString();
                    MdAchor mdAchor = new MdAchor();
                    mdAchor.setTitle(node.getText().toString());
                    mdAchor.setId(id);;
                    List<MdAchor> list = options.get(ARCHORS);
                    if(node.getLevel() == 1){
                        list.add(mdAchor);
                    }else{
                        list.get(list.size() -1 ).getChildren().add(mdAchor);
                    }
                    if (id != null) {
                        html.attr("id", id);
                    }
                }

                if (context.getHtmlOptions().sourcePositionParagraphLines) {
                    html.srcPos(node.getChars()).withAttr().tagLine("h" + node.getLevel(), () -> {
                        html.srcPos(node.getText()).withAttr().tag("span");
                        context.renderChildren(node);
                        html.tag("/span");
                    });
                } else {
                    html.srcPos(node.getText()).withAttr().tagLine("h" + node.getLevel(), () -> context.renderChildren(node));
                }
            } else {
                context.delegateRender();
            }
        }

        public static class Factory implements DelegatingNodeRendererFactory {
            
            @Override
            public NodeRenderer apply( DataHolder options) {
                return new HeadingNodeRenderer(options);
            }

            @Override
            public Set<Class<?>> getDelegates() {
                Set<Class<?>> delegates = new HashSet<>();
                delegates.add(AnchorLinkNodeRenderer.Factory.class);
                return delegates;
            }
        }
    }
    public static String render(String md,List<MdAchor> achors){
        DataHolder options = newOptions(achors);
        Parser parser = Parser.builder(options).build();

        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(md);

        String title = findTitle(document);
        System.out.println("Title: " + title + "\n");
        return renderer.render(document);
    }
    private static String findTitle(Node root) {
        if (root instanceof Heading) {
            Heading h = (Heading) root;
            if (h.getLevel() == 1 && h.hasChildren()) {
                TextCollectingVisitor collectingVisitor = new TextCollectingVisitor();
                return collectingVisitor.collectAndGetText(h);
            }
        }

        if (root instanceof Block && root.hasChildren()) {
            Node child = root.getFirstChild();
            while (child != null) {
                String title = findTitle(child);
                if (title != null) {
                    return title;
                }
                child = child.getNext();
            }
        }

        return null;
    }

    public static void main(String[] args) {
        List<MdAchor> mdAchors = new ArrayList<>();
        DataHolder options = newOptions(mdAchors);
        Parser parser = Parser.builder(options).build();

        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse("\n" +
                "# Plugin commit-message-length-validator\n" +
                "\n" +
                "Vendor\n" +
                ":  Gerrit Code Review\n" +
                "\n" +
                "Version\n" +
                ":  v2.15-rc2\n" +
                "\n" +
                "commitmessage.maxSubjectLength\n" +
                ":       Maximum length of the commit message's subject line.  Defaults\n" +
                "        to 50 if not specified or less than 0.\n" +
                "\n" +
                "## About\n" +
                "\n" +
                "This plugin checks the length of a commit's commit message subject and body, and reports warnings and errors to the git client if the lentghts are exceeded.\n" +
                "\n" +
                "## Documentation\n" +
                "\n" +
                "* [Commit Message Length Configuration](config.md)\n" +
                "* More Items\n" +
                "\n" +
                "");

        String title = findTitle(document);
        System.out.println("Title: " + title + "\n");
        String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"

        System.out.println(html);
        List<MdAchor> mdAchors1 = options.get(ARCHORS);
        System.out.println(mdAchors1);
    }
}
