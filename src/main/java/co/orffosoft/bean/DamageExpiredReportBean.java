package co.orffosoft.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.DamageExpiredReportDto;
import co.orffosoft.dto.itemWiseClosingStockDTO;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("damageexpiredreportbean")
@Scope("session")
public class DamageExpiredReportBean implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1989380120169061249L;
	
	private final String Show_Page = "/pages/billing/billing_report/DamageExpiredReport.xhtml?faces-redirect=true;";
	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	LoginBean loginBean;
	
	@Getter
	@Setter
	DamageExpiredReportDto damageExpiredReportdto;
	
	@Getter
	@Setter
	ProductVarietyMaster productVarietyMaster;
	
	@Getter
	@Setter
	List<ProductVarietyMaster> listproductVarietyMaster;
	
	@Getter
	@Setter
	List<DamageExpiredReportDto> listdamageExpiredReportdto=new ArrayList();
	
	@Getter
	@Setter
	double totaldmage=0.0,totlaexpired=0.0,totalloss=0.0;
	
	public String showPage() {
		
		damageExpiredReportdto=new DamageExpiredReportDto();
		listdamageExpiredReportdto=null;
		totaldmage=0.0;
		totlaexpired=0.0;
		totalloss=0.0;
		return Show_Page;
		
		
	}
	
	public void generateReport() {
		BaseDTO baseDTO = new BaseDTO();
		try {
			if(damageExpiredReportdto!=null) {
				if(damageExpiredReportdto.getFromdate()==null) {
					AppUtil.addWarn("Please Select From Date");
					return;
				}
				if(damageExpiredReportdto.getTodate()==null) {
					AppUtil.addWarn("Please Select To Date");
					return;
				}
				if(productVarietyMaster!=null && productVarietyMaster.getId()!=null) {
					damageExpiredReportdto.setProductvaritymaster(productVarietyMaster); 
				}
				String url = SERVER_URL + "/damageexpiredreportController/generetreport";
				baseDTO = httpService.post(url,damageExpiredReportdto);
				if(baseDTO.getStatusCode()==0) {
					listdamageExpiredReportdto = new ArrayList<DamageExpiredReportDto>();
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listdamageExpiredReportdto = mapper.readValue(jsonResponse, new TypeReference<List<DamageExpiredReportDto>>() {
					});
					totaldmage=listdamageExpiredReportdto.stream().mapToDouble(x->x.getDamageamount()!=null ? x.getDamageamount():0).sum();
					totlaexpired=listdamageExpiredReportdto.stream().mapToDouble(x->x.getExpireamount()!=null ? x.getExpireamount():0).sum();
					totalloss=totaldmage+totlaexpired;
				}
				
				
			}
			
			
		}catch (Exception e) {
			log.error("Exception Occured in generateReport load ", e);
		}
		
		
	}
	
	public List<ProductVarietyMaster> itemAutoSearch(String itemname) {
		log.info(":::<- Load itemAutoSearch in DamageExpiredReportBean ->::: ");
		BaseDTO baseDTO = new BaseDTO();
		try {
			if (itemname.trim() != null && !itemname.isEmpty()) {
				String url = SERVER_URL + "/damageexpiredreportController/itemAutoSearch/" + itemname;
				log.info("::: damageexpiredreportController Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get damageexpiredreportController Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					listproductVarietyMaster = new ArrayList<ProductVarietyMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listproductVarietyMaster = mapper.readValue(jsonResponse,
							new TypeReference<List<ProductVarietyMaster>>() {
							});
					log.info("itemAutocomplete load Successfully " + baseDTO.getTotalRecords());
					log.info("itemAutocomplete Page Convert Succes -->");
				} else {
					log.warn("itemAutocomplete Error ");
				}
			}
			
			
		}catch (Exception e) {
			log.error("Exception Occured in itemAutocomplete load ", e);
		}
		
		return listproductVarietyMaster;
	}
	
	
}
