package co.orffosoft.bean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppPreference;
import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.ErrorDescription;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.CustomerMasterDTO;
import co.orffosoft.dto.PaginationDTO;
import co.orffosoft.dto.ProductVariryMastersearchDTO;
import co.orffosoft.dto.ProductVarityMasterResponse;
import co.orffosoft.entity.AddressMaster;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.SupplierMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service("customerMasterBean")
@Log4j2
@Scope("session")
public class CustomerMasterBean {
	/**
	 * 
	 */
	// private static final long serialVersionUID = 654646L;
	// private static final String LIST_PAGE =
	// "/pages/masters/listCustomerMaster.xhtml?faces-redirect=true";
	// private static final String ADD_PAGE =
	// "/pages/masters/createCustomerMaster.xhtml?faces-redirect=true";
	// private static final String VIEW_PAGE =
	// "/pages/masters/viewCustomerMaster.xhtml?faces-redirect=true";

	private static final String LIST_PAGE = "/pages/masters/CustomerMaster.xhtml?faces-redirect=true";
	private static final String EDIT_PAGE = "/pages/masters/EditCustomerMaster.xhtml?faces-redirect=true";
	private static final String VIEW_PAGE = "/pages/masters/viewCustomerMaster.xhtml?faces-redirect=true";
	private static final String ADD_PAGE = "/pages/masters/addnewCustomer.xhtml?faces-redirect=true";

	@Getter
	@Setter
	private CustomerMasterDTO customerMasterdto;

	@Getter
	@Setter
	private CustomerMaster customerMaster;

	@Getter
	@Setter
	LazyDataModel<CustomerMaster> customerMasterList;

	@Autowired
	AppPreference appPreference;

	@Getter
	@Setter
	private Long talukMasterId;

	@Getter
	@Setter
	private AddressMaster customerAddress;

	@Getter
	@Setter
	private AddressMaster billingAddress;

	@Getter
	@Setter
	private AddressMaster shippingAddress;

	@Getter
	@Setter
	private String addressType, action, ifsc;

	@Getter
	@Setter
	List<CustomerMasterDTO> parentCustomerList = new ArrayList<CustomerMasterDTO>();

	@Getter
	@Setter
	Integer totalRecords;

	@Getter
	@Setter
	private String officeAddressTextArea, billingAddressTextArea, shippingAddressTextArea;

	@Getter
	@Setter
	private Boolean addButtonFlag = true;

	@Getter
	@Setter
	int size;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	HttpService httpService;

	ObjectMapper mapper = new ObjectMapper();

	String jsonResponse;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	BaseDTO baseDTO;

	@Autowired
	CommonDataBean commonDateBean;

	@Getter
	@Setter
	Boolean countryflag = false;
	@Getter
	@Setter
	String customerCode;

	public CustomerMasterBean() {
		customerMasterdto = new CustomerMasterDTO();
		baseDTO = new BaseDTO();
		// loadValues();
	}
	
	public String clear() {
		
		customerMaster = new CustomerMaster();
		return LIST_PAGE;
	}

	public String showList() {
		addButtonFlag = true;
		customerMaster = new CustomerMaster();
		size = 0;
		loadRetailProductvarityDTOLazy();
		return LIST_PAGE;
	}

	public String editpage() {
		try {
			if (customerMaster == null || customerMaster.getId() == null) {
				AppUtil.addWarn("Please Select At Least One Record");
				return null;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return EDIT_PAGE;
	}

	public void loadRetailProductvarityDTOLazy() {
		log.info("<--- loadLazySupplierList ---> ");

		customerMasterList = new LazyDataModel<CustomerMaster>() {

			private static final long serialVersionUID = -1540942419672760421L;

			@Override
			public List<CustomerMaster> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				List<CustomerMaster> data = new ArrayList<CustomerMaster>();
				try {
					BaseDTO baseDTO = getSearchData(first / pageSize, pageSize, sortField, sortOrder, filters);

					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());

					data = mapper.readValue(jsonResponse, new TypeReference<List<CustomerMaster>>() {
					});

					if (data != null) {
						this.setRowCount(baseDTO.getTotalRecords());
						log.info("<--- List Count --->  " + baseDTO.getTotalRecords());
						size = baseDTO.getTotalRecords();
					}
				} catch (Exception e) {
					log.error("Error in loadLazySupplierList()  ", e);
				}
				return data;
			}

			@Override
			public Object getRowKey(CustomerMaster res) {
				return res != null ? res.getId() : null;
			}

			public CustomerMaster getRowData(String rowKey) {
				List<CustomerMaster> responseList = (List<CustomerMaster>) getWrappedData();
				// Long value = Long.parseLong(rowKey) ;
				Long value = Long.parseLong(rowKey);
				for (CustomerMaster res : responseList) {
					if (res.getId().longValue() == value.longValue()) {
						customerMaster = res;
						return res;
					}
				}
				return null;
			}

		};
	}

