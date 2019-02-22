package com.eipbench.tpchgenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class TpchTableSchemaParser {

    public Map<String, TpchMetaTable> parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = factory.newDocumentBuilder();
        final Document document = docBuilder.parse(is);

        final Map<String, TpchMetaTable> foundTables = new LinkedHashMap<String, TpchMetaTable>();

        final Node rootNode = document.getChildNodes().item(0);
        final NodeList firstLevelNodes = rootNode.getChildNodes();
        for (int i = 0; i < firstLevelNodes.getLength(); ++i) {
            final Node firstLevelItem = firstLevelNodes.item(i);

            if (firstLevelItem.getNodeName().equals("tables")) {
                final NodeList tables = firstLevelItem.getChildNodes();
                for (int j = 0; j < tables.getLength(); ++j) {
                    final Node table = tables.item(j);
                    if (table.getNodeName().equals("table")) {
                        final Node tableName = table.getAttributes().getNamedItem("name");
                        final TpchMetaTable tpchMetaTable = new TpchMetaTable(tableName.getTextContent());
                        foundTables.put(tableName.getTextContent(), tpchMetaTable);

                        final NodeList tableElements = table.getChildNodes();
                        for (int k = 0; k < tableElements.getLength(); ++k) {
                            final Node tableElement = tableElements.item(k);

                            if (tableElement.getNodeName().equals("fields")) {
                                final NodeList fields = tableElement.getChildNodes();
                                for (int l = 0; l < fields.getLength(); ++l) {
                                    final Node field = fields.item(l);
                                    if (field.getNodeName().equals("field")) {
                                        final Node fieldName = field.getAttributes().getNamedItem("name");
                                        String fieldType = "VARCHAR";

                                        final NodeList fieldElements = field.getChildNodes();
                                        for (int h = 0; h < fields.getLength(); ++h) {
                                            final Node fieldElement = fieldElements.item(h);

                                            if (null != fieldElement && fieldElement.getNodeName().equals("type")) {
                                                fieldType = fieldElement.getTextContent();
                                            }
                                        }
                                        tpchMetaTable.addField(fieldName.getTextContent(), fieldType);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return foundTables;
    }
}
