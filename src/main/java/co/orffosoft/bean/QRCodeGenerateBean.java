package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.QrCodeDTO;
import co.orffosoft.dto.QrCodeItemDTO;
import co.orffosoft.entity.CustomerMaster;
import co.orffosoft.entity.GoodsReceiptNote_D;
import co.orffosoft.entity.GoodsReceiptNote_H;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("qrCodeGenerateBean")
@Scope("session")
public class QRCodeGenerateBean {
	
	private final String List_PAGE = "/pages/masters/lisrQRCodeGenerate.xhtml ?faces-redirect=true;"; 
	private final String Add_PAGE = "/pages/masters/AddQRGeneratorPage.xhtml ?faces-redirect=true;"; 
	
	@Autowired
	HttpService httpService;
	
	@Getter
	@Setter
	List<GoodsReceiptNote_H> listgrnh;
	
	@Getter
	@Setter
	List<GoodsReceiptNote_D> listgrnd;
	
	@Getter
	@Setter
	QrCodeItemDTO qrCodeItemDTO =new QrCodeItemDTO();
	 
	public static final String SERVER_URL = AppUtil.getPortalServerURL();
	
	public String showListPage() {
		
		
		return List_PAGE;
	}
	
	public String showAddPage() {
		BaseDTO baseDTO = new BaseDTO();
		try {
			
			String url = SERVER_URL + "/qrCodeGeneratorController/getgrns";
			baseDTO=httpService.get(url);
			if(baseDTO!=null) {
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				listgrnh = mapper.readValue(jsonResponse, new TypeReference<List<GoodsReceiptNote_H>>() {
				});
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return Add_PAGE;
	}
	
	public void getGRNDetail() {
		BaseDTO baseDTO = new BaseDTO();
		try {
			String url = SERVER_URL + "/qrCodeGeneratorController/getgrndetail";
			baseDTO=httpService.post(url,qrCodeItemDTO);
			if(baseDTO!=null) {
				listgrnd=new ArrayList();
				ObjectMapper mapper = new ObjectMapper();
				String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
				listgrnd = mapper.readValue(jsonResponse, new TypeReference<List<GoodsReceiptNote_D>>() {
				});
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
