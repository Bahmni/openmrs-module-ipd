package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.openmrs.module.ipd.model.IPDDrugOrder;
import org.openmrs.module.ipd.contract.IPDDrugOrderResponse;
import org.openmrs.module.ipd.service.IPDDrugOrderService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd")
@Slf4j
public class IPDDrugOrderController extends BaseRestController {

    private IPDDrugOrderService ipdDrugOrderService;

    @Autowired
    public IPDDrugOrderController(IPDDrugOrderService ipdDrugOrderService) {
        this.ipdDrugOrderService = ipdDrugOrderService;
    }

    @RequestMapping(value = "/drugOrders", method = RequestMethod.GET)
    @ResponseBody
    public List<IPDDrugOrderResponse> getVisitWisePrescribedAndOtherActiveOrders(
            @RequestParam(value = "patientUuid") String patientUuid,
            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
            @RequestParam(value = "visitUuids", required = false) List visitUuids,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "includes", required = false) List<String> includes,
            @RequestParam(value = "getEffectiveOrdersOnly", required = false) Boolean getEffectiveOrdersOnly) throws ParseException {

        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);

        List<IPDDrugOrder> prescribedOrders = ipdDrugOrderService.getPrescribedOrders(visitUuids, patientUuid, true, numberOfVisits, startDate, endDate, Boolean.TRUE.equals(getEffectiveOrdersOnly));
        return prescribedOrders.stream().map(IPDDrugOrderResponse::createFrom).collect(Collectors.toList());
    }
}
