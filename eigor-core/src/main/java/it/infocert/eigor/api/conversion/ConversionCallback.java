package it.infocert.eigor.api.conversion;

/**
 * Group a series of callback methods that are invoked
 * when some specific events occurs during an invoice conversion.
 *
 * <p>
 *     In each callback, a {@link ConversionContext} is provided,
 *     in order to allows the implementations to retrieve whatever detail they are interested in.
 * </p>
 */
public interface ConversionCallback {

        /** Always invoked, at the beginning of the transformation.
         * @see ConversionCallback#onTerminatedConversion(ConversionContext) Callback for conversion completion.
         */
        void onStartingConversion(ConversionContext ctx) throws Exception;

        /** Always invoked, when the transformation from the source format to the intermediate CEN
         * format is started. */
        void onStartingToCenTranformation(ConversionContext ctx) throws Exception;

        /** Invoked at most once, when and if the converstion to CEN completes successfully. */
        void onSuccessfullToCenTranformation(ConversionContext ctx) throws Exception;

        /** Invoked at most once, when and if the converstion to CEN fails. */
        void onFailedToCenConversion(ConversionContext ctx) throws Exception;

        /** Invoked just before the CEN rules are verified. */
        void onStartingVerifyingCenRules(ConversionContext ctx) throws Exception;

        /** Invoked only if the CEN rules have verified the CEN invoice. */
        void onSuccessfullyVerifiedCenRules(ConversionContext ctx) throws Exception;

        /** Invoked only if the CEN rules failed in verify the CEN invoice. */
        void onFailedVerifyingCenRules(ConversionContext ctx) throws Exception;

        /** Invoked once when the conversion from the CEN model to the target one is going to start. */
        void onStartingFromCenTransformation(ConversionContext ctx) throws Exception;

        /** Invoked in case the final transformation to the target format completed successfully. */
        void onSuccessfullFromCenTransformation(ConversionContext ctx) throws Exception;

        /** Invoked in case the final transformation to the target failed. */
        void onFailedFromCenTransformation(ConversionContext ctx) throws Exception;

        /** Invoked in case an unexpected or unmanaged exception occurs during any step of the conversion. */
        void onUnexpectedException(Exception e, ConversionContext ctx) throws Exception;

        /** Always invoked, at the end of the transformation.
         * @see ConversionCallback#onStartingConversion(ConversionContext) Callback for initiated conversion.
         */
        void onTerminatedConversion(ConversionContext ctx) throws Exception;

    }
