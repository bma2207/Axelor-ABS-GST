package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.gst.service.invoiceLineServiceimpls;
import com.axelor.apps.gst.service.invoiceServiceImp;
import com.axelor.db.JpaSupport;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.List;

public class invoiceListCalculation extends JpaSupport {
  @Inject private invoiceLineServiceimpls service;
  @Inject private invoiceServiceImp invoiceserviceimp;

  public void computeInvoiceLine(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = service.invoiceCalculation(invoice);
    response.setValues(invoice);
  }

  public void productIdsList(ActionRequest request, ActionResponse response) {

    if (request.getContext().get("productIds") != null) {
      // Invoice invoice = request.getContext().asType(Invoice.class);
      Invoice invoice = request.getContext().asType(Invoice.class);
      List<Integer> productids = (List<Integer>) request.getContext().get("productIds");
      List<InvoiceLine> invoiceLines = invoiceserviceimp.productList(invoice, productids);
      response.setValue("invoiceLineList", invoiceLines);
    }
  }

  public void invoiceCalculation(ActionRequest request, ActionResponse response) {

    if (request.getContext().get("productIds") != null) {
      Invoice invoice = request.getContext().asType(Invoice.class);
      List<InvoiceLine> invoiceLines = invoiceserviceimp.invoiceCalculationOnPartner(invoice);
      List<InvoiceLineTax> invoiceTaxLine = invoiceserviceimp.SetTaxLineOnChangePartner(invoice);
      invoice = invoiceserviceimp.setTotal(invoice);
      response.setValue("invoiceLineList", invoiceLines);
      response.setValue("invoiceLineTaxList", invoiceTaxLine);
      // response.setValues(invoice);
    }
  }
}
