
function popUp(URL, h, w, title) 
{
	window.open(URL,
	' + title + ',
	'toolbar=0,scrollbars=0,location=0,statusbar=0,menubar=0,resizable=1,width=' + w + ',height=' + h
	);
}

function checker(inputField, checkVal)
{          
    if (inputField == null) return;
                    
    if (inputField.length != null)
    {
        var size = inputField.length;
        for (var i = 0; i < size; i++)
        {
            var thisBox = inputField[i];
            if (thisBox.type == "checkbox")
            {               
                thisBox.checked = checkVal;
            }
        }
    }
    else
    {
        if (inputField.type == "checkbox")
        {
            inputField.checked = checkVal;
        }
    }       
}

function checkAllGallery()
{
   checker(document.forms.createGallery.CREATE_GALLERY_SELECTED_IMAGE, true);        
   checker(document.forms.createGallery.CREATE_GALLERY_SELECTED_COMP, true);        
}     
function unCheckAllGallery()
{
   checker(document.forms.createGallery.CREATE_GALLERY_SELECTED_IMAGE, false);        
   checker(document.forms.createGallery.CREATE_GALLERY_SELECTED_COMP, false);    
}
