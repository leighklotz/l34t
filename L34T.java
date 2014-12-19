package org.l4t;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import net.sf.saxon.s9api.ExtensionFunction;

import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.XdmValue;

public class L34T {
  public static final String L34T_EXTENSION_FUNCTION_NAMESPACE="http://l34t.org/2014/saxon-extension";

  final Processor processor;
  final XsltExecutable xsltExecutable;
  InstanceFunction instanceFunction = new InstanceFunction();

  L34T(String xsltPath, Collection<String> variableNames) {

    try {
      processor = new Processor(false);
      processor.registerExtensionFunction(instanceFunction);
      XsltCompiler compiler = processor.newXsltCompiler();
      String container = createL34TContainer(xsltPath, variableNames);
      xsltExecutable = compiler.compile(new StreamSource(new StringReader(container)));
    } catch (SaxonApiException sae) {
      throw new RuntimeException(sae);
    }
  }

  private String createL34TContainer(String xsltPath, Collection<String> variableNames) {
    StringBuilder sb = new StringBuilder();
    sb.append("<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:l34t='http://l34t.org/2014/saxon-extension' exclude-result-prefixes='l34t'>\n");
    {
      for (String name : variableNames) {
        sb.append("<xsl:param name='");
        sb.append(name);
        sb.append("' />");
      }
      sb.append("<xsl:include href='");
      sb.append(xsltPath);
      sb.append("' />\n");
    }
    sb.append("</xsl:stylesheet>");
    return sb.toString();
  }

  private void setVariables(XsltTransformer trans, Map<String,XdmAtomicValue> variables) {
    for (Map.Entry<String,XdmAtomicValue> entry : variables.entrySet()) {
      trans.setParameter(new QName(entry.getKey()), entry.getValue());
    }
  }

  public void transform(String sourcePath, Map<String,XdmAtomicValue> variables) {
    try {
      Serializer out = processor.newSerializer();
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      DocumentBuilder documentBuilder = processor.newDocumentBuilder();
      XdmNode source = documentBuilder.build(new StreamSource(new File(sourcePath)));
      instanceFunction.setNode(source);
      out.setOutputStream(System.out);
      XsltTransformer xsltTransformer = xsltExecutable.load();
      setVariables(xsltTransformer, variables);
      xsltTransformer.setInitialContextNode(source);
      xsltTransformer.setDestination(out);
      xsltTransformer.transform();
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }

  static XdmAtomicValue coerceValue(String name, String typeName, String stringValue) {
    switch(typeName) {
    case "string":
      return new XdmAtomicValue(stringValue);

    case "int":
      return new XdmAtomicValue(Integer.parseInt(stringValue));

    default:
      throw new RuntimeException(String.format("Unknown type=%s for parameter name=%s value=%s", typeName, name, stringValue));
    }
  }


  // This used to work fine in Saxon9, but Saxonica made it a for-pay feature in 9.4.
  // In Saxon 9.5 and greater, it works again.
  // Look at https://github.com/twl8n/saxon-9-he-samples for more info on writing extension functions.
  static class InstanceFunction implements ExtensionFunction {
    private XdmNode node;

    InstanceFunction() {
      super();
    }

    void setNode(XdmNode node) {
      this.node = node;
    }

    public QName getName() {
      return new QName(L34T_EXTENSION_FUNCTION_NAMESPACE, "instance");
    }
                        
    public SequenceType getResultType() {
      return SequenceType.makeSequenceType(ItemType.ANY_NODE, OccurrenceIndicator.ONE);
    }
                        
    public net.sf.saxon.s9api.SequenceType[] getArgumentTypes() {
      return new SequenceType[0];
    }
      
    public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
      return node;
    }
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("usage: java cp saxon9he.jar:. org.l34t.L34T xslfile xmlfile [varname vartype varvalue] [varname vartype varvalue] ..");
    }

    String xsl = args[0];
    String xml = args[1];

    Map<String,XdmAtomicValue> variables = new HashMap<>();

    for (int i = 2; i < args.length; i += 3) {
      String name = args[i];
      String typeName = args[i+1];
      String stringValue = args[i+2];
      XdmAtomicValue value = coerceValue(name, typeName, stringValue);
      variables.put(name, value);
    }
    new L34T(xsl, variables.keySet()).transform(xml, variables);
  }
}
