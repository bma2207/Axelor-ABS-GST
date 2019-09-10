package com.axelor.appps.gst.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gst.service.invoiceLineServiceimpls;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;

public class GstModule extends AxelorModule {

  @Override
  protected void configure() {
    // TODO Auto-generated method stub
    bind(InvoiceLineSupplychainService.class).to(invoiceLineServiceimpls.class);
  }
}
