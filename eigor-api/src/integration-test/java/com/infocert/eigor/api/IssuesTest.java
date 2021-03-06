package com.infocert.eigor.api;

import com.infocert.eigor.api.ConversionUtil.KeepAll;
import it.infocert.eigor.api.ConversionResult;
import it.infocert.eigor.api.IConversionIssue;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.codehaus.plexus.util.Base64;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static it.infocert.eigor.test.Utils.invoiceAsStream;
import static org.junit.Assert.*;


public class IssuesTest extends AbstractIssueTest {

    @Test
    public void issueEisiFromFattpaToPeppolCn() {

        ConversionResult<byte[]> result = conversion.assertConversionWithoutErrors(
                "/examples/fattpa/A10-Licenses-CreditNote.xml",
                "fatturapa",
                "peppolcn");

    }

    @Test
    public void issueEisi135() {

        ConversionResult<byte[]> result = conversion.assertConversionWithoutErrors(
                "/issues/issue-eisi-135-xmlcen.xml",
                "xmlcen",
                "fatturapa");

    }

    @Test
    public void issueEisi138() {

        ConversionResult<byte[]> result = conversion.assertConversionWithoutErrors(
                "/examples/fattpa/A10-Licenses.xml",
                "fatturapa",
                "peppolbis");

    }

    @Ignore("To be ignored 'til all mappings have been applied")
    @Test
    public void issue281FattpaToCII() throws Exception {
        //conversion.assertConversionWithoutErrors("/issues/issue-281-fattpa.xml", "fatturapa", "cii");

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-281-fattpa.xml");
        ConversionResult<byte[]> convert = api.convert("fatturapa", "cii", inputFatturaPaXml);
        for (IConversionIssue issue : convert.getIssues()) {
            assertTrue(issue.getMessage().contains("CL-19]-Coded allowance reasons MUST belong to the UNCL 4465 code list"));
        }
    }


    @Ignore("UBL-sch-fails")
    @Test
    public void issueeisi41() throws IOException, SAXException, TransformerException {

        ConversionResult<byte[]> conversion = this.conversion.assertConversionWithoutErrors("/examples/ubl/ubl-tc434-example1-CIUS-ITA.xml", "ubl", "fatturapa");
        String originalXml = printDocument(documentBuilder.parse(new ByteArrayInputStream(IOUtils.toString(getClass().getResourceAsStream("/examples/ubl/ubl-tc434-example1-CIUS-ITA.xml"), "UTF-8").getBytes())));
        String convertedXml = printDocument(documentBuilder.parse(new ByteArrayInputStream(conversion.getResult())));

        assertThat("========\n" + originalXml + "========\n" + convertedXml, convertedXml, CompareMatcher.isSimilarTo(originalXml).ignoreComments().ignoreWhitespace());

    }

