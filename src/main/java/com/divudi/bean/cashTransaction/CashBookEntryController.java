/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.cashTransaction;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.util.JsfUtil;
import com.divudi.data.PaymentMethod;
import static com.divudi.data.PaymentMethod.Card;
import static com.divudi.data.PaymentMethod.OnCall;
import static com.divudi.data.PaymentMethod.PatientDeposit;
import com.divudi.data.ReportTemplateRowBundle;
import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Payment;
import com.divudi.entity.cashTransaction.CashBook;
import com.divudi.entity.cashTransaction.CashBookEntry;
import com.divudi.facade.CashBookEntryFacade;
import com.divudi.facade.PaymentFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;

/**
 *
 * @author Lawan Chaamindu
 */
@Named
@SessionScoped
public class CashBookEntryController implements Serializable {

    @EJB
    private CashBookEntryFacade cashbookEntryFacade;
    @EJB
    PaymentFacade paymentFacade;

    private CashBook cashBook;
    @Inject
    private SessionController sessionController;

    CashBookEntry current;
    private List<CashBookEntry> cashBookEntryList;

    boolean doNotWriteCashBookEntriesAtBillingForAnyPaymentMethod = true;

    public void writeCashBookEntryAtPaymentCreation(Payment p) {
        if (p == null) {
            JsfUtil.addErrorMessage("Cashbook Entry Error !");
            return;
        }
        if (doNotWriteCashBookEntriesAtBillingForAnyPaymentMethod) {
            return;
        }
        if (!chackPaymentMethodForCashBookEntryAtPaymentMethodCreation(p.getPaymentMethod())) {
            return;
        }

        current = new CashBookEntry();
        current.setInstitution(p.getInstitution());
        current.setDepartment(p.getDepartment());
        current.setCreater(sessionController.getLoggedUser());
        current.setCreatedAt(new Date());
        current.setPaymentMethod(p.getPaymentMethod());
        current.setEntryValue(p.getPaidValue());
        current.setPayment(p);
        current.setCashBook(sessionController.getLoggedCashbook());
        current.setSite(sessionController.getDepartment().getSite());
        current.setBill(p.getBill());
        updateBalances(p.getPaymentMethod(), p.getPaidValue(), current);
        cashbookEntryFacade.create(current);

    }

    public List<CashBookEntry> writeCashBookEntryAtHandover(ReportTemplateRowBundle bundle, Bill bill, CashBook bundleCb) {
        System.out.println("writeCashBookEntryAtHandover by Bundle");

        if (bundle == null) {
            JsfUtil.addErrorMessage("Cashbook Entry Error !");
            return null;
        }

        List<CashBookEntry> cashbookEntries = new ArrayList<>();

        for (PaymentMethod pm : PaymentMethod.asList()) {
            Double value = 0.0;
            switch (pm) {
                case Cash:
                    value = bundle.getCashValue();
                    break;
                case Card:
                    value = bundle.getCardValue();
                    break;
                case Agent:
                    value = bundle.getAgentValue();
                    break;
                case Cheque:
                    value = bundle.getChequeValue();
                    break;
                case Credit:
                    value = bundle.getCreditValue();
                    break;
                case IOU:
                    value = bundle.getIouValue();
                    break;
                case MultiplePaymentMethods:
                    value = bundle.getMultiplePaymentMethodsValue();
                    break;
                case OnCall:
                    value = bundle.getOnCallValue();
                    break;
                case OnlineSettlement:
                    value = bundle.getOnlineSettlementValue();
                    break;
                case PatientDeposit:
                    value = bundle.getPatientDepositValue();
                    break;
                case PatientPoints:
                    value = bundle.getPatientPointsValue();
                    break;
                case Slip:
                    value = bundle.getSlipValue();
                    break;
                case Staff:
                    value = bundle.getStaffValue();
                    break;
                case Staff_Welfare:
                    value = bundle.getStaffWelfareValue();
                    break;
                case Voucher:
                    value = bundle.getVoucherValue();
                    break;
                case ewallet:
                    value = bundle.getEwalletValue();
                    break;
                default:
                    continue; // Skip the rest if not applicable
            }

            if (value == 0.0) {
                continue;
            }

            CashBookEntry newCbEntry = new CashBookEntry();

            newCbEntry.setInstitution(bundle.getDepartment().getInstitution());
            newCbEntry.setDepartment(bundle.getDepartment());
            newCbEntry.setSite(bundle.getDepartment().getSite());
            newCbEntry.setWebUser(bundle.getUser());

            newCbEntry.setCreater(sessionController.getLoggedUser());
            newCbEntry.setCreatedAt(new Date());

            newCbEntry.setPaymentMethod(pm);
            newCbEntry.setEntryValue(value);
            newCbEntry.setBill(bill);
            newCbEntry.setCashBook(bundleCb);
            cashbookEntryFacade.create(newCbEntry);
            cashbookEntries.add(newCbEntry);
            System.out.println("newCbEntry = " + newCbEntry);
            updateBalances(bundle.getPaymentMethod(), value, newCbEntry);

        }

        return cashbookEntries;
    }

