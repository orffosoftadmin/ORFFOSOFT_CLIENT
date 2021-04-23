package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.CollectAmountDTO;
import co.orffosoft.dto.ProfitAndLossReportDTO;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.UserMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("collectamountbean")
@Scope("session")
public class CollectAmountBean {

	private final String List_PAGE = "/pages/billing/billing_report/CollectAmount.xhtml?faces-redirect=true;";
	
	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	@Autowired
	HttpService httpService;

	@Getter
	@Setter
	CollectAmountDTO collectamountdto= new CollectAmountDTO();
	
	@Getter
	@Setter
	List<CollectAmountDTO> listusermaster; 
	
	@Getter
	@Setter
	UserMaster userMaster;
	
	@Getter
	@Setter
	List<CollectAmountDTO> listcollectamountdto;

	public String showPage() {
		collectamountdto = new CollectAmountDTO();
		listcollectamountdto = new ArrayList<CollectAmountDTO>();
		listusermaster=new ArrayList();
		getusers();
		return List_PAGE;
	}
	
	
	public void getusers() {
		BaseDTO basedto = new BaseDTO();
		try {
			String url = SERVER_URL + "/collectamountcontroller/getuser";
			basedto = httpService.get(url);
			
			if(basedto!=null && basedto.getStatusCode()==0) {
				ObjectMapper mapper = new ObjectMapper();
				listusermaster = new ArrayList<CollectAmountDTO>();
				String jsonResponse = mapper.writeValueAsString(basedto.getResponseContents());
				listusermaster = mapper.readValue(jsonResponse, new TypeReference<List<CollectAmountDTO>>() {
				});
			}
		}catch (Exception e) {
			log.info("Exception in bean", e);
		}
	}

	public void generateReport() {
		try {
        BaseDTO baseDTO = new  BaseDTO();
			if (collectamountdto != null) {
				if (collectamountdto.getFromdate() == null) {
					AppUtil.addWarn("Please Select From Date");
					return;
				} else {
					if (collectamountdto.getTodate() == null) {
						AppUtil.addWarn("Please Select To Date");
						return;
					}
				}
				
				String url = SERVER_URL + "/collectamountcontroller/genereatreport";
				baseDTO = httpService.post(url, collectamountdto);
				if(baseDTO!=null && baseDTO.getStatusCode()==0) {
					ObjectMapper mapper = new ObjectMapper();
					listcollectamountdto = new ArrayList<CollectAmountDTO>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listcollectamountdto = mapper.readValue(jsonResponse, new TypeReference<List<CollectAmountDTO>>() {
					});
				}
			}
		} catch (Exception e) {
			log.info("Exception in bean", e);
		}
	}

}