    @Ignore("To be ignored 'til all mappings have been applied")
    @Test
    public void issue279FromUblToFattPA() throws Exception {
        ConversionResult<byte[]> convert = conversion.assertConversionWithoutErrors("/issues/issue-279-ubl.xml", "ubl", "fatturapa");

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiGenerali']//*[local-name()='DatiTrasporto']//*[local-name()='DataOraConsegna']/text()");

        assertTrue(convert.getIssues().isEmpty()); // no warnings for text exceeding length limit

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "2017-10-15T00:00:00", evaluate);
    }

    @Ignore("To be ignored 'til all mappings have been applied")
    @Test
    public void issue259() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-259-fattpa.xml");

        ConversionResult<byte[]> convert = api.convert("fatturapa", "ubl", inputFatturaPaXml);


        String taxCategory = evalXpathExpressionAsString(convert, "//*[local-name()='AllowanceCharge'][*[local-name()='Amount']/text()='40.00']//*[local-name()='TaxCategory']//*[local-name()='ID']/text()");
        assertTrue(taxCategory != null && !taxCategory.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "E", taxCategory);

        String multiplier = evalXpathExpressionAsString(convert, "//*[local-name()='AllowanceCharge'][*[local-name()='Amount']/text()='40.00']//*[local-name()='MultiplierFactorNumeric']/text()");
        assertTrue(multiplier != null && !multiplier.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "4.00", multiplier);

        String baseAmount = evalXpathExpressionAsString(convert, "//*[local-name()='AllowanceCharge'][*[local-name()='Amount']/text()='40.00']//*[local-name()='BaseAmount']/text()");
        assertTrue(baseAmount != null && !baseAmount.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "1000.00", baseAmount);

        // Ritenuta will go to not-mapped-values attachment

        String evaluateAttachment = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/text()");
        String evaluateAttachmentMimeCode = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/@mimeCode");
        String evaluateAttachmentFileName = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/@filename");

        assertTrue(evaluateAttachment != null && !evaluateAttachment.trim().isEmpty());
        String attachment = new String(Base64.decodeBase64(evaluateAttachment.getBytes()));

        assertTrue(attachment.contains("Ritenuta: SI"));

        assertTrue(evaluateAttachmentMimeCode != null && !evaluateAttachmentMimeCode.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "text/csv", evaluateAttachmentMimeCode);

        assertTrue(evaluateAttachmentFileName != null && !evaluateAttachmentFileName.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "not-mapped-values.txt", evaluateAttachmentFileName);
    }

    @Test
    public void eisi121() throws IOException, SAXException, TransformerException {

        String invoice = "/eisi-121.xml";
        ConversionResult<byte[]> conversion = this.conversion.assertConversionWithoutErrors(invoice, "xmlcen", "xmlcen");

        String originalXml = printDocument(documentBuilder.parse(new ByteArrayInputStream(IOUtils.toString(getClass().getResourceAsStream(invoice), "UTF-8").getBytes())));

        ByteArrayInputStream convertedInvoice = new ByteArrayInputStream(conversion.getResult());
        String convertedXml = new String(conversion.getResult(), "UTF-8");

        try {
            documentBuilder.parse(convertedInvoice);
        } catch (Exception e) {
            fail("========\n" + originalXml + "========\n" + convertedXml);
        }

    }

    @Test
    public void convertXmlCenToCenToXmlCen() throws IOException, SAXException, TransformerException {

        // check conversion xmlcen -> xmlcen is without errors.
        ConversionResult<byte[]> conversion = this.conversion.assertConversionWithoutErrors("/examples/xmlcen/Test_EeISI_300_CENfullmodel.xml", "xmlcen", "xmlcen");

        String originalXml = printDocument(documentBuilder.parse(new ByteArrayInputStream(IOUtils.toString(getClass().getResourceAsStream("/examples/xmlcen/Test_EeISI_300_CENfullmodel.xml"), "UTF-8").getBytes())));
        String convertedXml = printDocument(documentBuilder.parse(new ByteArrayInputStream(conversion.getResult())));

        assertThat("========\n" + originalXml + "========\n" + convertedXml, convertedXml, CompareMatcher.isSimilarTo(originalXml).ignoreComments().ignoreWhitespace());

    }

    @Ignore("fails BT11 cardinality")
    @Test
    public void issue278FromUblToFattPA() {
        conversion.assertConversionWithoutErrors("/issues/issue-278-ubl.xml", "ubl", "fatturapa");
    }

    @Test
    public void issue261FromFattPAToUbl() {

        conversion.assertConversionWithoutErrors(
                "/issues/issue-261-fattpa.xml",
                "fatturapa",
                "ubl",
                new ConversionUtil.KeepXSDErrorsOnly()
                //new ConversionUtil.KeepAll()
        );
    }

    @Ignore("Fails UBL INPUT schematron")
    @Test
    public void issue276FromUblToUbl() {
        conversion.assertConversionWithoutErrors("/issues/issue-276-ubl.xml", "ubl", "ubl");
    }

    @Ignore("Fails CII schematron")
    @Test
    public void issue277ThisConversionShouldCompleteWithoutErrors() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-277-cii.xml", "cii", "cii");
    }

    @Ignore("Fails CII schematron")
    @Test
    public void fatturapaToCiiExamples1() {
        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/fatturapa/B2G-D_04B_ITBGRGDN77T10L117F_60FPA.xml",
                "fatturapa", "cii");
    }

    @Ignore("fails output CII chematron")
    @Test
    public void fatturapaToCiiExamples2() {
        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/fatturapa/B2G-D_04B_ITBGRGDN77T10L117F_PEC _91FAT.xml",
                "fatturapa", "cii");
    }

    @Ignore("Fails CII schematron")
    @Test
    public void ublToCiiExamples() {
        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/ubl/B2G-C_0X_ITBGRGDN77T10L117F_42CEN.XML",
                "ubl", "cii");

        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/ubl/B2G-C_0X_ITBGRGDN77T10L117F_PEC_42UBL.XML",
                "ubl", "cii");

        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/ubl/B2G-D_01C_ITBGRGDN77T10L117F_02UBL.XML",
                "ubl", "cii");

        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/ubl/B2G-D_01C_ITBGRGDN77T10L117F_PEC _02CEN.XML",
                "ubl", "cii");

        conversion.assertConversionWithoutErrors(
                "/issues/cii-examples/ubl/urn_notier_SOGG-NOT-00196_2018_9780030222_CP_FATTURA_01_CEN.xml",
                "ubl", "cii");

    }

    @Ignore("Fails output schematron")
    @Test
    public void issue254FromFattPaToCii() {
        conversion.assertConversionWithoutErrors("/issues/254/fatturapa_newB2G-D_04A_ITBGRGDN77T10L117F_50FPA.XML", "fatturapa", "cii");
    }

    @Ignore("Fails CII schematron")
    @Test
    public void issue254FromUblToCii_scenario2() {
        conversion.assertConversionWithoutErrors("/issues/254/ubl_newB2G-C_01C_CII.XML", "ubl", "cii");
    }

    @Ignore("Fails CII schematron")
    @Test
    public void issue254FromUblToCii_scenario1() {
        conversion.assertConversionWithoutErrors("/issues/254/ubl_B2G-D_01A_ITBGRGDN77T10L117F_36CEN.XML", "ubl", "cii");
    }


    @Test
    public void issue252ThisConversionShouldCompleteWithoutErrors() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-252-fattpa.xml", "fatturapa", "ubl");

    }


    @Test
    @Ignore("waiting for example invoice")
    public void issue238ThisConversionShouldCompleteWithoutErrors() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-238-ubl.xml", "ubl", "fatturapa");

    }

    @Ignore("1 - 1 mapping rule seems wrong")
    @Test
    public void issue207ThisConversionShouldCompleteWithoutErrors() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-207-ubl.xml", "ubl", "fatturapa");

    }

    @Test
    @Ignore("waiting for example invoice")
    public void issue208ThisConversionShouldCompleteWithoutErrors() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-208-ubl.xml", "ubl", "fatturapa");

    }

    @Test
    public void issue269() throws Exception {
        InputStream ciiInStream = invoiceAsStream("/issues/issue-269-cii.xml");
        ConversionResult<byte[]> convert = api.convert("cii", "fatturapa", ciiInStream);
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CessionarioCommittente']//*[local-name()='IdCodice']/text()");
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "97735020584", evaluate);
    }

    @Test
    public void issue245() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-245-fattpa.xml");

        ConversionResult<byte[]> convert = api.convert("fatturapa", "ubl", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyTaxScheme']//*[local-name()='ID']/text()");

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "VAT", evaluate);
    }

    @Ignore("Ignore until find a solution to strange schematron issue regarding DocumentReference on INvoiceLine in UBL")
    @Test
    public void issue256() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-256-fattpa.xml");

        ConversionResult<byte[]> convert = api.convert("fatturapa", "ubl", inputFatturaPaXml);

        String evaluateAttachment = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/text()");
        String evaluateAttachmentMimeCode = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/@mimeCode");
        String evaluateAttachmentFileName = evalXpathExpressionAsString(convert, "//*[local-name()='AdditionalDocumentReference']//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']/@filename");

        assertTrue(evaluateAttachment != null && !evaluateAttachment.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "ZUlHT1IgYXR0YWNobWVudCB0ZXN0", evaluateAttachment);

        assertTrue(evaluateAttachmentMimeCode != null && !evaluateAttachmentMimeCode.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "text/csv", evaluateAttachmentMimeCode);

        assertTrue(evaluateAttachmentFileName != null && !evaluateAttachmentFileName.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "Allegato", evaluateAttachmentFileName);
    }


    @Test
    public void issue257() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-257-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DatiRiepilogo']//*[local-name()='RiferimentoNormativo']/text()");
        Assert.assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "Text about exemption reason Art15", evaluate);
    }

    @Test
    public void issue207() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-207-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String bt70 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DettaglioLinee']//*[local-name()='AltriDatiGestionali'][./TipoDato/text()='BT-70']//*[local-name()='RiferimentoTesto']/text()");
        assertTrue(bt70 != null && !bt70.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "Delivery party name", bt70);

        String bt71 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DettaglioLinee']//*[local-name()='AltriDatiGestionali'][./TipoDato/text()='BT-71']//*[local-name()='RiferimentoTesto']/text()");
        assertTrue(bt71 != null && !bt71.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "6754238987648", bt71);

        String bt71_1 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DettaglioLinee']//*[local-name()='AltriDatiGestionali'][./TipoDato/text()='BT-71-1']//*[local-name()='RiferimentoTesto']/text()");
        assertTrue(bt71_1 != null && !bt71_1.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "scheme00", bt71_1);
    }

    @Ignore("Wrong assertion")
    @Test
    public void issue208() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-208-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiPagamento']//*[local-name()='DettaglioPagamento']//*[local-name()='Beneficiario']/text()");

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "PARTY NAME ACCOUNT NAME", evaluate);
    }


    @Test
    public void issue220() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-220-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String arrotondamento0 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiGenerali']//*[local-name()='DatiGeneraliDocumento']//*[local-name()='Arrotondamento']/text()");
        assertTrue(arrotondamento0 != null && !arrotondamento0.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "0.00", arrotondamento0);

        String arrotondamento1 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DatiRiepilogo'][./AliquotaIVA/text()='10.00']//*[local-name()='Arrotondamento']/text()");
        assertTrue(arrotondamento1 != null && !arrotondamento1.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "-0.00400000", arrotondamento1);

        String arrotondamento2 = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DatiRiepilogo'][./AliquotaIVA/text()='23.00']//*[local-name()='Arrotondamento']/text()");
        assertTrue(arrotondamento2 != null && !arrotondamento2.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "0.00160000", arrotondamento2);
    }


    @Test
    public void issue238() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-238-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DatiRiepilogo']//*[local-name()='RiferimentoNormativo']/text()");

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "Text about exemption reason", evaluate);
    }

    @Test
    public void issue242() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-242-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='Allegati'][./Attachment/text()='WlVsSFQxSWdZWFIwWVdOb2JXVnVkQ0IwWlhOMA==']//*[local-name()='NomeAttachment']/text()");

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "IDattachment-eIGOR.csv", evaluate);
    }

    @Test
    public void issue280() throws Exception {

        InputStream inputFatturaPaXml = invoiceAsStream("/issues/issue-280-ubl.xml");

        ConversionResult<byte[]> convert = api.convert("ubl", "fatturapa", inputFatturaPaXml);

        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='FatturaElettronicaBody']//*[local-name()='DatiBeniServizi']//*[local-name()='DettaglioLinee']//*[local-name()='AliquotaIVA']/text()");

        assertTrue(evaluate != null && !evaluate.trim().isEmpty());
        Assert.assertEquals(conversion.buildMsgForFailedAssertion(convert, new KeepAll(), null), "10.00", evaluate);
    }

    @Test
    public void issue281FattpaToUBL() throws Exception {
        conversion.assertConversionWithoutErrors("/issues/issue-281-fattpa.xml", "fatturapa", "ubl");
    }

    @Test
    public void issue239UblToUBL() throws Exception {
        InputStream inputFatturaUblXml = invoiceAsStream("/issues/issue-239-ubl_.xml");
        api.convert("ubl", "ubl", inputFatturaUblXml);
    }

    @Test
    public void issue236UblToUBL() throws Exception {
        InputStream inputFatturaUblXml = invoiceAsStream("/issues/issue-236-ubl_.xml");
        ConversionResult<byte[]> convert = api.convert("ubl", "ubl", inputFatturaUblXml);
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='LegalMonetaryTotal']/text()");
        assertNotNull(evaluate);
        evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='LegalMonetaryTotal']//*[local-name()='LineExtensionAmount']/text()");
        assertNotNull(evaluate);
        evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='LegalMonetaryTotal']//*[local-name()='PrepaidAmount']/text()");
        assertTrue(evaluate == null || "".equals(evaluate));
        evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='LegalMonetaryTotal']//*[local-name()='PayableRoundingAmount']/text()");
        assertTrue(evaluate == null || "".equals(evaluate));
    }

    @Test
    public void issue231CenToPeppolcn() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-231-xmlcen.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolcn", inputFatturaCenXml);
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='AdditionalDocumentReference']/text()");
        assertNotNull(evaluate);
        evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='AdditionalDocumentReference']//*[local-name()='ID']/text()");
        assertEquals("456", evaluate);
    }

    @Test
    public void issue231CenToUblcn() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-231-xmlcen.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolcn", inputFatturaCenXml);
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='AdditionalDocumentReference']/text()");
        assertNotNull(evaluate);
        evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='AdditionalDocumentReference']//*[local-name()='ID']/text()");
        assertEquals("456", evaluate);
    }

    @Test
    public void issue237CenToPeppolbisWithoutNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-without-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolbis", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertFalse(invoice.contains("<cbc:Note />"));
        assertFalse(invoice.contains("<cbc:Note/>"));
    }

    @Test
    public void issue237CenToPeppolcnWithoutNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-without-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolcn", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertFalse(invoice.contains("<cbc:Note />"));
        assertFalse(invoice.contains("<cbc:Note/>"));
    }

    @Test
    public void issue237CenToUblWithoutNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-without-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "ubl", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertFalse(invoice.contains("<cbc:Note />"));
        assertFalse(invoice.contains("<cbc:Note/>"));
    }

    @Test
    public void issue237CenToUblcnWithoutNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-without-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "ublcn", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertFalse(invoice.contains("<cbc:Note />"));
        assertFalse(invoice.contains("<cbc:Note/>"));
    }

    @Test
    public void issue237CenToPeppolbisWithNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-with-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolbis", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertTrue(invoice.contains("<cbc:Note"));
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='Note']/text()");
        assertEquals("test1-test2", evaluate);
    }

    @Test
    public void issue237CenToPeppolcnWithNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-with-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "peppolcn", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertTrue(invoice.contains("<cbc:Note"));
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='Note']/text()");
        assertEquals("test1-test2", evaluate);
    }

    @Test
    public void issue237CenToUblWithNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-with-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "ubl", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertTrue(invoice.contains("<cbc:Note"));
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='Invoice']//*[local-name()='Note']/text()");
        assertEquals("#test1#test2", evaluate);
    }

    @Test
    public void issue237CenToUblcnWithNote() throws Exception {
        InputStream inputFatturaCenXml = invoiceAsStream("/issues/issue-237-xmlcen-with-note.xml");
        ConversionResult<byte[]> convert = api.convert("xmlcen", "ublcn", inputFatturaCenXml);
        String invoice = new String(convert.getResult());
        assertTrue(invoice.contains("<cbc:Note"));
        String evaluate = evalXpathExpressionAsString(convert, "//*[local-name()='CreditNote']//*[local-name()='Note']/text()");
        assertEquals("#test1#test2", evaluate);
    }

    @Test
    public void issue241CheckSupportedSourceFormats() throws Exception {
        Set<String> supportedSourceFormats = api.supportedSourceFormats();
        assertNotNull(supportedSourceFormats);
        List<String> supportedSourceFormatList = Lists.newArrayList(supportedSourceFormats);
        supportedSourceFormatList.sort(String::compareToIgnoreCase);
        String joinedSourceFormats = String.join(", ", supportedSourceFormatList);
        assertEquals(joinedSourceFormats, "cii, csvcen, fatturapa, ubl, ublcn, xmlcen");
    }

    @Test
    public void issue241CheckSupportedTargetFormats() throws Exception {
        Set<String> supportedTargetFormats = api.supportedTargetFormats();
        assertNotNull(supportedTargetFormats);
        List<String> supportedTargetFormatList = Lists.newArrayList(supportedTargetFormats);
        supportedTargetFormatList.sort(String::compareToIgnoreCase);
        String joinedTargetFormats = String.join(", ", supportedTargetFormatList);
        assertEquals(joinedTargetFormats, "cii, fatturapa, peppolbis, peppolcn, ubl, ublcn, xmlcen");
    }
}
