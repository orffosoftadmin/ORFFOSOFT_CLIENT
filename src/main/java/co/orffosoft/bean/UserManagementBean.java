package co.orffosoft.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.ProductVarityMasterResponse;
import co.orffosoft.dto.UserMasterDTO;
import co.orffosoft.entity.EntityMaster;
import co.orffosoft.entity.UserMaster;
import co.orffosoft.utill.ErrorMap;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service("userManagementBean")
@Log4j2
@Scope("session")
public class UserManagementBean implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	UserMasterDTO userMasterDTO = null;

	@Getter
	@Setter
	EntityMaster entityMaster;

	@Getter
	@Setter
	String action, status, rolename, roletype;

	@Getter
	@Setter
	Long parentid = 0L;

	@Getter
	@Setter
	String shopName = null;

	@Getter
	@Setter
	String userType = null;

	private final String SIGNUP_NEWUSER = "/signup.xhtml?faces-redirect=true;";
	private final String LOGIN_PAGE = "/login.xhtml?faces-redirect=true;";
	private final String ADD_PAGE = "/pages/userManagement/createUser.xhtml?faces-redirect=true;";
	private final String LIST_PAGE = "/pages/userManagement/listUser.xhtml?faces-redirect=true;";
	private final String VIEW_PAGE = "/pages/userManagement/viewUser.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	String serverURL = null;

	@Autowired
	HttpService httpService;

	@Autowired
	ErrorMap errorMap;

	@Getter
	@Setter
	private boolean hideStatusBar = true;

	@Autowired
	LoginBean loginBean;

	@Getter
	@Setter
	List<UserMaster> userMasterList = new ArrayList<UserMaster>();

	@Getter
	@Setter
	private Integer userMasterListSize;

	public static final String SERVER_URL = AppUtil.getPortalServerURL();

	@Getter
	@Setter
	UserMaster selectedUser = new UserMaster();

	@Getter
	@Setter
	List<EntityMaster> shopNamesList = new ArrayList<>();

	@Value("${server.session.timeout}")
	public Integer sessionTimeOut;

	@Getter
	@Setter
	boolean diseabledStatus;

	@PostConstruct
	public void init() {
		userMasterDTO = new UserMasterDTO();
		serverURL = AppUtil.getPortalServerURL();
	}

	/**
	 * @return
	 */
	public String newUser() {
		if (action.equalsIgnoreCase("NEW_SIGNUP")) {
			userMasterDTO = new UserMasterDTO();
			entityMaster = null;
			return SIGNUP_NEWUSER;
		} else {
			userMasterDTO = new UserMasterDTO();
			entityMaster = null;
			return LOGIN_PAGE;
		}

	}

	public String addUser() {
		userMasterDTO = new UserMasterDTO();
		userType = "WON_SHOP";

		shopNamesList = new ArrayList<>();
		entityMaster = null;
		rolename = loginBean.getUserMaster().getUserType();
		getShopNames();
		return ADD_PAGE;

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

	public void onRowSelect(SelectEvent event) {
		log.info("Product Varity Master onRowSelect method started");
		selectedUser = ((UserMaster) event.getObject());
		if (selectedUser.getId().equals(loginBean.getUserMaster().getId())) {
			diseabledStatus = true;
		} else {
			diseabledStatus = false;
		}
	}

	/**
	 * 
	 */
	public String submit() {
		try {
			boolean valid = validate();
			if (valid) {
				if (action.equals("EDIT") && userMasterDTO != null) {
					userMasterDTO.setEntityMaster(entityMaster);
					userMasterDTO.setModifiedBy(loginBean.getUserDetailSession().getEntityId());
					BaseDTO baseDTO = new BaseDTO();
					String url = serverURL + "/user/updatee";
					baseDTO = httpService.post(url, userMasterDTO);
					if (baseDTO != null) {
						AppUtil.addInfo("Successfull Updated");
						return showList();
					}

				} else {
					if (userMasterDTO != null) {
						if (action.equalsIgnoreCase("REGISTER_WITH_NEW_STORE")) {

							if (entityMaster == null) {
								entityMaster = new EntityMaster();
								entityMaster.setCode(userMasterDTO.getStoreCode().toString());
								entityMaster.setName(userMasterDTO.getStoreName());
							}
							userMasterDTO.setEntityMaster(entityMaster);
							userMasterDTO.setUserType("Admin");
							userMasterDTO.setStatus(true);
							BaseDTO baseDTO = new BaseDTO();
							String url = serverURL + "/user/add";

							ObjectMapper obj = new ObjectMapper();
							log.info("" + obj.writeValueAsString(userMasterDTO));
							baseDTO = httpService.post(url, userMasterDTO);
							if (baseDTO != null && baseDTO.getStatusCode() == 0) {
								if (baseDTO.getGeneralContent() != null) {
									AppUtil.addWarn(baseDTO.getGeneralContent());
								} else {
									AppUtil.addInfo("Successfully Registered . Please Login ");
									RequestContext context = RequestContext.getCurrentInstance();
									context.execute("PF('goToLoginPage').show();");
									newUser();
								}

							} else {
								AppUtil.addError(baseDTO.getGeneralContent());
							}
						} else {
							userMasterDTO.setId(loginBean.getUserMaster().getId());
							userMasterDTO.setUserType(userMasterDTO.getUserType());

							userMasterDTO.setEntityMaster(entityMaster);

							userMasterDTO.setStatus(true);
							BaseDTO baseDTO = new BaseDTO();
							String url = serverURL + "/user/add";
							baseDTO = httpService.post(url, userMasterDTO);
							if (baseDTO != null && baseDTO.getStatusCode() == 0) {
								AppUtil.addInfo("Successfully Registered ");
								return showList();
							} else if (baseDTO.getStatusCode() == 1) {
								AppUtil.addWarn(baseDTO.getGeneralContent());
								action = "ADD";
							} else {
								AppUtil.addWarn(baseDTO.getGeneralContent());
								action = "ADD";
							}
						}

					}

				}

			}

		} catch (Exception e) {
			log.error("Exception Occured in Submit Button UserManagementBean== ", e);
		}
		return null;

	}

	private boolean validate() {
		boolean valid = true;
		userMasterDTO.setUsername(userMasterDTO.getUsername().trim());

		if (userMasterDTO.getUsername() == null || userMasterDTO.getUsername().isEmpty()) {
			AppUtil.addError("Please Enter User Name");
			valid = false;
		} else if (userMasterDTO.getPassword() == null || userMasterDTO.getPassword().isEmpty()) {
			AppUtil.addError("Please Enter Password");
			valid = false;
		} else if (userMasterDTO.getConfirmPassword() == null || userMasterDTO.getConfirmPassword().isEmpty()) {
			AppUtil.addError("Please Enter Confirm Password");
			valid = false;
		} else if (!userMasterDTO.getPassword().equals(userMasterDTO.getConfirmPassword())) {
			AppUtil.addError("Confirm Password Not Matching to Password");
			valid = false;
		}

		if (action.equalsIgnoreCase("REGISTER_WITH_NEW_STORE")) {

			if (userMasterDTO.getStoreCode() == null || userMasterDTO.getStoreCode().equals("")) {
				AppUtil.addError("Please Enter Store Code");
				valid = false;
			} else {
				if (userMasterDTO.getStoreName() == null || userMasterDTO.getStoreName().isEmpty()) {
					AppUtil.addError("Please Enter Store Name");
					valid = false;
				} else {
					if (userMasterDTO.getStoreName() == null || userMasterDTO.getStoreName().isEmpty()) {
						AppUtil.addError("Please Enter Store Name");
						valid = false;
					} else {
						if (userMasterDTO.getMobileNumber() == null) {
							AppUtil.addError("Please Enter MobileNumber");
							valid = false;
						} else {
							if (userMasterDTO.getRoleType() == null || userMasterDTO.getRoleType().contentEquals("")) {
								AppUtil.addError("Please Select UserType");
								valid = false;

							} else {
								if (userMasterDTO.getWonername() == null || userMasterDTO.getWonername().equals("")) {
									AppUtil.addError("Please Enter Woner Name");
									valid = false;
								}else {
									if(userMasterDTO.getEmailId()==null || userMasterDTO.getEmailId().isEmpty()) {
										AppUtil.addError("Please Enter EmailId");
										valid = false;
									}
								}
							}
						}

					}
				}
			}

			userMasterDTO.setStoreName(userMasterDTO.getStoreName().trim());
			userMasterDTO.setStoreCode(userMasterDTO.getStoreCode());
		}

		if (action.equalsIgnoreCase("REGISTER_WITH_OLD_STORE")) {
			if (userMasterDTO.getUserType() == null || userMasterDTO.getUserType().isEmpty()) {
				AppUtil.addError("Please Select Role Name");
				valid = false;
			}

		}

		return valid;

	}

	public String showList() {
		log.info("<---------- Loading user list page---------->");
		serverURL = AppUtil.getPortalServerURL();
		selectedUser = new UserMaster();
		loadAllUserList();
		return "/pages/userManagement/listUser.xhtml?faces-redirect=true";
	}

	public void loadAllUserList() {
		log.info("<<<< ----------Start UsermangementBean . loadAllUserList ------- >>>>");
		try {
			BaseDTO baseDTO = new BaseDTO();
			String url = serverURL + "/user/getall";
			baseDTO = httpService.get(url);
			userMasterList = new ArrayList<>();
			if (baseDTO != null) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				userMasterList = mapper.readValue(jsonResponse, new TypeReference<List<UserMaster>>() {
				});
			}
		} catch (JsonProcessingException jpEx) {
			log.error("Json processing exception occured while converting .... ", jpEx);
		} catch (IOException ioEx) {
			log.error("IO Exception occured .... ", ioEx);
		} catch (Exception e) {
			log.error("Exception occured .... ", e);
		}

		if (userMasterList != null)
			userMasterListSize = userMasterList.size();
		else
			userMasterListSize = 0;
		log.info("<<<< ----------End UsermangementBean . loadAllUserList ------- >>>>");
	}

	public String showEditForm() {

		if (selectedUser == null || selectedUser.getId() == null) {
			AppUtil.addWarn("Please Select At Least One Record");
			return null;
		}
		userMasterDTO.setUsername(selectedUser.getUsername());
		try {
			userType = "WON_SHOP";
			BaseDTO baseDTO = new BaseDTO();
			String url = serverURL + "/user/getbyId";
			baseDTO = httpService.post(url, selectedUser);
			if (baseDTO != null && baseDTO.getResponseContent() != null && baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
				userMasterDTO = mapper.readValue(jsonResponse, UserMasterDTO.class);
				if (userMasterDTO != null && userMasterDTO.getId() != null) {
					if (userMasterDTO.getId().equals(loginBean.getUserMaster().getId())) {
						long l = loginBean.getUserMaster().getId();
						hideStatusBar = false;
					} else {
						hideStatusBar = true;
					}

				}
			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return ADD_PAGE;
	}

	public String showViewForm() {

		if (selectedUser == null || selectedUser.getId() == null) {
			AppUtil.addWarn("Please Select At Least One Record");
			return null;
		}
		try {
			BaseDTO baseDTO = new BaseDTO();
			String url = serverURL + "/user/getbyId";
			baseDTO = httpService.post(url, selectedUser);
			if (baseDTO != null && baseDTO.getResponseContent() != null && baseDTO.getStatusCode() == 0) {
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContent());
				userMasterDTO = mapper.readValue(jsonResponse, UserMasterDTO.class);
			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}

		return VIEW_PAGE;
	}

	public String clear() {
		selectedUser = new UserMaster();

		return "/pages/userManagement/listUser.xhtml?faces-redirect=true";
	}

	public String updateUserStatus() {
		log.info("updateUserStatus called......");
		BaseDTO baseDTO = new BaseDTO();
		try {

			if (status.equals("Active")) {
				if (selectedUser != null) {
					if (selectedUser.getStatus() == false) {
						String url = serverURL + "/user/updateSupplierStatus/" + selectedUser.getId() + "/" + status;
						baseDTO = httpService.get(url);
						log.info("BaseDTO Dto :" + baseDTO.getResponseContent());
						if (baseDTO != null) {
							AppUtil.addInfo("Item Status Activated Successfully");
							loadAllUserList();
							return LIST_PAGE;
						}
					} else {
						AppUtil.addInfo("Item Status Already Activated");
						return LIST_PAGE;
					}
				} else {
					AppUtil.addWarn(" Please Select A Record To Active");
					return LIST_PAGE;
				}

			} // else if close
			else if (status.equals("In-Active")) {
				if (selectedUser != null) {
					if (selectedUser.getStatus() == true) {
						String url = serverURL + "/user/updateSupplierStatus/" + selectedUser.getId() + "/" + status;
						baseDTO = httpService.get(url);
						log.info("BaseDTO Dto :" + baseDTO.getResponseContent());
						if (baseDTO != null) {
							AppUtil.addInfo("Item Status In-Activated Successfully");
							loadAllUserList();
							return LIST_PAGE;
						}
					} else {
						AppUtil.addInfo("Item Status Already In-Activated");
						return LIST_PAGE;
					}
				} else {
					AppUtil.addWarn(" Please Select A Record To In-Active");
					return LIST_PAGE;
				}
			} // else if close

			log.info("Status In-Active End......");
		} catch (Exception e) {
			log.error("<<<=======  Error in statusStockUpdate =====>>" + e);
		}
		return null;

	}

}
