package de.unknownreality.dataframe.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * Created by Alex on 07.06.2016.
 */
public class DataFrameMetaWriter {
    private static final Logger logger = LoggerFactory.getLogger(DataFrameMetaWriter.class);

    public static void write(DataFrameMeta metaFile, File file) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("dataFrameMeta");
            doc.appendChild(rootElement);

            Element readerBuilder = doc.createElement("readerBuilder");
            rootElement.appendChild(readerBuilder);

            Attr attr = doc.createAttribute("class");
            attr.setValue(metaFile.getReaderBuilderClass().getCanonicalName());
            readerBuilder.setAttributeNode(attr);

            for (String attrName : metaFile.getAttributes().keySet()) {
                Element readerAttribute = doc.createElement("readerAttribute");
                Attr name = doc.createAttribute("name");
                name.setValue(attrName);
                readerAttribute.setAttributeNode(name);

                Attr value = doc.createAttribute("value");
                value.setValue(metaFile.getAttributes().get(attrName));
                readerAttribute.setAttributeNode(value);
                readerBuilder.appendChild(readerAttribute);
            }

            Element columns = doc.createElement("columns");
            rootElement.appendChild(columns);

            for (String colName : metaFile.getColumns().keySet()) {
                Element colAttribute = doc.createElement("column");
                Attr name = doc.createAttribute("name");
                name.setValue(colName);
                colAttribute.setAttributeNode(name);

                Attr type = doc.createAttribute("type");
                type.setValue(metaFile.getColumns().get(colName).getCanonicalName());
                colAttribute.setAttributeNode(type);
                columns.appendChild(colAttribute);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            if (!file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (Exception e) {
            logger.error("error writing data frame meta file", e);
        }
    }
}
