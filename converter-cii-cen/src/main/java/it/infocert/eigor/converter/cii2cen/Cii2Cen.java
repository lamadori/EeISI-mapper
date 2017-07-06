package it.infocert.eigor.converter.cii2cen;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import it.infocert.eigor.api.*;
import it.infocert.eigor.api.conversion.ConversionRegistry;
import it.infocert.eigor.model.core.model.BG0000Invoice;

/**
 * The CII to CEN format converter
 */
@SuppressWarnings("unchecked")
public class Cii2Cen extends Abstract2CenConverter{
	
	private static final Logger log = LoggerFactory.getLogger(Cii2Cen.class);
	private static final String FORMAT = "cii";
	private static final ConversionRegistry conversionRegistry = new ConversionRegistry();
 
	public Cii2Cen(Reflections reflections) {
		super(reflections, conversionRegistry);
	}

	@Override
	public ConversionResult<BG0000Invoice> convert(InputStream sourceInvoiceStream)
			throws SyntaxErrorInInvoiceFormatException {
		
		List<ConversionIssue> issues = null;
		
		File xsdFile = new File("converterdata/converter-cii-cen/cii/xsd/uncoupled/data/standard/CrossIndustryInvoice_100pD16B.xsd");
		XSDValidator xsdValidator = new XSDValidator(xsdFile);
		
		try {
			byte[] bytes = ByteStreams.toByteArray(sourceInvoiceStream);
			issues = xsdValidator.validate(bytes);
			if(issues.isEmpty()){
				log.info("Cii2Cen xsd validation succesful!");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BG0000Invoice x = new BG0000Invoice();
		ConversionResult<BG0000Invoice> result = new ConversionResult<>(issues, x);
		return result;
	}

	@Override
	public boolean support(String format) {
		if(format == null){
			log.error("Format is null!");
			return false;
		}
		return FORMAT.equals(format.toLowerCase().trim());
	}

	@Override
	public Set<String> getSupportedFormats() {
		return new HashSet<>(Arrays.asList(FORMAT));
	}
	
	
	
}