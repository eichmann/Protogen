var start = 300;
var timer = start;
function timeout(pp){

	if(timer > 0){
			timer -= 1;
		setTimeout("timeout(\'"+ pp +"\')",1500);
	}
	else{
		ok = confirm("Session timed out due to inactivity. \n Would you like to close this session." );
		if (ok)
		{
			location.href= pp;
		}
	
		
	}
}


function toggle(divid,display) {
	var ele = document.getElementById(divid);
	if(display == "hide") {
    		ele.style.display = "none";
		
  	}
	else {
		ele.style.display = "block";
		
	}
}
function toggleV(divid) {
	var ele = document.getElementById(divid);
	
	if(ele.style.display == "block") {
    		ele.style.display = "none";
		
  	}
	else {
		ele.style.display = "block";
	
	}
} 

function moveOnMax(field,nextFieldName){
	  if(field.value.length >= field.maxLength){
	    document.getElementsByName(nextFieldName)[0].focus();
	  }
	}
function getdate() {
var myDate= new Date();
var output =  myDate.getFullYear() + "-"+ (myDate.getMonth()+1) + "-" + myDate.getDate(); 
return output;
}

function selectElement(elem, value, type) {

		var elementnames = new Array();
		elementnames[0] = elem;

		for (i = 0; i < elementnames.length; i++) {
			var elementgroup = document.getElementsByName(elementnames[i]);

			if (type == "radio") {
				for (j = 0; j < elementgroup.length; j++)
					if (elementgroup[j].value == value)
						elementgroup[j].checked = true;

			} else if (type == "select") {
				var options = elementgroup[i].options;
				for (j = 0; j < options.length; j++)
					if (options[j].value == value)
						elementgroup[i].selectedIndex = j;

			
		} else if (type == "checkbox") {
			for (j = 0; j < elementgroup.length; j++)
				if (elementgroup[j].value == value)
					elementgroup[j].checked = true;

		}

		}

}



function boolToAnswer(bool)
{
	if(bool == 'true')
		document.write("Yes");
		else if (bool == 'false')
			
		document.write("No");
		else
			document.write("N/A");

}

function processReqChange(req) {
    // only if req shows "loaded"
    if (req.readyState == 4) {
        // only if "OK"
        if (req.status == 200) {
            // ...processing statements go here...
        } else {
            alert("There was a problem retrieving the XML data:\n" +
                req.statusText);
        }
    }
}



function sessionKeepAlive(url){

	
	touchPage(url);
	setTimeout("sessionKeepAlive(\'"+ url +"\')",900000);


}

function touchPage(url)
{

		var req = false;
	    // branch for native XMLHttpRequest object
	    if(window.XMLHttpRequest && !(window.ActiveXObject)) {
	    	try {
				req = new XMLHttpRequest();
			} catch(e) {
				req = false;
	        }
	    // branch for IE/Windows ActiveX version
	    } else if(window.ActiveXObject) {
	       	try {
	        	//req = new ActiveXObject("Msxml2.XMLHTTP");
				xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
				xmlDoc.async=false;
				xmlDoc.validateOnParse = false;
				xmlDoc.resolveExternals = false;
				xmlDoc.preserveWhiteSpace = false;
				xmlDoc.load(url);
				return xmlDoc;
	      	} catch(e) {
	        	try {
	          		req = new ActiveXObject("Microsoft.XMLHTTP");
					
	        	} catch(e) {
	          		req = false;
	        	}
			}
	    }
		if(req) {
			req.onreadystatechange = processReqChange(req);
			req.open("GET", url, false);
			req.send("");
			return req.responseXML;
		}
		return "";
	}

