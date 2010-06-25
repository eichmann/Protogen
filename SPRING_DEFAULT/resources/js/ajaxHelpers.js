
function displayOutputInElement(destId,output)
{
	document.getElementById(destId).innerHTML = output;
	
}

function callRemotePage(page,keyvalues, destId)
{
$.get(
	 page,keyvalues,
	 function(data){displayOutputInElement(destId,data);},
	 "html"
	);
}

