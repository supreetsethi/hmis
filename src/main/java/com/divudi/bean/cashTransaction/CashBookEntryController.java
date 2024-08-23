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
import com.divudi.entity.Payment;
import com.divudi.entity.cashTransaction.CashBook;
import com.divudi.entity.cashTransaction.CashBookEntry;
import com.divudi.facade.CashBookEntryFacade;
import com.divudi.facade.CashBookFacade;
import com.divudi.facade.PaymentFacade;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

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

    public void writeCashBookEntry(Payment p) {
        if (p == null) {
            JsfUtil.addErrorMessage("Cashbook Entry Error !");
            return;
        }

        if (!chackPaymentMethodForCashBookEntry(p.getPaymentMethod())) {
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
        cashbookEntryFacade.create(current);

    }

    public boolean chackPaymentMethodForCashBookEntry(PaymentMethod pm) {
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
