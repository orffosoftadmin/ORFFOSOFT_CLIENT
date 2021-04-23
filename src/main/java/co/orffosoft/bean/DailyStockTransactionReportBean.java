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
import co.orffosoft.dto.DailyStockTransactionReportDTO;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("dailyStockTransactionReportBean")
@Scope("session")
@Data
public class DailyStockTransactionReportBean implements Serializable {
	
	/**
	* 
	*/
	private static final long serialVersionUID = -3927409251841374148L;

	private final String DAILY_STOCK_TRANSACTION = "/pages/billing/billing_report/DailyStockTransactionReport.xhtml?faces-redirect=true;";

	@Autowired
	HttpService httpService;
	
	List<Map<String, Object>> listMapForExcel = new ArrayList<>();
	
	private String excelFileUploadPath =  "F:/DailyStockTransactionReport.xlsx";

	@Autowired
	AppPreference appPreference;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	LoginBean loginBean;

	@Autowired
	CommonDataBean commonDataBean;

	DailyStockTransactionReportDTO dailyStockTransactionReportDTO;

	List<DailyStockTransactionReportDTO> dailyStockTransactionReportDTOList;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	ExcelUtil excelUtil;
	
	@Getter
	@Setter
	ProductVarietyMaster productVarietyMaster;
	
	@Getter
	@Setter
	List<ProductVarietyMaster> productVarietyMasterList;
	
	@Getter
	@Setter
	StreamedContent file;

	/**
	 * @return
	 */
	public String loadListPage() {

		dailyStockTransactionReportDTO = new DailyStockTransactionReportDTO();
		dailyStockTransactionReportDTOList = new ArrayList<>();
		listMapForExcel=null;
		productVarietyMaster = new ProductVarietyMaster();
		return DAILY_STOCK_TRANSACTION;
	}

	/**
	 * 
	 */
	public void generateReport() {

		log.info(":::<-   DailyStockTransactionReportBean >> generateReport ->::: ");
		BaseDTO baseDTO = null;
		try {

			if (dailyStockTransactionReportDTO != null) {

				if (dailyStockTransactionReportDTO.getFromDate() == null) {
					AppUtil.addWarn("Please Select From Date");
					return;
				}
				if (dailyStockTransactionReportDTO.getToDate() == null) {
					AppUtil.addWarn("Please Select To Date");
					return;
				}
				dailyStockTransactionReportDTO.setUserId(loginBean.getUserDetailSession().getId());
				dailyStockTransactionReportDTO.setStoreCode(loginBean.getEntityMaster().getCode());
				dailyStockTransactionReportDTO.setStoreName(loginBean.getEntityMaster().getName());
				dailyStockTransactionReportDTO.setProductVarietyMaster(productVarietyMaster);
				String url = SERVER_URL + "/dailyStockTransactionReportController/generateReport";
				log.info("::: DailyTransactionReportBean >>  generateReport URL :::", url);
				baseDTO = httpService.post(url,dailyStockTransactionReportDTO);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					dailyStockTransactionReportDTOList = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					dailyStockTransactionReportDTOList = mapper.readValue(jsonResponse,
							new TypeReference<List<DailyStockTransactionReportDTO>>() {
							});
					
					// For Excel Sheet Download
					listMapForExcel = new ArrayList<>();
					String jsonResponseForExcel = mapper.writeValueAsString(baseDTO.getListOfData());
					listMapForExcel = mapper.readValue(jsonResponseForExcel,
							new TypeReference<List<Map<String, Object>>>() {
							});
					
					
					
					log.info("DailyStockTransactionReportBean >>  generateReport Method  Succes -->");
				} else {
					AppUtil.addInfo("No Record Found");
					log.info("DailyStockTransactionReportBean >>  generateReport Method  >> No Record Found -->");
				}
			}
		} catch (Exception e) {
			log.error("Exception Occured in DailyStockTransactionReportBean >>  generateReport Method ", e);
		}

	}
	public void excelDownload() {
		try {
			excelUtil=new ExcelUtil();
			if(listMapForExcel!=null) {
				excelUtil.buildExcel(excelFileUploadPath, Constants.DAILY_STOCK_TRANSACTION_REPORT_SHEET, listMapForExcel);
			}
			file = excelUtil.getDownloadFile(excelFileUploadPath);
			
		}catch (Exception e) {
			AppUtil.addWarn("Download Failed");
         e.printStackTrace();			
		}
		
	}

	/**
	 * 
	 */
	public void clearData() {
		
		loadListPage();
	}
	
	/**
	 * @param item
	 */
	public void removeItem(DailyStockTransactionReportDTO item) {
		
	}
	public List<ProductVarietyMaster> itemAutoSearch(String itemName) {

		log.info(":::<- Load itemAutocomplete TypeStart ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (itemName.trim() != null && !itemName.isEmpty()) {
				String url = SERVER_URL + "/stockuploadcontroller/autocompleteitemName/" + itemName;
				log.info("::: loadProductVarietyDetails Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get itemAutocomplete Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					productVarietyMasterList = new ArrayList<ProductVarietyMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					productVarietyMasterList = mapper.readValue(jsonResponse,
							new TypeReference<List<ProductVarietyMaster>>() {
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

		return productVarietyMasterList;

	}
	
	
}
