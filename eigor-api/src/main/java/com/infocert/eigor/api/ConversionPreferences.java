package com.infocert.eigor.api;

public final class ConversionPreferences {

    private Boolean forceConversion;
    private boolean intermediateValidation;
    private boolean skipDefaultCallbacks;

    public ConversionPreferences() {
        forceConversion = null;
        intermediateValidation = false;
        skipDefaultCallbacks = false;
    }

    private ConversionPreferences(Boolean forceConversion, boolean intermediateValidation) {
        this.forceConversion = forceConversion;
        this.intermediateValidation = intermediateValidation;
        this.skipDefaultCallbacks = false;
    }

    /**
     * Define that the conversion should be "forced" to be executed up to the end
     * despite any error that may occur during the process.
     * If invoked, this override the preference set in the {@link EigorApiBuilder#enableForce()}.
     */
    public ConversionPreferences withForceConversion() {
        forceConversion = true;
        return this;
    }

    /**
     * Define that the conversion should stop at the first error that may occur.
     * If invoked, this override the preference set in the {@link EigorApiBuilder#enableForce()}.
     */
    public ConversionPreferences withoutForceConversion() {
        forceConversion = false;
        return this;
    }

    /**
     * Add an intermediate validation step that force the CEN model produced by the first
     * xxx-CEN converter to be a valid CEN.
     */
    public ConversionPreferences withIntermediateValidation() {
        intermediateValidation = true;
        return this;
    }

    public Boolean forceConversion() {
        return forceConversion;
    }

    public boolean validateIntermediateCen() {
        return intermediateValidation;
    }
    
    public boolean isSkipDefaultCallbacks() {
        return skipDefaultCallbacks;
    }

    public ConversionPreferences withSkipDefaultCallbacks() {
        skipDefaultCallbacks = true;
        return this;
    }
}
