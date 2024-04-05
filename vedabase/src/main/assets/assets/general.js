
function showContentPage(recid)
{
    window.location.href = 'content.vbp?r=' + recid;
}

function hideElem(eid)
{
    document.getElementById(eid).style.display='none';
}

function showElem(eid)
{
    document.getElementById(eid).style.display='block';
}

function moveElemAfter(id1,id2)
{
    var movedNode = document.getElementById(id1);
    var ref = document.getElementById(id2);

    ref.parentNode.insertBefore(movedNode, ref.nextSibling);
}

function showEditIcons()
{
    var x = document.getElementsByClassName("editIcon");
    var i;

    for(i =0; i < x.length; i++)
    {
        x[i].style.display = 'inline';
    }
}

function hideEditIcons()
{
    var x = document.getElementsByClassName("editIcon");
    var i;

    for(i =0; i < x.length; i++)
    {
        x[i].style.display = 'none';
    }
}

function saveVar(vn,vv)
{
    document.getElementById(vn).innerText = vv;
}

function getVar(vn)
{
    return document.getElementById(vn).innerText;
}

function preparePopupMenu(idv,currType)
{
    hideEditIcons();
    saveVar('currentId',idv);
    saveVar('currentType',currType);
    moveElemAfter('popupMenuEdit','R' + idv);
    showElem('popupMenuEdit');
}

function showDeleteDialog()
{
    hideElem('popupMenuEdit');
    moveElemAfter('dialogDelete', 'R' + getVar('currentId'));
    saveVar('delDlgText', getVar('T' + getVar('currentId')));
    showElem('dialogDelete');
}

function showMoveDialog()
{
    hideElem('popupMenuEdit');
    showHideClassElements("movingNotice","inline");
    saveVar('movingMode', '1');
}

function onCancelMoving()
{
    showHideClassElements("movingNotice","none");
    saveVar('movingMode', '0');
}

function showHideClassElements(className,visb)
{
    var x = document.getElementsByClassName(className);
    var i;

    for(i =0; i < x.length; i++)
    {
        x[i].style.display = visb;
    }
}

function onClickNoteDir(targetDirId)
{
    var mode = getVar('movingMode');
    if (mode == 0) {
        Textabase.showNotesPage(targetDirId);
    } else {
        Textabase.moveCustomItem(getVar('currentId'),'note',targetDirId);
        Textabase.showNotesPage(null);
    }
}

function onClickBookmarkDir(targetDirId)
{
    var mode = getVar('movingMode');
    if (mode == 0) {
        Textabase.showBookmarkPage(targetDirId);
    } else {
        Textabase.moveCustomItem(getVar('currentId'),'bookmark',targetDirId);
        Textabase.showBookmarkPage(null);
    }
}

function onClickHighsDir(targetDirId)
{
    var mode = getVar('movingMode');
    if (mode == 0) {
        Textabase.showHighsPage(targetDirId);
    } else {
        Textabase.moveCustomItem(getVar('currentId'),'hightext',targetDirId);
        Textabase.showHighsPage(null);
    }
}
