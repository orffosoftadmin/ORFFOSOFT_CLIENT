package co.orffosoft.bean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.core.util.ErrorDescription;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.ExpensiveDTO;
import co.orffosoft.dto.PaginationDTO;
import co.orffosoft.dto.StockUploadDTO;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("expensivebean")
@Scope("session")
public class ExpensiveBean {

	private final String Add_Page = "/pages/masters/Expensive.xhtml?faces-redirect=true;";
	private final String List_Page = "/pages/masters/listExpensive.xhtml?faces-redirect=true;";
	
	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();
	
	@Autowired
	ErrorMap errorMap;
	
	@Autowired
	HttpService httpService;

	@Getter
	@Setter
	ExpensiveDTO expensivedto = new ExpensiveDTO();
	
	@Getter
	@Setter
	ExpensiveDTO selectexpensivedto ;
	
	@Getter
	@Setter
	LazyDataModel<ExpensiveDTO> expensiveLazyList;
	
	@Getter
	@Setter
	String action;
	
	@Getter
	@Setter
	int size;

	public String addPage() {
		expensivedto = new ExpensiveDTO();

		return Add_Page;

	}
	
	public String getlistpage() {
		expensivedto = new ExpensiveDTO();
		selectexpensivedto=null;
		loadLazyIntRequirementList();
		return List_Page;
	}

	
	public void loadLazyIntRequirementList() {
		log.info("<--- loadLazyStockTransfer RequirementList ---> ");
		expensiveLazyList = new LazyDataModel<ExpensiveDTO>() {
			private static final long serialVersionUID = -1540942419672760421L;

			@Override
			public List<ExpensiveDTO> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				List<ExpensiveDTO> data = new ArrayList<ExpensiveDTO>();
				try {
					BaseDTO baseDTO = getSearchData(first / pageSize, pageSize, sortField, sortOrder, filters);

					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());

					data = mapper.readValue(jsonResponse, new TypeReference<List<ExpensiveDTO>>() {
					});

					if (data != null) {
						this.setRowCount(baseDTO.getTotalRecords());
						log.info("<--- List Count --->  " + baseDTO.getTotalRecords());
						size = baseDTO.getTotalRecords();
					}
				} catch (Exception e) {
					log.error("Error in loadLazyStockTransfer RequirementList()  ", e);
				}
				return data;
			}

			@Override
			public Object getRowKey(ExpensiveDTO res) {
				return res != null ? res.getId() : null;
			}

			@Override
			public ExpensiveDTO getRowData(String rowKey) {
				List<ExpensiveDTO> responseList = (List<ExpensiveDTO>) getWrappedData();
				Long value = Long.valueOf(rowKey);
				for (ExpensiveDTO res : responseList) {
					if (res.getId().longValue() == value.longValue()) {
						return res;
					}
				}
				return null;
			}

		};
	}
	
	public BaseDTO getSearchData(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) throws ParseException {

	

		BaseDTO baseDTO = new BaseDTO();
		log.info("page:[" + first + "] " + "pageSize:[" + pageSize + "] " + "sortOrder:[" + sortOrder + "] "
				+ "sortField:[" + sortField + "]");

		ExpensiveDTO expensiveeDTO = new ExpensiveDTO();

		PaginationDTO paginationDTO = new PaginationDTO(first, pageSize, sortField, sortOrder.toString(),filters);
		expensiveeDTO.setPaginationDTO(paginationDTO);

		try {
			String URL = serverURL + "/expensiveController/searchData";
			baseDTO = httpService.post(URL, expensiveeDTO);
		} catch (Exception e) {
			errorMap.notify(ErrorDescription.INTERNAL_ERROR.getCode());
			log.error("Exception in getSearchData() ", e);
		}

		return baseDTO;
	}
	
	
	public String submit() {
		log.info("caalling  bean" );
		BaseDTO baseDTO = new BaseDTO();
		try {
			if(action.equals("ADD")) {
			if (expensivedto != null) {

				if (expensivedto.getItemname() == null) {
					AppUtil.addError("Please Enter Des/itemName");
					return null;
				} else {
					if (expensivedto.getAmount() == null) {
						AppUtil.addError("Please Enter Amount");
						return null;
					} else {
						if (expensivedto.getVendorname() == null) {
							AppUtil.addError("Please Enter VendorName");
							return null;
						} else {
							if (expensivedto.getDate() == null) {
								AppUtil.addError("Please Enter Date");
								return null;
							}
						}
					}
				}
				
				String url = serverURL + "/expensiveController/save";
				baseDTO = httpService.post(url, expensivedto);
				if(baseDTO!=null && baseDTO.getStatusCode()==0) {
					AppUtil.addInfo("ADD Sucessfull");
					return null;
				}else {
					AppUtil.addError("Faild to Save");
					return null;
				}

			}
			}else {
				if(action.equals("EDIT")) {
					if (expensivedto != null) {

						if (expensivedto.getItemname() == null) {
							AppUtil.addError("Please Enter Des/itemName");
							return null;
						} else {
							if (expensivedto.getAmount() == null) {
								AppUtil.addError("Please Enter Amount");
								return null;
							} else {
								if (expensivedto.getVendorname() == null) {
									AppUtil.addError("Please Enter VendorName");
									return null;
								} else {
									if (expensivedto.getDate() == null) {
										AppUtil.addError("Please Enter Date");
										return null;
									}
								}
							}
						}
						
						String url = serverURL + "/expensiveController/update";
						baseDTO = httpService.post(url, expensivedto);
						if(baseDTO!=null && baseDTO.getStatusCode()==0) {
							AppUtil.addInfo("Update Sucessfull");
							return List_Page;
						}else {
							AppUtil.addError("Faild to update");
							return null;
						}

					}
					
					
				}
			}

		} catch (Exception e) {
			log.info("Exception in bean" + e);
		}
		return null;

	}
	
	public String editpage() {
		try {
			expensivedto = new ExpensiveDTO();
			if(selectexpensivedto!=null && selectexpensivedto.getId()!=null) {
				expensivedto = selectexpensivedto;
			}else {
				AppUtil.addWarn("Please Select Record To Edit");
				return null;
			}
			
		}catch (Exception e) {
			log.info("Exception in bean" + e);
		}
		
		return Add_Page;
	}
	
	public void onRowSelect(SelectEvent event) {
		log.info(" onRowSelect method started");
		selectexpensivedto = ((ExpensiveDTO) event.getObject());
	}

}