    public void writeCashBookEntryAtHandover(Payment p, CashBook cb) {
        System.out.println("writeCashBookEntryAtHandover");
        System.out.println("p = " + p);
        System.out.println("cb = " + cb);
        if (p == null) {
            JsfUtil.addErrorMessage("Cashbook Entry Error !");
            return;
        }

        if (!chackPaymentMethodForCashBookEntryAtHandover(p.getPaymentMethod())) {
            return;
        }
        CashBookEntry newCbEntry = new CashBookEntry();
        newCbEntry.setInstitution(p.getInstitution());
        newCbEntry.setDepartment(p.getDepartment());
        newCbEntry.setSite(p.getDepartment().getSite());
        newCbEntry.setCreater(sessionController.getLoggedUser());
        newCbEntry.setCreatedAt(new Date());
        newCbEntry.setPaymentMethod(p.getPaymentMethod());
        newCbEntry.setEntryValue(p.getPaidValue());
        newCbEntry.setPayment(p);
        newCbEntry.setBill(p.getHandoverAcceptBill());
        newCbEntry.setCashBook(cb);
        updateBalances(p.getPaymentMethod(), p.getPaidValue(), newCbEntry);

        p.setCashbook(cb);
        p.setCashbookEntry(newCbEntry);
        paymentFacade.edit(p);
        cashbookEntryFacade.create(newCbEntry);

    }

    public void updateBalances(PaymentMethod pm, Double Value, CashBookEntry cbe) {
        Map m = new HashMap<>();
        String jpql = "Select cbe from CashBookEntry cbe where "
                + " cbe.paymentMethod=:pm";

        m.put("pm", pm);

        if (cbe.getDepartment() != null) {
            jpql += " and cbe.department=:dep ";
            m.put("dep", cbe.getDepartment());
        }
        if (cbe.getInstitution() != null) {
            jpql += " and cbe.institution=:ins ";
            m.put("ins", cbe.getInstitution());
        }
        if (cbe.getDepartment() != null) {
            jpql += " and cbe.site=:si ";
            m.put("si", cbe.getSite());
        }

        jpql += "order by cbe.id desc";

        CashBookEntry lastCashBookEntry = cashbookEntryFacade.findFirstByJpql(jpql, m);

        Double lastDepartmentBalance;
        Double lastInstitutionBalance;
        Double lastSiteBalance;

        if (lastCashBookEntry == null) {
            lastDepartmentBalance = 0.0;
            lastInstitutionBalance = 0.0;
            lastSiteBalance = 0.0;
        } else {
            if (lastCashBookEntry.getDepartmentBalance() != null) {
                lastDepartmentBalance = lastCashBookEntry.getDepartmentBalance();
            } else {
                lastDepartmentBalance = 0.0;
            }

            if (lastCashBookEntry.getInstitutionBalance() != null) {
                lastInstitutionBalance = lastCashBookEntry.getInstitutionBalance();
            } else {
                lastInstitutionBalance = 0.0;
            }

            if (lastCashBookEntry.getSiteBalance() != null) {
                lastSiteBalance = lastCashBookEntry.getSiteBalance();
            } else {
                lastSiteBalance = 0.0;
            }
        }

        Double newDepartmentBalance = lastDepartmentBalance + Value;
        Double newInstitutionBalance = lastInstitutionBalance + Value;
        Double newSiteBalance = lastSiteBalance + Value;

        cbe.setDepartmentBalance(newDepartmentBalance);
        cbe.setInstitutionBalance(newInstitutionBalance);
        cbe.setSiteBalance(newSiteBalance);

    }

