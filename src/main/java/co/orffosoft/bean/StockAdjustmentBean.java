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
import co.orffosoft.dto.DailyTransactionReportDTO;
import co.orffosoft.dto.StockAdjustmentDTO;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("stockAdjustmentBean")
@Scope("session")
@Data
public class StockAdjustmentBean implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -3927409251841374148L;

	private final String DAILY_TRANSACTION = "/pages/billing/billing_report/DailyTransactionReport.xhtml?faces-redirect=true;";
	private final String STOCK_ADJUSTMENT = "/pages/sales/StockADJM.xhtml?faces-redirect=true;";

	@Autowired
	HttpService httpService;
	
	@Getter
	@Setter
	boolean chngeprice;

	@Autowired
	AppPreference appPreference;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	LoginBean loginBean;

	@Autowired
	CommonDataBean commonDataBean;

	DailyTransactionReportDTO dailyTransactionReportDTO;

	List<DailyTransactionReportDTO> dailyTransactionReportDTOList;
	
	@Getter
	@Setter
	List<ProductVarietyMaster> productVarietyMasterList;
	
	ProductVarietyMaster pvm =new ProductVarietyMaster();
	
	
	@Getter
	@Setter
	StockAdjustmentDTO stockAdjustmentDTO;
	
	List<StockAdjustmentDTO> stockAdjustmentDTOlist;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	/**
	 * @return
	 */
	public String loadListPage() {

		stockAdjustmentDTO = new StockAdjustmentDTO();
		stockAdjustmentDTOlist = new ArrayList<>();
		pvm =new ProductVarietyMaster();
		chngeprice=false;
		return STOCK_ADJUSTMENT;
	}

	public List<ProductVarietyMaster> itemAutocomplete(String itemName) {

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
	
	public void generateReport()  {
		
		BaseDTO baseDTO = null;
	   try {
			if(pvm != null) {
				if (pvm.getName() == null) {
					AppUtil.addWarn("Please Select ItemName");
					return;
				}
				pvm.setUserId(loginBean.getUserDetailSession().getId());
				stockAdjustmentDTO.setStoreCode(loginBean.getEntityMaster().getCode());
				stockAdjustmentDTO.setStoreName(loginBean.getEntityMaster().getName());
				
				String url = SERVER_URL + "/stockAdjustmentController/generateReport";
				baseDTO = httpService.post(url, pvm);
				
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					stockAdjustmentDTOlist = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					stockAdjustmentDTOlist = mapper.readValue(jsonResponse, new TypeReference<List<StockAdjustmentDTO>>() {
					});
					log.info("SalesReturnBean >>  generateReport Method  Succes -->");
				} else {
					AppUtil.addInfo("No Record Found");
					log.info("stockAdjustmentbean >>  generateReport1 Method  >> No Record Found -->");
				}
			}
		   
	   }catch(Exception e) {
			log.error("Exception Occured in StockAdjustmentBean >>  generateReport Method ", e);
		}
		
	}
	
	public void generatStockAdjustment()  {
		BaseDTO baseDTO = null;
		try {
			if(stockAdjustmentDTOlist != null) {
				for(StockAdjustmentDTO dto : stockAdjustmentDTOlist) {
					if (dto.getAction() == null || dto.getAction().isEmpty()) {
						AppUtil.addWarn("Please Select Action ");
						return;
					} else if (dto.getActionQty() == null && !dto.getAction().equals("CHANGE_ONLY_PRICE")) {
						AppUtil.addWarn("Please Enter Quantity ");
						return;
					}
					if(dto.isChngeprice()) {
						if(dto.getSellingprice()==null && dto.getSellingprice() == 0.0) {
							AppUtil.addWarn("Please Enter Sellingprice ");
							return;
						}
						if(dto.getPurchesprice()==null && dto.getPurchesprice()==0.0) {
							AppUtil.addWarn("Please Enter Purchesprice ");
							return;
						}
					}
					Double g=dto.getRateWiseQnty1().get(dto.getUnitRate());
					if (  dto.getActionQty() > dto.getRateWiseQnty1().get(dto.getUnitRate())) {
						AppUtil.addWarn("Please Enter Lessthen from item Qnty ");
						return;
					}
					
					stockAdjustmentDTO.setName(dto.getName());
					stockAdjustmentDTO.setItemId(dto.getItemId());
					stockAdjustmentDTO.setItemQty(dto.getItemQty());
					stockAdjustmentDTO.setAction(dto.getAction());
					stockAdjustmentDTO.setActionQty(dto.getActionQty());
					stockAdjustmentDTO.setUnitRate(dto.getUnitRate());
					stockAdjustmentDTO.setSellingPrices(dto.getSellingPrices());
					stockAdjustmentDTO.setChngeprice(dto.isChngeprice());
					stockAdjustmentDTO.setSellingprice(dto.getSellingprice());
					stockAdjustmentDTO.setPurchesprice(dto.getPurchesprice());
					stockAdjustmentDTO.setSgstamount(dto.getSgstamount());
					stockAdjustmentDTO.setCgstamount(dto.getCgstamount());
					
				}
				stockAdjustmentDTO.setUserId(loginBean.getUserDetailSession().getId());
				stockAdjustmentDTO.setStoreCode(loginBean.getEntityMaster().getCode());
				stockAdjustmentDTO.setStoreName(loginBean.getEntityMaster().getName());
				stockAdjustmentDTO.setUomName(pvm.getUomMaster().getName());
			}
			String url = SERVER_URL + "/stockAdjustmentController/generatStockAdjustment";
			baseDTO = httpService.post(url, stockAdjustmentDTO);
			log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
			if (baseDTO != null) {
				if (baseDTO.getStatusCode() != null && baseDTO.getStatusCode() == 0) {
						log.info("stockAdjustment updated successfully");
						AppUtil.addInfo(" selected "+stockAdjustmentDTO.getName()+" Updated successfully");
						generateReport();
						
				} else {
					AppUtil.addWarn(baseDTO.getMessage());
					
				}
				

			}
			
		}catch(Exception e){
			log.error("Exception Occured in StockAdjustmentBean  >>  generatStockAdjustmen Method ", e);
		}
		
	}
	
	public StockAdjustmentDTO updateAction(StockAdjustmentDTO dto) {
		
        if(dto!=null) {
        	dto.setChngeprice(chngeprice);
        }
		return dto;
		
		
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
	public void removeItem(DailyTransactionReportDTO item) {

	}

}
