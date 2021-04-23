package co.orffosoft.bean;

import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.orffosoft.core.util.AppUtil;
import co.orffosoft.dto.BaseDTO;
import co.orffosoft.dto.GoodsChangesHistoryReportDTO;
import co.orffosoft.dto.ProductVarityMasterResponse;
import co.orffosoft.entity.GoodsReceiptNote_D;
import co.orffosoft.entity.GoodsReceiptNote_H;
import co.orffosoft.entity.Goods_Change_History;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("goodsChangesHistoryReportBean")
@Scope("session")
public class GoodsChangesHistoryReportBean {

	private final String List_Page = "/pages/masters/GoodsChangeHistoryReport.xhtml?faces-redirect=true;";
	private final String View_Page = "/pages/masters/GoodsChangesHistoryViewPage.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	GoodsChangesHistoryReportDTO goodsChangesHistoryReportDTO = new GoodsChangesHistoryReportDTO();

	@Getter
	@Setter
	List<GoodsChangesHistoryReportDTO> listgoodsChangesHistoryReportDTO;

	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();

	@Getter
	@Setter
	GoodsReceiptNote_H grnh;
	
	@Getter
	@Setter
	GoodsReceiptNote_H selectgrnh = new GoodsReceiptNote_H() ;

	@Getter
	@Setter
	List<GoodsReceiptNote_H> listgrnh;
	
	@Getter
	@Setter
	List<GoodsReceiptNote_D> listgrnd;
	
	@Getter
	@Setter
	List<Goods_Change_History> listgoodchange;
	
	@Getter
	@Setter
	List<GoodsReceiptNote_H> listgrnh1;
	
	@Autowired
	HttpService httpService;

	public String showPage() {
		
		grnh=null;
		listgrnh= new ArrayList();
		listgrnh1=new ArrayList();
		goodsChangesHistoryReportDTO = new GoodsChangesHistoryReportDTO();
		listgoodsChangesHistoryReportDTO=new ArrayList();
		return List_Page;

	}
	public String showViewForm() {
		try {
			BaseDTO baseDTO = new BaseDTO();
			if(selectgrnh!=null && selectgrnh.getGrn_h_id()!=null) {
				String url = serverURL + "/goodschangeshistoryReportcontroller/showviewform";
				baseDTO = httpService.post(url, selectgrnh);
				if(baseDTO!=null) {
					listgoodchange=new ArrayList();
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listgoodchange = mapper.readValue(jsonResponse, new TypeReference<List<Goods_Change_History>>() {
					});
					
				}
			}
			
		}catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return View_Page;
	}

	public void generateReport() {
		try {
			BaseDTO baseDTO = new BaseDTO();
			if (goodsChangesHistoryReportDTO != null) {
                if(grnh!=null) {
                	goodsChangesHistoryReportDTO.setGrnh(grnh);
                }
				String url = serverURL + "/goodschangeshistoryReportcontroller/generetreport";
				baseDTO = httpService.post(url, goodsChangesHistoryReportDTO);
				if(baseDTO!=null) {
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listgrnh1 = mapper.readValue(jsonResponse, new TypeReference<List<GoodsReceiptNote_H>>() {
					});
					
				}

			}
		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}

	}
	
	public void onRowSelect(SelectEvent event) {
		log.info("goodsrecepitchanges onRowSelect method started");
		selectgrnh = ((GoodsReceiptNote_H) event.getObject());
		
	}

	public List<GoodsReceiptNote_H> itemAutoSearch(String itemName) {
		try {
			BaseDTO baseDTO = new BaseDTO();
			if (itemName.trim() != null && !itemName.isEmpty()) {
				String url = serverURL + "/goodschangeshistoryReportcontroller/autosearch/" + itemName;
				baseDTO = httpService.get(url);
				if (baseDTO != null) {
					listgrnh = new ArrayList();
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listgrnh = mapper.readValue(jsonResponse, new TypeReference<List<GoodsReceiptNote_H>>() {
					});

				}

			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return listgrnh;

	}

}
