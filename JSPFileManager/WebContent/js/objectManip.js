function getObject(objectId) 
{
    // checkW3C DOM, then MSIE 4, then NN 4.
    //
    if(document.getElementById && document.getElementById(objectId)) 
    {
        return document.getElementById(objectId);
    }
    else  if (document.all && document.all(objectId)) 
    {  
        return document.all(objectId);
    }
    else if (document.layers && document.layers[objectId]) 
    { 
        return document.layers[objectId];
    } 
    else 
    {
        return false;
    }
}

function getObjectStyle(objectId) 
{
    // checkW3C DOM, then MSIE 4, then NN 4.
    //
    if(document.getElementById && document.getElementById(objectId)) 
    {
        return document.getElementById(objectId).style;
    }
    else  if (document.all && document.all(objectId)) 
    {  
        return document.all(objectId).style;
    }
    else if (document.layers && document.layers[objectId]) 
    { 
        return document.layers[objectId].style;
    } 
    else 
    {
        return false;
    }
}

function changeObjectVisibility(objectId, newVisibility) 
{
    // first get the object's stylesheet
    var styleObject = getStyleObject(objectId);

    // then if we find a stylesheet, set its visibility
    // as requested
    //
    if (styleObject) 
    {
        styleObject.visibility = newVisibility;
        return true;
    } 
    else 
    {
        return false;
    }
}
