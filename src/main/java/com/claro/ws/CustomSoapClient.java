/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.ws;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Albert
 */
public class CustomSoapClient {
    private String url;
    private boolean enableLog;
    private boolean errordetected;
    private String messageError;   

    public String getMessageError()
  {
    return this.messageError;
  }
  
  public void setMessageError(String messageError)
  {
    this.messageError = messageError;
  }
  
  public boolean isErrordetected()
  {
    return this.errordetected;
  }
  
  public void setErrordetected(boolean errordetected)
  {
    this.errordetected = errordetected;
  }
  
    public CustomSoapClient(boolean enableLog)
    {
        this.url = "";
        this.errordetected = false;
        this.enableLog = enableLog;
        this.messageError = "";
    }
    
    public SOAPMessage callMethodSoap(String urlWSDL, String xml, String soapAction)
  {
      SOAPMessage soapResponse = null;
      setUrl(urlWSDL);
      this.errordetected = false;
      setMessageError("OK");
      try{
          SOAPMessage message = createSoapMessageFromString(xml, soapAction);
      
      SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection soapConnection = soapConnectionFactory.createConnection();
      
      soapResponse = soapConnection.call(message, urlWSDL);
      if (isEnableLog())
      {
        System.out.print("Response SOAP Message:");
        soapResponse.writeTo(System.out);
        
        soapConnection.close();
      }
      } catch (SOAPException ex)
        {setErrordetected(true);
        setMessageError(ex.getMessage());
        Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
      }
        catch (IOException ex)
      {
        setErrordetected(true);
        setMessageError(ex.getMessage());
        Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
      }
        catch (UnsupportedOperationException ex)
      {
        setErrordetected(true);
        setMessageError(ex.getMessage());
        Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
      }
      return soapResponse;
  }
   
    public String getStringResponse(SOAPMessage soapResponse)
  {
    String strMsg = null;
    try
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      soapResponse.writeTo(out);
      strMsg = new String(out.toByteArray());
    }
    catch (SOAPException ex)
    {
      setErrordetected(true);
      setMessageError(ex.getMessage());
      Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      setErrordetected(true);
      setMessageError(ex.getMessage());
      Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    return strMsg;
  }
  
    
  public Document loadXMLFromString(String xml)
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xml));
      Document doc = builder.parse(is);
      doc.normalize();
      
      System.out.println("Leyendo Archivo de Configuracion " + doc.getDocumentElement().getNodeName());
      
      NodeList listNode = doc.getChildNodes();
      
      return doc;
    }
    catch (SAXException ex)
    {
      setErrordetected(true);
      setMessageError(ex.getMessage());
      Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      setErrordetected(true);
      setMessageError(ex.getMessage());
      Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ParserConfigurationException ex)
    {
      setErrordetected(true);
      setMessageError(ex.getMessage());
      Logger.getLogger(CustomSoapClient.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
  
  public String getValueFromDocumentXML(Document doc, String pathXML)
  {
    NodeList listNode = doc.getChildNodes();
    
    String requestValue = getNodeXml(listNode, pathXML);
    
    return requestValue;
  }
  
  
  private String getNodeXml(NodeList listNodeXml, String pathXML)
  {
    StringTokenizer tokens = new StringTokenizer(pathXML, "/");
    String nodeName = "";
    String nextPaht = "";
    String value = "";
    
    int count = 0;
    while (tokens.hasMoreTokens())
    {
      nodeName = tokens.nextToken();
      if ((nodeName != null) && (!nodeName.equals("")))
      {
        int index = pathXML.indexOf("/".concat(nodeName).concat("/"));
        nextPaht = pathXML.substring(index + nodeName.length() + 1, pathXML.length());
      }
      if (count == 1) {
        break;
      }
      count++;
    }
    Node firstNode = listNodeXml.item(0);
    if (firstNode.getNodeType() == 1)
    {
      Element elementConfiguration = (Element)firstNode;
      
      NodeList elementNode = elementConfiguration.getElementsByTagName(nodeName);
      Element firstNameElement = (Element)elementNode.item(0);
      NodeList textFNList = firstNameElement.getChildNodes();
      
      value = getNodeXml(textFNList, nextPaht);
    }
    else if (firstNode.getNodeType() == 3)
    {
      value = firstNode.getNodeValue().trim();
    }
    return value;
  }
  
      private SOAPMessage createSoapMessageFromString(String xml, String soapAction)
    throws SOAPException, IOException
  {
    MessageFactory factory = MessageFactory.newInstance();
    
    SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
    
    MimeHeaders headers = message.getMimeHeaders();
    headers.addHeader("SOAPAction", soapAction);
    
    message.saveChanges();
    if (isEnableLog())
    {
      System.out.print("Request SOAP Message:");
      message.writeTo(System.out);
      System.out.println();
    }
    return message;
  }
      
      public boolean isEnableLog()
  {
    return this.enableLog;
  }
  
  public void setEnableLog(boolean enableLog)
  {
    this.enableLog = enableLog;
  }
  
  public String getUrl()
  {
    return this.url;
  }
    public void setUrl(String url)
  {
    this.url = url;
  }
}
    
   