package co.orffosoft.bean;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import co.orffosoft.dto.DashboardDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service("dashboardBean")
@Log4j2
@Scope("session")
public class DashboardBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5639488502200872028L;
	
	private final static String PROFILE_PAGE="/profile.xhtml"; 

	@Autowired
	LoginBean loginBean;

	@Getter
	@Setter
	DashboardDTO headerDTO;

	public DashboardBean() {
		headerDTO = new DashboardDTO();
	}

	public String formatDecimalNumber(Double number) {
		
		loginBean.calculateTodaySale();
		
		log.info("Number:::" + number);
		if (number == null)
			return "0.00";
		DecimalFormat decim = new DecimalFormat("0.00");
		String price = decim.format(number);
		return price;
	}
	
	public String loadCustomerProfile() {
		
		return PROFILE_PAGE;
	}

}
