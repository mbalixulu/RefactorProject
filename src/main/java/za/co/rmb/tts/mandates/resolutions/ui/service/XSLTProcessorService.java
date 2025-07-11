package za.co.rmb.tts.mandates.resolutions.ui.service;

public interface XSLTProcessorService {

  String generatePage(String xsl, Object dataSource);

  String returnPage(String xml);
}
