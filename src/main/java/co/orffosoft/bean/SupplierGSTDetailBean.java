package co.orffosoft.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.ExcelUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.DailyTransactionReportDTO;
import co.orffosoft.dto.SupplierGST_Detail_DTO;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.EntityMaster;
import co.orffosoft.entity.SupplierMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("supplierGSTdetailBean")
@Scope("session")
public class SupplierGSTDetailBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4890478355136945706L;

	private final String list_Page = "/pages/masters/SupplierGSTDetail.xhtml?faces-redirect=true;";

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	@Getter
	@Setter
	List<Map<String, Object>> listMapForExcel = new ArrayList<>();

	@Getter
	@Setter
	List<SupplierMaster> supplierMasterList = new ArrayList<>();

	@Getter
	@Setter
	SupplierMaster selectedSupplierMaster;

	ExcelUtil excelUtil;

	@Getter
	@Setter
	EntityMaster entityMaster;

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Autowired
	LoginBean loginBean;

	@Getter
	@Setter
	SupplierGST_Detail_DTO suppliergstdetsildto=new SupplierGST_Detail_DTO();

	@Getter
	@Setter
	List<SupplierGST_Detail_DTO> listsuppliergstdetsildto;
	
	@Getter
	@Setter
	List<SupplierGST_Detail_DTO> viewbilldetaillist;
	
	@Getter
	@Setter
	double withgstamount;
	
	@Getter
	@Setter
	double withoutgstamount;

	public String ShowListPage() {
		suppliergstdetsildto = new SupplierGST_Detail_DTO();
		listsuppliergstdetsildto=null;
		supplierMasterList=null;
			return list_Page;

	}

	public List<SupplierMaster> suplierAutoSearch(String supliername) {
		log.info(":::<- Load itemAutoSearch in supplier_GST_detailBean ->::: ");
		BaseDTO baseDTO = null;
		try {
			if (supliername.trim() != null && !supliername.isEmpty()) {
				String url = SERVER_URL + "/SupplierGSTdetailcontroller/suplierAutoSearch/" + supliername;
				log.info("::: SupplierGSTdetailcontroller Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get SupplierGSTdetailcontroller Response :");
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					supplierMasterList = new ArrayList<SupplierMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					supplierMasterList = mapper.readValue(jsonResponse, new TypeReference<List<SupplierMaster>>() {
					});
					log.info("suplierAutoSearch load Successfully " + baseDTO.getTotalRecords());
					log.info("suplierAutoSearch Page Convert Succes -->");
				} else {
					log.warn("suplierAutoSearch Error ");
				}
			}

		} catch (Exception e) {
			log.error("Exception Occured in suplierAutoSearch load in supplier_GST_detailBean ", e);
		}

		return supplierMasterList;

	}

	public void generateReport() {

		BaseDTO basedto = new BaseDTO();
		try {

			if (suppliergstdetsildto.getFromdate() == null) {
				AppUtil.addWarn("Please Select From Date");
				return;
			}
			if (suppliergstdetsildto.getTodate() == null) {
				AppUtil.addWarn("Please Select To Date");
				return;
			}

			if (selectedSupplierMaster != null) {

				suppliergstdetsildto.setSupplierMaster(selectedSupplierMaster);
			}
			if (suppliergstdetsildto != null || suppliergstdetsildto.getTodate() == null
					|| suppliergstdetsildto.getTodate() == null) {

				String url = SERVER_URL + "/SupplierGSTdetailcontroller/generate";
				log.info("::: SupplierGSTdetailcontroller Controller calling  1 :::");
				
				basedto = httpService.post(url, suppliergstdetsildto);
				
				log.info("::: get SupplierGSTdetailcontroller Response :");
				
				if(basedto!=null){
					ObjectMapper mapper = new ObjectMapper();
					listsuppliergstdetsildto = new ArrayList<SupplierGST_Detail_DTO>();
					String jsonResponse = mapper.writeValueAsString(basedto.getResponseContents());
					listsuppliergstdetsildto = mapper.readValue(jsonResponse, new TypeReference<List<SupplierGST_Detail_DTO>>() {
					});
					withgstamount=listsuppliergstdetsildto.stream().mapToDouble(x->x.getNetamount()).sum()
							+listsuppliergstdetsildto.stream().mapToDouble(x->x.getCgstamt()).sum()+listsuppliergstdetsildto.stream().mapToDouble(x->x.getSgstamt()).sum();
					withoutgstamount=listsuppliergstdetsildto.stream().mapToDouble(x->x.getNetamount()).sum();
				}else {
					log.warn("generateReport Error ");
				}

			}

		} catch (Exception e) {
			log.error("Exception Occured in generateReport load in supplier_GST_detailBean ", e);
		}
	}
	
	public void viewBillDetails(SupplierGST_Detail_DTO dto) {
		log.info(":::<-   SupplierGSTDetailBean >> viewBillDetails ->::: ");
		BaseDTO baseDTO=new BaseDTO();
		try {
			if(dto!=null&& dto.getGrnhpk()!=null) {
				String url = SERVER_URL + "/SupplierGSTdetailcontroller/viewbill";
				log.info("::: DailyTransactionReportBean >>  viewBillDetails URL :::", url);
				baseDTO = httpService.post(url,dto);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					viewbilldetaillist = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					viewbilldetaillist = mapper.readValue(jsonResponse, new TypeReference<List<SupplierGST_Detail_DTO>>() {
					});
					log.info("supplierGSTdetailBean >>  viewBillDetails Method  Succes -->");
				} else {
					AppUtil.addInfo("No Record Found");
					log.info("SalesReturnBean >>  viewBillDetails Method  >> No Record Found -->");
				}
			}
		}catch (Exception e) {
			log.error("Exception Occured in SupplierGSTDetailBean >>  viewBillDetails Method ", e);
		}
		
		
	}

	public void cleardata() {
		suppliergstdetsildto = new SupplierGST_Detail_DTO();
		listsuppliergstdetsildto=null;
		supplierMasterList=null;
		
	}
}
