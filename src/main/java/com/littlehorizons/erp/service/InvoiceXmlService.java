package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TaxCalculator.TaxBreakdown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InvoiceXmlService {

    private static final String INVOICES_DIR = "invoices";
    private static final String SCHOOL_NAME = "COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S.";
    private static final String SCHOOL_NIT = "900543210";
    private static final String SCHOOL_DV = "9";
    private static final String SCHOOL_ADDRESS = "Calle 127 # 45-12, Bogotá D.C.";
    private static final String DIAN_RES_NUM = "18764000129845";

    public InvoiceXmlService() {
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File dir = new File(INVOICES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Mapea el método de pago al catálogo de medios de pago DIAN / MUISCA UBL 2.1:
     * 10 = Efectivo, 42 = Consignación bancaria / Transferencia, 48 = Tarjeta Crédito, 49 = Tarjeta Débito.
     */
    private String mapPaymentMeansCode(String method) {
        if (method == null) return "10";
        String m = method.toLowerCase();
        if (m.contains("trans") || m.contains("consigna")) return "42";
        if (m.contains("crédito") || m.contains("credito")) return "48";
        if (m.contains("débito") || m.contains("debito")) return "49";
        return "10";
    }

    /**
     * Mapea tipo de documento a código DIAN UBL 2.1:
     * 13 = Cédula de Ciudadanía, 22 = Cédula de Extranjería, 11 = Registro Civil, 12 = Tarjeta de Identidad, 41 = Pasaporte.
     */
    private String mapDocumentTypeCode(String docType) {
        if (docType == null) return "13";
        switch (docType.toUpperCase()) {
            case "TI": return "12";
            case "RC": return "11";
            case "CE": return "22";
            case "PASAPORTE": return "41";
            default: return "13";
        }
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Construye el documento XML en estándar UBL 2.1 reglamentado por la DIAN (estándar MUISCA).
     */
    public String generateXmlContent(String invoiceNumber, LocalDate issueDate, String cufe,
                                     Student student, Guardian guardian, PaymentConcept concept,
                                     Payment payment, TaxBreakdown tax) {

        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss'-05:00'");
        String issueTime = LocalTime.now().format(timeFmt);

        String guardianDoc = (guardian != null && guardian.getDocumentNumber() != null)
                ? guardian.getDocumentNumber() : "222222222222";
        String guardianDocType = (guardian != null && guardian.getDocumentType() != null)
                ? guardian.getDocumentType() : "CC";
        String guardianName = (guardian != null && guardian.getFullName() != null)
                ? guardian.getFullName() : "Consumidor Final";
        String guardianAddr = (guardian != null && guardian.getAddress() != null)
                ? guardian.getAddress() : "Bogotá D.C.";

        String studentName = (student != null && student.getFullName() != null)
                ? student.getFullName() : "Estudiante";

        String conceptCode = (concept != null && concept.getConceptCode() != null) ? concept.getConceptCode() : "SRV-001";
        String conceptName = (concept != null && concept.getConceptName() != null) ? concept.getConceptName() : "Servicio Educativo";
        String payMethod = (payment != null && payment.getPaymentMethod() != null) ? payment.getPaymentMethod() : "Transferencia";
        String payMeansCode = mapPaymentMeansCode(payMethod);
        String docTypeCode = mapDocumentTypeCode(guardianDocType);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        xml.append("<Invoice\n");
        xml.append("    xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n");
        xml.append("    xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n");
        xml.append("    xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"\n");
        xml.append("    xmlns:dian=\"dian:gov:co:facturaelectronica:Structures-2-1\"\n");
        xml.append("    xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\"\n");
        xml.append("    xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n");

        // UBLExtensions con DIAN Extensions
        xml.append("  <ext:UBLExtensions>\n");
        xml.append("    <ext:UBLExtension>\n");
        xml.append("      <ext:ExtensionContent>\n");
        xml.append("        <dian:DianExtensions>\n");
        xml.append("          <dian:InvoiceControl>\n");
        xml.append(String.format("            <dian:InvoiceAuthorization>%s</dian:InvoiceAuthorization>\n", DIAN_RES_NUM));
        xml.append("            <dian:AuthorizationPeriod>\n");
        xml.append("              <cbc:StartDate>2024-01-10</cbc:StartDate>\n");
        xml.append("              <cbc:EndDate>2026-01-10</cbc:EndDate>\n");
        xml.append("            </dian:AuthorizationPeriod>\n");
        xml.append("            <dian:AuthorizedInvoices>\n");
        xml.append("              <dian:Prefix>FE-LH</dian:Prefix>\n");
        xml.append("              <dian:From>1</dian:From>\n");
        xml.append("              <dian:To>9999</dian:To>\n");
        xml.append("            </dian:AuthorizedInvoices>\n");
        xml.append("          </dian:InvoiceControl>\n");
        xml.append("          <dian:InvoiceSource>\n");
        xml.append("            <cbc:IdentificationCode listAgencyID=\"6\" listSchemeURI=\"urn:oasis:names:specification:ubl:codelist:gc:CountryIdentificationCode-2.1\">CO</cbc:IdentificationCode>\n");
        xml.append("          </dian:InvoiceSource>\n");
        xml.append("          <dian:SoftwareProvider>\n");
        xml.append(String.format("            <dian:ProviderID schemeAgencyID=\"195\" schemeAgencyName=\"CO, DIAN\" schemeID=\"%s\">%s</dian:ProviderID>\n", SCHOOL_DV, SCHOOL_NIT));
        xml.append("            <dian:SoftwareID>lh-erp-2024-sec</dian:SoftwareID>\n");
        xml.append("          </dian:SoftwareProvider>\n");
        xml.append("          <dian:SoftwareSecurityCode>DIAN-SECURE-LH-CODE-2024</dian:SoftwareSecurityCode>\n");
        xml.append(String.format("          <dian:QRCode>NumFac:%s,FecFac:%s,ValFac:%s,CodImp:01,ValImp:%s,DocAdq:%s,CUFE:%s</dian:QRCode>\n",
                invoiceNumber, issueDate.format(dtf), tax.getTotalAmount(), tax.getTaxAmount(), guardianDoc, cufe));
        xml.append("        </dian:DianExtensions>\n");
        xml.append("      </ext:ExtensionContent>\n");
        xml.append("    </ext:UBLExtension>\n");
        xml.append("    <ext:UBLExtension>\n");
        xml.append("      <ext:ExtensionContent>\n");
        xml.append("        <ds:Signature Id=\"xmldsig-LittleHorizons-2024\">\n");
        xml.append(String.format("          <ds:SignatureValue>%s</ds:SignatureValue>\n", cufe));
        xml.append("        </ds:Signature>\n");
        xml.append("      </ext:ExtensionContent>\n");
        xml.append("    </ext:UBLExtension>\n");
        xml.append("  </ext:UBLExtensions>\n\n");

        // Metadatos de Factura
        xml.append("  <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>\n");
        xml.append("  <cbc:CustomizationID>10</cbc:CustomizationID>\n");
        xml.append("  <cbc:ProfileID>DIAN 2.1: Factura Electrónica de Venta</cbc:ProfileID>\n");
        xml.append("  <cbc:ProfileExecutionID>1</cbc:ProfileExecutionID>\n");
        xml.append(String.format("  <cbc:ID>%s</cbc:ID>\n", escapeXml(invoiceNumber)));
        xml.append(String.format("  <cbc:UUID schemeID=\"CUFE-SHA384\" schemeName=\"CUFE-SHA384\">%s</cbc:UUID>\n", escapeXml(cufe)));
        xml.append(String.format("  <cbc:IssueDate>%s</cbc:IssueDate>\n", issueDate.format(dtf)));
        xml.append(String.format("  <cbc:IssueTime>%s</cbc:IssueTime>\n", issueTime));
        xml.append(String.format("  <cbc:DueDate>%s</cbc:DueDate>\n", issueDate.plusDays(30).format(dtf)));
        xml.append("  <cbc:InvoiceTypeCode>01</cbc:InvoiceTypeCode>\n");
        xml.append(String.format("  <cbc:Note>%s</cbc:Note>\n", escapeXml(tax.getLegalReference())));
        xml.append(String.format("  <cbc:Note>Estudiante beneficiario: %s</cbc:Note>\n", escapeXml(studentName)));
        xml.append("  <cbc:DocumentCurrencyCode>COP</cbc:DocumentCurrencyCode>\n");
        xml.append("  <cbc:LineCountNumeric>1</cbc:LineCountNumeric>\n\n");

        // Emisor (Colegio Little Horizons)
        xml.append("  <cac:AccountingSupplierParty>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyIdentification>\n");
        xml.append(String.format("        <cbc:ID schemeAgencyID=\"195\" schemeAgencyName=\"CO, DIAN\" schemeID=\"%s\">%s</cbc:ID>\n", SCHOOL_DV, SCHOOL_NIT));
        xml.append("      </cac:PartyIdentification>\n");
        xml.append("      <cac:PartyName>\n");
        xml.append(String.format("        <cbc:Name>%s</cbc:Name>\n", escapeXml(SCHOOL_NAME)));
        xml.append("      </cac:PartyName>\n");
        xml.append("      <cac:PhysicalLocation>\n");
        xml.append("        <cac:Address>\n");
        xml.append(String.format("          <cbc:AddressLine><cbc:Line>%s</cbc:Line></cbc:AddressLine>\n", escapeXml(SCHOOL_ADDRESS)));
        xml.append("          <cac:Country><cbc:IdentificationCode>CO</cbc:IdentificationCode></cac:Country>\n");
        xml.append("        </cac:Address>\n");
        xml.append("      </cac:PhysicalLocation>\n");
        xml.append("      <cac:PartyTaxScheme>\n");
        xml.append(String.format("        <cbc:RegistrationName>%s</cbc:RegistrationName>\n", escapeXml(SCHOOL_NAME)));
        xml.append(String.format("        <cbc:CompanyID schemeAgencyID=\"195\" schemeAgencyName=\"CO, DIAN\" schemeID=\"%s\">%s</cbc:CompanyID>\n", SCHOOL_DV, SCHOOL_NIT));
        xml.append("        <cbc:TaxLevelCode>R-99-PN</cbc:TaxLevelCode>\n");
        xml.append("        <cac:TaxScheme>\n");
        xml.append("          <cbc:ID>01</cbc:ID>\n");
        xml.append("          <cbc:Name>IVA</cbc:Name>\n");
        xml.append("        </cac:TaxScheme>\n");
        xml.append("      </cac:PartyTaxScheme>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingSupplierParty>\n\n");

        // Adquirente / Cliente
        xml.append("  <cac:AccountingCustomerParty>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyIdentification>\n");
        xml.append(String.format("        <cbc:ID schemeAgencyID=\"195\" schemeAgencyName=\"CO, DIAN\" schemeName=\"%s\">%s</cbc:ID>\n",
                docTypeCode, escapeXml(guardianDoc)));
        xml.append("      </cac:PartyIdentification>\n");
        xml.append("      <cac:PartyName>\n");
        xml.append(String.format("        <cbc:Name>%s</cbc:Name>\n", escapeXml(guardianName)));
        xml.append("      </cac:PartyName>\n");
        xml.append("      <cac:PhysicalLocation>\n");
        xml.append("        <cac:Address>\n");
        xml.append(String.format("          <cbc:AddressLine><cbc:Line>%s</cbc:Line></cbc:AddressLine>\n", escapeXml(guardianAddr)));
        xml.append("          <cac:Country><cbc:IdentificationCode>CO</cbc:IdentificationCode></cac:Country>\n");
        xml.append("        </cac:Address>\n");
        xml.append("      </cac:PhysicalLocation>\n");
        xml.append("      <cac:PartyTaxScheme>\n");
        xml.append(String.format("        <cbc:RegistrationName>%s</cbc:RegistrationName>\n", escapeXml(guardianName)));
        xml.append(String.format("        <cbc:CompanyID schemeAgencyID=\"195\" schemeAgencyName=\"CO, DIAN\">%s</cbc:CompanyID>\n", escapeXml(guardianDoc)));
        xml.append("        <cbc:TaxLevelCode>R-99-PN</cbc:TaxLevelCode>\n");
        xml.append("        <cac:TaxScheme>\n");
        xml.append(String.format("          <cbc:ID>%s</cbc:ID>\n", tax.getDianTaxSchemeCode()));
        xml.append(String.format("          <cbc:Name>%s</cbc:Name>\n", tax.getDianTaxSchemeName()));
        xml.append("        </cac:TaxScheme>\n");
        xml.append("      </cac:PartyTaxScheme>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingCustomerParty>\n\n");

        // Medio de Pago
        xml.append("  <cac:PaymentMeans>\n");
        xml.append(String.format("    <cbc:PaymentMeansCode>%s</cbc:PaymentMeansCode>\n", payMeansCode));
        xml.append(String.format("    <cbc:PaymentDueDate>%s</cbc:PaymentDueDate>\n", issueDate.format(dtf)));
        xml.append("  </cac:PaymentMeans>\n\n");

        // Impuestos Totales (Discriminación Art. 617 y Art. 476 E.T.)
        xml.append("  <cac:TaxTotal>\n");
        xml.append(String.format("    <cbc:TaxAmount currencyID=\"COP\">%s</cbc:TaxAmount>\n", tax.getTaxAmount().toPlainString()));
        xml.append("    <cac:TaxSubtotal>\n");
        xml.append(String.format("      <cbc:TaxableAmount currencyID=\"COP\">%s</cbc:TaxableAmount>\n", tax.getBaseAmount().toPlainString()));
        xml.append(String.format("      <cbc:TaxAmount currencyID=\"COP\">%s</cbc:TaxAmount>\n", tax.getTaxAmount().toPlainString()));
        xml.append("      <cac:TaxCategory>\n");
        xml.append(String.format("        <cbc:Percent>%s</cbc:Percent>\n", tax.getTaxRate().multiply(new java.math.BigDecimal("100")).stripTrailingZeros().toPlainString()));
        xml.append("        <cac:TaxScheme>\n");
        xml.append(String.format("          <cbc:ID>%s</cbc:ID>\n", tax.getDianTaxSchemeCode()));
        xml.append(String.format("          <cbc:Name>%s</cbc:Name>\n", tax.getDianTaxSchemeName()));
        xml.append("        </cac:TaxScheme>\n");
        xml.append("      </cac:TaxCategory>\n");
        xml.append("    </cac:TaxSubtotal>\n");
        xml.append("  </cac:TaxTotal>\n\n");

        // Totales Monetarios
        xml.append("  <cac:LegalMonetaryTotal>\n");
        xml.append(String.format("    <cbc:LineExtensionAmount currencyID=\"COP\">%s</cbc:LineExtensionAmount>\n", tax.getBaseAmount().toPlainString()));
        xml.append(String.format("    <cbc:TaxExclusiveAmount currencyID=\"COP\">%s</cbc:TaxExclusiveAmount>\n", tax.getBaseAmount().toPlainString()));
        xml.append(String.format("    <cbc:TaxInclusiveAmount currencyID=\"COP\">%s</cbc:TaxInclusiveAmount>\n", tax.getTotalAmount().toPlainString()));
        xml.append(String.format("    <cbc:PayableAmount currencyID=\"COP\">%s</cbc:PayableAmount>\n", tax.getTotalAmount().toPlainString()));
        xml.append("  </cac:LegalMonetaryTotal>\n\n");

        // Línea de Factura (Item)
        xml.append("  <cac:InvoiceLine>\n");
        xml.append("    <cbc:ID>1</cbc:ID>\n");
        xml.append("    <cbc:InvoicedQuantity unitCode=\"EA\">1.000000</cbc:InvoicedQuantity>\n");
        xml.append(String.format("    <cbc:LineExtensionAmount currencyID=\"COP\">%s</cbc:LineExtensionAmount>\n", tax.getBaseAmount().toPlainString()));
        xml.append("    <cac:Item>\n");
        xml.append(String.format("      <cbc:Description>%s</cbc:Description>\n", escapeXml(conceptName)));
        xml.append("      <cac:SellersItemIdentification>\n");
        xml.append(String.format("        <cbc:ID>%s</cbc:ID>\n", escapeXml(conceptCode)));
        xml.append("      </cac:SellersItemIdentification>\n");
        xml.append("    </cac:Item>\n");
        xml.append("    <cac:Price>\n");
        xml.append(String.format("      <cbc:PriceAmount currencyID=\"COP\">%s</cbc:PriceAmount>\n", tax.getBaseAmount().toPlainString()));
        xml.append("      <cbc:BaseQuantity unitCode=\"EA\">1.000000</cbc:BaseQuantity>\n");
        xml.append("    </cac:Price>\n");
        xml.append("  </cac:InvoiceLine>\n");

        xml.append("</Invoice>\n");
        return xml.toString();
    }

    /**
     * Escribe el archivo .xml en el sistema de archivos.
     */
    public boolean writeXmlFile(String filePath, String xmlContent) {
        try {
            ensureDirectoryExists();
            File file = new File(filePath);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(xmlContent);
            }
            LoggerUtil.info("Generated XML invoice successfully: " + filePath);
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Failed to write XML invoice file: " + filePath, e);
            return false;
        }
    }
}
