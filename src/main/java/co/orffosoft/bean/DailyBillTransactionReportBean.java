package co.orffosoft.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.Constants;
import co.orffosoft.core.util.ExcelUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.DailyTransactionReportDTO;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("dailyBillTransactionReportBean")
@Scope("session")
@Data
public class DailyBillTransactionReportBean implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -3927409251841374148L;

	private final String DAILY_TRANSACTION = "/pages/billing/billing_report/DailyBillTransactionReport.xhtml?faces-redirect=true;";

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	LoginBean loginBean;

	@Autowired
	CommonDataBean commonDataBean;

	DailyTransactionReportDTO dailyTransactionReportDTO;

	@Getter
	@Setter
	List<DailyTransactionReportDTO> dailyTransactionReportDTOList;
	
	@Getter
	@Setter
	List<DailyTransactionReportDTO> viewbilldetaillist;
	
	@Getter
	@Setter
	List<DailyTransactionReportDTO> viewreturnlist;
	
	List<Map<String, Object>> listMapForExcel = new ArrayList<>();
	ExcelUtil excelUtil;
	private String excelFileUploadPath = "/DATA/PROGRAMS/ORFFOSOFT_RETAIL/DAILY_TRANSACTION/UPLOADED/DailyBillingReport.xlsx";

	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	@Getter
	@Setter
	StreamedContent file;
	
	@Getter
	@Setter
	double totalNetPeice=0.0;

	/**
	 * @return
	 */
	public String loadListPage() {

		dailyTransactionReportDTO = new DailyTransactionReportDTO();
		dailyTransactionReportDTOList = new ArrayList<>();
		listMapForExcel=null;
		totalNetPeice = 0;
		return DAILY_TRANSACTION;
	}

	/**
	 * 
	 */
	public void generateReport() {

		log.info(":::<-   DailyTransactionReportBean >> generateReport ->::: ");
		BaseDTO baseDTO = null;
		try {
			boolean valid = false;
			if (dailyTransactionReportDTO != null) {

				if (dailyTransactionReportDTO.getFromDate() != null || dailyTransactionReportDTO.getToDate() != null) {
					valid = true;
					if (dailyTransactionReportDTO.getFromDate() == null) {
						AppUtil.addWarn("Please Select From Date");
						return;
					} else if (dailyTransactionReportDTO.getToDate() == null) {
						AppUtil.addWarn("Please Select To Date");
						return;
					}
				}
				if (!(dailyTransactionReportDTO.getBillNo().isEmpty())) {
					valid = true;
				} if (!(dailyTransactionReportDTO.getCustomerName().isEmpty())) {
					valid = true;
				} if (dailyTransactionReportDTO.getCustomerMobileNo() != null) {
					valid = true;
				}
				if (!valid) {
					AppUtil.addWarn("Enter At Least One Field");
					return;
				}
				
				dailyTransactionReportDTO.setUserId(loginBean.getUserDetailSession().getId());
				dailyTransactionReportDTO.setStoreCode(loginBean.getEntityMaster().getCode());
				dailyTransactionReportDTO.setStoreName(loginBean.getEntityMaster().getName());

				String url = SERVER_URL + "/dailyTransactionReportController/generateReport";
				log.info("::: DailyTransactionReportBean >>  generateReport URL :::", url);
				baseDTO = httpService.post(url,dailyTransactionReportDTO);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					dailyTransactionReportDTOList = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					dailyTransactionReportDTOList = mapper.readValue(jsonResponse,
							new TypeReference<List<DailyTransactionReportDTO>>() {
							});
					totalNetPeice=dailyTransactionReportDTOList.stream().mapToDouble(x -> x.getNetPrice()).sum();
					// For Excel Sheet Download
					listMapForExcel = new ArrayList<>();
					String jsonResponseForExcel = mapper.writeValueAsString(baseDTO.getListOfData());
					listMapForExcel = mapper.readValue(jsonResponseForExcel,
							new TypeReference<List<Map<String, Object>>>() {
							});
					log.info("DailyTransactionReportBean >>  generateReport Method  Succes -->");
				} else {
					AppUtil.addError("No Record Found");
					log.info("DailyTransactionReportBean >>  generateReport Method  >> No Record Found -->");
				}
			}
		} catch (Exception e) {
			log.error("Exception Occured in DailyTransactionReportBean >>  generateReport Method ", e);
		}

	}

	/**
	 * 
	 */
	public void clearData() {
		loadListPage();
	}
	
	public void viewBillDetails(DailyTransactionReportDTO item) {
		log.info(":::<-   DailyTransactionReportBean >> viewBillDetails ->::: ");
		BaseDTO baseDTO = null;
		try {
			if(item!=null&& item.getBillhpk()!=null) {
				String url = SERVER_URL + "/dailyTransactionReportController/viewBillDetails";
				log.info("::: DailyTransactionReportBean >>  viewBillDetails URL :::", url);
				baseDTO = httpService.post(url,item);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					viewbilldetaillist = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					viewbilldetaillist = mapper.readValue(jsonResponse, new TypeReference<List<DailyTransactionReportDTO>>() {
					});
					log.info("dailyTransactionReportBean >>  viewBillDetails Method  Succes -->");
				} else {
					AppUtil.addInfo("No Record Found");
					log.info("SalesReturnBean >>  viewBillDetails Method  >> No Record Found -->");
				}
			}
			
			
		}catch (Exception e) {
			log.error("Exception Occured in DailyTransactionReportBean >>  viewBillDetails Method ", e);
		}
		
	}
	public void viewReturnDetails(DailyTransactionReportDTO item) {
		log.info(":::<-   DailyTransactionReportBean >> viewBillDetails ->::: ");
		BaseDTO baseDTO = null;
		try {
			if(item!=null&& item.getBillhpk()!=null) {
				String url = SERVER_URL + "/dailyTransactionReportController/viewReturnDetails";
				log.info("::: DailyTransactionReportBean >>  viewBillDetails URL :::", url);
				baseDTO = httpService.post(url,item);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					viewreturnlist = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					viewreturnlist = mapper.readValue(jsonResponse, new TypeReference<List<DailyTransactionReportDTO>>() {
					});
					log.info("dailyTransactionReportBean >>  viewBillDetails Method  Succes -->");
				} else {
					AppUtil.addInfo("No Record Found");
					log.info("SalesReturnBean >>  viewBillDetails Method  >> No Record Found -->");
				}
			}
			
			
		}catch (Exception e) {
			log.error("Exception Occured in DailyTransactionReportBean >>  viewBillDetails Method ", e);
		}
		
	}
	
	/**
	 * @param item
	 */
	public void removeItem(DailyTransactionReportDTO item) {
		
	}
	public void excelDownload() {
		try {
			excelUtil=new ExcelUtil();
			if(listMapForExcel!=null) {
				excelUtil.buildExcel(excelFileUploadPath, Constants.DAILY_BILLING_REPORT_SHEET, listMapForExcel);
			}
			file = excelUtil.getDownloadFile(excelFileUploadPath);
			
			
		}catch (Exception e) {
			AppUtil.addWarn("Download Failed");
        	log.error("",e);	
		}
		
	}

}
