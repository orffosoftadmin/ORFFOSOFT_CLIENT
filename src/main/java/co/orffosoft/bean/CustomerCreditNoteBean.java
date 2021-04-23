package co.orffosoft.bean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.jpa.criteria.expression.function.AbsFunction;
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
import co.orffosoft.dto.CustomerCreditNoteDTO;
import co.orffosoft.dto.PaginationDTO;
import co.orffosoft.dto.UserDTO;
import co.orffosoft.entity.CustomerCreditNoteDetail;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.entity.SupplierMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("customerCreditNoteBean")
@Scope("session")
@Data
public class CustomerCreditNoteBean {
	private final String LOAD_LIST_PAGE = "/pages/masters/listCreditNote.xhtml?faces-redirect=true;";

	private final String LOAD_ADD_PAGE = "/pages/masters/addCreditNote.xhtml?faces-redirect=true;";

	private final String LOAD_VIEW_PAGE = "/pages/masters/viewCreditNote.xhtml?faces-redirect=true;";
	
	private final String LOAD_Edit_PAGE = "/pages/masters/CustomerCreitNoteEdit.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();

	@Getter
	@Setter
	String action = null;

	@Autowired
	HttpService httpService;

	@Autowired
	AppPreference appPreference;

	@Autowired
	ErrorMap errorMap;

	@Autowired
	LoginBean loginBean;

	BaseDTO baseDTO = new BaseDTO();

	@Getter
	@Setter
	CustomerCreditNoteDTO customerCreditNoteDTO = new CustomerCreditNoteDTO();

	@Getter
	@Setter
	Boolean disabledCustomer = true;

	@Getter
	@Setter
	List<CustomerCreditNoteDTO> listcustomerCreditNoteDTO;
	
	@Getter
	@Setter
	List<CustomerCreditNoteDTO> listcustomerDTO;

	@Getter
	@Setter
	List<CustomerCreditNoteDetail> listcustomerCreditNoteDetails;

	@Getter
	@Setter
	LazyDataModel<CustomerCreditNoteDTO> customerCreditNoteDTOList;

	@Getter
	@Setter
	int size;

	@Getter
	@Setter
	CustomerCreditNoteDTO selectedCustomerCreditNoteDTO;

	@Getter
	@Setter
	CustomerMaster customerMaster = new CustomerMaster();

	@Getter
	@Setter
	String primaryContactNumber;

	@Getter
	@Setter
	String custmername;
	
	@Getter
	@Setter
	Double totalpaid,credit,Pending;
	
	@Getter
	@Setter
	Double totalpaidamt,totalcreditamt,totalPendingamt;
	
//	public void viewBillDetails(CustomerCreditNoteDTO data) {
//	
//		try {
//			if(data!=null && data.getBillhbillno()!=null) {
//				String url = serverURL + "/customerCreditNoteController/getdetailbybillno";
//				baseDTO = httpService.post(url, data);
//				if(baseDTO!=null&& baseDTO.getStatusCode()==0) {
//					
////					listcustomerCreditNoteDTO1=new ArrayList<CustomerCreditNoteDTO>();
////					log.info("Product Variety Master view");
////					ObjectMapper mapper = new ObjectMapper();
////					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
////					listcustomerCreditNoteDTO1 = mapper.readValue(jsonResponse, new TypeReference<CustomerCreditNoteDTO>() {
////					});
//					
//					
//					ObjectMapper mapper = new ObjectMapper();
//					listcustomerCreditNoteDTO1 = new ArrayList<CustomerCreditNoteDTO>();
//					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
//					listcustomerCreditNoteDTO1 = mapper.readValue(jsonResponse, new TypeReference<List<CustomerCreditNoteDTO>>() {
//					});
//				}
//				
//			}
//		} catch (Exception e) {
//			log.error("Error in viewBillDetails  ", e);
//		}
//		
//	}
	
