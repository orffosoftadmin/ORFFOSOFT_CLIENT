package co.orffosoft.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.Constants;
import co.orffosoft.core.util.ErrorDescription;
import co.orffosoft.core.util.ExcelUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.PaginationDTO;
import co.orffosoft.dto.ProductVarietyMasterDTO;
import co.orffosoft.dto.ProductVariryMastersearchDTO;
import co.orffosoft.dto.ProductVarityMasterResponse;
import co.orffosoft.dto.UserDTO;
import co.orffosoft.dto.WrapperDTO;
import co.orffosoft.entity.UomMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Pratap Kumar Sahu
 *
 */
@Log4j2
@Service("productVarietyMasterBean")
@Scope("session")
public class ProductVarietyMasterBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	String statusValue;
	
	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();


	@Autowired
	LoginBean loginBean;

	@Autowired
	LanguageBean languageBean;

	/** for client server connection */
	RestTemplate restTemplate;

	@Getter
	@Setter
	String action = null;

	@Getter
	@Setter
	ProductVarietyMasterDTO productVarietyMasterDTO = new ProductVarietyMasterDTO();

	@Getter
	@Setter
	List<ProductVarietyMasterDTO> productVarietyMasterDTOList;

	@Getter
	@Setter
	boolean visible;

	String jsonResponse;
	ObjectMapper mapper = new ObjectMapper();

	// new code properties
	@Getter
	@Setter
	LazyDataModel<ProductVarietyMasterDTO> productVarietyMasterLazyDTOList;

	@Getter
	@Setter
	LazyDataModel<ProductVarityMasterResponse> productVarietyResponseLazyDTOList;

	@Getter
	@Setter
	List<ProductVarityMasterResponse> productVarietyResponseDTOList;

	@Getter
	@Setter
	boolean addButtonFlag = false;

	@Getter
	@Setter
	Integer planSize;

	@Autowired
	HttpService httpService;

	@Autowired
	ErrorMap errorMap;

	BaseDTO baseDTO = new BaseDTO();

	@Getter
	@Setter
	ProductVarityMasterResponse productVarityMasterResponse = new ProductVarityMasterResponse();

	@Getter
	@Setter
	List<UomMaster> uomMasterList;

	@Getter
	@Setter
	UomMaster uomMaster;

	@Getter
	@Setter
	String pageHead = "Create";

	@Getter
	@Setter
	String status;
	
	@Getter
	@Setter
	List<Map<String, Object>> listMapForExcel = new ArrayList<>();
	
	ExcelUtil excelUtil;
	
	private String excelFileUploadPath =  "/DATA/PROGRAMS/ORFFOSOFT_RETAIL/ITEM_MASTER/UPLOADED/ProductVarietyMaster.xlsx";
	
	@Getter
	@Setter
	StreamedContent file;

	private final String LIST_PRODUCT_VARITY_MASTER = "/pages/masters/listProductVariety.xhtml?faces-redirect=true;";
	private final String CREATE_PRODUCT_VARITY_MASTER = "/pages/masters/createProductVariety.xhtml?faces-redirect=true;";
	private final String VIEW_PRODUCT_VARITY_MASTER = "/pages/masters/viewProductVariety.xhtml?faces-redirect=true;";
	
	ProductVarietyMasterBean() {
		productVarietyMasterDTO = new ProductVarietyMasterDTO();
	}
	
	public void getData(){
		try {
			excelUtil=new ExcelUtil();
			if(listMapForExcel!=null) {
				excelUtil.buildExcel(excelFileUploadPath, Constants.PRODUCT_VARIETY_MASTER, listMapForExcel);
			}
			file = excelUtil.getDownloadFile(excelFileUploadPath);
			
		}catch (Exception e) {
			AppUtil.addWarn("Download Failed");
         e.printStackTrace();			
		}
		
	}

	public String submitForm() {

		log.info("--submitForm---");
		try {
			if (action.equalsIgnoreCase("ADD")) {
				log.info("<<======= Action::" + action);
				visible = false;

				UserDTO userDto = new UserDTO();
				userDto.setId(loginBean.getUserDetailSession().getId());
				productVarietyMasterDTO.setCreatedBy(userDto);
				productVarietyMasterDTO.setCreatedDate(new Date());

				String url = serverURL + "/itemmaster/save";

				baseDTO = httpService.post(url, productVarietyMasterDTO);

				log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
				if (baseDTO != null) {

					if (baseDTO.getStatusCode() == 1) {
						AppUtil.addError("Item Code Already Present");
						return CREATE_PRODUCT_VARITY_MASTER;
					} else if (baseDTO.getStatusCode() == 2) {
						AppUtil.addError("Item Name Allready Present");
						return CREATE_PRODUCT_VARITY_MASTER;
					} else {
						log.info("Product Variety Master has been Added");
						AppUtil.addInfo(" Item Saved Successfuly , You Can See items In Main Page");
						productVarietyMasterDTO = new ProductVarietyMasterDTO();
						//showList();
						return CREATE_PRODUCT_VARITY_MASTER;
					}
				}

			} else if (action.equalsIgnoreCase("EDIT")) {
				log.info("action EDIT");
				visible = true;

				productVarietyMasterDTO.setCode(productVarietyMasterDTO.getCode().trim());
				productVarietyMasterDTO.setName(productVarietyMasterDTO.getName().trim());
				productVarietyMasterDTO.setUserMaster(loginBean.getUserDetailSession());
				productVarietyMasterDTO.setModifiedDate(productVarityMasterResponse.getModifyDate());
				if (productVarietyMasterDTO != null) {
					String url = serverURL + "/itemmaster/update";
					baseDTO = httpService.post(url, productVarietyMasterDTO);
					log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
					if (baseDTO != null) {
						if (baseDTO.getStatusCode() != null && baseDTO.getStatusCode() == 0) {
								log.info("ItemMasterBean updated successfully");
								AppUtil.addInfo("Item Updated successfully");
								showList();
								return LIST_PRODUCT_VARITY_MASTER;
						} else {
							AppUtil.addWarn(baseDTO.getMessage());
							return CREATE_PRODUCT_VARITY_MASTER;
						}
						

					}
				}
			}

		} catch (Exception e) {
			log.error(" == Exception Occured In save method of ProductVariety Master Bean class ", e);
		}
		return null;
	}


	public String getVarityList() {
		productVarietyMasterDTO = new ProductVarietyMasterDTO();
		log.info("<--- Inside getvarityMasterList() ---> ");
		addButtonFlag = false;
		loadRetailProductvarityDTOLazy();
		log.info("<--- Inside getvarityMasterList()  completed ---> ");
		return LIST_PRODUCT_VARITY_MASTER;
	}

	public String getProductVarietyList() {
		log.info("-getProductVarietyList-----");
		productVarietyMasterDTOList = new ArrayList<ProductVarietyMasterDTO>();
		WrapperDTO WrapperDto = new WrapperDTO();
		RestTemplate restTemplate = new RestTemplate();
		String url = serverURL + "/itemmaster/getall";
		try {

			HttpEntity<WrapperDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, loginBean.requestEntity,
					WrapperDTO.class);
			WrapperDto = responseEntity.getBody();
			if (WrapperDto != null) {
				if (WrapperDto.getProductVarietyMasterDtoCollection() != null) {

					productVarietyMasterDTOList = (List<ProductVarietyMasterDTO>) WrapperDto
							.getProductVarietyMasterDtoCollection();

					if (productVarietyMasterDTOList == null || productVarietyMasterDTOList.size() < 0) {

						productVarietyMasterDTOList = new ArrayList<ProductVarietyMasterDTO>();
					}
					log.info("No of productVarietyMasterDTOList :" + productVarietyMasterDTOList.size());
				} else {
					productVarietyMasterDTOList = new ArrayList<ProductVarietyMasterDTO>();
					String msg = WrapperDto.getErrorDescription();
					log.error("Status code:" + WrapperDto.getStatusCode() + " Error Message: " + msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		productVarietyMasterDTO = new ProductVarietyMasterDTO();

		return "/pages/master/itemmasterMasterList.xhtml?faces-redirect=true;";
	}

	public String showEditForm() {
		if (productVarityMasterResponse == null || productVarityMasterResponse.getVarityId() == null) {
			AppUtil.addWarn("Please Select At Least One Record");
			return null;
		}
		try {
			log.info("<<====  variety Id:: " + productVarityMasterResponse.getVarityId());
			productVarietyMasterDTO.setId(productVarityMasterResponse.getVarityId());
			log.info("<<=====   selected Dto == " + productVarietyMasterDTO.getId());
			getUOM();
			BaseDTO baseDTO = new BaseDTO();
			String url = serverURL + "/itemmaster/getById";
			log.info("<<==  URL:: " + url);
			log.info("Headers:: " + loginBean.headers);
			baseDTO = httpService.post(url, productVarietyMasterDTO);

			if (baseDTO != null && baseDTO.getResponseContent() != null && baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
				productVarietyMasterDTO = mapper.readValue(jsonResponse, ProductVarietyMasterDTO.class);
			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return CREATE_PRODUCT_VARITY_MASTER;
	}

	public String showViewForm() {

		if (productVarityMasterResponse == null || productVarityMasterResponse.getVarityId() == null) {
			AppUtil.addWarn("Please Select At Least One Record");
			return null;
		}
		try {
			productVarietyMasterDTO = new ProductVarietyMasterDTO();
			log.info("<<====  variety Id:: " + productVarityMasterResponse.getVarityId());
			productVarietyMasterDTO.setId(productVarityMasterResponse.getVarityId());
			log.info("<<=====   selected Dto == " + productVarietyMasterDTO.getId());
			getUOM();
			BaseDTO baseDTO = new BaseDTO();
			String url = serverURL + "/itemmaster/getById";
			log.info("<<==  URL:: " + url);
			log.info("Headers:: " + loginBean.headers);
			baseDTO = httpService.post(url, productVarietyMasterDTO);
			log.info("BaseDTO Dto :" + baseDTO.getResponseContent());
			ObjectMapper mapper = new ObjectMapper();
			String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
			productVarietyMasterDTO = mapper.readValue(jsonResponse, ProductVarietyMasterDTO.class);

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return VIEW_PRODUCT_VARITY_MASTER;
	}

	public void clear() {
		log.info("--productVarietycMaster   clear--");
		productVarietyMasterDTO = new ProductVarietyMasterDTO();
		productVarityMasterResponse = new ProductVarityMasterResponse();
		statusValue = null;
		visible = false;
	}

	public String showList() {
		log.info("<--- Loading supplierMasterBean showList list page --->");
		loadRetailProductvarityDTOLazy();
		return LIST_PRODUCT_VARITY_MASTER;
	}

	/**
	 * This is for product Variety master search and get service
	 */
	public void loadRetailProductvarityDTOLazy() {
		log.info("Lazy load == product varity master  ==>>");
		productVarietyResponseLazyDTOList = new LazyDataModel<ProductVarityMasterResponse>() {

			private static final long serialVersionUID = 2784959485860775580L;

			@Override
			public List<ProductVarityMasterResponse> load(int first, int pageSize, String sortField,
					SortOrder sortOrder, Map<String, Object> filters) {

				List<ProductVarityMasterResponse> data = null;
				try {
					BaseDTO baseDTO = getSearchData(first / pageSize, pageSize, sortField, sortOrder, filters);

					ObjectMapper objectMapper = new ObjectMapper();
					if (baseDTO != null) {
						String jsonResponse = objectMapper.writeValueAsString(baseDTO.getResponseContents());
						data = objectMapper.readValue(jsonResponse,
								new TypeReference<List<ProductVarityMasterResponse>>() {
								});
					}
					if (data != null) {
						this.setRowCount(baseDTO.getTotalRecords());
						planSize = baseDTO.getTotalRecords();

					}
				} catch (Exception e) {
					log.error("Error ", e);
				}

				return data;

			}

			@Override
			public Object getRowKey(ProductVarityMasterResponse res) {
				log.info("Get Row Key called" + res);
				return res != null ? res.getVarityId() : null;
			}

			@Override
			public ProductVarityMasterResponse getRowData(String rowKey) {
				log.info("Get Row Data called" + rowKey);
				List<ProductVarityMasterResponse> responseList = (List<ProductVarityMasterResponse>) getWrappedData();
				Long value = Long.valueOf(rowKey);

				for (ProductVarityMasterResponse res : responseList) {
					if (res.getVarityId().longValue() == value.longValue()) {
						log.info("Returning row data " + res);
						return res;
					}
				}
				log.info("Returning null row data ");
				return null;
			}

		};

	}

	public BaseDTO getSearchData(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) throws ParseException {
		log.info("<---Inside search called--->");

		log.info("First [" + first + "] pageSize [" + pageSize + "] sortOrder [" + sortOrder + "] sortField ["
				+ sortField + "]");

		productVarityMasterResponse = new ProductVarityMasterResponse();

		BaseDTO baseDTO = new BaseDTO();

		ProductVariryMastersearchDTO request = new ProductVariryMastersearchDTO();
		PaginationDTO paginationDTO = new PaginationDTO(first, pageSize, sortField, sortOrder.toString());
		request.setPaginationDTO(paginationDTO);

		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			String value = entry.getValue().toString();
			String key = entry.getKey();

			log.info("Key : " + key + " Value : " + value);

			if (key.equals("varityCodeOrName")) {
				request.setVarityCodeOrName(value);
			} else if (key.equals("activeStatus")) {
				request.setActiveStatus(value.equals("true") ? true : false);
			} else if (key.equals("createdDate")) {
				request.setCreatedDate(AppUtil.serverDateFormat(value));
			} else if (key.equals("modifiedDate")) {
				request.setModifyDate(AppUtil.serverDateFormat(value));
			}
		}

		try {
			String url = serverURL + "/itemmaster/searchvaritys";
			log.info("Before Search Call WITH URI >>>"+url);
			baseDTO = httpService.post(url, request);
			log.info("After Post call on "+url);
			log.info("Response ><> "+baseDTO);
			
			// For Excel Sheet Download
			listMapForExcel = new ArrayList<>();
			String jsonResponseForExcel = mapper.writeValueAsString(baseDTO.getListOfData());
			listMapForExcel = mapper.readValue(jsonResponseForExcel,
					new TypeReference<List<Map<String, Object>>>() {
					});
			
			
		} catch (Exception e) {
			log.error("Exception ", e);
			baseDTO.setStatusCode(ErrorDescription.FAILURE_RESPONSE.getCode());
		}
		return baseDTO;

	}
	
	public void excelDownload() {

		
	}


	public void onRowSelect(SelectEvent event) {
		log.info("Product Varity Master onRowSelect method started");
		productVarityMasterResponse = ((ProductVarityMasterResponse) event.getObject());
		addButtonFlag = true;
	}

	public void processDelete() {
		log.info("<---Inside processDelete()--->");
		if (productVarityMasterResponse == null) {
			log.info("<---if(productVarityMasterResponse == null)--->");
			RequestContext.getCurrentInstance().execute("PF('dlg1').hide();");
			log.info("Please Select atleast One product variety");
			errorMap.notify(ErrorDescription.SELECT_ATLEAST_ONE_RECORD.getCode());
			return;
		} else {
			log.info("<---else(productVarityMasterResponse == null)--->");
			RequestContext.getCurrentInstance().execute("PF('dlg1').show();");
		}
	}

	public String confirmDelete() {
		log.info("<---Inside deleteAction called with varity Id---> " + productVarityMasterResponse.getVarityId());
		String url = serverURL + "/itemmaster/delete/" + productVarityMasterResponse.getVarityId();

		BaseDTO baseDTO = httpService.delete(url);

		if (baseDTO != null) {
			if (baseDTO.getStatusCode() == 0) {
				log.info("<--- product variety has been Deleted Successfully ---> ");
				// errorMap.notify(ErrorDescription.INFO_PRODUCT_VARIETY_MASTER_DELETED.getCode());
				log.info("<---else(productVarityMasterResponse == null)--->");
				AppUtil.addInfo(" Item Deleted Sucessfully ");
			} else {
				String msg = baseDTO.getErrorDescription();
				errorMap.notify(baseDTO.getStatusCode());
				log.error("Status code:" + baseDTO.getStatusCode() + " Error Message: " + msg);
				return null;
			}
		} else {
			log.info("<--- Internal Server Error ---> ");
			errorMap.notify(ErrorDescription.INTERNAL_ERROR.getCode());
		}
		return LIST_PRODUCT_VARITY_MASTER;
	}

	public String itemAddPage() throws Exception {
		productVarietyMasterDTO = new ProductVarietyMasterDTO();
		getUOM();
		pageHead = "Create";
		log.info("Inside get UOMS" + productVarietyMasterDTO);
		return CREATE_PRODUCT_VARITY_MASTER;
	}

	@SuppressWarnings("unchecked")
	public void getUOM() {
		log.info("Inside get UOMS");
		String url = serverURL + "/itemmaster/getuoms";

		try {
			BaseDTO baseDTO = httpService.get(url);

			if (baseDTO != null) {
				ObjectMapper mapper = new ObjectMapper();
				if (baseDTO.getResponseContent() != null) {
					String jsonValue = mapper.writeValueAsString(baseDTO.getResponseContent());
					uomMasterList = (List<UomMaster>) mapper.readValue(jsonValue, new TypeReference<List<UomMaster>>() {
					});
					if (uomMasterList != null) {
						log.info("RetailProductionPlanList with Size of : " + uomMasterList.size());
					} else {
						errorMap.notify(ErrorDescription.ERROR_EMPTY_LIST.getCode());
						log.info(" RetailProductionPlanList is Empty ");
					}
				}

			}

		} catch (Exception e) {
			log.error("exception in getUOM", e);
		}

	}

	public String updateItemStatus() {
		log.info("Status In-Active called......");
		BaseDTO baseDTO = new BaseDTO();
		try {

			if (status.equals("Active")) {
				if (productVarityMasterResponse != null) {
					if (productVarityMasterResponse.getActiveStatus() == false) {
						String url = serverURL + "/itemmaster/updateItemStatus/"
								+ productVarityMasterResponse.getVarityId() + "/" + status;
						baseDTO = httpService.get(url);
						log.info("BaseDTO Dto :" + baseDTO.getResponseContent());
						if (baseDTO != null) {
							AppUtil.addInfo("Item Status Activated Successfully");
							loadRetailProductvarityDTOLazy();
							return LIST_PRODUCT_VARITY_MASTER;
						}
					} else {
						AppUtil.addInfo("Item Status Already Activated");
						return LIST_PRODUCT_VARITY_MASTER;
					}
				} else {
					AppUtil.addWarn(" Please Select A Record To Active");
					return LIST_PRODUCT_VARITY_MASTER;
				}

			} // else if close
			else if (status.equals("In-Active")) {
				if (productVarityMasterResponse != null) {
					if (productVarityMasterResponse.getActiveStatus() == true) {
						String url = serverURL + "/itemmaster/updateItemStatus/"
								+ productVarityMasterResponse.getVarityId() + "/" + status;
						baseDTO = httpService.get(url);
						log.info("BaseDTO Dto :" + baseDTO.getResponseContent());
						if (baseDTO != null) {
							AppUtil.addInfo("Item Status In-Activated Successfully");
							loadRetailProductvarityDTOLazy();
							return LIST_PRODUCT_VARITY_MASTER;
						}
					} else {
						AppUtil.addInfo("Item Status Already In-Activated");
						return LIST_PRODUCT_VARITY_MASTER;
					}
				} else {
					AppUtil.addWarn(" Please Select A Record To In-Active");
					return LIST_PRODUCT_VARITY_MASTER;
				}
			} // else if close

			log.info("Status In-Active End......");
		} catch (Exception e) {
			log.error("<<<=======  Error in statusStockUpdate =====>>" + e);
		}
		return null;

	}

}
