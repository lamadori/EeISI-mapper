package it.infocert.eigor.converter.commons.cen2ubl;

import it.infocert.eigor.api.IConversionIssue;
import it.infocert.eigor.api.errors.ErrorCode;
import it.infocert.eigor.model.core.model.BG0000Invoice;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

public class OriginatorDocumentReferenceConverter extends FirstLevelElementsConverter {

    @Override
    public void customMap(BG0000Invoice invoice, Document document, List<IConversionIssue> errors, ErrorCode.Location callingLocation) {

        if (!invoice.getBT0017TenderOrLotReference().isEmpty()) {
            final Element despatchDocumentReference = new Element("OriginatorDocumentReference");
            final String value = invoice.getBT0017TenderOrLotReference(0).getValue();
            final Element id = new Element("ID").setText(value);
            despatchDocumentReference.addContent(id);
            root.addContent(despatchDocumentReference);
        }
    }
}
