package edu.uiowa.icts.webutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/*")
public class DefaultController
{
	
	private static final Log log = LogFactory.getLog(DefaultController.class);
	

	@RequestMapping(value = "{page}.html", method = RequestMethod.GET)
	public ModelAndView displayDefault(@PathVariable String page,HttpServletRequest req,HttpServletResponse res)
	{
		ModelMap model = new ModelMap();
		
		model.addAttribute("pagename",page);



		return new ModelAndView("custom",model);
			
	}
	@RequestMapping(value = "index.html", method = RequestMethod.GET)
	public String index(ModelMap model)
	{
		log.debug("In DefaultContoller..index");

		return "index";
		
	}
	@RequestMapping(value = "admin/index.html", method = RequestMethod.GET)
	public String admin(ModelMap model)
	{
		log.debug("In DefaultContoller..admin");

		return "admin";
		
	}
	@RequestMapping(value = "auth/index.html", method = RequestMethod.GET)
	public String authenticated(ModelMap model)
	{
		log.debug("In DefaultContoller..authenticated");

		return "auth";
		
	}
	
}