	public String PaymentRecive() {
		log.info("<--- PaymentRecive ---> ");
		
		try {
			if(!listcustomerDTO.isEmpty()) {
				
				for(CustomerCreditNoteDTO dto:listcustomerDTO) {
					if(dto.getReceiveAmount()!=null&&dto.getReceiveAmount()>0.0) {
						
						if(dto.getTotalPendingAmount() < dto.getReceiveAmount()) {
							AppUtil.addWarn("Recived Amount Should Be Lessthen Pending Amount");
							return null;
						}
					}
					
				}
				
			String url = serverURL + "/customerCreditNoteController/PaymentRecive";
			baseDTO = httpService.post(url, listcustomerDTO);
			if(baseDTO!=null&& baseDTO.getStatusCode()==0) {
				log.info("Payment Recived");
				AppUtil.addInfo(" Pyment Recived Successfuly");
				return LOAD_LIST_PAGE;
			}else {
				AppUtil.addWarn("Failed");
				return LOAD_LIST_PAGE;
			}
			
		}
			
		}catch (Exception e) {
			log.error("Error in viewBillDetails  ", e);
		}
		return null;
		
	}
	
//	public void EnterReciveAMT(CustomerCreditNoteDTO dto) {
//		 
//	}

	public String loadListPage() {
		customerCreditNoteDTO = new CustomerCreditNoteDTO();
		selectedCustomerCreditNoteDTO = new CustomerCreditNoteDTO();
		primaryContactNumber = null;
		custmername = null;
		customerMaster = null;
		listcustomerCreditNoteDTO=null;
		totalpaidamt=0.0;
		totalcreditamt=0.0;
		totalPendingamt=0.0;
	

		log.info("<--- Loading supplierMasterBean showList list page --->");
		loadLazySupplierList();

		return LOAD_LIST_PAGE;
	}

