/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.egf.web.controller.expensebill;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.egov.commons.service.ChartOfAccountsService;
import org.egov.egf.budget.model.BudgetControlType;
import org.egov.egf.budget.service.BudgetControlTypeService;
import org.egov.egf.expensebill.service.ExpenseBillService;
import org.egov.egf.utils.FinancialUtils;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.model.bills.EgBillregister;
import org.egov.utils.FinancialConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author venki
 *
 */

@Controller
@RequestMapping(value = "/expensebill")
public class CreateExpenseBillController extends BaseBillController {

    public CreateExpenseBillController(final AppConfigValueService appConfigValuesService) {
        super(appConfigValuesService);
    }

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Autowired
    private ExpenseBillService expenseBillService;

    @Autowired
    @Qualifier("chartOfAccountsService")
    private ChartOfAccountsService chartOfAccountsService;

    @Autowired
    private BudgetControlTypeService budgetControlTypeService;

    @Autowired
    private FinancialUtils financialUtils;

    @Override
    protected void setDropDownValues(final Model model) {
        super.setDropDownValues(model);
    }

    @RequestMapping(value = "/newform", method = RequestMethod.GET)
    public String showNewForm(@ModelAttribute("egBillregister") final EgBillregister egBillregister, final Model model) {

        setDropDownValues(model);
        model.addAttribute("stateType", egBillregister.getClass().getSimpleName());
        prepareWorkflow(model, egBillregister, new WorkflowContainer());
        final List<AppConfigValues> cutOffDateconfigValue = appConfigValuesService
                .getConfigValuesByModuleAndKey(FinancialConstants.MODULE_NAME_APPCONFIG,
                        FinancialConstants.KEY_DATAENTRYCUTOFFDATE);

        if (!cutOffDateconfigValue.isEmpty()) {
            final DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            List<String> validActions = Collections.emptyList();
            validActions = Arrays.asList(FinancialConstants.BUTTONFORWARD, FinancialConstants.CREATEANDAPPROVE);
            model.addAttribute("validActionList", validActions);
            try {
                model.addAttribute("cutOffDate",
                        DateUtils.getDefaultFormattedDate(df.parse(cutOffDateconfigValue.get(0).getValue())));
            } catch (final ParseException e) {

            }
        }
        egBillregister.setBilldate(new Date());
        return "expensebill-form";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("egBillregister") final EgBillregister egBillregister, final Model model,
            final BindingResult resultBinder, final HttpServletRequest request, @RequestParam final String workFlowAction)
            throws IOException {
        populateBillDetails(egBillregister);
        validateBillNumber(egBillregister, resultBinder);
        validateLedgerAndSubledger(egBillregister, resultBinder);

        if (resultBinder.hasErrors()) {
            setDropDownValues(model);
            egBillregister.setBilldate(new Date());
            model.addAttribute("stateType", egBillregister.getClass().getSimpleName());
            model.addAttribute("approvalDesignation", request.getParameter("approvalDesignation"));
            model.addAttribute("approvalPosition", request.getParameter("approvalPosition"));
            prepareWorkflow(model, egBillregister, new WorkflowContainer());
            model.addAttribute("approvalDesignation", request.getParameter("approvalDesignation"));
            model.addAttribute("approvalPosition", request.getParameter("approvalPosition"));
            model.addAttribute("designation", request.getParameter("designation"));
            egBillregister.getBillPayeedetails().clear();
            prepareBillDetailsForView(egBillregister);
            return "expensebill-form";
        } else {
            Long approvalPosition = 0l;
            String approvalComment = "";
            if (request.getParameter("approvalComment") != null)
                approvalComment = request.getParameter("approvalComent");
            if (request.getParameter("approvalPosition") != null && !request.getParameter("approvalPosition").isEmpty())
                approvalPosition = Long.valueOf(request.getParameter("approvalPosition"));
            EgBillregister savedEgBillregister;

            try {
                savedEgBillregister = expenseBillService.create(egBillregister, approvalPosition, approvalComment,
                        null,
                        workFlowAction);
            } catch (final ValidationException e) {
                // TODO: Used ApplicationRuntimeException for time being since
                // there is issue in session after
                // checkBudgetAndGenerateBANumber API call. Needs to replace
                // with errors.reject
                throw new ApplicationRuntimeException("error.expense.bill.budgetcheck.insufficient.amount");
            }

            final String approverDetails = financialUtils.getApproverDetails(savedEgBillregister.getStatus(),
                    savedEgBillregister.getState(), savedEgBillregister.getId(), approvalPosition);

            return "redirect:/expensebill/success?approverDetails= " + approverDetails + "&billNumber="
                    + savedEgBillregister.getBillnumber();

        }
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String showContractorBillSuccessPage(@RequestParam("billNumber") final String billNumber, final Model model,
            final HttpServletRequest request) {
        final String[] keyNameArray = request.getParameter("approverDetails").split(",");
        Long id = 0L;
        String approverName = "";
        String currentUserDesgn = "";
        String nextDesign = "";
        if (keyNameArray.length != 0 && keyNameArray.length > 0)
            if (keyNameArray.length == 1)
                id = Long.parseLong(keyNameArray[0].trim());
            else if (keyNameArray.length == 3) {
                id = Long.parseLong(keyNameArray[0].trim());
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
            } else {
                id = Long.parseLong(keyNameArray[0].trim());
                approverName = keyNameArray[1];
                currentUserDesgn = keyNameArray[2];
                nextDesign = keyNameArray[3];
            }

        if (id != null)
            model.addAttribute("approverName", approverName);
        model.addAttribute("currentUserDesgn", currentUserDesgn);
        model.addAttribute("nextDesign", nextDesign);

        final EgBillregister expenseBill = expenseBillService.getByBillnumber(billNumber);

        final String message = getMessageByStatus(expenseBill, approverName, nextDesign);

        model.addAttribute("message", message);

        return "expensebill-success";
    }

    private String getMessageByStatus(final EgBillregister expenseBill, final String approverName, final String nextDesign) {
        String message = "";

        if (FinancialConstants.CONTINGENCYBILL_CREATED_STATUS.equals(expenseBill.getStatus().getCode())) {
            if (org.apache.commons.lang.StringUtils
                    .isNotBlank(expenseBill.getEgBillregistermis().getBudgetaryAppnumber())
                    && !BudgetControlType.BudgetCheckOption.NONE.toString()
                            .equalsIgnoreCase(budgetControlTypeService.getConfigValue()))
                message = messageSource.getMessage("msg.expense.bill.create.success.with.budgetappropriation",
                        new String[] { expenseBill.getBillnumber(), approverName, nextDesign,
                                expenseBill.getEgBillregistermis().getBudgetaryAppnumber() },
                        null);
            else
                message = messageSource.getMessage("msg.expense.bill.create.success",
                        new String[] { expenseBill.getBillnumber(), approverName, nextDesign }, null);

        } else if (FinancialConstants.CONTINGENCYBILL_APPROVED_STATUS.equals(expenseBill.getStatus().getCode()))
            message = messageSource.getMessage("msg.expense.bill.approved.success",
                    new String[] { expenseBill.getBillnumber() }, null);
        else if (FinancialConstants.WORKFLOW_STATE_REJECTED.equals(expenseBill.getState().getValue()))
            message = messageSource.getMessage("msg.expense.bill.reject",
                    new String[] { expenseBill.getBillnumber(), approverName, nextDesign }, null);
        else if (FinancialConstants.WORKFLOW_STATE_CANCELLED.equals(expenseBill.getState().getValue()))
            message = messageSource.getMessage("msg.expense.bill.cancel",
                    new String[] { expenseBill.getBillnumber() }, null);

        return message;
    }

}