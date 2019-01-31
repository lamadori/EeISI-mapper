<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" xmlns:u="utils" schemaVersion="iso"
    queryBinding="xslt2">

    <title>Rules for PEPPOL BIS 3.0 Billing</title>

    <ns uri="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100" prefix="rsm"/>
    <ns uri="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100" prefix="udt"/>
    <ns uri="urn:un:unece:uncefact:data:standard:QualifiedDataType:100" prefix="qdt"/>
    <ns uri="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
        prefix="ram"/>
    <ns uri="http://www.w3.org/2001/XMLSchema" prefix="xs"/>
    <ns uri="utils" prefix="u"/>

    <!-- Parameters -->

    <let name="profile"
        value="
            if (/rsm:CrossIndustryInvoice/rsm:ExchangedDocumentContext/ram:BusinessProcessSpecifiedDocumentContextParameter and matches(normalize-space(/rsm:CrossIndustryInvoice/rsm:ExchangedDocumentContext/ram:BusinessProcessSpecifiedDocumentContextParameter/ram:ID), 'urn:fdc:peppol.eu:2017:poacc:billing:([0-9]{2}):1.0')) then
                tokenize(normalize-space(/rsm:CrossIndustryInvoice/rsm:ExchangedDocumentContext/ram:BusinessProcessSpecifiedDocumentContextParameter/ram:ID), ':')[7]
            else
                'Unknown'"/>
    <let name="supplierCountry"
        value="
            if (/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction[1]/ram:ApplicableHeaderTradeAgreement[1]/ram:SellerTradeParty[1]/ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)) then
                upper-case(normalize-space(/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction[1]/ram:ApplicableHeaderTradeAgreement[1]/ram:SellerTradeParty[1]/ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)))
            else
                if (/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTaxRepresentativeTradeParty/ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)) then
                    upper-case(normalize-space(/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTaxRepresentativeTradeParty/ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)))
                else
                    if (/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CountryID) then
                        upper-case(normalize-space(/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:PostalTradeAddress/ram:CountryID))
                    else
                        'XX'"/>

    <!-- -->





    <let name="documentCurrencyCode"
        value="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode"/>

    <!-- Functions -->

    <function xmlns="http://www.w3.org/1999/XSL/Transform" name="u:slack" as="xs:boolean">
        <param name="exp" as="xs:decimal"/>
        <param name="val" as="xs:decimal"/>
        <param name="slack" as="xs:decimal"/>
        <value-of
            select="xs:decimal($exp + $slack) &gt;= $val and xs:decimal($exp - $slack) &lt;= $val"/>
    </function>
    
    <function xmlns="http://www.w3.org/1999/XSL/Transform" name="u:mod11" as="xs:boolean">
        <param name="val"/>
        <variable name="length" select="string-length($val) - 1"/>
        <variable name="digits" select="reverse(for $i in string-to-codepoints(substring($val, 0, $length + 1)) return $i - 48)"/>
        <variable name="weightedSum" select="sum(for $i in (0 to $length - 1) return $digits[$i + 1] * (($i mod 6) + 2))"/>
        <value-of select="number($val) &gt; 0 and (11 - ($weightedSum mod 11)) mod 11 = number(substring($val, $length + 1, 1))"/>
    </function>

    <pattern>

        <rule context="rsm:ExchangedDocumentContext">
            <assert id="PEPPOL-EN16931-R001"
                test="ram:BusinessProcessSpecifiedDocumentContextParameter/ram:ID" flag="fatal"
                >Business process MUST be provided.</assert>
            <assert id="PEPPOL-EN16931-R004"
                test="starts-with(normalize-space(ram:GuidelineSpecifiedDocumentContextParameter/ram:ID/text()), 'urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0')"
                flag="fatal">Specification identifier MUST have the value
                'urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0'.</assert>
        </rule>

        <rule context="ram:ApplicableHeaderTradeAgreement">
            <assert id="PEPPOL-EN16931-R003"
                test="ram:BuyerReference or ram:BuyerOrderReferencedDocument/ram:IssuerAssignedID"
                flag="fatal">A buyer reference or purchase order reference MUST be
                provided.</assert>
            <assert id="PEPPOL-EN16931-R006"
                test="count(ram:AdditionalReferencedDocument[ram:TypeCode='130']) &lt;=1"
                flag="fatal">Only one invoiced object is allowed on document level</assert>
            <assert id="PEPPOL-EN16931-R080"
                test="count(ram:AdditionalReferencedDocument[ram:TypeCode='50']) &lt;=1"
                flag="fatal">Only one project reference is allowed on document level</assert>
        </rule>
        


        <rule context="ram:ApplicableHeaderTradeSettlement">
            <assert id="PEPPOL-EN16931-R005"
                test="not(ram:TaxCurrencyCode) or normalize-space(ram:TaxCurrencyCode/text()) != normalize-space(ram:InvoiceCurrencyCode/text())"
                flag="fatal">VAT accounting currency code MUST be different from invoice currency
                code when provided.</assert>
           
            <assert id="PEPPOL-EN16931-R053"
                test="count(ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:TaxTotalAmount[@currencyID = $documentCurrencyCode]) = 1"
                flag="fatal">Only one tax total with tax subtotals MUST be provided.</assert>
            <assert id="PEPPOL-EN16931-R054"
                test="
                    count(ram:SpecifiedTradeSettlementHeaderMonetarySummation/ram:TaxTotalAmount[@currencyID != $documentCurrencyCode]) = (if (ram:TaxCurrencyCode) then
                        1
                    else
                        0)"
                flag="fatal">Only one tax total without tax subtotals MUST be provided when tax
                currency code is provided.</assert>
        </rule>

        <!-- PEPPOL-EN16931-R051 is obsolete in CII. -->

        <rule context="rsm:ExchangedDocument">
            <assert id="PEPPOL-EN16931-R002"
                test="count(ram:IncludedNote) &lt;= 1 and not(ram:IncludedNote/ram:SubjectCode)"
                flag="fatal">No more than one note is allowed on document level.</assert>
        </rule>

        <rule context="ram:BuyerTradeParty">
            <assert id="PEPPOL-EN16931-R010" test="ram:URIUniversalCommunication/ram:URIID"
                flag="fatal">Buyer electronic address MUST be provided</assert>
        </rule>

        <rule context="ram:SellerTradeParty">
            <assert id="PEPPOL-EN16931-R020" test="ram:URIUniversalCommunication/ram:URIID"
                flag="fatal">Seller electronic address MUST be provided</assert>
        </rule>

        <rule
            context="ram:SpecifiedTradeAllowanceCharge[ram:CalculationPercent and not(ram:BasisAmount)]">
            <assert id="PEPPOL-EN16931-R041" test="false()" flag="fatal">Allowance/charge base
                amount MUST be provided when allowance/charge percentage is provided.</assert>
        </rule>

        <rule
            context="ram:SpecifiedTradeAllowanceCharge[not(ram:CalculationPercent) and ram:BasisAmount]">
            <assert id="PEPPOL-EN16931-R042" test="false()" flag="fatal">Allowance/charge percentage
                MUST be provided when allowance/charge base amount is provided.</assert>
        </rule>

        <rule context="ram:SpecifiedTradeAllowanceCharge">
            <assert id="PEPPOL-EN16931-R040"
                test="
                    not(ram:CalculationPercent and ram:BasisAmount) or u:slack(if (ram:ActualAmount) then
                        ram:ActualAmount
                    else
                        0, (xs:decimal(ram:BasisAmount) * xs:decimal(ram:CalculationPercent)) div 100, 0.02)"
                flag="fatal">Allowance/charge amount must equal base amount * percentage/100 if base
                amount and percentage exists</assert>
        </rule>



        <rule
            context="
                ram:SpecifiedTradeSettlementPaymentMeans[some $code in tokenize('49 59', '\s')
                    satisfies normalize-space(ram:TypeCode) = $code]">
            <assert id="PEPPOL-EN16931-R061"
                test="../ram:SpecifiedTradePaymentTerms/ram:DirectDebitMandateID" flag="fatal"
                >Mandate reference MUST be provided for direct debit.</assert>
        </rule>

        <rule
            context="rsm:SupplyChainTradeTransaction[ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime]/ram:IncludedSupplyChainTradeLineItem/ram:SpecifiedLineTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime">
            <assert id="PEPPOL-EN16931-R110"
                test="udt:DateTimeString &gt;= ../../../../ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime/udt:DateTimeString"
                flag="fatal">Start date of line period MUST be within invoice period.</assert>
        </rule>

        <rule
            context="rsm:SupplyChainTradeTransaction[ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:EndDateTime]/ram:IncludedSupplyChainTradeLineItem/ram:SpecifiedLineTradeSettlement/ram:BillingSpecifiedPeriod/ram:EndDateTime">
            <assert id="PEPPOL-EN16931-R111"
                test="udt:DateTimeString &lt;= ../../../../ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:EndDateTime/udt:DateTimeString"
                flag="fatal">End date of line period MUST be within invoice period.</assert>
        </rule>

        <rule context="ram:IncludedSupplyChainTradeLineItem">
            <let name="lineExtensionAmount"
                value="
                    if (ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount) then
                        xs:decimal(ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount)
                    else
                        0"/>
            <let name="quantity"
                value="
                    if (ram:SpecifiedLineTradeDelivery/ram:BilledQuantity) then
                        xs:decimal(ram:SpecifiedLineTradeDelivery/ram:BilledQuantity)
                    else
                        1"/>
            <let name="priceAmount"
                value="
                    if (ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:ChargeAmount) then
                        xs:decimal(ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:ChargeAmount)
                    else
                        0"/>
            <let name="baseQuantity"
                value="
                    if (ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:BasisQuantity and xs:decimal(ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:BasisQuantity) != 0) then
                        xs:decimal(ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:BasisQuantity)
                    else
                        1"/>
            <let name="allowancesTotal"
                value="
                    if (ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'false']) then
                        xs:decimal(sum(ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'false']/ram:ActualAmount))
                    else
                        0"/>
            <let name="chargesTotal"
                value="
                    if (ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'true']) then
                        xs:decimal(sum(ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'true']/ram:ActualAmount))
                    else
                        0"/>

            <assert id="PEPPOL-EN16931-R120"
                test="u:slack($lineExtensionAmount, ($quantity * ($priceAmount div $baseQuantity)) + $chargesTotal - $allowancesTotal, 0.02)"
                flag="fatal">Invoice line net amount MUST equal (Invoiced quantity * (Item net
                price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount</assert>
            
            <assert id="PEPPOL-EN16931-R100" test="count(ram:SpecifiedLineTradeSettlement/ram:AdditionalReferencedDocument[ram:TypeCode='130']) &lt;=1" 
                flag="fatal">Only one invoiced object is allowed pr line</assert>
            
            <assert id="PEPPOL-EN16931-R101" test="(not(ram:SpecifiedLineTradeSettlement/ram:AdditionalReferencedDocument) or (ram:SpecifiedLineTradeSettlement/ram:AdditionalReferencedDocument/ram:TypeCode='130'))"
                flag="fatal">Element Document reference can only be used for Invoice line object</assert>
        </rule>

        <rule context="ram:NetPriceProductTradePrice | ram:GrossPriceProductTradePrice">
            <assert id="PEPPOL-EN16931-R121"
                test="not(ram:BasisQuantity) or xs:decimal(ram:BasisQuantity) &gt; 0" flag="fatal"
                >Base quantity MUST be a positive number above zero.</assert>
        </rule>

        <!-- PEPPOL-EN16931-R044 and PEPPOL-EN16931-R046 are not needed due to lack of elements for the EN. -->

        <!-- Price -->
        <rule
            context="ram:NetPriceProductTradePrice/ram:BasisQuantity[@unitCode] | ram:GrossPriceProductTradePrice/ram:BasisQuantity[@unitCode]">
            <assert id="PEPPOL-EN16931-R130"
                test="@unitCode = ../../../ram:SpecifiedLineTradeDelivery/ram:BilledQuantity/@unitCode"
                flag="fatal">Unit code of price base quantity MUST be same as invoiced
                quantity.</assert>
        </rule>

    </pattern>

    <!-- National rules -->

    <pattern>

        <!-- Norway -->
        <rule context="ram:SellerTradeParty[$supplierCountry = 'NO']">
           
            <assert id="NO-R-002"
                test="ram:SpecifiedTaxRegistration/ram:ID[@schemeID = 'FC'][normalize-space(text()) = 'Foretaksregisteret']"
                flag="warning">Most invoice issuers are required to append "Foretaksregisteret" to
                their invoice. "Dersom selger er aksjeselskap, allmennaksjeselskap eller filial av
                utenlandsk selskap skal også ordet «Foretaksregisteret» fremgå av salgsdokumentet,
                jf. foretaksregisterloven § 10-2."</assert>
            
            <assert id="NO-R-001"
                test="ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)='NO' and matches(ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID,3) , '^[0-9]{9}MVA$') 
                and u:mod11(substring(ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/ram:ID, 3, 9)) or not(ram:SpecifiedTaxRegistration[ram:ID/@schemeID = 'VAT']/substring(ram:ID, 1, 2)='NO')"
                flag="fatal">For Norwegian suppliers, a VAT number MUST be the country code prefix NO followed by a valid Norwegian organization number (nine numbers) followed by the letters MVA.</assert>
            
        </rule>
    </pattern>

    <pattern>
        <!-- Denmark -->

        <!-- Document level -->
        <rule context="rsm:CrossIndustryInvoice">
            <!--Check for AccountinCode-->
            <assert id="DK-R-001"
                test="
                    not(($supplierCountry = 'DK')
                    and (normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ReceivableSpecifiedTradeAccountingAccount/ram:ID/text()) = '')
                    )"
                flag="warning">For Danish suppliers when the Accounting is known it should be
                referred on the Invoice</assert>
            <!--Check for Legal entity-->
            <assert id="DK-R-002"
                test="
                    not($supplierCountry = 'DK'
                    and not(normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedLegalOrganization/ram:ID/text()) != '')
                    )"
                flag="fatal">Danish suppliers MUST provide legal entity.</assert>
            <!--Check for Non VAT Tax code-->
            <assert id="DK-R-004"
                test="
                    not(($supplierCountry = 'DK')
                    and (rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge/ram:ReasonCode = 'ZZZ')
                    and not((string-length(normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge/ram:Reason/text())) = 4
                    and number(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge/ram:Reason) &gt;= 0)
                    and number(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge/ram:Reason &lt;= 9999))
                    )"
                flag="fatal">When specifying non-VAT Taxes, Danish suppliers MUST use the
                SpecifiedTradeAllowanceCharge/ReasonCode="ZZZ" and the 4-digit Tax category MUST be
                specified as Reason</assert>
            <assert id="DK-R-013"
                test="
                    not(($supplierCountry = 'DK')
                    and (((boolean(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:GlobalID))
                    and (normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:GlobalID/@schemeID) = ''))
                    or
                    ((boolean(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:GlobalID))
                    and (normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:GlobalID/@schemeID) = ''))
                    )
                    )"
                flag="fatal">For Danish Suppliers it is mandatory to use schemeID when GlobalID is
                used for SellerTradeParty or BuyerTradeParty</assert>
            <assert id="DK-R-014"
                test="
                    not(($supplierCountry = 'DK')
                    and (((boolean(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedLegalOrganization/ram:ID))
                    and (normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedLegalOrganization/ram:ID/@schemeID) = ''))
                    or
                    ((boolean(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:SpecifiedLegalOrganization/ram:ID))
                    and (normalize-space(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:SpecifiedLegalOrganization/ram:ID/@schemeID) = ''))
                    )
                    )"
                flag="fatal">For Danish Suppliers it is mandatory to use schemeID when
                SpecifiedLegalOrganization is used for SellerTradeParty or BuyerTradeParty</assert>

        </rule>

        <rule
            context="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:IncludedSupplyChainTradeLineItem">
            <!--Chedk for commodityCode on linelevel-->
            <assert id="DK-R-003"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:SpecifiedTradeProduct/ram:DesignatedProductClassification/ram:ClassCode/@listID = 'MP')
                    and not((ram:SpecifiedTradeProduct/ram:DesignatedProductClassification/ram:ClassCode/@listVersionID = '19.05.01')
                    or (ram:SpecifiedTradeProduct/ram:DesignatedProductClassification/ram:ClassCode/@listVersionID = '19.0501')
                    )
                    )"
                flag="warning">If ItemClassification is provided from Danish suppliers, UNSPSC
                version 19.0501 should be used</assert>
        </rule>



        <rule
            context="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans">
            <!-- Findes der kun een TypeCode??? -->
            <assert id="DK-R-005"
                test="
                    not(($supplierCountry = 'DK')
                    and not(contains(' 1 10 31 42 48 49 50 58 59 93 97 ', concat(' ', ram:TypeCode, ' ')))
                    )"
                flag="fatal">For Danish suppliers the following Payment means type codes are
                allowed: 1, 10, 31, 42, 48, 49, 50, 58, 59, 93 and 97</assert>
            <assert id="DK-R-006"
                test="
                    not(($supplierCountry = 'DK')
                    and ((ram:TypeCode = '31') or (ram:TypeCode = '42'))
                    and not((normalize-space(ram:PayeePartyCreditorFinancialAccount/ram:IBANID/text()) != '') and (normalize-space(ram:PayerSpecifiedDebtorFinancialInstitution/ram:BICID/text()) != ''))
                    )"
                flag="fatal">For Danish suppliers, bank account and registration account are
                mandatory if payment means is 31 or 42</assert>
            <assert id="DK-R-007"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '49')
                    and not((normalize-space(../ram:CreditorReferenceID/text()) != '')
                    and (normalize-space(ram:SpecifiedTradePaymentTerms/ram:DirectDebitMandateID/text()) != ''))
                    )"
                flag="fatal">For Danish suppliers DirectDebitMandateID and CreditorReferenceID are
                mandatory when payment means is 49</assert>
            <assert id="DK-R-008"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '50')
                    and not(((substring(../ram:PaymentReference, 0, 4) = '01#')
                    or (substring(../ram:PaymentReference, 0, 4) = '04#')
                    or (substring(../ram:PaymentReference, 0, 4) = '15#'))
                    and (string-length(ram:PayeePartyCreditorFinancialAccount/ram:IBANID/text()) = 7)
                    )
                    )"
                flag="fatal">For Danish Suppliers PaymentReference is mandatory and MUST start with
                01#, 04# or 15# (kortartkode), and PayeePartyCreditorFinancialAccount/IBANID (Giro
                kontonummer) is mandatory and must be 7 characters long, when payment means equals
                50 (Giro)</assert>
            <assert id="DK-R-009"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '50')
                    and ((substring(../ram:PaymentReference, 0, 4) = '04#')
                    or (substring(../ram:PaymentReference, 0, 4) = '15#'))
                    and not(string-length(../ram:PaymentReference) = 19)
                    )"
                flag="fatal">For Danish Suppliers if the PaymentReference is prefixed with 04# or
                015# the 16 digits instruction Id must be added to the PaymentReference eg.
                "04#1234567890123456" when Payment means equals 50 (Giro)</assert>
            <assert id="DK-R-010"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '93')
                    and not(((substring(../ram:PaymentReference, 0, 4) = '71#')
                    or (substring(../ram:PaymentReference, 0, 4) = '73#')
                    or (substring(../ram:PaymentReference, 0, 4) = '75#'))
                    and (string-length(ram:PayeePartyCreditorFinancialAccount/ram:IBANID/text()) = 8)
                    )
                    )"
                flag="fatal">For Danish Suppliers the PaymentReference is mandatory and MUST start
                with 71#, 73# or 75# (kortartkode) and and PayeePartyCreditorFinancialAccount/IBANID
                (Kreditornummer) is mandatory and must be exactly 8 characters long, when Payment
                means equals 93 (FIK)</assert>
            <assert id="DK-R-011"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '93')
                    and ((substring(../ram:PaymentReference, 0, 4) = '71#')
                    or (substring(../ram:PaymentReference, 0, 4) = '75#'))
                    and not((string-length(../ram:PaymentReference) = 18)
                    or (string-length(../ram:PaymentReference) = 19))
                    )"
                flag="fatal">For Danish Suppliers if the PaymentReference is prefixed with 71# or
                75# the 15-16 digits instruction Id must be added to the PaymentReference eg.
                "71#1234567890123456" when payment Method equals 93 (FIK)</assert>
            <assert id="DK-R-012"
                test="
                    not(($supplierCountry = 'DK')
                    and (ram:TypeCode = '97')
                    )"
                flag="warning">For Danish suppliers when Payment means equals 97, the payment is
                made to "NemKonto"</assert>
        </rule>
    </pattern>
    
	<!-- Italian rules -->
    <pattern>
 		<rule context="ram:SellerTradeParty[$supplierCountry = 'IT']">
			<assert id="IT-R-001"
				test=" matches(ram:SpecifiedTaxRegistration/ram:ID[@schemeID ='FC'] ,'^[A-Z0-9]{11,16}$') "
				flag="fatal"> [IT-R-001] BT-32 (Seller tax registration identifier) - For Italian suppliers BT-32 minimum lenght 11 and maximum lenght shall be 16.  Per i fornitori italiani il BT-32 deve avere una lunghezza tra 11 e 16 caratteri</assert>
			<assert id="IT-R-002" 
				test= "(ram:PostalTradeAddress/ram:LineOne)"  
				flag="fatal"> [IT-R-002] BT-35 (Seller address line 1) - Italian suppliers MUST provide the postal address line 1 - I fornitori italiani devono indicare l'indirizzo postale.</assert>
			<assert id="IT-R-003" 
				test= "(ram:PostalTradeAddress/ram:CityName)" 
				flag="fatal"> [IT-R-003] BT-37 (Seller city) - Italian suppliers MUST provide the postal address city - I fornitori italiani devono indicare la città di residenza.</assert>
			<assert id="IT-R-004"
				test= "(ram:PostalTradeAddress/ram:PostcodeCode)"
				flag="fatal"> [IT-R-004] BT-38 (Seller post code) - Italian suppliers MUST provide the postal address post code - I fornitori italiani devono indicare il CAP di residenza.            
            </assert>
		</rule> 
	</pattern>	
    
    <!-- Swedish rules -->
    <pattern>
        <rule
            context="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos; and ram:SpecifiedTaxRegistration/substring(ram:ID[@schemeID = &apos;VAT&apos;], 1, 2) = &apos;SE&apos;]">

            <assert id="SE-R-001"
                test="string-length(normalize-space(ram:SpecifiedTaxRegistration/ram:ID[@schemeID = &apos;VAT&apos;])) = 14"
                flag="fatal">For Swedish suppliers, Swedish VAT-numbers must consist of 14
                characters.</assert>

            <assert id="SE-R-002"
                test="string(number(substring(ram:SpecifiedTaxRegistration/ram:ID[@schemeID = &apos;VAT&apos;], 3, 12))) != &apos;NaN&apos;"
                flag="fatal">For Swedish suppliers, the Swedish VAT-numbers must have the trailing
                12 characters in numeric form</assert>
        </rule>


        <rule
            context="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:SpecifiedLegalOrganization[../ram:CountryID = &apos;SE&apos; and ram:SpecifiedLegalOrganization/ram:ID]">
            <assert id="SE-R-003" test="string(number(ram:ID)) != &apos;NaN&apos;" flag="warning"
                >Swedish organisation numbers should be numeric.</assert>

            <assert id="SE-R-004" test="string-length(normalize-space(ram:ID)) = 10" flag="warning"
                >Swedish organisation numbers consist of 10 characters.</assert>
        </rule>

        <rule
            context="rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos; and ram:SpecifiedLegalOrganization/ram:ID]/ram:SpecifiedTaxRegistration[ram:ID/@schemeID = &apos;FC&apos;]/ram:ID">
            <assert id="SE-R-005"
                test="normalize-space(upper-case(.)) = &apos;GODKÄND FÖR F-SKATT&apos;" flag="fatal"
                >For Swedish suppliers, when using Seller tax registration identifier, 'Godkänd för
                F-skatt' must be stated</assert>
        </rule>

        <rule
            context="//ram:ApplicableTradeTax[/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos;]/ram:SpecifiedTaxRegistration[substring(ram:ID[@schemeID = &apos;VAT&apos;], 1, 2) = &apos;SE&apos;]] | //ram:CategoryTradeTax[/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos;]/ram:SpecifiedTaxRegistration[substring(ram:ID[@schemeID = &apos;VAT&apos;], 1, 2) = &apos;SE&apos;]]">
            <assert id="SE-R-006"
                test="number(ram:RateApplicablePercent) = 25 or number(ram:RateApplicablePercent) = 12 or number(ram:RateApplicablePercent) = 6"
                flag="fatal">For Swedish suppliers, only standard VAT rate of 6, 12 or 25 are
                used</assert>
        </rule>


        <rule
            context="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction[ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos;]]/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[normalize-space(ram:TypeCode) = &apos;30&apos; and normalize-space(ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID) = &apos;SE:PLUSGIRO&apos;]/ram:PayeePartyCreditorFinancialAccount/ram:ProprietaryID">
            <assert id="SE-R-007" test="string(number(normalize-space(.))) != &apos;NaN&apos;"
                flag="warning">For Swedish suppliers using Plusgiro, the Account ID must be numeric </assert>

            <assert id="SE-R-010"
                test="string-length(normalize-space(.)) &gt;= 2 and string-length(normalize-space(.)) &lt;= 8"
                flag="warning">For Swedish suppliers using Plusgiro, the Account ID must have 2-8
                characteres</assert>
        </rule>

        <rule
            context="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction[ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos;]]/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[normalize-space(ram:TypeCode) = &apos;30&apos; and normalize-space(ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID) = &apos;SE:BANKGIRO&apos;]/ram:PayeePartyCreditorFinancialAccount/ram:ProprietaryID">
            <assert id="SE-R-008" test="string(number(normalize-space(.))) != &apos;NaN&apos;"
                flag="warning">For Swedish suppliers using Bankgiro, the Account ID must be numeric </assert>

            <assert id="SE-R-009"
                test="string-length(normalize-space(.)) = 7 or string-length(normalize-space(.)) = 8"
                flag="warning">For Swedish suppliers using Bankgiro, the Account ID must have 7-8
                characters</assert>
        </rule>


        <rule
            context="/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction[ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty[ram:PostalTradeAddress/ram:CountryID = &apos;SE&apos;]]/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans[normalize-space(ram:TypeCode) = &apos;50&apos; or normalize-space(ram:TypeCode) = &apos;56&apos;]">
            <assert id="SE-R-011" test="false()" flag="warning">For Swedish suppliers using Swedish
                Bankgiro or Plusgiro, the proper way to indicate this is to use Code 30 for
                PaymentMeans and FinancialInstitutionBranch ID with code SE:BANKGIRO or
                SE:PLUSGIRO</assert>
        </rule>
    </pattern>



    <!-- Restricted code lists and formatting -->
    <pattern>
        <let name="ISO3166"
            value="tokenize('AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW', '\s')"/>
        <let name="ISO4217"
            value="tokenize('AFN EUR ALL DZD USD AOA XCD XCD ARS AMD AWG AUD AZN BSD BHD BDT BBD BYN BZD XOF BMD INR BTN BOB BOV USD BAM BWP NOK BRL USD BND BGN XOF BIF CVE KHR XAF CAD KYD XAF XAF CLP CLF CNY AUD AUD COP COU KMF CDF XAF NZD CRC XOF HRK CUP CUC ANG CZK DKK DJF XCD DOP USD EGP SVC USD XAF ERN ETB FKP DKK FJD XPF XAF GMD GEL GHS GIP DKK XCD USD GTQ GBP GNF XOF GYD HTG USD AUD HNL HKD HUF ISK INR IDR XDR IRR IQD GBP ILS JMD JPY GBP JOD KZT KES AUD KPW KRW KWD KGS LAK LBP LSL ZAR LRD LYD CHF MOP MKD MGA MWK MYR MVR XOF USD MRO MUR XUA MXN MXV USD MDL MNT XCD MAD MZN MMK NAD ZAR AUD NPR XPF NZD NIO XOF NGN NZD AUD USD NOK OMR PKR USD PAB USD PGK PYG PEN PHP NZD PLN USD QAR RON RUB RWF SHP XCD XCD XCD WST STD SAR XOF RSD SCR SLL SGD ANG XSU SBD SOS ZAR SSP LKR SDG SRD NOK SZL SEK CHF CHE CHW SYP TWD TJS TZS THB USD XOF NZD TOP TTD TND TRY TMT USD AUD UGX UAH AED GBP USD USD USN UYU UYI UZS VUV VEF VND USD USD XPF MAD YER ZMW ZWL XBA XBB XBC XBD XTS XXX XAU XPD XPT XAG', '\s')"/>
        <let name="MIMECODE"
            value="tokenize('application/pdf image/png image/jpeg text/csv application/vnd.openxmlformats-officedocument.spreadsheetml.sheet application/vnd.oasis.opendocument.spreadsheet', '\s')"/>
        <let name="UNCL2005" value="tokenize('3 35 432', '\s')"/>
        <let name="UNCL5189"
            value="tokenize('41 42 60 62 63 64 65 66 67 68 70 71 88 95 100 102 103 104 105', '\s')"/>
        <let name="UNCL7161"
            value="tokenize('AA AAA AAC AAD AAE AAF AAH AAI AAS AAT AAV AAY AAZ ABA ABB ABC ABD ABF ABK ABL ABN ABR ABS ABT ABU ACF ACG ACH ACI ACJ ACK ACL ACM ACS ADC ADE ADJ ADK ADL ADM ADN ADO ADP ADQ ADR ADT ADW ADY ADZ AEA AEB AEC AED AEF AEH AEI AEJ AEK AEL AEM AEN AEO AEP AES AET AEU AEV AEW AEX AEY AEZ AJ AU CA CAB CAD CAE CAF CAI CAJ CAK CAL CAM CAN CAO CAP CAQ CAR CAS CAT CAU CAV CAW CD CG CS CT DAB DAD DL EG EP ER FAA FAB FAC FC FH FI GAA HAA HD HH IAA IAB ID IF IR IS KO L1 LA LAA LAB LF MAE MI ML NAA OA PA PAA PC PL RAB RAC RAD RAF RE RF RH RV SA SAA SAD SAE SAI SG SH SM SU TAB TAC TT TV V1 V2 WH XAA YY ZZZ', '\s')"/>
        <let name="UNCL5305" value="tokenize('AE E S Z G O K L M', '\s')"/>
        <let name="eaid" value="tokenize('0002 0007 0009 0037 0060 0088 0096 0097 0106 0135 0142 0184 0190 0191 0192 0193 0195 0196 9902 9904 9905 9906 9907 9910 9913 9914 9915 9917 9918 9919 9920 9921 9922 9923 9924 9925 9926 9927 9928 9929 9930 9931 9932 9933 9934 9935 9936 9937 9938 9939 9940 9941 9942 9943 9944 9945 9946 9947 9948 9949 9950 9951 9952 9953 9955 9956 9957 9958', '\s')"/>
        
        <rule context="ram:AttachmentBinaryObject[@mimeCode]">
            <assert id="PEPPOL-EN16931-CL001"
                test="
                    some $code in $MIMECODE
                        satisfies @mimeCode = $code"
                flag="fatal">Invalid mime code.</assert>
        </rule>

        <rule
            context="ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'false']/ram:ReasonCode">
            <assert id="PEPPOL-EN16931-CL002"
                test="
                    some $code in $UNCL5189
                        satisfies normalize-space(text()) = $code"
                flag="fatal">Reason code MUST be according to subset of UNCL 5189 D.16B.</assert>
        </rule>

        <rule
            context="ram:SpecifiedTradeAllowanceCharge[normalize-space(ram:ChargeIndicator/udt:Indicator) = 'true']/ram:ReasonCode">
            <assert id="PEPPOL-EN16931-CL003"
                test="
                    some $code in $UNCL7161
                        satisfies normalize-space(text()) = $code"
                flag="fatal">Reason code MUST be according to UNCL 7161 D.16B.</assert>
        </rule>

        <rule context="ram:CountryID">
            <assert id="PEPPOL-EN16931-CL005"
                test="
                    some $code in $ISO3166
                        satisfies normalize-space(text()) = $code"
                flag="fatal">Country code MUST be according to ISO 3166 Alpha-2.</assert>
        </rule>


        <!-- PEPPOL-EN16931-CL006 is omitted due to lack of description code for invoice period in CII syntax. -->

        <rule context="ram:TaxTotalAmount[@currencyID]">
            <assert id="PEPPOL-EN16931-CL007"
                test="
                    some $code in $ISO4217
                        satisfies @currencyID = $code"
                flag="fatal">Currency code must be according to ISO 4217:2005</assert>
        </rule>

        <rule context="ram:ExchangedDocument/ram:TypeCode">
            <assert id="PEPPOL-EN16931-P0100"
                test="
                    $profile != '01' or (some $code in tokenize('380 383 386 393 82 80 84 395 575 623 780 381 396 81 83 532', '\s')
                        satisfies normalize-space(text()) = $code)"
                flag="fatal">Invoice type code MUST be set according to the profile.</assert>
        </rule>

        <!-- PEPPOL-EN16931-P0101 is part of PEPPOL-EN16931-P0100. -->

        <rule context="udt:DateTimeString">
            <assert id="PEPPOL-EN16931-F001"
                test="normalize-space(@format) = '102' and string-length(text()) = 8 and matches(normalize-space(text()), '20[0-9]{6}')"
                flag="fatal">A date MUST be formatted YYYYMMDD.</assert>
        </rule>
        <rule context="ram:BuyerTradeParty/ram:URIUniversalCommunication/ram:URIID | ram:SellerTradeParty/ram:URIUniversalCommunication/ram:URIID">
            <assert id="PEPPOL-EN16931-CL008"
                test="
                some $code in $eaid
                satisfies @schemeID = $code"
                flag="fatal">Electronic address identifier scheme must be from the codelist "Electronic Address Identifier Scheme"</assert>
        </rule>
    </pattern>
    


</schema>