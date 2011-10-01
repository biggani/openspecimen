package edu.wustl.catissuecore.domain.service.globus;

import edu.wustl.catissuecore.domain.service.Catissue_cacoreImpl;

import java.rmi.RemoteException;

/** 
 * DO NOT EDIT:  This class is autogenerated!
 *
 * This class implements each method in the portType of the service.  Each method call represented
 * in the port type will be then mapped into the unwrapped implementation which the user provides
 * in the Catissue_cacoreImpl class.  This class handles the boxing and unboxing of each method call
 * so that it can be correctly mapped in the unboxed interface that the developer has designed and 
 * has implemented.  Authorization callbacks are automatically made for each method based
 * on each methods authorization requirements.
 * 
 * @created by Introduce Toolkit version 1.4
 * 
 */
public class Catissue_cacoreProviderImpl{
	
	Catissue_cacoreImpl impl;
	
	public Catissue_cacoreProviderImpl() throws RemoteException {
		impl = new Catissue_cacoreImpl();
	}
	

    public edu.wustl.catissuecore.domain.stubs.InsertResponse insert(edu.wustl.catissuecore.domain.stubs.InsertRequest params) throws RemoteException, gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType {
    edu.wustl.catissuecore.domain.stubs.InsertResponse boxedResult = new edu.wustl.catissuecore.domain.stubs.InsertResponse();
    boxedResult.setAbstractDomainObject(impl.insert(params.getObject().getAbstractDomainObject()));
    return boxedResult;
  }

    public edu.wustl.catissuecore.domain.stubs.UpdateResponse update(edu.wustl.catissuecore.domain.stubs.UpdateRequest params) throws RemoteException, gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType {
    edu.wustl.catissuecore.domain.stubs.UpdateResponse boxedResult = new edu.wustl.catissuecore.domain.stubs.UpdateResponse();
    boxedResult.setAbstractDomainObject(impl.update(params.getObject().getAbstractDomainObject()));
    return boxedResult;
  }

    public edu.wustl.catissuecore.domain.stubs.DisableResponse disable(edu.wustl.catissuecore.domain.stubs.DisableRequest params) throws RemoteException, gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType {
    edu.wustl.catissuecore.domain.stubs.DisableResponse boxedResult = new edu.wustl.catissuecore.domain.stubs.DisableResponse();
    boxedResult.setAbstractDomainObject(impl.disable(params.getObject().getAbstractDomainObject()));
    return boxedResult;
  }

}
