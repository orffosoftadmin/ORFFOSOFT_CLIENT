package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.ExpensiveDTO;
import co.orffosoft.entity.EntityMaster;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service("expensivereportbean")
@Scope("session")
@Log4j2
public class ExpensiveReportBean {
	
 @Getter
 @Setter
 ExpensiveDTO expensivedto = new ExpensiveDTO();
 
 @Getter
 @Setter
 List<ExpensiveDTO> listexpensivedto; 
 
 @Getter
 @Setter
 String itemname;
 
 @Getter
 @Setter
 List<String> itemnames; 
 
	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();
	
	@Getter
	@Setter
	 EntityMaster entitymaster;
	
	@Getter
	@Setter
	List<EntityMaster> shopNamesList = new ArrayList<>();
	
	@Autowired
	ErrorMap errorMap;
	
	@Getter
	@Setter
	String roletype;

	@Getter
	@Setter
	Long parentid = 0L;
	
	@Autowired
	HttpService httpService;
	
	@Autowired
	LoginBean loginBean;
	
	private final String Add_Page = "/pages/billing/billing_report/ExpensiveReport.xhtml?faces-redirect=true;";
	
	public String showpage() {
		expensivedto = new ExpensiveDTO();
		shopNamesList = new ArrayList<>();
		entitymaster=null;
		listexpensivedto=null;
		itemname=null;
		itemnames=null;
		return Add_Page;
	}
	
	public String generatereport() {
		log.info("generatereport calling");
		BaseDTO baseDTO = new BaseDTO();
		try {
			
			if(expensivedto!=null) {
				expensivedto.setEntitymaster(entitymaster);
				expensivedto.setItemname(itemname);
				if(expensivedto.getFromdate()==null) {
					AppUtil.addError("Please Enter From Date");
					return null;
				}else {
					if(expensivedto.getTodate()==null) {
						AppUtil.addError("Please Enter To Date");
						return null;
					}
				}
				
				String url = serverURL + "/expensivereportcontroller/generate";
				baseDTO = httpService.post(url, expensivedto);
				if(baseDTO!=null && baseDTO.getStatusCode()==0) {
					ObjectMapper mapper = new ObjectMapper();
					listexpensivedto = new ArrayList<ExpensiveDTO>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listexpensivedto = mapper.readValue(jsonResponse,
							new TypeReference<List<ExpensiveDTO>>() {
							});
				}else {
					AppUtil.addError("Item Not available");
					listexpensivedto=null;
					return null;
				}
				
			}
			
			
		} catch (Exception e) {
			log.info("Exception in bean"+e);
		}
		return null;
	}
	
	
	
	public List<String> itemAutoSearch(String itemName) {

		log.info(":::<- Load itemAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (itemName.trim() != null && !itemName.isEmpty()) {
				String url = serverURL + "/expensivereportcontroller/autocompleteitemName/" + itemName;
				log.info("::: loadProductVarietyDetails Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get itemAutocomplete Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					itemnames = new ArrayList<String>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					itemnames = mapper.readValue(jsonResponse,
							new TypeReference<List<String>>() {
							});
					log.info("itemAutocomplete load Successfully " + baseDTO.getTotalRecords());
					log.info("itemAutocomplete Page Convert Succes -->");
				} else {
					log.warn("itemAutocomplete Error ");
				}
			}
		} catch (Exception ee) {
			log.error("Exception Occured in itemAutocomplete load ", ee);
		}

		return itemnames;

	}
	
	
	
	private void getShopNames() {
		log.info(":::<- Load loadProductVarietyDetails TypeStart ->::: ");
		BaseDTO baseDTO = null;
		shopNamesList = new ArrayList<>();
		try {

			// stockItemInwardPNSDTO.setCurentUserMasterid(loginBean.getUserMaster().getId());
			long userMasterId = loginBean.getEntityMaster().getId();
			long parentId = loginBean.getUserMaster().getId();
			String url = serverURL + "/expensivereportcontroller/getshopname/" + userMasterId + "/" + parentId;
			baseDTO = httpService.get(url);
			roletype = loginBean.getUserMaster().getRoleType();
			parentid = baseDTO.getParentIdOfUserMaster();

			if (baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				// shopNamesList = new Array<EntityMaster>();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				shopNamesList = mapper.readValue(jsonResponse, new TypeReference<List<EntityMaster>>() {
				});
			}

		} catch (Exception e) {
			log.error("== Exception Occured on getShopNames == " + e);
		}

	}
}
