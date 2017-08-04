package it.infocert.eigor.converter.ubl2cen;

import it.infocert.eigor.api.*;
import it.infocert.eigor.api.conversion.StringToDoubleConverter;
import it.infocert.eigor.api.errors.ErrorMessage;
import it.infocert.eigor.model.core.enums.Untdid5189ChargeAllowanceDescriptionCodes;
import it.infocert.eigor.model.core.enums.Untdid5305DutyTaxFeeCategories;
import it.infocert.eigor.model.core.model.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * The Document Level Allowances Custom Converter
 */
public class DocumentLevelAllowancesConverter extends CustomConverterUtils implements CustomMapping<Document> {

    public ConversionResult<BG0000Invoice> toBG0020(Document document, BG0000Invoice invoice, List<IConversionIssue> errors) {

        StringToDoubleConverter strDblConverter = new StringToDoubleConverter();

        BG0020DocumentLevelAllowances bg0020 = null;
        
        Element rootElement = document.getRootElement();
        List<Namespace> namespacesInScope = rootElement.getNamespacesIntroduced();

        List<Element> allowanceCharge = findNamespaceChildren(rootElement, namespacesInScope, "AllowanceCharge");

        for(Element elemAllowance : allowanceCharge) {

        	Element chargeIndicator = findNamespaceChild(elemAllowance, namespacesInScope, "ChargeIndicator");

        	if (chargeIndicator != null && chargeIndicator.getText().equals("false")) {

        		bg0020 = new BG0020DocumentLevelAllowances();

        		Element amount = findNamespaceChild(elemAllowance, namespacesInScope, "Amount");
        		if (amount != null) {
                    try {
                        BT0092DocumentLevelAllowanceAmount bt0092 = new BT0092DocumentLevelAllowanceAmount(strDblConverter.convert(amount.getValue()));
                        bg0020.getBT0092DocumentLevelAllowanceAmount().add(bt0092);
                    }catch (NumberFormatException e) {
                        EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage()).action("DocumentLevelAllowancesConverter").build());
                        errors.add(ConversionIssue.newError(ere));
                    }
                }
        		
        		Element baseAmount = findNamespaceChild(elemAllowance, namespacesInScope, "BaseAmount");
        		if (baseAmount != null) {
                    try {
                        BT0093DocumentLevelAllowanceBaseAmount bt0093 = new BT0093DocumentLevelAllowanceBaseAmount(strDblConverter.convert(baseAmount.getText()));
                        bg0020.getBT0093DocumentLevelAllowanceBaseAmount().add(bt0093);
                    }catch (NumberFormatException e) {
                        EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage()).action("DocumentLevelAllowancesConverter").build());
                        errors.add(ConversionIssue.newError(ere));
                    }
                }
        		
        		Element multiplierFactorNumeric = findNamespaceChild(elemAllowance, namespacesInScope, "MultiplierFactorNumeric");
        		if (multiplierFactorNumeric != null) {
                    try{
                        BT0094DocumentLevelAllowancePercentage bt0094 = new BT0094DocumentLevelAllowancePercentage(strDblConverter.convert(multiplierFactorNumeric.getText()));
                        bg0020.getBT0094DocumentLevelAllowancePercentage().add(bt0094);
                    }catch (NumberFormatException e) {
                        EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage()).action("DocumentLevelAllowancesConverter").build());
                        errors.add(ConversionIssue.newError(ere));
                    }
                }
        		
        		Element taxCategory = findNamespaceChild(elemAllowance, namespacesInScope, "TaxCategory");
        		if (taxCategory != null) {
        			Element id = findNamespaceChild(taxCategory, namespacesInScope, "ID");
        			if (id != null) {
        			    try{
                            BT0095DocumentLevelAllowanceVatCategoryCode bt0095 = new BT0095DocumentLevelAllowanceVatCategoryCode(Untdid5305DutyTaxFeeCategories.valueOf(id.getText()));
                            bg0020.getBT0095DocumentLevelAllowanceVatCategoryCode().add(bt0095);
                        }catch (IllegalArgumentException e) {
                            EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message("Untdid5305DutyTaxFeeCategories not found").action("DocumentLevelAllowancesConverter").build());
                            errors.add(ConversionIssue.newError(ere));
                        }
                    }
        			
        			Element percent = findNamespaceChild(taxCategory, namespacesInScope, "Percent");
        			if (percent != null) {
        				try {
        					BT0096DocumentLevelAllowanceVatRate bt0096 = new BT0096DocumentLevelAllowanceVatRate(strDblConverter.convert(percent.getText()));
        					bg0020.getBT0096DocumentLevelAllowanceVatRate().add(bt0096);
        				}catch (NumberFormatException e) {
                            EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message(e.getMessage()).action("DocumentLevelAllowancesConverter").build());
        					errors.add(ConversionIssue.newError(ere));
        				}
        			}
        		}
        		
        		Element allowanceChargeReason = findNamespaceChild(elemAllowance, namespacesInScope, "AllowanceChargeReason");
        		if (allowanceChargeReason != null) {
                    BT0097DocumentLevelAllowanceReason bt0097 = new BT0097DocumentLevelAllowanceReason(allowanceChargeReason.getText());
                    bg0020.getBT0097DocumentLevelAllowanceReason().add(bt0097);
                }
        		
        		Element allowanceChargeReasonCode = findNamespaceChild(elemAllowance, namespacesInScope, "AllowanceChargeReasonCode");
        		if (allowanceChargeReasonCode != null) {
        		    try{
                        BT0098DocumentLevelAllowanceReasonCode bt0098 = new BT0098DocumentLevelAllowanceReasonCode(Untdid5189ChargeAllowanceDescriptionCodes.valueOf("Code"+allowanceChargeReasonCode.getText()));
                        bg0020.getBT0098DocumentLevelAllowanceReasonCode().add(bt0098);
                    }catch (IllegalArgumentException e) {
                        EigorRuntimeException ere = new EigorRuntimeException(e, ErrorMessage.builder().message("Untdid5189ChargeAllowanceDescriptionCodes not found").action("DocumentLevelAllowancesConverter").build());
                        errors.add(ConversionIssue.newError(ere));
                    }
                }        
                            
        		invoice.getBG0020DocumentLevelAllowances().add(bg0020);
        	}
        }
        return new ConversionResult<>(errors, invoice);
    }

    @Override
    public void map(BG0000Invoice cenInvoice, Document document, List<IConversionIssue> errors) {
        toBG0020(document, cenInvoice, errors);
    }
}