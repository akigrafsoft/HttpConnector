/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.apachehttpkonnector.endpoint;

import org.akigrafsoft.apachehttpkonnector.ApacheHttpDataobject;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.ExceptionComposeFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowOperation;
import com.akigrafsoft.knetthreads.FlowResult;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.Message.ExceptionNotFound;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.routing.KonnectorRouter;

public class RestEndpoint extends Endpoint {

	public RestEndpoint(String name) throws ExceptionDuplicate {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public KonnectorRouter getKonnectorRouter(Message message, KonnectorDataobject dataobject) {
		// TODO Auto-generated method stub
		return null;
	}

	public enum Request implements RequestEnum {
		HEAD, GET, POST, PUT, DELETE
	}

	@Override
	public RequestEnum classifyInboundMessage(Message message, KonnectorDataobject dataobject) {
		ApacheHttpDataobject l_do = (ApacheHttpDataobject) dataobject;

		switch (l_do.httpRequest.getRequestLine().getMethod().toUpperCase()) {
		case HttpHead.METHOD_NAME:
			return Request.HEAD;
		case HttpGet.METHOD_NAME:
			return Request.GET;
		case HttpPost.METHOD_NAME:
			return Request.POST;
		case HttpPut.METHOD_NAME:
			return Request.PUT;
		case HttpDelete.METHOD_NAME:
			return Request.DELETE;
		default:
			break;
		}

		return null;
	}

	public static class CommonRequest extends FlowOperation {

		private String konnectorDoName = "ApacheHttpDataobject";
		private String jsonDoName = "RESTEndpoint_json";

		public CommonRequest(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		public enum Result implements FlowResult {
			pass, fail
		}

		public String getKonnectorDoName() {
			return konnectorDoName;
		}

		public void setKonnectorDOName(String konnectorDOName) {
			this.konnectorDoName = konnectorDOName;
		}

		public String getJsonDoName() {
			return jsonDoName;
		}

		public void setJsonDoName(String doName) {
			this.jsonDoName = doName;
		}

		@Override
		public FlowResult parseInboundData(Message message, KonnectorDataobject kdataobject) {
			// associate the KonnectorDataobject to message for use in the
			// response
			message.associateDataobject(getKonnectorDoName(), kdataobject);
			ApacheHttpDataobject l_do = (ApacheHttpDataobject) kdataobject;
			try {
				message.associateDataobject(getJsonDoName(), new JSONParser().parse(l_do.getHttpRequestBody()));
			} catch (ParseException e) {
				return Result.fail;
			}
			return Result.pass;
		}

		@Override
		public KonnectorDataobject composeOutboundData(Message message) throws ExceptionComposeFailed {
			ApacheHttpDataobject o_kdataobject = null;
			try {
				o_kdataobject = (ApacheHttpDataobject) message.getDataobject(getKonnectorDoName());
				JSONObject l_jsonObj = (JSONObject) message.getDataobject(getJsonDoName());
				o_kdataobject.httpResponse.setEntity(
						new StringEntity(l_jsonObj.toJSONString(), ContentType.create("application/json", "UTF-8")));
			} catch (ExceptionNotFound e) {
				e.printStackTrace();
			}
			return o_kdataobject;
		}
	}

	public static class HEAD extends CommonRequest {
		public HEAD(String name) {
			super(name);
		}
	}

	public static class GET extends CommonRequest {
		public GET(String name) {
			super(name);
		}
	}

}