var showsidebar=true;
function toggleSidebar()
{
	var message='&larr; hide menu';
	if (showsidebar)
	{
	var sidebar = document.getElementById("leftCol");
	var center = document.getElementById("centerCol");
	sidebar.style.display = 'block';
	center.style.width= '750px';

	}
	else 
	{
	var sidebar = document.getElementById("leftCol");
	var center = document.getElementById("centerCol");
	sidebar.style.display = 'none';
	center.style.width= '930px';
	message='show menu &rarr;';
	 
	}
	showsidebar=!showsidebar;
	
	document.getElementById("showhidebutton").innerHTML = '<a href="#" onclick="javascript:toggleSidebar();">' + message + '</a>';

}


function classDisplay(theClass,display){
	 var allTags = document.getElementsByTagName('*');
	 for(i=0; i<allTags.length; i++){
	  if(allTags[i].className == theClass) allTags[i].style.display = display;
	 }
	}

//chk if an object is an array or not.
function isArray(obj) {
	var length = obj.length;
	
if(length == undefined)
	return false;
	else
		return true;
}




function getValueOfElement(element)
{

	values = new Array();
	valueCount = 0;
	
	var type = element.type;
	var name = element.name;

	var elements;
	if(type =="radio" || type =="checkbox")	
	{
		elements = document.getElementsByName(name);
		
	}
	else if(type =="select")	
	{
		elements = document.getElementsByName(name);
		
	}
	else
	{
		values[valueCount]=element.value;
		return values;
	}

	for(i=0;i<elements.length;i++)
	{
		var isSelected;
		if (type == "select")
			isSelected = elements[i].selectedIndex;
		else
			isSelected = elements[i].checked;
			
		if(isSelected)
			{
				values[valueCount]=elements[i].value;
				valueCount++;
				//alert("value found"+ elements[i].value);
		}
	}	

	return values;
}


function setValueOfElementByName(elementId,value)
{
	var elements = document.getElementsByName(elementId);
	if(elements  == undefined)
		return false;

	 
	for(i in elements)
	{
	var type = elements[i].type;
	var name = elements[i].name;

	if(type =="radio")
	{
		selectElement(name, value, "radio");
		return true;
	}
	else if( type =="checkbox")	
	{
		selectElement(name, value, "radio");
		return true;
	}
	else if( type =="select")	
	{
		selectElement(name, value, "select");
		return true;
	}
	else
	{
		elements[i].value = value;
		return true;
	}
	
	}



}

function compareTwoArraysOfValues(operator,value1,value2)
{
	
	if(value1.length != value2.length)
	{
		return false;
	}
	
	
	for(i=0;i<value1.length;i++)
	{
		if(!compareValues(operator,value1[i],value2[i]))
		{
			return false;
		}
	}

	return true;
	
}


function compareValues(operator, value1,value2)
{
	
	
	if(value1 == '' || value2 == '' || value1 == null || value2 == null || value1 == undefined || value2==undefined)
		return false;

	

	if(operator == "==")
		return value1 == value2;
	else if(operator == ">=")
		return value1 >= value2;
	else if(operator == ">")
		return value1 > value2;
	else if(operator == "<=")
		return value1 <= value2;
	else if(operator == "<")
		return value1 < value2;
	else if(operator == "!=")
		return value1 != value2;
	else
		return false;

}

function toArray(value)
{
	result = new Array();
	result[0] = value;
	return result;
	
}

function getIdOfElement(element)
{
	type = element.type;

	if(!isArray(element))
	{
		return element.id;

	}
	else
	{
		return element[0].id;
		
	}
	
}

function matchValues(regex,values) 
{
	  test = true;
	  
	  if(regex == '')
		return test;
	  var re = new RegExp(regex);

	  for(i = 0; i< values.length; i++)
	  {
		  var value = values[i];
		  if (!value.match(re))
			  test= false;
	  }
	  
	  return test;
}

function checkInput(regex,values,errorDivId)
{

	if (regex=='')
		return;
		
		
	if(!matchValues(regex,values))
		document.getElementById(errorDivId).innerHTML = "invalid input format";
	else
	{
			document.getElementById(errorDivId).innerHTML = '';
	}
	return;
	
	
}


function doSomething(e) {
	var targ;
	if (!e) var e = window.event;
	if (e.target) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;
}


