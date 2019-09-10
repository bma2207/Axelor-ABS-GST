package com.axelor.appps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.gst.service.invoicePrint;

public class InvoicePrintModule extends AxelorModule {

  @Override
  protected void configure() {
    // TODO Auto-generated method stub
    bind(InvoicePrintServiceImpl.class).to(invoicePrint.class);
  }
}