	public void loadLazySupplierList() {
		log.info("<--- loadLazyCreditNoteList ---> ");

		customerCreditNoteDTOList = new LazyDataModel<CustomerCreditNoteDTO>() {

			private static final long serialVersionUID = -1540942419672760421L;

			@Override
			public List<CustomerCreditNoteDTO> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				List<CustomerCreditNoteDTO> data = new ArrayList<CustomerCreditNoteDTO>();
				try {
					BaseDTO baseDTO = getSearchData(first / pageSize, pageSize, sortField, sortOrder, filters);

					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());

					data = mapper.readValue(jsonResponse, new TypeReference<List<CustomerCreditNoteDTO>>() {
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
			public Object getRowKey(CustomerCreditNoteDTO res) {
				return res != null ? res.getCusHeaderCreditNotePk() : null;
			}

			@Override
			public CustomerCreditNoteDTO getRowData(String rowKey) {
				List<CustomerCreditNoteDTO> responseList = (List<CustomerCreditNoteDTO>) getWrappedData();
				Long value = Long.valueOf(rowKey);
				for (CustomerCreditNoteDTO res : responseList) {
					if (res.getCusHeaderCreditNotePk().longValue() == value.longValue()) {
						selectedCustomerCreditNoteDTO = res;
						return res;
					}
				}
				return null;
			}

		};
	}

	public BaseDTO getSearchData(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) throws ParseException {

		customerCreditNoteDTO = new CustomerCreditNoteDTO();
		
		CustomerCreditNoteDTO dto;

		BaseDTO baseDTO = new BaseDTO();
		log.info("page:[" + first + "] " + "pageSize:[" + pageSize + "] " + "sortOrder:[" + sortOrder + "] "
				+ "sortField:[" + sortField + "]");

		CustomerCreditNoteDTO customerCreditNoteDto = new CustomerCreditNoteDTO();

		PaginationDTO paginationDTO = new PaginationDTO(first, pageSize, sortField, sortOrder.toString(),filters);
		customerCreditNoteDTO.setPaginationDTO(paginationDTO);

		log.info("supplierMaster  " + customerCreditNoteDto);
		customerCreditNoteDto.setFilters(filters);
		log.info(" customerCreditNote Controller URL == " + appPreference.getPortalServerURL()
				+ "/customerCreditNoteController/search");
		try {
			String supplierMasterSearchUrl = appPreference.getPortalServerURL()
					+ "/customerCreditNoteController/search";
			log.info("Supplier Master Search Url " + supplierMasterSearchUrl);
			baseDTO = httpService.post(supplierMasterSearchUrl, customerCreditNoteDTO);
			if(baseDTO!=null) {
				
				ObjectMapper mapper = new ObjectMapper();
				dto=new CustomerCreditNoteDTO();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
				dto = mapper.readValue(jsonResponse, new TypeReference<CustomerCreditNoteDTO>() {
				});
				log.info("Lazy Search =========>> ");
				totalpaidamt=dto.getTotalPaidAmount();
				totalcreditamt=dto.getTotalCreditAmount();
				totalPendingamt= totalcreditamt-totalpaidamt ;
				}
			
		} catch (Exception e) {
			errorMap.notify(ErrorDescription.INTERNAL_ERROR.getCode());
			log.error("Exception in getSearchData() ", e);
		}

		return baseDTO;
	}

	public void onRowSelect(SelectEvent event) {
		log.info("FDS Plan onRowSelect method started");
		// supplierMaster = ((SupplierMaster) event.getObject());
		// addButtonFlag = true;
	}

	public String loadAddPage() {
		customerMaster = new CustomerMaster();
		primaryContactNumber = null;
		return LOAD_ADD_PAGE;
	}

	public String loadViewPage() {
		log.info("--submitForm---");
		try {
			
			if(selectedCustomerCreditNoteDTO == null || selectedCustomerCreditNoteDTO.getCusHeaderCreditNotePk()==null) {
				AppUtil.addWarn("Please Select At Least One Record");
				return null;
			}
			log.info("<<======= Action::" + action);
			UserDTO userDto = new UserDTO();
			userDto.setId(loginBean.getUserDetailSession().getId());
			ObjectMapper obj = new ObjectMapper();
			String oo = obj.writeValueAsString(customerCreditNoteDTO);
			System.out.println(oo);

			String url = serverURL + "/customerCreditNoteController/view/"
					+ selectedCustomerCreditNoteDTO.getCusHeaderCreditNotePk();
			baseDTO = httpService.get(url);
			log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
			if (baseDTO != null) {
				log.info("Product Variety Master view");
				ObjectMapper mapper = new ObjectMapper();
				listcustomerCreditNoteDTO=new ArrayList<CustomerCreditNoteDTO>();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				listcustomerCreditNoteDTO = mapper.readValue(jsonResponse, new TypeReference<List<CustomerCreditNoteDTO>>() {
				});
				credit=listcustomerCreditNoteDTO.stream().mapToDouble(x->x.getTotalCreditAmount()).sum();
				 totalpaid=listcustomerCreditNoteDTO.stream().mapToDouble(x->x.getPaidAmount()).sum();
				Pending=0.0;
				return LOAD_VIEW_PAGE;
			}

		} catch (Exception e) {
			log.error(" == Exception Occured In save method of ProductVariety Master Bean class ", e);
		}

		return LOAD_LIST_PAGE;
	}


	
	
	public String PaymentRecivePage() {
		BaseDTO baseDTO = new BaseDTO();
		try {
			if(selectedCustomerCreditNoteDTO == null || selectedCustomerCreditNoteDTO.getCusHeaderCreditNotePk()==null) {
				AppUtil.addWarn("Please Select At Least One Record");
				return null;
			}
			String url = serverURL + "/customerCreditNoteController/getdetail";
			baseDTO = httpService.post(url, selectedCustomerCreditNoteDTO);

			log.info("jhdhcgsdsdhgghdsssghdsgdg" + baseDTO.getResponseContent());
			if(baseDTO!=null && baseDTO.getStatusCode()==0) {
				
				
				ObjectMapper mapper = new ObjectMapper();
				listcustomerDTO = new ArrayList<CustomerCreditNoteDTO>();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				listcustomerDTO = mapper.readValue(jsonResponse, new TypeReference<List<CustomerCreditNoteDTO>>() {
				});
			}
			
		}catch (Exception e) {
			log.error("Error in getdetail loadEditPage() ", e);
		}
		
		return LOAD_Edit_PAGE;
		
		
	}

	public void clear() {
		log.info("--creditNoteList  clear--");
	}

	public String save() {

		log.info("--submitForm---");
		try {

			customerCreditNoteDTO.setCustomerName(custmername);
			customerCreditNoteDTO.setCustomerMobileNo(Long.parseLong(primaryContactNumber));
			if (customerMaster == null) {
				customerMaster = new CustomerMaster();
				customerMaster.setName(custmername);
				customerMaster.setPrimaryContactNumber(primaryContactNumber);

			}
			if (customerCreditNoteDTO.getCustomerMaster() == null) {
				customerCreditNoteDTO.setCustomerMaster(customerMaster);
			}

			if (action.equalsIgnoreCase("ADD")) {

				log.info("<<======= Action::" + action);

				UserDTO userDto = new UserDTO();
				userDto.setId(loginBean.getUserDetailSession().getId());
				ObjectMapper obj = new ObjectMapper();
				String oo = obj.writeValueAsString(customerCreditNoteDTO);
				System.out.println(oo);

				String url = serverURL + "/customerCreditNoteController/save";

				baseDTO = httpService.post(url, customerCreditNoteDTO);

				log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
				if (baseDTO != null && baseDTO.getStatusCode() == 0) {
					log.info("Product Variety Master has been Added");
					AppUtil.addInfo(" Item Saved Successfuly , You Can See items In Main Page");
					return LOAD_LIST_PAGE;
				} else {
					if (baseDTO != null && baseDTO.getStatusCode() == 1) {
						AppUtil.addWarn(baseDTO.getMessage());
					}
				}

			} else if (action.equalsIgnoreCase("EDIT")) {
				log.info("action EDIT");

				if (customerCreditNoteDTO != null) {
					String url = serverURL + "/customerCreditNoteController/update";
					baseDTO = httpService.post(url, customerCreditNoteDTO);
					log.info("BaseDTO Status Code " + baseDTO.getStatusCode());
					if (baseDTO != null) {

						log.info("ItemMasterBean updated successfully");
						AppUtil.addInfo("Item Updated successfully");
						// showList();
						return LOAD_LIST_PAGE;
					}

				}
			} else {
				if (action.equalsIgnoreCase("PaymentRecive")) {
					if (customerCreditNoteDTO != null) {
						String url = serverURL + "/customerCreditNoteController/PaymentRecive";
						baseDTO = httpService.post(url, customerCreditNoteDTO);
						if (baseDTO != null) {

							log.info("ItemMasterBean updated successfully");
							AppUtil.addInfo("Item Updated successfully");
							// showList();
							return LOAD_LIST_PAGE;
						}
					}
				}

				if (action.equalsIgnoreCase("AddPayment")) {
					if (customerCreditNoteDTO != null && customerCreditNoteDTO.getAddamount() != null) {
						String url = serverURL + "/customerCreditNoteController/addamount";
						baseDTO = httpService.post(url, customerCreditNoteDTO);
						if (baseDTO != null) {

							log.info("ItemMasterBean updated successfully");
							AppUtil.addInfo("Item Updated successfully");
							// showList();
							return LOAD_LIST_PAGE;
						}
					}
				}
			}
			// loadRetailProductvarityDTOLazy();

		} catch (Exception e) {
			log.error(" == Exception Occured In save method of ProductVariety Master Bean class ", e);
		}
		return null;
	}

	public List<CustomerMaster> autosearchcustomer(String name) {
		List<CustomerMaster> listcustmermaster = new ArrayList();
		BaseDTO baseDTO = new BaseDTO();
		try {
			if (name.trim() != null && !name.isEmpty()) {
				custmername = name;
				String url = serverURL + "/customerCreditNoteController/autocompleteitemName/" + name;
				log.info("::: loadProductVarietyDetails Controller calling  1 :::");
				baseDTO = httpService.get(url);
				log.info("::: get itemAutocomplete Response :");

				if (baseDTO.getStatusCode() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					listcustmermaster = new ArrayList<CustomerMaster>();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listcustmermaster = mapper.readValue(jsonResponse, new TypeReference<List<CustomerMaster>>() {
					});
					log.info("itemAutocomplete load Successfully " + baseDTO.getTotalRecords());
					log.info("itemAutocomplete Page Convert Succes -->");
				} else {
					log.warn("itemAutocomplete Error ");
				}
			}

		} catch (Exception e) {
			log.error("Exception in CustomerCreditNoteean autosearchcustomer() ", e);
		}

		return listcustmermaster;
	}

	public void updateMobileNumber() {

		if (customerMaster != null && customerMaster.getId() != null) {
			primaryContactNumber = customerMaster.getPrimaryContactNumber();
			disabledCustomer = true;
		} else {
			disabledCustomer = false;
		}

	}

	public String confirmDelete() {

		return null;

	}
	
	public void chekmobilenumber() {
		try {
			String mobno=primaryContactNumber;
			if(!mobno.trim().isEmpty()) {
				
				String url = serverURL + "/customerMasterController/checkmobileno/"+mobno;
				log.info("Before Search Call WITH URI >>>" + url);
				baseDTO = httpService.get(url);
				log.info("After Search Call WITH URI >>>");
				if(baseDTO!=null && !baseDTO.getStatusCode().equals(0)) {
					AppUtil.addWarn("Mobile Number Alredy Exist");
					primaryContactNumber=null;
				}
			}
			
		} catch (Exception e) {
			log.error("Exception ", e);
		}
	
	}

}
