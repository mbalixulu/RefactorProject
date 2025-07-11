package za.co.rmb.tts.mandates.resolutions.ui.service.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.service.XSLTProcessorService;

@Service
public class XSLTProcessorImpl implements XSLTProcessorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(XSLTProcessorImpl.class);

  @Value("${app.domain}")
  private String appDomain;

  @Override
  public String generatePage(String xsl,
                             Object dataSource) {
    String page;
    try {
      // 1) Marshal Java object to XML
      JAXBContext jaxbContext = JAXBContext.newInstance(dataSource.getClass());
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter xmlWriter = new StringWriter();
      jaxbMarshaller.marshal(dataSource, xmlWriter);

      // 2) Load XSL and set up secure TransformerFactory
      try (InputStream xslStream = getClass().getResourceAsStream(xsl)) {
        if (xslStream == null) {
          throw new IOException("XSL resource not found: " + xsl);
        }
        StreamSource styleSource = new StreamSource(xslStream);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Disable external entities & enable secure processing
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer(styleSource);

        // 3) Transform XML → final page
        StringReader xmlReader = new StringReader(xmlWriter.toString());
        StreamSource xmlSource = new StreamSource(xmlReader);

        StringWriter pageWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(pageWriter));
        page = pageWriter.toString();
      }

    } catch (JAXBException | TransformerException | IOException e) {
      LOGGER.error("Exception processing XSLT", e);
      page = e.getMessage();
    }

    // 4) Replace domain placeholder
    if (page != null) {
      page = page.replace("app-domain", appDomain);
    }
    return page;
  }

  @Override
  public String returnPage(String xml) {
    try (InputStream inputStream = getClass().getResourceAsStream(xml)) {
      if (inputStream == null) {
        LOGGER.warn("XML resource not found: {}", xml);
        return "";
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
          .replace("app-domain", appDomain);
    } catch (IOException e) {
      LOGGER.error("Problem returning xml file as string", e);
      return "";
    }
  }
}