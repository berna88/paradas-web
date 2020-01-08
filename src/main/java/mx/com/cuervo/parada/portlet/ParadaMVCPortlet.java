package mx.com.cuervo.parada.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import mx.com.cuervo.parada.constants.ParadaMVCPortletKeys;
import mx.com.cuervo.rutas.model.Parada;
import mx.com.cuervo.rutas.model.Ruta;
import mx.com.cuervo.rutas.service.ParadaLocalService;
import mx.com.cuervo.rutas.service.RutaLocalService;

/**
 * @author Jonathan
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=Cuervo",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=ParadaMVC",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + ParadaMVCPortletKeys.PARADAMVC,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class ParadaMVCPortlet extends MVCPortlet {
	
	@SuppressWarnings({ "deprecation" })
	public void addParada(ActionRequest request, ActionResponse response)
            throws PortalException {

        ServiceContext serviceContext = ServiceContextFactory.getInstance(
            Parada.class.getName(), request);

        String nombreParada = ParamUtil.getString(request, "nombreParada");
        String descripcion = ParamUtil.getString(request, "descripcion");
        String horario = ParamUtil.getString(request, "horario");
        String nombreCarpeta = ParamUtil.getString(request, "nombreCarpeta");
        String nombreArchivo = ParamUtil.getString(request, "nombreArchivo");
      
        long rutaId = ParamUtil.getLong(request, "rutaId");
        long paradaId = ParamUtil.getLong(request, "paradaId");

    if (paradaId > 0) {

        try {

            _paradaLocalService.updateParada(
                serviceContext.getUserId(), paradaId, nombreParada,
                descripcion, horario,nombreCarpeta,nombreArchivo, serviceContext);
      
            response.setRenderParameter(
                "rutaId", Long.toString(rutaId));

        }
        catch (Exception e) {
            System.out.println(e);

            PortalUtil.copyRequestParameters(request, response);

            response.setRenderParameter(
                "mvcPath", "/edit_parada.jsp");
        }

    }
    else {

        try {
            _paradaLocalService.addParada(
                serviceContext.getUserId(), rutaId, nombreParada, descripcion,
                horario, nombreCarpeta, nombreArchivo, serviceContext);

            SessionMessages.add(request, "paradaAdded");

            response.setRenderParameter(
                "rutaId", Long.toString(rutaId));

        }
        catch (Exception e) {
            SessionErrors.add(request, e.getClass().getName());

            PortalUtil.copyRequestParameters(request, response);

            response.setRenderParameter(
                "mvcPath", "/edit_parada.jsp");
        }
    }
}
	
	
	@SuppressWarnings("deprecation")
	public void deleteParada(ActionRequest request, ActionResponse response) throws PortalException {
        long paradaId = ParamUtil.getLong(request, "paradaId");
        long rutaId = ParamUtil.getLong(request, "rutaId");

        ServiceContext serviceContext = ServiceContextFactory.getInstance(
            Parada.class.getName(), request);

        try {

            response.setRenderParameter(
                "rutaId", Long.toString(rutaId));

            _paradaLocalService.deleteParada(paradaId, serviceContext);
        }

        catch (Exception e) {
            Logger.getLogger(ParadaMVCPortlet.class.getName()).log(
                Level.SEVERE, null, e);
        }
}
	
	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
	        throws IOException, PortletException {

	        try {
	            ServiceContext serviceContext = ServiceContextFactory.getInstance(
	                Ruta.class.getName(), renderRequest);

	            long groupId = serviceContext.getScopeGroupId();

	            long rutaId = ParamUtil.getLong(renderRequest, "rutaId");

	            System.out.println(rutaId);
	            List<Ruta> rutas = _rutaLocalService.getRutas(
	                groupId);

	            if (rutas.isEmpty()) {
	                Ruta ruta = 
	                		_rutaLocalService.addRuta(serviceContext.getUserId(), 
	                				"Main", 0, 0, serviceContext);
	                
	                rutaId = ruta.getRutaId();
	            }

	            if (rutaId == 0) {
	            	rutaId = rutas.get(0).getRutaId();
	            }

	            renderRequest.setAttribute("rutaId", rutaId);
	        }
	        catch (Exception e) {
	           System.out.println("Exception");
	        }

	        super.render(renderRequest, renderResponse);
	}


	@Reference(unbind = "-")
	private RutaLocalService _rutaLocalService;
	@Reference(unbind = "-")
	private ParadaLocalService _paradaLocalService;
}