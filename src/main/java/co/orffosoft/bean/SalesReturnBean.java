package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.DailyTransactionReportDTO;
import co.orffosoft.dto.SalesReturnDTO;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("salesReturnBean")
@Scope("session")
@Data
public class SalesReturnBean {

	/**
	* 
	*/
	private static final long serialVersionUID = -3927409251841374148L;

	private final String SALES_RETURN = "/pages/sales/salesReturn.xhtml?faces-redirect=true;";

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

	SalesReturnDTO salesReturnDTO;

	List<SalesReturnDTO> salesReturnDTOList;

	List<SalesReturnDTO> viewBillDetailsList;
	
	@Getter
	@Setter
	Double returnedamt;
	
	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	/**
	 * @return
	 */
	public String loadListPage() {

		salesReturnDTO = new SalesReturnDTO();
		salesReturnDTOList = new ArrayList<>();
		viewBillDetailsList = new ArrayList<>();
		returnedamt=0.0;
		return SALES_RETURN;
	}

	/**
	 * 
	 */
	public void generateReport() {

		log.info(":::<-   SalesReturnBean >> generateReport ->::: ");
		BaseDTO baseDTO = null;
		returnedamt = 0D;
		try {
			boolean valid = false;
			if (salesReturnDTO != null) {
				
				if (salesReturnDTO.getFromDate() != null || salesReturnDTO.getToDate() != null) {
					valid = true;
					if (salesReturnDTO.getFromDate() == null) {
						AppUtil.addWarn("Please Select From Date");
						return;
					} else if (salesReturnDTO.getToDate() == null) {
						AppUtil.addWarn("Please Select To Date");
						return;
					}
				}
				if (!(salesReturnDTO.getBillNo().isEmpty())) {
					valid = true;
				} if (!(salesReturnDTO.getCustomerName().isEmpty())) {
					valid = true;
				} if (salesReturnDTO.getCustomerMobileNo() != null) {
					valid = true;
				}
				if (!valid) {
					AppUtil.addWarn("Enter At Least One Field");
					return;
				}

				salesReturnDTO.setUserId(loginBean.getUserDetailSession().getId());
				salesReturnDTO.setStoreCode(loginBean.getEntityMaster().getCode());
				salesReturnDTO.setStoreName(loginBean.getEntityMaster().getName());

				String url = SERVER_URL + "/salesReturnController/generateReport";
				log.info("::: SalesReturnBean >>  generateReport URL :::", url);
				baseDTO = httpService.post(url, salesReturnDTO);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					salesReturnDTOList = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					salesReturnDTOList = mapper.readValue(jsonResponse, new TypeReference<List<SalesReturnDTO>>() {
					});
					log.info("SalesReturnBean >>  generateReport Method  Succes -->");
				} else {
					AppUtil.addWarn("No Record Found");
					log.info("SalesReturnBean >>  generateReport Method  >> No Record Found -->");
				}
			}
		} catch (Exception e) {
			log.error("Exception Occured in SalesReturnBean >>  generateReport Method ", e);
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
	public void removeItem(DailyTransactionReportDTO item) {

	}

	public void salesReturnProcess(SalesReturnDTO dto) {

	}

	public void viewBillDetails(SalesReturnDTO dto) {
		try {
			if (dto.getBill_h_pk() != null) {
				returnedamt = 0D;
				String url = SERVER_URL + "/salesReturnController/getbilldetailsbyid";
				log.info("::: SalesReturnBean >>  viewBillDetails URL :::", url);
				dto.setUserId(loginBean.getUserDetailSession().getId());
				dto.setStoreCode(loginBean.getEntityMaster().getCode());
				dto.setStoreName(loginBean.getEntityMaster().getName());

				BaseDTO baseDTO = httpService.post(url, dto);
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					viewBillDetailsList = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					viewBillDetailsList = mapper.readValue(jsonResponse, new TypeReference<List<SalesReturnDTO>>() {
					});
					log.info("SalesReturnBean >>  viewBillDetails Method  Succes -->");
				} else {
					AppUtil.addWarn("No Record Found");
					log.info("SalesReturnBean >>  viewBillDetails Method  >> No Record Found -->");
				}
			}
		} catch (Exception e) {
			log.error("Exception Occured In Sales Returnbean ", e);
		}
		log.info(" === >>>>> End Of SalesReturn Bean ViewBillDetails === >>>>>");

	}

