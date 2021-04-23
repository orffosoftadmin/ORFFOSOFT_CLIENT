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
import co.orffosoft.dto.DirectBillingReportDTO;
import co.orffosoft.entity.DirectBilling_D;
import co.orffosoft.entity.DirectBilling_H;
import co.orffosoft.utill.HttpService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("directbillingbean")
@Scope("session")
public class DirectBillingReportBean {

	String List_Page = "/pages/billing/billing_report/DirectBillingReport.xhtml?faces-redirect=true;";
	String View_Page = "/pages/billing/billing_report/DirectbillingReportView.xhtml?faces-redirect=true;";

	@Getter
	@Setter
	DirectBillingReportDTO directBillingReportDTO;

	@Getter
	@Setter
	DirectBilling_H selectdirectbillh = new DirectBilling_H();

	@Getter
	@Setter
	List<DirectBilling_H> listdirectbillingh = new ArrayList();

	@Getter
	@Setter
	List<DirectBilling_D> listdirectbillingd = new ArrayList();

	@Getter
	@Setter
	String serverURL = AppUtil.getPortalServerURL();

	@Autowired
	HttpService httpService;

	public String showPage() {
		directBillingReportDTO = new DirectBillingReportDTO();
		listdirectbillingh = null;
		selectdirectbillh=null;
		return List_Page;

	}

	public void generateReport() {
		try {
			BaseDTO baseDTO = new BaseDTO();
			boolean valid = false;
			if (directBillingReportDTO != null) {

				if (directBillingReportDTO.getFromdate() != null || directBillingReportDTO.getTodate() != null) {
					valid = true;
					if (directBillingReportDTO.getFromdate() == null) {
						AppUtil.addWarn("Please Select From Date");
						return;
					} else if (directBillingReportDTO.getTodate() == null) {
						AppUtil.addWarn("Please Select To Date");
						return;
					}
				}
				if (!(directBillingReportDTO.getBillNo().isEmpty())) {
					valid = true;
				}
				if (!(directBillingReportDTO.getCustomername().isEmpty())) {
					valid = true;
				}
				if (directBillingReportDTO.getMobilenumber() != null) {
					valid = true;
				}
			}
			if (!valid) {
				AppUtil.addWarn("Enter At Least One Field");
				return;
			}
			if (directBillingReportDTO != null) {
				String url = serverURL + "/directBillingReportController/generetreport";
				baseDTO = httpService.post(url, directBillingReportDTO);

				if (baseDTO != null) {
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listdirectbillingh = mapper.readValue(jsonResponse, new TypeReference<List<DirectBilling_H>>() {
					});

				}
			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
	}

	public void onRowSelect(SelectEvent event) {
		log.info("goodsrecepitchanges onRowSelect method started");
		selectdirectbillh = ((DirectBilling_H) event.getObject());
	}

	public String showViewForm() {

		try {
			BaseDTO baseDTO = new BaseDTO();

			if (selectdirectbillh == null) {
				AppUtil.addWarn("Select At Least One Record");
				return null;
			}

			if (selectdirectbillh != null) {
				String url = serverURL + "/directBillingReportController/showViewForm";
				baseDTO = httpService.post(url, selectdirectbillh);
				if (baseDTO != null) {
					ObjectMapper mapper = new ObjectMapper();
					String jsonResponse = mapper.writeValueAsString(baseDTO.getResponseContents());
					listdirectbillingd = mapper.readValue(jsonResponse, new TypeReference<List<DirectBilling_D>>() {
					});

				}
			}

		} catch (Exception e) {
			log.error("<<====   ERROR::: " + e);
		}
		return View_Page;

	}

}
