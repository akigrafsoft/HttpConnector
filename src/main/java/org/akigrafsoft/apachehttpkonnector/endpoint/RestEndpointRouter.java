/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.apachehttpkonnector.endpoint;

import org.akigrafsoft.apachehttpkonnector.ApacheHttpDataobject;

import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.EndpointController;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.routing.EndpointRouter;

public class RestEndpointRouter implements EndpointRouter {

	@Override
	public Endpoint resolveKonnector(Message message, KonnectorDataobject dataobject) {
		ApacheHttpDataobject l_do = (ApacheHttpDataobject) dataobject;
		String URI = l_do.httpRequest.getRequestLine().getUri();
		return EndpointController.INSTANCE.getEndpointByName(URI);
	}

}
