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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ${domainPackageName}.*;
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

    private static final Log log = LogFactory.getLog( ${className}.class );

    @RequestMapping( value = "list_alt.html", method = RequestMethod.GET )
    public ModelAndView listNoScript( ModelMap model ) {
        model.addAttribute( "${lowerDomainName}List", ${daoServiceName}.get${domainName}Service().list() );
        return new ModelAndView( "${pathPrefix}/list_alt", model );
    }

    @RequestMapping(value = "list.html", method = RequestMethod.GET)
    public ModelAndView list() {
        return new ModelAndView( "${pathPrefix}/list" );
    }

${datatableMethod}

    @RequestMapping( value = "add.html", method = RequestMethod.GET )
    public ModelAndView add( ModelMap model ) {
        model.addAttribute( "${lowerDomainName}", new ${domainName}() );
${addEditListDependencies}
        return new ModelAndView( "${pathPrefix}/edit", model );
    }

    @RequestMapping( value = "edit.html", method = RequestMethod.GET )
    public ModelAndView edit( ModelMap model, ${requestParameterIdentifier} ) {
${addEditListDependencies}
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${pathPrefix}/edit", model );
    }

    @RequestMapping( value = "show.html", method = RequestMethod.GET )
    public ModelAndView show( ModelMap model, ${requestParameterIdentifier} ) {
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${pathPrefix}/show", model );
    }

    @RequestMapping( value = "save.html", method = RequestMethod.POST )
    public ModelAndView save(${foreignClassParameters}@ModelAttribute( "${lowerDomainName}" ) ${domainName} ${lowerDomainName} ) {
${compositeKey}
${compositeKeySetter}
${foreignClassSetters}
        ${daoServiceName}.get${domainName}Service().saveOrUpdate( ${lowerDomainName} );
        return new ModelAndView( new RedirectView( "list.html", true, true, false ) );
    }

    @RequestMapping( value = "delete.html", method = RequestMethod.GET )
    public ModelAndView confirmDelete( ModelMap model, ${requestParameterIdentifier} ) {
${compositeKey}
        model.addAttribute( "${lowerDomainName}", ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        return new ModelAndView( "${pathPrefix}/delete", model );
    }

    @RequestMapping( value = "delete.html", method = RequestMethod.POST )
    public ModelAndView doDelete( @RequestParam( value = "submit" ) String submitButtonValue, ${requestParameterIdentifier} ) {
${compositeKey}
        if ( StringUtils.equalsIgnoreCase( submitButtonValue, "yes" ) ) {
            ${daoServiceName}.get${domainName}Service().delete( ${daoServiceName}.get${domainName}Service().findById( ${lowerDomainName}Id ) );
        }
        return new ModelAndView( new RedirectView( "list.html", true, true, false ) );
    }
}