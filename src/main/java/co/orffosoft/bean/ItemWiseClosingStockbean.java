package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.Constants;
import co.orffosoft.core.util.ExcelUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.itemWiseClosingStockDTO;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("itemwiseclosingstockbean")
@Scope("session")
public class ItemWiseClosingStockbean {
	
	private final String item_Wise_closing="/pages/billing/billing_report/ItemwiseClosingStock.xhtml?faces-redirect=true;"; 
	
	@Getter
	@Setter
	List<Map<String, Object>> listMapForExcel = new ArrayList<>();
	
	private String excelFileUploadPath =  "F:/ItemWiseClosingStockReport.xlsx";
	
	@Getter
	@Setter
	itemWiseClosingStockDTO itemWiseClosingStockdto = new itemWiseClosingStockDTO();
	
	@Getter
	@Setter
	ProductVarietyMaster productVarietyMaster;
	
	@Getter
	@Setter
	List<ProductVarietyMaster> productVarietyMasterList;
	
	@Getter
	@Setter
	List<itemWiseClosingStockDTO> itemWiseClosingStockdtoList;
	
	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	@Autowired
	HttpService httpService;

	ExcelUtil excelUtil;
	
	public void generateReport() {
		log.info(":::<-   itemwiseclosingstockbean >> generateReport ->::: ");
		BaseDTO baseDTO = null;
		try {
			if(itemWiseClosingStockdto!=null) {
				if (itemWiseClosingStockdto.getToDate() == null) {
					AppUtil.addWarn("Please Select To Date");
					return;
				}
				itemWiseClosingStockdto.setProductVarietyMaster(productVarietyMaster);
				String url = SERVER_URL + "/itemWiseClosingStockController/generetreport";
				baseDTO = httpService.post(url,itemWiseClosingStockdto);
				if(baseDTO.getStatusCode()==0) {
					itemWiseClosingStockdtoList = new ArrayList<itemWiseClosingStockDTO>();
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					itemWiseClosingStockdtoList = mapper.readValue(jsonResponse, new TypeReference<List<itemWiseClosingStockDTO>>() {
					});
				}
			}
			
		}catch (Exception e) {
			log.error("Exception Occured on generateReport = "+e);
		}
		
	}
 
	public String showPage() {
		listMapForExcel= null;
		productVarietyMaster=new ProductVarietyMaster();
		itemWiseClosingStockdto = new itemWiseClosingStockDTO();
		itemWiseClosingStockdtoList=new ArrayList();
		return item_Wise_closing;
	}
	
	public void excelDownload() {
		try {
			excelUtil=new ExcelUtil();
			if(listMapForExcel!=null) {
				excelUtil.buildExcel(excelFileUploadPath, Constants.DAILY_STOCK_TRANSACTION_REPORT_SHEET, listMapForExcel);
			}
			
			
		}catch (Exception e) {
			AppUtil.addWarn("Download Failed");
         e.printStackTrace();			
		}
		
	}
	public void clearData() {
		showPage();
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