	public void onRowSelect(SelectEvent event) {
		log.info("FDS Plan onRowSelect method started");
		customerMaster = ((CustomerMaster) event.getObject());
		addButtonFlag = true;
	}

	public BaseDTO getSearchData(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) throws ParseException {
		log.info("<---Inside search called--->");

		log.info("First [" + first + "] pageSize [" + pageSize + "] sortOrder [" + sortOrder + "] sortField ["
				+ sortField + "]");

		customerMaster = new CustomerMaster();

		BaseDTO baseDTO = new BaseDTO();

		CustomerMaster request = new CustomerMaster();
		PaginationDTO paginationDTO = new PaginationDTO(first, pageSize, sortField, sortOrder.toString());
		request.setPaginationDTO(paginationDTO);
		request.setFilters(filters);

		try {
			String url = SERVER_URL + "/customerMasterController/getall";
			log.info("Before Search Call WITH URI >>>" + url);
			baseDTO = httpService.post(url, request);
			log.info("After Post call on " + url);
			log.info("Response ><> " + baseDTO);

		} catch (Exception e) {
			log.error("Exception ", e);
			baseDTO.setStatusCode(ErrorDescription.FAILURE_RESPONSE.getCode());
		}
		return baseDTO;

	}

//	public String update() {
//		BaseDTO basedto = new BaseDTO();
//		try {
//			if (customerMaster != null && customerMaster.getId() != null) {
//				String url = SERVER_URL + "/customerMasterController/update";
//				log.info("Before Search Call WITH URI >>>" + url);
//				baseDTO = httpService.post(url, customerMaster);
//				log.info("After Post call on " + url);
//				log.info("Response ><> " + baseDTO);
//				if (baseDTO != null && baseDTO.getStatusCode() == 0) {
//					AppUtil.addInfo("Update Successfully");
//					return showList();
//				} else {
//					log.warn(baseDTO.getMessage());
//					AppUtil.addWarn(baseDTO.getMessage());
//					return showList();
//				}
//
//			} else {
//				AppUtil.addWarn("Please Select At Least One Record");
//				return null;
//			}
//
//		} catch (Exception e) {
//			log.error("Exception ", e);
//		}
//		return null;
//
//	}

	public String viewpage() {
		BaseDTO basedto = new BaseDTO();
		try {
			if (customerMaster == null || customerMaster.getId() == null) {
				AppUtil.addWarn("Please Select At Least One Record");
				return null;

			} else {

				return VIEW_PAGE;
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		return null;
	}

	public String addpage() {
		customerMaster = new CustomerMaster();
		return ADD_PAGE;

	}

	public String addOrUpdatecustomer() {
		BaseDTO basedto = new BaseDTO();
		try {
			if (customerMaster != null) {
				if (customerMaster.getName() == null || customerMaster.getName().isEmpty()) {
					AppUtil.addWarn("Please Enter Customer Name");
					return null;
				} else {
					if (customerMaster.getPrimaryContactNumber() == null || customerMaster.getPrimaryContactNumber().isEmpty()) {
						AppUtil.addWarn("Please Enter Customer MobileNumber");
						return null;
					}
				}

				String url = SERVER_URL + "/customerMasterController/addorupdate";
				log.info("Before Search Call WITH URI >>>" + url);
				baseDTO = httpService.post(url, customerMaster);
				log.info("After Post call on " + url);
				log.info("Response ><> " + baseDTO);
				if (baseDTO != null && baseDTO.getStatusCode() == 0) {
					AppUtil.addInfo("Update Successfully");
					return showList();
				} else {
					log.warn(baseDTO.getMessage());
					AppUtil.addWarn(baseDTO.getMessage());
					return showList();
				}
				
			}
		} catch (Exception e) {
			log.error("Exception ", e);
		}
		return null;
	}
	
	public void chekmobilenumber() {
		try {
			String mobno=customerMaster.getPrimaryContactNumber();
			if(!mobno.trim().isEmpty()) {
				
				String url = SERVER_URL + "/customerMasterController/checkmobileno/"+mobno;
				log.info("Before Search Call WITH URI >>>" + url);
				baseDTO = httpService.get(url);
				log.info("After Search Call WITH URI >>>");
				if(baseDTO!=null && !baseDTO.getStatusCode().equals(0)) {
					AppUtil.addWarn("Mobile Number Alredy Exist");
					customerMaster.setPrimaryContactNumber(null);
				}
			}
			
		} catch (Exception e) {
			log.error("Exception ", e);
		}
	
	}

}