	public void submit() {

		try {

			if (salesReturnDTOList != null && salesReturnDTOList.size() > 0) {
				salesReturnDTO.setSalesReturnDTOList(viewBillDetailsList);
				String url = SERVER_URL + "/salesReturnController/submitsalesreturn";
				log.info("::: SalesReturnBean >>  generateReport URL :::", url);
				BaseDTO baseDTO = httpService.post(url, salesReturnDTO);
				log.info("::: SalesReturnBean >>  generateReport URL :::", baseDTO.getStatusCode());
				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					salesReturnDTOList = new ArrayList<>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					salesReturnDTOList = mapper.readValue(jsonResponse, new TypeReference<List<SalesReturnDTO>>() {
					});
					AppUtil.addInfo("Successfully Retuned");
					RequestContext.getCurrentInstance().execute("PF('viewBillDetails').hide();");
					log.info("SalesReturnBean >>  generateReport Method  Succes -->");
				} else {
					AppUtil.addWarn(baseDTO.getMessage());
					log.info("SalesReturnBean >>  generateReport Method  >> No Record Found -->");
				}

			}
			generateReport();

		} catch (Exception e) {
			log.error("Exception Occured in SalesReturnBean", e);
		}

	}

	public void updateReturnQnty(SalesReturnDTO dto) {

		if (dto != null) {
			if (!(dto.getReturnedQnty() <= dto.getQnty())) {
				AppUtil.addWarn("Return Qnty More Then Purchased Qnty");
				dto.setReturnedQnty(0.0);
			} else if (dto.getReturnedQnty() > 0) {
				if (dto.getDiscount() == null) {
					dto.setDiscount(0D);
				}
				if (dto.getDiscountPercentage() == null) {
					dto.setDiscountPercentage(0D);
				}
				Double totalUnitPrice = dto.getReturnedQnty() * dto.getUnitPrice();
				Double discountAmount = totalUnitPrice * dto.getDiscountPercentage() / 100;
				totalUnitPrice = totalUnitPrice - discountAmount;
				if (dto.getCgstPercent() == null) {
					dto.setReturnedCgstPercent(0D);
				}
				if (dto.getSgstPercent() == null) {
					dto.setReturnedSgstPercent(0D);
				}
				Double cgstAmount = totalUnitPrice * dto.getCgstPercent() / 100;
				Double sgstAmount = totalUnitPrice * dto.getSgstPercent() / 100;
				dto.setReturnedCgstAmount(Double.valueOf(commonDataBean.formatDecimalNumber(cgstAmount)));
				dto.setReturnedSgstAmount(Double.valueOf(commonDataBean.formatDecimalNumber(sgstAmount)));
				Double returnedAmount = Double.valueOf(commonDataBean.formatDecimalNumber(totalUnitPrice + dto.getReturnedCgstAmount() + dto.getReturnedSgstAmount()));
				dto.setReturnedAmount(returnedAmount);
				returnedamt=returnedAmount;
			}
			salesReturnDTO.setBill_d_pk(dto.getBill_d_pk());
			salesReturnDTO.setBill_h_pk(dto.getBill_h_pk());
		}
	}
	
	public void allItemReturn() {
		log.info("SalesReturnBean >>  allItemReturn Method  >> START-->");
		
		if(!viewBillDetailsList.isEmpty() && viewBillDetailsList!=null) {
			for(SalesReturnDTO dto: viewBillDetailsList) {
				dto.setReturnedQnty(dto.getQnty());
			}
			submit();
		}
		
	}

}
