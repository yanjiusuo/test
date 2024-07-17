import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class TestXml {
    public static String createSampleForElement(  )
    {
        XmlObject xml = XmlObject.Factory.newInstance();

        XmlCursor c = xml.newCursor();
        c.currentTokenType();
        c.toNextToken();
        c.insertElement("sid");
        c.toPrevToken();
        System.out.println(getName(c));
        c.insertElement( "person");
        c.toNextToken();
        System.out.println(getName(c));

        c.insertElement("kkk");
        c.toPrevToken();

        System.out.println(getName(c));
        c.toPrevToken();
        c.insertChars("fdsfds");

        c.dispose();

        XmlOptions options = new XmlOptions();
        options.put( XmlOptions.SAVE_PRETTY_PRINT );
        options.put( XmlOptions.SAVE_PRETTY_PRINT_INDENT, 3 );
        options.put( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES );
        options.setSaveOuter();
        String result = xml.xmlText( options );

        return result;
    }
    private static QName getName(XmlCursor xmlc){
        if(xmlc.isStart()) return xmlc.getName();
        XmlCursor parent = xmlc.newCursor();
        parent.toParent();
        QName name = parent.getName();
        parent.dispose();
        return name;
    }
    public static void main(String[] args) {
        String name = createSampleForElement();
        System.out.println(name);
        List<Integer> list= new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println(list.subList(2,4));

    }
}