    public boolean chackPaymentMethodForCashBookEntryAtPaymentMethodCreation(PaymentMethod pm) {
        boolean check = false;
        if (pm == null) {
            JsfUtil.addErrorMessage("Payment method is not found !");
            return false;
        }
        switch (pm) {
            case Card:
                check = true;
                break;

            case Cash:
                check = false;
                break;

            case Cheque:
                check = false;
                break;

            case Agent:
                check = false;
                break;

            case Credit:
                check = false;
                break;

            case OnCall:
                check = false;
                break;

            case PatientDeposit:
                check = true;
                break;

            case Slip:
                check = true;
                break;

            case Staff:
                check = false;
                break;

            case Staff_Welfare:
                check = false;
                break;

            case ewallet:
                check = true;
                break;

            case OnlineSettlement:
                check = true;
                break;

            default:

        }

        return check;

    }

    public boolean chackPaymentMethodForCashBookEntryAtHandover(PaymentMethod pm) {
        boolean check = false;
        if (pm == null) {
            JsfUtil.addErrorMessage("Payment method is not found !");
            return false;
        }
        switch (pm) {
            case Card:
                check = false;
                break;

            case Cash:
                check = true;
                break;

            case Cheque:
                check = false;
                break;

            case Agent:
                check = true;
                break;

            case Credit:
                check = false;
                break;

            case OnCall:
                check = true;
                break;

            case PatientDeposit:
                check = true;
                break;

            case Slip:
                check = false;
                break;

            case Staff:
                check = false;
                break;

            case Staff_Welfare:
                check = false;
                break;

            case ewallet:
                check = false;
                break;

            case OnlineSettlement:
                check = false;
                break;

            default:

        }

        return check;

    }

    public List<CashBookEntry> genarateCashBookEntries(Date fromDate, Date toDate, Institution site, Institution ins, Department dept) {
        String jpql;
        Map m = new HashMap<>();
        jpql = "select b from CashBookEntry b where b.retired=:ret and b.createdAt between :fromDate and :toDate ";
        if (site != null) {
            jpql += "and b.site=:site ";
            m.put("site", site);
        }
        if (ins != null) {
            jpql += "and b.institution=:ins ";
            m.put("ins", ins);
        }
        if (dept != null) {
            jpql += "and b.department=:dept ";
            m.put("dept", dept);
        }
        m.put("fromDate", fromDate);
        m.put("toDate", toDate);
        m.put("ret", false);
        cashBookEntryList = cashbookEntryFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        return cashBookEntryList;
    }

    public CashBookEntryController() {
    }

    public CashBookEntryFacade getCashbookEntryFacade() {
        return cashbookEntryFacade;
    }

    public void setCashbookEntryFacade(CashBookEntryFacade CashbookEntryFacade) {
        this.cashbookEntryFacade = CashbookEntryFacade;
    }

    public CashBook getCashBook() {
        return cashBook;
    }

    public void setCashBook(CashBook cashBook) {
        this.cashBook = cashBook;
    }

    public List<CashBookEntry> getCashBookEntryList() {
        return cashBookEntryList;
    }

    public void setCashBookEntryList(List<CashBookEntry> cashBookEntryList) {
        this.cashBookEntryList = cashBookEntryList;
    }

    /**
     *
     */
    @FacesConverter(forClass = CashBookEntry.class)
    public static class CashBookEntryConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CashBookEntryController controller = (CashBookEntryController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "CashBookEntryController");
            return controller.getCashbookEntryFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CashBookEntry) {
                CashBookEntry o = (CashBookEntry) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + CashBookEntry.class.getName());
            }
        }
    }

}
