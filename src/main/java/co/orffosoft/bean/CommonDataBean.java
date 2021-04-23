package co.orffosoft.bean;

import java.text.DecimalFormat;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("commonDataService")
@Scope("session")
public class CommonDataBean {

	// return formatedNumber
	public String formatDecimalNumber(Double number) {
		log.info("Number:::" + number);
		if (number == null)
			return "0.00";
		DecimalFormat decim = new DecimalFormat("0.00");
		String price = decim.format(number);
		return price;
	}

}
