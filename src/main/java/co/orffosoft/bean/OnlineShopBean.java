package co.orffosoft.bean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import co.orffosoft.dto.OnlineShopResponse;
import co.orffosoft.dto.PaginationDTO;
import co.orffosoft.dto.ProductVariryMastersearchDTO;
import co.orffosoft.dto.ProductVarityMasterResponse;
import co.orffosoft.entity.ProductVarietyMaster;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("onlineShopBean")
@Scope("session")
public class OnlineShopBean {

	private final String online_Shop = "/pages/masters/Onlineshop.xhtml?faces-redirect=true;";

	@Autowired
	HttpService httpService;

	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();

	OnlineShopResponse OnlineShopResponse = new OnlineShopResponse();

	@Getter
	@Setter
	LazyDataModel<OnlineShopResponse> onlineShopResponseLazyDTOList;

	public String getVarityList() {
		
		log.info("<--- Inside getvarityMasterList()  completed ---> ");
		loadRetailProductvarityDTOLazy();
		return online_Shop;
	}

	private void loadRetailProductvarityDTOLazy() {
		log.info("Lazy load == product varity master  ==>>");
		onlineShopResponseLazyDTOList = new LazyDataModel<OnlineShopResponse>() {

			private static final long serialVersionUID = 2784959485860775580L;

			@Override
			public List<OnlineShopResponse> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {

				List<OnlineShopResponse> data = null;
				try {
					BaseDTO baseDTO = getSearchData(first / pageSize, pageSize, sortField, sortOrder, filters);

					ObjectMapper objectMapper = new ObjectMapper();
					if (baseDTO != null) {
						String jsonResponse = objectMapper.writeValueAsString(baseDTO.getResponseContents());
						data = objectMapper.readValue(jsonResponse, new TypeReference<List<OnlineShopResponse>>() {
						});
					}
					if (data != null) {
						this.setRowCount(baseDTO.getTotalRecords());

					}

				} catch (Exception e) {
					log.error("Error ", e);
				}

				return data;

			}
			@Override
			public Object getRowKey(OnlineShopResponse res) {
				log.info("Get Row Key called" + res);
				return res != null ? res.getVarityId() : null;
			}

			@Override
			public OnlineShopResponse getRowData(String rowKey) {
				log.info("Get Row Data called" + rowKey);
				List<OnlineShopResponse> responseList = (List<OnlineShopResponse>) getWrappedData();
				Long value = Long.valueOf(rowKey);

				for (OnlineShopResponse res : responseList) {
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

	private BaseDTO getSearchData(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) throws ParseException {

		OnlineShopResponse = new OnlineShopResponse();

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
			String url = serverURL + "/onlineShopController/searchvaritys";
			baseDTO = httpService.post(url, request);
			
		//	String urll = serverURL + "/onlineShopController/searchprice";
			//baseDTO = httpService.post(urll,request);

		} catch (Exception e) {
			log.error("Exception ", e);
			baseDTO.setStatusCode(ErrorDescription.FAILURE_RESPONSE.getCode());
		}
		return baseDTO;
	}
	

}
