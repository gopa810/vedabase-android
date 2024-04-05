
onscroll = function ()
{
    cr = getCurrId(100, 24);
    textPageGroup.setCurrentRecord(cr);
	alert(cr);
}

function increaseText() 
{
    if (document.body.style.fontSize == "") 
    {
        document.body.style.fontSize = "14pt";
    }
    document.body.style.fontSize = parseFloat(document.body.style.fontSize) + 1.0 + "pt";
    return document.body.style.fontSize;
}

function decreaseText() 
{
    if (document.body.style.fontSize == "") 
    {
        document.body.style.fontSize = "14pt";
    }
    document.body.style.fontSize = parseFloat(document.body.style.fontSize) - 1.0 + "pt";
    return document.body.style.fontSize;
}

function getCurrIdPrim(x,y)
{
	el = document.elementFromPoint(x,y);
	while(el && el.tagName != "P")
	{
		el = el.parentNode;
	}
	if (!el)
		return "";
	return el.id;
}
function getCurrId(x,y)
{
	el = getCurrIdPrim(x,y);
	if (el != "")
		return el;
	el = getCurrIdPrim(x,y-24);
	if (el != "")
		return el;
	el = getCurrIdPrim(x,y+24);
	if (el != "")
		return el;	
	return "";
}

function gotoElement(a)
{
	el = document.getElementById(a);
	window.scrollTo(0,el.offsetTop);
}

function appendText(text)
{
	el = document.getElementById('pageBody');
	if (el)
	{
		var para = document.createElement("div");
		el.appendChild(para);
		para.innerHTML = text;
	}
}

function removeTexts(start,end)
{
    var count = 0;
    for(var i = start; i <= end; i++)
    {
        el = document.getElementById('rec' + i);
        if (el)
        {
            par = el.parentNode;
            par.removeChild(el);
            count ++;
        }
    }
    return count;
}

function eth_expand(elid, openfile, closefile)
{
    var elem = document.getElementById(elid);
	var segments = elem.src.split("/");
	var file = segments[segments.length - 1];
	//alert(file);
	if (file == openfile)
	{
		elem.src = "vbase://stylist_images/" + closefile;
	}
	else
	{
		elem.src = "vbase://stylist_images/" + openfile;
	}
}

function eth_show_hide(elid)
{
    var elem = document.getElementById(elid);
	if (elem.style.display == 'block')
	{
		elem.style.display = 'none';
	}
	else
	{
		elem.style.display = 'block';
	}
}

function positionInParent(node)
{
    i = 0;
    len = 0;
    parent = node.parentNode;
    children = parent.childNodes;
    
    for(i = 0; i < children.length; i++)
    {
         if (children[i] == node)
         {
             return len;
         }
         else
         {
             len = len + children[i].textContent.length;
         }
    }
    return len;
}

function calculateAbsoluteIndex(startNode, startIndex)
{
    stop = 0;
    startId = "#";
    while(stop == 0)
    {
		if (startNode != null)
		{
		   if (startNode.nodeType == 1)
		   {
			   idatr = startNode.getAttribute('id');
			   if (idatr == null)
			   {
					startIndex += positionInParent(startNode);
					startNode = startNode.parentNode;
			   }
			   else
			   {
				  startId = idatr;
				  stop = 1;
			   }
		   }
		   else 
		   {
				startIndex += positionInParent(startNode);
				startNode = startNode.parentNode;
		   }
		}
		else
		{
			stop = 1;
		}
    }

    return startId + "," + startIndex;    
}

function getSelectedRangeIds()
{
    var selection = window.getSelection();
    var range = selection.getRangeAt(0);

    return calculateAbsoluteIndex(range.startContainer, range.startOffset) + ","
          + calculateAbsoluteIndex(range.endContainer, range.endOffset);    
}

function setParaText(recid,htmlText)
{
   var elem = document.getElementById(recid);
   if (elem != null)
   {
       elem.innerHTML = htmlText;
   }
}
