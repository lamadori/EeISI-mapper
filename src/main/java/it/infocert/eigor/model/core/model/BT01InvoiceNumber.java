package it.infocert.eigor.model.core.model;

import com.google.common.base.Preconditions;
import it.infocert.eigor.model.core.datatypes.Identifier;

public class BT01InvoiceNumber {

    private final Identifier invoiceNumber;

    public BT01InvoiceNumber(Identifier invoiceNumber) {
        this.invoiceNumber = Preconditions.checkNotNull( invoiceNumber );
    }

    @Override
    public String toString() {
        return "1234";
    }
}
