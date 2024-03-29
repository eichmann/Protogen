package ${packageName};

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import $
import edu.uiowa.icts.spring.GenericDaoListOptions;
import edu.uiowa.icts.util.SortColumn;
import edu.uiowa.icts.util.DataTableHeader;

/**
 * Generated by Protogen 
 * @since ${date}
 */
@Controller
@RequestMapping( "${pathPrefix}/*" )
public class ${className} extends ${abstractControllerClassName} {

	static Logger log = LogManager.getLogger(${className}.class);

    @RequestMapping( value = "list_alt${pathExtension}", method = RequestMethod.GET )
    public String listNoScript(Model model) {
        model.addAttribute( "${lowerDomainName}List", ${daoServiceName}.get${domainName}Service().list() );
        return "${jspPath}/list_alt";
    }

    @RequestMapping(value = {"list${pathExtension}", "", "/"}, method = RequestMethod.GET)
    public String list() {
        return "${jspPath}/list";
    }

${datatableMethod}

    @RequestMapping( value = "add${pathExtension}", method = RequestMethod.GET )
    public ModelAndView add( ModelMap model ) {
        model.addAttribute( "${lowerDomainName}", new ${domainName}() );
${addEditListDependencies}
        return new ModelAndView( "${jspPath}/edit", model );
    }

    @RequestMapping( value = "edit${pathExtension}", method = RequestMethod.GET )
    public ModelAndView edit( ModelMap model, ${requestParameterIdentifier} ) {
${addEditListDependencies}
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${jspPath}/edit", model );
    }

    @RequestMapping( value = "show${pathExtension}", method = RequestMethod.GET )
    public ModelAndView show( ModelMap model, ${requestParameterIdentifier} ) {
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${jspPath}/show", model );
    }

    @RequestMapping( value = "save${pathExtension}", method = RequestMethod.POST )
    public ModelAndView save(${foreignClassParameters}@ModelAttribute( "${lowerDomainName}" ) ${domainName} ${lowerDomainName} ) {
${compositeKey}
${compositeKeySetter}
${foreignClassSetters}
        ${daoServiceName}.get${domainName}Service().saveOrUpdate( ${lowerDomainName} );
        return new ModelAndView( new RedirectView( "list${pathExtension}", true, true, false ) );
    }

    @RequestMapping( value = "delete${pathExtension}", method = RequestMethod.GET )
    public ModelAndView confirmDelete( ModelMap model, ${requestParameterIdentifier} ) {
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${jspPath}/delete", model );
    }

    @RequestMapping( value = "delete${pathExtension}", method = RequestMethod.POST )
    public ModelAndView doDelete( @RequestParam( value = "submit" ) String submitButtonValue, ${requestParameterIdentifier} ) {
${compositeKey}
        if ( StringUtils.equalsIgnoreCase( submitButtonValue, "yes" ) ) {
            ${daoServiceName}.get${domainName}Service().delete( ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        }
        return new ModelAndView( new RedirectView( "list${pathExtension}", true, true, false ) );
    }
}