package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.Constants;
import co.orffosoft.core.util.ExcelUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.ProfitAndLossReportDTO;
import co.orffosoft.entity.EntityMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("profitAndLossBean")
@Scope("session")
public class ProfitAndLossBean {

	private final String List_PAGE = "/pages/billing/billing_report/ProfitAndLossReport.xhtml ?faces-redirect=true;";

	private String excelFileUploadPath = "/DATA/PROGRAMS/ORFFOSOFT_RETAIL/PROFIT_LOSS_REPORT/UPLOADED/ProfitAndLossReport.xlsx";

	@Getter
	@Setter
	ProfitAndLossReportDTO profitAndLossReportDTO = new ProfitAndLossReportDTO();

	@Autowired
	LoginBean loginBean;

	@Getter
	@Setter
	EntityMaster entityMaster;

	@Getter
	@Setter
	String roletype;

	@Getter
	@Setter
	Long parentid = 0L;

	@Getter
	@Setter
	List<ProfitAndLossReportDTO> listProfitAndLossReportDTO;

	@Getter
	@Setter
	List<EntityMaster> shopNamesList = new ArrayList<>();

	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	@Autowired
	HttpService httpService;

	List<Map<String, Object>> listMapForExcel = new ArrayList<>();

	ExcelUtil excelUtil;

	@Getter
	@Setter
	StreamedContent file;
	
	@Getter
	@Setter
	Double pp=0.0,sp=0.0,tpp=0.0,tsp=0.0,profit=0.0;

	public String showPage() {

		profitAndLossReportDTO = new ProfitAndLossReportDTO();
		listProfitAndLossReportDTO = null;
		shopNamesList = new ArrayList<>();
		entityMaster = null;
		getShopNames();
		return List_PAGE;
	}

	private void getShopNames() {
		log.info(":::<- Load loadProductVarietyDetails TypeStart ->::: ");
		BaseDTO baseDTO = null;
		shopNamesList = new ArrayList<>();
		try {

			// stockItemInwardPNSDTO.setCurentUserMasterid(loginBean.getUserMaster().getId());
			long userMasterId = loginBean.getEntityMaster().getId();
			long parentId = loginBean.getUserMaster().getId();
			String url = SERVER_URL + "/user/getshopname/" + userMasterId + "/" + parentId;
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

	public void generateReport() {
		BaseDTO baseDTO = new BaseDTO();
		try {

			if (profitAndLossReportDTO.getFromdate() == null) {
				AppUtil.addWarn("Please Select From Date");
				return;
			} else if (profitAndLossReportDTO.getTodate() == null) {
				AppUtil.addWarn("Please Select To Date");
				return;
			}

			profitAndLossReportDTO.setEntityMaster(entityMaster);

			String url = SERVER_URL + "/profitAndLossController/genereat";
			baseDTO = httpService.post(url, profitAndLossReportDTO);
			if (baseDTO != null && baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();

				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				listProfitAndLossReportDTO = mapper.readValue(jsonResponse,
						new TypeReference<List<ProfitAndLossReportDTO>>() {
						});

				// For Excel Sheet Download
				listMapForExcel = new ArrayList<>();
				String jsonResponseForExcel = mapper.writeValueAsString(baseDTO.getListOfData());
				listMapForExcel = mapper.readValue(jsonResponseForExcel,
						new TypeReference<List<Map<String, Object>>>() {
						});
				
				        pp=listProfitAndLossReportDTO.stream().mapToDouble(x->x.getPurchasePrice()).sum();
						sp=listProfitAndLossReportDTO.stream().mapToDouble(x->x.getSellingPrice()).sum();;
						tpp=listProfitAndLossReportDTO.stream().mapToDouble(x->x.getTotalPurchasePrice()).sum();;
						tsp=listProfitAndLossReportDTO.stream().mapToDouble(x->x.getTotalSellingPrice()).sum();;
						profit=listProfitAndLossReportDTO.stream().mapToDouble(x->x.getTotalNetProfit()).sum();

			}

		} catch (Exception e) {
			log.error("Exception occured on ProfitLossBean>> generateReport  " + e);
		}

	}

	public void excelDownload() {
		try {
			excelUtil = new ExcelUtil();
			if (listMapForExcel != null) {
				excelUtil.buildExcel(excelFileUploadPath, Constants.DAILY_STOCK_TRANSACTION_REPORT_SHEET,
						listMapForExcel);
			}
			file = excelUtil.getDownloadFile(excelFileUploadPath);

		} catch (Exception e) {
			AppUtil.addWarn("Download Failed");
			e.printStackTrace();
		}

	}

}
