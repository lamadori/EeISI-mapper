package it.infocert.eigor.api.conversion;

import it.infocert.eigor.api.BinaryConversionResult;
import it.infocert.eigor.api.ConversionResult;
import it.infocert.eigor.model.core.model.BG0000Invoice;

/**
 * Gives access to all the info used by the converter during a transformation.
 */
public class ConversionContext {

    private ConversionResult<BG0000Invoice> toCenResult;
    private BinaryConversionResult fromCenResult;
    private byte[] invoiceInSourceFormat;
    private boolean forceConversion;
    private String invoiceFileName;
    private String targetInvoiceExtension;

    /**
     * If XML->CEN transformation has already taken place, this returns the related conversion result,
     * {@literal null} otherwise.
     */
    public ConversionResult<BG0000Invoice> getToCenResult() {
        return toCenResult;
    }

    void setToCenResult(ConversionResult<BG0000Invoice> toCenResult) {
        this.toCenResult = toCenResult;
    }

    /**
     * If CEN rule verification has already taken place, this returns the related result,
     * {@literal null} otherwise.
     */
    public BinaryConversionResult getFromCenResult() {
        return fromCenResult;
    }

    void setFromCenResult(BinaryConversionResult fromCenResult) {
        this.fromCenResult = fromCenResult;
    }

    /**
     * Get the input invoice in source format. Always available.
     */
    public byte[] getInvoiceInSourceFormat() {
        return invoiceInSourceFormat;
    }

    void setInvoiceInSourceFormat(byte[] invoiceInSourceFormat) {
        this.invoiceInSourceFormat = invoiceInSourceFormat;
    }

    /**
     * Whether the conversion will goes on until the end or will stop at the first error.
     */
    public boolean isForceConversion() {
        return forceConversion;
    }

    void setForceConversion(boolean forceConversion) {
        this.forceConversion = forceConversion;
    }

    void setInvoiceFileName(String invoiceFileName) {
        this.invoiceFileName = invoiceFileName;
    }

    /**
     * The name of the original file being converted.
     */
    public String getSourceInvoiceFileName() {
        return invoiceFileName;
    }

    /**
     * The preferred extension for the destination invoice.
     */
    public String getTargetInvoiceExtension() {
        return targetInvoiceExtension;
    }

    void setTargetInvoiceExtension(String targetInvoiceExtension) {
        this.targetInvoiceExtension = targetInvoiceExtension;
    }
}
